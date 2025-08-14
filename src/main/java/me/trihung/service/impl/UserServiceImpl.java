package me.trihung.service.impl;

import java.time.LocalDateTime;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import me.trihung.auth.TokenGenerator;
import me.trihung.dto.TokenDTO;
import me.trihung.dto.request.LoginRequest;
import me.trihung.dto.request.RefreshTokenRequest;
import me.trihung.dto.request.SignUpRequest;
import me.trihung.entity.RefreshToken;
import me.trihung.entity.User;
import me.trihung.enums.RoleType;
import me.trihung.exception.BadRequestException;
import me.trihung.exception.UnauthorizedException;
import me.trihung.mapper.UserMapper;
import me.trihung.repository.RefreshTokenRepository;
import me.trihung.repository.RoleRepository;
import me.trihung.repository.UserRepository;
import me.trihung.service.UserService;

@Service
@Slf4j
public class UserServiceImpl implements UserService{
	@Autowired
    private UserRepository userRepository;
	
	@Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    TokenGenerator tokenGenerator;
    @Autowired
    DaoAuthenticationProvider daoAuthenticationProvider;
    @Autowired
    @Qualifier("jwtRefreshTokenAuthProvider")
    JwtAuthenticationProvider refreshTokenAuthProvider;
    
    @Override
	public void signUp(SignUpRequest signUpRequest) {
    	if (!StringUtils.equals(signUpRequest.getPassword(), signUpRequest.getRetypedPassword())) {
            throw BadRequestException.message("Mật khẩu không khớp");
        }
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw BadRequestException.message("Username đã tồn tại");
        }
        User user = UserMapper.INSTANCE.toEntity(signUpRequest);
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        //user.setRoles(Set.of(roleRepository.findByName(RoleType.USER.getValue())));
        user = userRepository.save(user);
        log.debug("UserService", "Registered for user "+user.getUsername());
	}
    
    private Authentication authenticateUser(LoginRequest loginRequest) {
        try {
        	UsernamePasswordAuthenticationToken authtoken = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.getUsername(), loginRequest.getPassword());
    		Authentication authentication = authenticationManager.authenticate(authtoken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("hello passs login");
            return authentication;

        } catch (AuthenticationException ex) {
            String message = authenticationExceptionMessage(ex);
            throw BadRequestException.message(message);
        }
    }
    
    private String authenticationExceptionMessage(AuthenticationException ex) {
        String message = "Lỗi xác thực không xác định";
        if (ex instanceof BadCredentialsException) {
            message = "Sai tên đăng nhập hoặc mật khẩu";
        } else if (ex instanceof DisabledException) {
            message = "Tài khoản chưa được xác thực, vui lòng kiểm tra email";
        } else if (ex instanceof LockedException) {
            message = "Tài khoản đã bị khóa";
        } else if (ex instanceof AccountExpiredException) {
            message = "Tài khoản đã hết hạn";
        }
        return message;
    }
    
	@Override
	public TokenDTO login(LoginRequest loginRequest) {
		log.debug("run login");
		System.out.println("hello login 2");
		Authentication authentication = authenticateUser(loginRequest);
		System.out.println("hello login 3");
		UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        if (userDetails == null) {
            throw BadRequestException.message("Không tìm thấy người dùng: " + loginRequest.getUsername());
        }
        System.out.println("hello login 4");
		return tokenGenerator.createToken(authentication);
	}

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> UnauthorizedException.message("Không tìm thấy user"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw BadRequestException.message("Mật khẩu không khớp");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.debug("UserService", "Đã thay đổi mật khẩu cho user "+user.getUsername());
    }

    @Override
    public boolean userExists(String Username) {
        return userRepository.findByUsername(Username).isPresent();
    }

	
	@Override
	public String refreshAccessToken(RefreshTokenRequest refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken.getRefreshToken())
                .orElseThrow(() -> BadRequestException.message("Token không hợp lệ"));

        if (token.getExpireTime().isBefore(LocalDateTime.now())) {
            throw BadRequestException.message("Token đã hết hạn");
        }
        BearerTokenAuthenticationToken bearerTokenAuthenticationToken = new BearerTokenAuthenticationToken(refreshToken.getRefreshToken());
        Authentication authentication = refreshTokenAuthProvider.authenticate(bearerTokenAuthenticationToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(token.getUsername());
        
        TokenDTO tokenDTO = tokenGenerator.createToken(authentication);
        log.debug("UserService", "Đã làm mới access token cho user "+userDetails.getUsername());
        return tokenDTO.getAccessToken();
    }
}
