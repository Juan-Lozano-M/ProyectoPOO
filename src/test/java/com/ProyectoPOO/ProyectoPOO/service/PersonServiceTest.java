package com.ProyectoPOO.ProyectoPOO.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PersonServiceTest {

    @Test
    void shouldBuildLoginWithMnemonicRule() {
        String login = PersonService.buildLogin("Carlos", "Perez", "12345678");
        Assertions.assertEquals("cp12345678", login);
    }

    @Test
    void passwordCodecShouldHashAndMatch() {
        PasswordCodec codec = new PasswordCodec();
        String encoded = codec.hash("secret-123");
        Assertions.assertTrue(codec.matches("secret-123", encoded));
        Assertions.assertFalse(codec.matches("secret-xxx", encoded));
    }
}

