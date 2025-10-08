package me.trihung.auth;

import java.util.Collections;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JWTtoUserConvertor implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

	@Override
	public UsernamePasswordAuthenticationToken convert(Jwt source) {
		List<String> roles = source.getClaimAsStringList("roles");
        if (roles == null) {
            roles = Collections.emptyList();
        }
        
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role)) // or "ROLE_" + role if using hasRole
                .toList();
        
		UserDetails user =  new org.springframework.security.core.userdetails.User(
                source.getSubject(),
                "",
                authorities
        );
		return new UsernamePasswordAuthenticationToken(user, source, authorities);
	}

}