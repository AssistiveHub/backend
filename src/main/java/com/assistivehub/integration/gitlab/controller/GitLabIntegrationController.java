package com.assistivehub.integration.gitlab.controller;

import com.assistivehub.integration.gitlab.dto.GitLabManualSetupRequest;
import com.assistivehub.integration.gitlab.service.GitLabManualSetupService;
import com.assistivehub.integration.gitlab.service.GitLabOAuthService;
import com.assistivehub.entity.GitLabIntegration;
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
@RequestMapping("/api/integrations/gitlab")
@CrossOrigin(origins = "*")
public class GitLabIntegrationController {

    @Autowired
    private GitLabManualSetupService gitLabManualSetupService;

    @Autowired
    private GitLabOAuthService gitLabOAuthService;

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
     * 깃랩 OAuth 인증 URL 생성
     */
    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, Object>> getGitLabAuthUrl(
            @RequestParam(required = false) String state,
            HttpServletRequest httpRequest) {

        try {
            getCurrentUserId(httpRequest); // 인증 확인

            String authUrl = gitLabOAuthService.generateAuthUrl(state);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("authUrl", authUrl);
            result.put("message", "깃랩 인증 URL이 생성되었습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 깃랩 OAuth 콜백 처리
     */
    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleGitLabCallback(
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

            GitLabIntegration integration = gitLabOAuthService
                    .exchangeCodeForToken(user, code, redirectUri);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "깃랩 OAuth 연동이 성공적으로 완료되었습니다.");
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
     * 깃랩 수동 연동 생성
     */
    @PostMapping("/manual-setup")
    public ResponseEntity<Map<String, Object>> createManualGitLabIntegration(
            @Valid @RequestBody GitLabManualSetupRequest request,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            User user = userService.findById(userId);

            GitLabIntegration integration = gitLabManualSetupService
                    .createManualGitLabIntegration(user, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "깃랩 연동이 성공적으로 완료되었습니다.");
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
     * 깃랩 토큰 유효성 검증
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateGitLabToken(
            @RequestBody Map<String, String> tokenRequest,
            HttpServletRequest httpRequest) {

        try {
            getCurrentUserId(httpRequest); // 인증 확인

            String token = tokenRequest.get("token");
            String gitlabUrl = tokenRequest.getOrDefault("gitlabUrl", "https://gitlab.com");

            if (token == null || token.trim().isEmpty()) {
                throw new RuntimeException("토큰이 필요합니다.");
            }

            boolean isValid = gitLabManualSetupService.validateGitLabToken(token, gitlabUrl);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("valid", isValid);

            if (isValid) {
                try {
                    GitLabManualSetupService.GitLabUserInfo userInfo = gitLabManualSetupService.getGitLabUserInfo(token,
                            gitlabUrl);
                    result.put("userInfo", userInfo);
                    result.put("message", "유효한 깃랩 토큰입니다.");
                } catch (Exception e) {
                    result.put("message", "토큰은 유효하지만 사용자 정보를 가져올 수 없습니다.");
                }
            } else {
                result.put("message", "유효하지 않은 깃랩 토큰입니다.");
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