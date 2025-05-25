package com.wecredit.auth.Service;

import com.wecredit.auth.Config.JwtUtil;
import com.wecredit.auth.Dto.RegisterRequest;
import com.wecredit.auth.Dto.UserResponse;
import com.wecredit.auth.Model.User;
import com.wecredit.auth.Repository.UserRepository;
import com.wecredit.auth.exception.ResourceAlreadyExistsException;
import com.wecredit.auth.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ResponseEntity<String> register(RegisterRequest request, String fingerprint) {
        try {
            log.info("Registering user with mobile: {}", request.getMobile());

            Optional<User> optionalUser = userRepository.findByMobile(request.getMobile());
            if (optionalUser.isPresent()) {
                log.warn("User already exists with mobile: {}", request.getMobile());
                throw new ResourceAlreadyExistsException("User already exists.");
            }

            User user = User.builder()
                    .name(request.getName())
                    .mobile(request.getMobile())
                    .fingerprint(fingerprint)
                    .build();

            userRepository.save(user);
            log.info("User registered successfully: {}", request.getMobile());

            return ResponseEntity.ok("User registered successfully.");
        } catch (ResourceAlreadyExistsException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error occurred while registering user: {}", request.getMobile(), ex);
            return ResponseEntity.internalServerError().body("User registration failed.");
        }
    }

    public UserResponse getCurrentUser(String token) {
        try {
            String mobile = jwtUtil.extractMobile(token.replace("Bearer ", ""));
            log.info("Extracting current user from token for mobile: {}", mobile);

            User user = userRepository.findByMobile(mobile)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found."));

            return new UserResponse(user.getName(), user.getMobile());
        } catch (ResourceNotFoundException ex) {
            log.warn("User not found while retrieving current user from token");
            throw ex;
        } catch (Exception ex) {
            log.error("Error retrieving current user", ex);
            throw new RuntimeException("Failed to retrieve user.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String mobile) throws UsernameNotFoundException {
        try {
            log.info("Loading user by mobile: {}", mobile);

            User user = userRepository.findByMobile(mobile)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with mobile: " + mobile));

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getMobile())
                    .password("")  // password not required in OTP-based login
                    .authorities("ROLE_USER")
                    .build();
        } catch (UsernameNotFoundException ex) {
            log.warn("Username not found: {}", mobile);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while loading user by username", ex);
            throw new RuntimeException("Internal error while loading user.");
        }
    }
}
