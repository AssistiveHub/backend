package com.assistivehub.integration.notion.controller;

import com.assistivehub.integration.notion.dto.NotionManualSetupRequest;
import com.assistivehub.integration.notion.dto.NotionIntegrationResponse;
import com.assistivehub.integration.notion.service.NotionManualSetupService;
import com.assistivehub.integration.notion.service.NotionOAuthService;
import com.assistivehub.entity.NotionIntegration;
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
@RequestMapping("/api/integrations/notion")
@CrossOrigin(origins = "*")
public class NotionIntegrationController {

    @Autowired
    private NotionManualSetupService notionManualSetupService;

    @Autowired
    private NotionOAuthService notionOAuthService;

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
     * 노션 OAuth 인증 URL 생성
     */
    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, Object>> getNotionAuthUrl(
            @RequestParam(required = false) String state,
            HttpServletRequest httpRequest) {

        try {
            getCurrentUserId(httpRequest); // 인증 확인

            String authUrl = notionOAuthService.generateAuthUrl(state);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("authUrl", authUrl);
            result.put("message", "노션 인증 URL이 생성되었습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 노션 OAuth 콜백 처리
     */
    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleNotionCallback(
            @RequestBody Map<String, String> callbackData,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            User user = userService.findById(userId);

            String code = callbackData.get("code");
            String redirectUri = callbackData.get("redirect_uri");

            if (code == null || code.trim().isEmpty()) {
                throw new RuntimeException("인증 코드가 필요합니다.");
            }

            NotionIntegration integration = notionOAuthService
                    .exchangeCodeForToken(user, code, redirectUri);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "노션 OAuth 연동이 성공적으로 완료되었습니다.");
            result.put("data", integration);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 노션 수동 연동 생성
     */
    @PostMapping("/manual-setup")
    public ResponseEntity<Map<String, Object>> createManualNotionIntegration(
            @Valid @RequestBody NotionManualSetupRequest request,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            User user = userService.findById(userId);

            NotionIntegrationResponse integration = notionManualSetupService
                    .createManualNotionIntegration(user, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "노션 연동이 성공적으로 완료되었습니다.");
            result.put("data", integration);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 노션 토큰 유효성 검증
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateNotionToken(
            @RequestBody Map<String, String> tokenRequest,
            HttpServletRequest httpRequest) {

        try {
            getCurrentUserId(httpRequest); // 인증 확인

            String token = tokenRequest.get("token");
            if (token == null || token.trim().isEmpty()) {
                throw new RuntimeException("토큰이 필요합니다.");
            }

            boolean isValid = notionManualSetupService.validateNotionToken(token);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("valid", isValid);

            if (isValid) {
                result.put("message", "유효한 노션 토큰입니다.");
            } else {
                result.put("message", "유효하지 않은 노션 토큰입니다.");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}