package com.wecredit.auth.Service;

import com.wecredit.auth.Config.JwtUtil;
import com.wecredit.auth.Dto.LoginRequest;
import com.wecredit.auth.Model.User;
import com.wecredit.auth.Repository.UserRepository;
import com.wecredit.auth.Utils.FingerprintUtil;
import com.wecredit.auth.Utils.OtpUtil;
import com.wecredit.auth.exception.InvalidOtpException;
import com.wecredit.auth.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpUtil otpUtil;
    private final JwtUtil jwtUtil;

    public ResponseEntity<String> sendOtp(String mobile, String fingerprint) {
        try {
            log.info("Attempting to send OTP to mobile: {}, fingerprint: {}", mobile, fingerprint);

            Optional<User> userOpt = userRepository.findByMobile(mobile);
            if (userOpt.isEmpty()) {
                log.warn("Mobile number not registered: {}", mobile);
                throw new ResourceNotFoundException("Mobile number not registered.");
            }

            User user = userOpt.get();

            if (user.getFingerprint() != null && !FingerprintUtil.isSameDevice(user.getFingerprint(), fingerprint)) {
                log.warn("Fingerprint mismatch for mobile: {}", mobile);
                return ResponseEntity.badRequest().body("Fingerprint mismatch.");
            }

            String otp = otpUtil.generateOtp();
            long expiry = Instant.now().plusSeconds(300).toEpochMilli();

            user.setOtp(otp);
            user.setOtpExpiryTime(expiry);
            user.setFingerprint(fingerprint);
            userRepository.save(user);

            log.info("OTP generated and saved for mobile: {}", mobile);
            return ResponseEntity.ok("OTP sent: " + otp); // Replace with SMS gateway in prod

        } catch (Exception ex) {
            log.error("Error sending OTP to mobile: {}", mobile, ex);
            return ResponseEntity.internalServerError().body("Failed to send OTP.");
        }
    }

    public ResponseEntity<String> verifyOtp(LoginRequest request, String fingerprint) {
        try {
            log.info("Verifying OTP for mobile: {}, fingerprint: {}", request.getMobile(), fingerprint);

            Optional<User> userOpt = userRepository.findByMobile(request.getMobile());
            if (userOpt.isEmpty()) {
                log.warn("User not found for mobile: {}", request.getMobile());
                throw new ResourceNotFoundException("User not found.");
            }

            User user = userOpt.get();

            if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
                log.warn("Invalid OTP for mobile: {}", request.getMobile());
                throw new InvalidOtpException("Invalid OTP.");
            }

            if (Instant.now().toEpochMilli() > user.getOtpExpiryTime()) {
                log.warn("OTP expired for mobile: {}", request.getMobile());
                return ResponseEntity.badRequest().body("OTP expired.");
            }

            if (!FingerprintUtil.isSameDevice(user.getFingerprint(), fingerprint)) {
                log.warn("Unrecognized device for mobile: {}", request.getMobile());
                return ResponseEntity.badRequest().body("Unrecognized device.");
            }

            String token = jwtUtil.generateToken(user.getMobile());

            user.setOtp(null);
            user.setOtpExpiryTime(0L);
            userRepository.save(user);

            log.info("OTP verified and token issued for mobile: {}", request.getMobile());
            return ResponseEntity.ok(token);

        } catch (InvalidOtpException | ResourceNotFoundException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error verifying OTP for mobile: {}", request.getMobile(), ex);
            return ResponseEntity.internalServerError().body("OTP verification failed.");
        }
    }
}
