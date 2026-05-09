package com.ibizabroker.lms.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void generatedTokenContainsUsernameAndIsValid() {
        UserDetails userDetails = new User("admin", "password", Collections.emptyList());

        String token = jwtUtil.generateToken(userDetails);

        assertEquals("admin", jwtUtil.getUsernameFromToken(token));
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }
}
