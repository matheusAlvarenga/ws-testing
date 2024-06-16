package com.alvarenga.ws_testing.providers;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.security.PublicKey;

@Component
public class JsonWebKeyProvider implements KeyProvider {

    private final UrlJwkProvider provider;

    public JsonWebKeyProvider(@Value("${app.auth.jwks-url}") final String jwksUrl) {
        try {
            this.provider = new UrlJwkProvider(new URI(jwksUrl).toURL());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JWK provider: ", e);
        }
    }

    @Cacheable("public-keys")
    @Override
    public PublicKey getPublicKey(String keyId) {
        try {
            final Jwk jwk = provider.get(keyId);
            return jwk.getPublicKey();
        } catch (JwkException e) {
            throw new RuntimeException("Failed to ", e);
        }
    }
}
