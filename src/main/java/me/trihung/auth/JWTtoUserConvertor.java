package me.trihung.auth;

import java.util.Collections;
import java.util.UUID;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import me.trihung.entity.User;

@Component
public class JWTtoUserConvertor implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

	@Override
	public UsernamePasswordAuthenticationToken convert(Jwt source) {
		User user = new User();
		user.setUsername(source.getSubject());
		return new UsernamePasswordAuthenticationToken(user, source, Collections.EMPTY_LIST);
	}

}