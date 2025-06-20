package com.assistivehub.integration.github.controller;

import com.assistivehub.integration.github.dto.GitHubManualSetupRequest;
import com.assistivehub.integration.github.service.GitHubManualSetupService;
import com.assistivehub.integration.github.service.GitHubOAuthService;
import com.assistivehub.entity.GitHubIntegration;
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
@RequestMapping("/api/integrations/github")
@CrossOrigin(origins = "*")
public class GitHubIntegrationController {

    @Autowired
    private GitHubManualSetupService gitHubManualSetupService;

    @Autowired
    private GitHubOAuthService gitHubOAuthService;

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
     * 깃허브 OAuth 인증 URL 생성
     */
    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, Object>> getGitHubAuthUrl(
            @RequestParam(required = false) String state,
            HttpServletRequest httpRequest) {

        try {
            getCurrentUserId(httpRequest); // 인증 확인

            String authUrl = gitHubOAuthService.generateAuthUrl(state);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("authUrl", authUrl);
            result.put("message", "깃허브 인증 URL이 생성되었습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 깃허브 OAuth 콜백 처리
     */
    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleGitHubCallback(
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

            GitHubIntegration integration = gitHubOAuthService
                    .exchangeCodeForToken(user, code, redirectUri);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "깃허브 OAuth 연동이 성공적으로 완료되었습니다.");
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
     * 사용자의 모든 깃허브 연동 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getGitHubIntegrations(
            @RequestParam(defaultValue = "false") boolean activeOnly,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            User user = userService.findById(userId);

            List<GitHubIntegration> integrations = gitHubManualSetupService
                    .getGitHubIntegrationsByUser(user, activeOnly);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", integrations);
            result.put("message", "깃허브 연동 목록을 성공적으로 조회했습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 특정 깃허브 연동 조회
     */
    @GetMapping("/{integrationId}")
    public ResponseEntity<Map<String, Object>> getGitHubIntegration(
            @PathVariable Long integrationId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);

            GitHubIntegration integration = gitHubManualSetupService
                    .getGitHubIntegrationById(userId, integrationId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", integration);
            result.put("message", "깃허브 연동 정보를 성공적으로 조회했습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 깃허브 연동 해제
     */
    @DeleteMapping("/{integrationId}")
    public ResponseEntity<Map<String, Object>> deleteGitHubIntegration(
            @PathVariable Long integrationId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);

            gitHubOAuthService.revokeIntegration(userId, integrationId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "깃허브 연동이 성공적으로 해제되었습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 깃허브 연동 활성화/비활성화 토글
     */
    @PatchMapping("/{integrationId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleGitHubIntegration(
            @PathVariable Long integrationId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);

            GitHubIntegration integration = gitHubManualSetupService
                    .toggleGitHubIntegration(userId, integrationId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", integration);
            result.put("message", "깃허브 연동 상태가 성공적으로 변경되었습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 깃허브 연동 상태 확인
     */
    @GetMapping("/{integrationId}/validate")
    public ResponseEntity<Map<String, Object>> validateGitHubIntegration(
            @PathVariable Long integrationId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);

            GitHubIntegration integration = gitHubManualSetupService
                    .getGitHubIntegrationById(userId, integrationId);

            boolean isValid = gitHubOAuthService.validateIntegration(integration);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("valid", isValid);
            result.put("message", isValid ? "깃허브 연동이 유효합니다." : "깃허브 연동이 유효하지 않습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 깃허브 수동 연동 생성
     */
    @PostMapping("/manual-setup")
    public ResponseEntity<Map<String, Object>> createManualGitHubIntegration(
            @Valid @RequestBody GitHubManualSetupRequest request,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            User user = userService.findById(userId);

            GitHubIntegration integration = gitHubManualSetupService
                    .createManualGitHubIntegration(user, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "깃허브 연동이 성공적으로 완료되었습니다.");
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
     * 깃허브 토큰 유효성 검증
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateGitHubToken(
            @RequestBody Map<String, String> tokenRequest,
            HttpServletRequest httpRequest) {

        try {
            getCurrentUserId(httpRequest); // 인증 확인

            String token = tokenRequest.get("token");
            if (token == null || token.trim().isEmpty()) {
                throw new RuntimeException("토큰이 필요합니다.");
            }

            boolean isValid = gitHubManualSetupService.validateGitHubToken(token);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("valid", isValid);

            if (isValid) {
                try {
                    GitHubManualSetupService.GitHubUserInfo userInfo = gitHubManualSetupService
                            .getGitHubUserInfo(token);
                    result.put("userInfo", userInfo);
                    result.put("message", "유효한 깃허브 토큰입니다.");
                } catch (Exception e) {
                    result.put("message", "토큰은 유효하지만 사용자 정보를 가져올 수 없습니다.");
                }
            } else {
                result.put("message", "유효하지 않은 깃허브 토큰입니다.");
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