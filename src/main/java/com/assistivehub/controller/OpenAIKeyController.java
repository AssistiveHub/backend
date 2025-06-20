package com.assistivehub.controller;

import com.assistivehub.dto.OpenAIKeyRequest;
import com.assistivehub.dto.OpenAIKeyResponse;
import com.assistivehub.dto.OpenAIKeyUpdateRequest;
import com.assistivehub.entity.User;
import com.assistivehub.service.OpenAIKeyService;
import com.assistivehub.service.UserService;
import com.assistivehub.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/openai-keys")
@CrossOrigin(origins = "*")
public class OpenAIKeyController {

    @Autowired
    private OpenAIKeyService openAIKeyService;

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
     * OpenAI 키 생성
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOpenAIKey(
            @Valid @RequestBody OpenAIKeyRequest request,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            OpenAIKeyResponse response = openAIKeyService.createOpenAIKey(userId, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "OpenAI 키가 성공적으로 생성되었습니다.");
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
     * 사용자의 모든 OpenAI 키 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserOpenAIKeys(
            @RequestParam(defaultValue = "false") boolean activeOnly,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            List<OpenAIKeyResponse> keys = openAIKeyService.getUserOpenAIKeys(userId, activeOnly);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", keys);
            result.put("count", keys.size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 특정 OpenAI 키 조회
     */
    @GetMapping("/{keyId}")
    public ResponseEntity<Map<String, Object>> getOpenAIKey(
            @PathVariable Long keyId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            OpenAIKeyResponse response = openAIKeyService.getOpenAIKey(userId, keyId);

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
     * OpenAI 키 수정
     */
    @PutMapping("/{keyId}")
    public ResponseEntity<Map<String, Object>> updateOpenAIKey(
            @PathVariable Long keyId,
            @Valid @RequestBody OpenAIKeyUpdateRequest request,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            OpenAIKeyResponse response = openAIKeyService.updateOpenAIKey(userId, keyId, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "OpenAI 키가 성공적으로 수정되었습니다.");
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
     * OpenAI 키 비활성화 (소프트 삭제)
     */
    @PatchMapping("/{keyId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateOpenAIKey(
            @PathVariable Long keyId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            openAIKeyService.deactivateOpenAIKey(userId, keyId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "OpenAI 키가 비활성화되었습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * OpenAI 키 완전 삭제
     */
    @DeleteMapping("/{keyId}")
    public ResponseEntity<Map<String, Object>> deleteOpenAIKey(
            @PathVariable Long keyId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            openAIKeyService.deleteOpenAIKey(userId, keyId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "OpenAI 키가 삭제되었습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 복호화된 API 키 조회 (실제 OpenAI API 호출 시 사용)
     */
    @GetMapping("/{keyId}/decrypt")
    public ResponseEntity<Map<String, Object>> getDecryptedApiKey(
            @PathVariable Long keyId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            String decryptedKey = openAIKeyService.getDecryptedApiKey(userId, keyId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("apiKey", decryptedKey);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 사용자의 활성 OpenAI 키 개수 조회
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countActiveKeys(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            long count = openAIKeyService.countActiveKeys(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("activeKeyCount", count);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}