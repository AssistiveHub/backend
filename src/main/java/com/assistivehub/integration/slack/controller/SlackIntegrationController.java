package com.assistivehub.integration.slack.controller;

import com.assistivehub.integration.slack.dto.SlackIntegrationRequest;
import com.assistivehub.integration.slack.dto.SlackIntegrationResponse;
import com.assistivehub.integration.slack.dto.SlackManualSetupRequest;
import com.assistivehub.integration.slack.service.SlackIntegrationService;
import com.assistivehub.integration.slack.service.SlackManualSetupService;
import com.assistivehub.integration.slack.service.SlackOAuthService;
import com.assistivehub.entity.SlackIntegration;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/integrations/slack")
@CrossOrigin(origins = "*")
public class SlackIntegrationController {

    @Autowired
    private SlackIntegrationService slackIntegrationService;

    @Autowired
    private SlackManualSetupService slackManualSetupService;

    @Autowired
    private SlackOAuthService slackOAuthService;

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
     * 슬랙 OAuth 인증 URL 생성
     */
    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, Object>> getSlackAuthUrl(
            @RequestParam(required = false) String state,
            HttpServletRequest httpRequest) {

        try {
            getCurrentUserId(httpRequest); // 인증 확인

            String authUrl = slackOAuthService.generateAuthUrl(state);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("authUrl", authUrl);
            result.put("message", "슬랙 인증 URL이 생성되었습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 슬랙 OAuth 콜백 처리
     */
    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleSlackCallback(
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

            SlackIntegration integration = slackOAuthService
                    .exchangeCodeForToken(user, code, redirectUri);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "슬랙 OAuth 연동이 성공적으로 완료되었습니다.");
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
     * 사용자의 모든 슬랙 연동 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserSlackIntegrations(
            @RequestParam(defaultValue = "false") boolean activeOnly,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            List<SlackIntegrationResponse> integrations;

            if (activeOnly) {
                integrations = slackIntegrationService.getActiveUserSlackIntegrations(userId);
            } else {
                integrations = slackIntegrationService.getUserSlackIntegrations(userId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", integrations);
            result.put("count", integrations.size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 특정 슬랙 연동 조회
     */
    @GetMapping("/{integrationId}")
    public ResponseEntity<Map<String, Object>> getSlackIntegration(
            @PathVariable Long integrationId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            SlackIntegrationResponse integration = slackIntegrationService
                    .getSlackIntegration(userId, integrationId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
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
     * 슬랙 연동 해제
     */
    @DeleteMapping("/{integrationId}")
    public ResponseEntity<Map<String, Object>> disconnectSlackIntegration(
            @PathVariable Long integrationId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            slackIntegrationService.disconnectSlackIntegration(userId, integrationId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "슬랙 연동이 해제되었습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 슬랙 연동 상태 확인
     */
    @GetMapping("/{integrationId}/validate")
    public ResponseEntity<Map<String, Object>> validateSlackIntegration(
            @PathVariable Long integrationId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            boolean isValid = slackIntegrationService.validateSlackIntegration(userId, integrationId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("isValid", isValid);
            result.put("message", isValid ? "연동이 정상입니다." : "연동에 문제가 있습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 슬랙 연동 활성화/비활성화
     */
    @PatchMapping("/{integrationId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleSlackIntegration(
            @PathVariable Long integrationId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            SlackIntegrationResponse integration = slackIntegrationService
                    .toggleSlackIntegration(userId, integrationId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", integration.getIsActive() ? "연동이 활성화되었습니다." : "연동이 비활성화되었습니다.");
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
     * 수동 슬랙 연동 생성
     */
    @PostMapping("/manual-setup")
    public ResponseEntity<Map<String, Object>> createManualSlackIntegration(
            @Valid @RequestBody SlackManualSetupRequest request,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            User user = userService.findById(userId);

            SlackIntegrationResponse integration = slackManualSetupService
                    .createManualSlackIntegration(user, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "슬랙 수동 연동이 성공적으로 완료되었습니다.");
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
     * 슬랙 토큰 유효성 검증
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateSlackToken(
            @RequestBody Map<String, String> tokenRequest,
            HttpServletRequest httpRequest) {

        try {
            getCurrentUserId(httpRequest); // 인증 확인

            String token = tokenRequest.get("token");
            if (token == null || token.trim().isEmpty()) {
                throw new RuntimeException("토큰이 제공되지 않았습니다.");
            }

            boolean isValid = slackManualSetupService.validateSlackToken(token);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("isValid", isValid);

            if (isValid) {
                // 토큰이 유효하면 사용자 정보도 함께 반환
                try {
                    SlackManualSetupService.SlackUserInfo userInfo = slackManualSetupService.getSlackUserInfo(token);
                    result.put("userInfo", userInfo);
                    result.put("message", "유효한 토큰입니다.");
                } catch (Exception e) {
                    result.put("message", "토큰은 유효하지만 사용자 정보 조회에 실패했습니다.");
                }
            } else {
                result.put("message", "유효하지 않은 토큰입니다.");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 슬랙 연동 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSlackIntegrationStats(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            long totalCount = slackIntegrationService.countUserSlackIntegrations(userId);
            long activeCount = slackIntegrationService.countActiveUserSlackIntegrations(userId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalIntegrations", totalCount);
            stats.put("activeIntegrations", activeCount);
            stats.put("inactiveIntegrations", totalCount - activeCount);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", stats);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}