package com.alvarenga.ws_testing.providers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Component
public class JwtTokenProvider implements TokenProvider {

    private KeyProvider keyProvider;

    public JwtTokenProvider(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @Override
    public Map<String, String> decode(String token) {
        DecodedJWT jwt = JWT.decode(token);
        
        PublicKey publicKey = keyProvider.getPublicKey(jwt.getKeyId());
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);
        algorithm.verify(jwt);

        boolean expired = jwt.getExpiresAtAsInstant().atZone(ZoneId.systemDefault()).isBefore(ZonedDateTime.now());
        if (expired) {
            throw new IllegalArgumentException("Token expired");
        }

        return Map.of(
                "id", jwt.getSubject(),
                "name", jwt.getClaim("name").asString(),
                "picture", jwt.getClaim("picture").asString()
        );
    }
}
