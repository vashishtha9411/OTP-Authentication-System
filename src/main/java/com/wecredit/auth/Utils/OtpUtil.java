package com.wecredit.auth.Utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@Slf4j
public class OtpUtil {

    private final SecureRandom random = new SecureRandom();

    public String generateOtp() {
        try {
            int otp = 100000 + random.nextInt(900000); 
            String otpStr = String.valueOf(otp);
            log.info("Generated OTP: {}", otpStr);
            return otpStr;
        } catch (Exception e) {
            log.error("Error while generating OTP", e);
            throw new RuntimeException("Failed to generate OTP.");
        }
    }
}
