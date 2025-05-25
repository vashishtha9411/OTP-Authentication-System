package com.wecredit.auth.Controller;

import com.wecredit.auth.Dto.LoginRequest;
import com.wecredit.auth.Dto.OtpRequest;
import com.wecredit.auth.Dto.RegisterRequest;
import com.wecredit.auth.Dto.UserResponse;
import com.wecredit.auth.Service.AuthService;
import com.wecredit.auth.Service.UserService;
import com.wecredit.auth.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request,
                                           @RequestHeader(value = "Fingerprint") String fingerprint) {
        try {
            log.info("Register request received for mobile: {}, fingerprint: {}", request.getMobile(), fingerprint);
            return userService.register(request, fingerprint);
        } catch (Exception ex) {
            log.error("Error during registration for mobile: {}", request.getMobile(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + ex.getMessage());
        }
    }

    @PostMapping("/login/request")
    public ResponseEntity<String> requestOtp(@RequestBody OtpRequest request,
                                             @RequestHeader(value = "Fingerprint") String fingerprint) {
        try {
            log.info("OTP request received for mobile: {}, fingerprint: {}", request.getMobile(), fingerprint);
            return authService.sendOtp(request.getMobile(), fingerprint);
        } catch (Exception ex) {
            log.error("Error sending OTP to mobile: {}", request.getMobile(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("OTP request failed: " + ex.getMessage());
        }
    }

    @PostMapping("/login/verify")
    public ResponseEntity<String> verifyOtp(@RequestBody LoginRequest request,
                                            @RequestHeader(value = "Fingerprint") String fingerprint) {
        try {
            log.info("OTP verification attempt for mobile: {}, fingerprint: {}", request.getMobile(), fingerprint);
            return authService.verifyOtp(request, fingerprint);
        } catch (Exception ex) {
            log.error("Error verifying OTP for mobile: {}", request.getMobile(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("OTP verification failed: " + ex.getMessage());
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody OtpRequest request,
                                            @RequestHeader(value = "Fingerprint") String fingerprint) {
        try {
            log.info("Resend OTP request received for mobile: {}, fingerprint: {}", request.getMobile(), fingerprint);
            return authService.sendOtp(request.getMobile(), fingerprint);
        } catch (Exception ex) {
            log.error("Error resending OTP to mobile: {}", request.getMobile(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Resend OTP failed: " + ex.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            String token = authorizationHeader.substring(7);
            log.info("Fetching current user for token: {}", token);
            UserResponse user = userService.getCurrentUser(token);

            if (user == null) {
                log.warn("Unauthorized access attempt with token: {}", token);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            return ResponseEntity.ok(user);
        } catch (Exception ex) {
            log.error("Error retrieving current user", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
