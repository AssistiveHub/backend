package com.assistivehub.controller;

import com.assistivehub.dto.UserSettingRequest;
import com.assistivehub.dto.UserSettingResponse;
import com.assistivehub.service.UserService;
import com.assistivehub.service.UserSettingService;
import com.assistivehub.entity.User;
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
@RequestMapping("/api/user-settings")
@CrossOrigin(origins = "*")
public class UserSettingController {

    @Autowired
    private UserSettingService userSettingService;

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
     * 사용자 설정 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserSetting(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            UserSettingResponse response = userSettingService.getUserSetting(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 사용자 설정 업데이트
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateUserSetting(
            @Valid @RequestBody UserSettingRequest request,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            UserSettingResponse response = userSettingService.updateUserSetting(userId, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "사용자 설정이 성공적으로 업데이트되었습니다.");
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 사용자 설정 초기화 (기본값으로 리셋)
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetUserSetting(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            UserSettingResponse response = userSettingService.resetUserSetting(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "사용자 설정이 기본값으로 초기화되었습니다.");
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 특정 기능 토글 (on/off 전환)
     */
    @PatchMapping("/toggle/{settingName}")
    public ResponseEntity<Map<String, Object>> toggleSetting(
            @PathVariable String settingName,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            UserSettingResponse response = userSettingService.toggleSetting(userId, settingName);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "설정이 성공적으로 전환되었습니다.");
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 사용자 설정 존재 여부 확인
     */
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Object>> checkUserSettingExists(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            boolean exists = userSettingService.hasUserSetting(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("exists", exists);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}