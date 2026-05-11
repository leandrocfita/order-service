package com.fiap.cheffyorderservice.infrastructure.security.resolver;

import com.fiap.cheffyorderservice.infrastructure.security.model.CurrentUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserResolver {

    public CurrentUser from(Jwt jwt){
        return new CurrentUser(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaims().get("login").toString()
        );
    }
}