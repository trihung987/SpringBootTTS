package me.trihung.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import me.trihung.auth.annotation.HasAnyRole;
import me.trihung.dto.TokenDTO;
import me.trihung.dto.request.LoginRequest;
import me.trihung.dto.request.RefreshTokenRequest;
import me.trihung.dto.request.SignUpRequest;
import me.trihung.enums.RoleType;
import me.trihung.service.UserService;

@RestController
//@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {
	@Autowired
	UserService userService;

	@Operation(summary = "Đăng ký mới tài khoản")
	@PostMapping("/register")
	public ResponseEntity<String> register(@RequestBody @Valid SignUpRequest signUpRequest) {
		userService.signUp(signUpRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký tài khoản thành công");
	}
	
	@Operation(summary = "Đăng nhập tài khoản")
	@PostMapping("/login")
	public ResponseEntity<TokenDTO> login(@RequestBody @Valid LoginRequest loginRequest) {
		System.out.println("hello login");
		TokenDTO tokenDTO = userService.login(loginRequest);
		return ResponseEntity.status(200).body(tokenDTO);
	}
	
	@Operation(summary = "Làm mới access token")
	@PostMapping("/refresh")
	public ResponseEntity<String> login(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
		return ResponseEntity.status(200).body(userService.refreshAccessToken(refreshTokenRequest));
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/testauth")
	public ResponseEntity<String> test(){
		return ResponseEntity.status(200).body("ok api");
	}

}
