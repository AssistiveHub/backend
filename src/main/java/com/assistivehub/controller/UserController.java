package com.assistivehub.controller;

import com.assistivehub.dto.UserUpdateRequest;
import com.assistivehub.entity.User;
import com.assistivehub.service.UserService;
import com.assistivehub.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 현재 요청에서 사용자 ID 추출
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) {
            throw new RuntimeException("인증 토큰이 필요합니다.");
        }
        String email = jwtUtil.extractUsername(token);
        User user = userService.findByEmail(email);
        return user.getId();
    }

    /**
     * 요청 헤더에서 JWT 토큰 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            User user = userService.findById(userId);

            // 비밀번호 제외하고 응답
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("email", user.getEmail());
            userInfo.put("name", user.getName());
            userInfo.put("phoneNumber", user.getPhoneNumber());
            userInfo.put("birthDate", user.getBirthDate());
            userInfo.put("gender", user.getGender());
            userInfo.put("address", user.getAddress());
            userInfo.put("profileImageUrl", user.getProfileImageUrl());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("lastLoginAt", user.getLastLoginAt());
            userInfo.put("status", user.getStatus());
            userInfo.put("emailVerified", user.getEmailVerified());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", userInfo);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 사용자 정보 업데이트
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateUserProfile(
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            User updatedUser = userService.updateUserInfo(userId, request);

            // 비밀번호 제외하고 응답
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", updatedUser.getId());
            userInfo.put("email", updatedUser.getEmail());
            userInfo.put("name", updatedUser.getName());
            userInfo.put("phoneNumber", updatedUser.getPhoneNumber());
            userInfo.put("birthDate", updatedUser.getBirthDate());
            userInfo.put("gender", updatedUser.getGender());
            userInfo.put("address", updatedUser.getAddress());
            userInfo.put("profileImageUrl", updatedUser.getProfileImageUrl());
            userInfo.put("createdAt", updatedUser.getCreatedAt());
            userInfo.put("lastLoginAt", updatedUser.getLastLoginAt());
            userInfo.put("status", updatedUser.getStatus());
            userInfo.put("emailVerified", updatedUser.getEmailVerified());
            userInfo.put("updatedAt", updatedUser.getUpdatedAt());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "사용자 정보가 성공적으로 업데이트되었습니다.");
            result.put("data", userInfo);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 계정 비활성화 (소프트 삭제)
     */
    @DeleteMapping("/account")
    public ResponseEntity<Map<String, Object>> deactivateAccount(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            userService.deleteUser(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "계정이 비활성화되었습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}