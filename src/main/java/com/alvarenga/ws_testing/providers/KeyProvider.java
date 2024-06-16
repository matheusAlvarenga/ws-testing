package com.alvarenga.ws_testing.providers;

import java.security.PublicKey;

public interface KeyProvider {

    PublicKey getPublicKey(String keyId);

}
