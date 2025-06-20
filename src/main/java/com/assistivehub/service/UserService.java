package com.assistivehub.service;

import com.assistivehub.dto.AuthResponse;
import com.assistivehub.dto.LoginRequest;
import com.assistivehub.dto.SignupRequest;
import com.assistivehub.dto.UserUpdateRequest;
import com.assistivehub.entity.User;
import com.assistivehub.repository.UserRepository;
import com.assistivehub.repository.OpenAIKeyRepository;
import com.assistivehub.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OpenAIKeyRepository openAIKeyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>());
    }

    public AuthResponse signup(SignupRequest signupRequest) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // 사용자 생성
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setName(signupRequest.getName());
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setBirthDate(signupRequest.getBirthDate());
        user.setGender(signupRequest.getGender());
        user.setAddress(signupRequest.getAddress());
        user.setProfileImageUrl(signupRequest.getProfileImageUrl());
        user.setStatus(User.UserStatus.ACTIVE);
        user.setEmailVerified(false);

        // 데이터베이스에 저장
        User savedUser = userRepository.save(user);

        // JWT 토큰 생성
        UserDetails userDetails = loadUserByUsername(savedUser.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        // OpenAI 키 보유 여부 확인 (신규 가입자는 항상 false)
        boolean hasOpenAIKey = openAIKeyRepository.countByUserAndIsActiveTrue(savedUser) > 0;

        return new AuthResponse(token, savedUser, hasOpenAIKey);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        // 사용자 조회
        User user = userRepository.findByEmailAndStatus(loginRequest.getEmail(), User.UserStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 마지막 로그인 시간 업데이트
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // JWT 토큰 생성
        UserDetails userDetails = loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        // OpenAI 키 보유 여부 확인
        boolean hasOpenAIKey = openAIKeyRepository.countByUserAndIsActiveTrue(user) > 0;

        return new AuthResponse(token, user, hasOpenAIKey);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setStatus(User.UserStatus.DELETED);
        userRepository.save(user);
    }

    /**
     * 사용자 정보 업데이트
     */
    public User updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // null이 아닌 값들만 업데이트 (부분 업데이트 지원)
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress().trim());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl().trim());
        }

        return userRepository.save(user);
    }

    /**
     * 사용자 ID로 조회
     */
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
}