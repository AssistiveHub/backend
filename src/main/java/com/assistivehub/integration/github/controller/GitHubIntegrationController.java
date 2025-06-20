package com.assistivehub.integration.github.controller;

import com.assistivehub.integration.github.dto.GitHubManualSetupRequest;
import com.assistivehub.integration.github.service.GitHubManualSetupService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/integrations/github")
@CrossOrigin(origins = "*")
public class GitHubIntegrationController {

    @Autowired
    private GitHubManualSetupService gitHubManualSetupService;

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