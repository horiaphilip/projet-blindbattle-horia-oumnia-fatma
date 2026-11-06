package fr.miage.blindbattle.security;

import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

public class AuthUserConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        AuthUser user = new AuthUser(Long.parseLong(jwt.getSubject()), jwt.getClaimAsString("email"));
        return new UsernamePasswordAuthenticationToken(user, jwt, List.of());
    }
}
