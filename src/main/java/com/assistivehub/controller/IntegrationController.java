package com.assistivehub.controller;

import com.assistivehub.entity.IntegratedService;
import com.assistivehub.entity.User;
import com.assistivehub.integration.common.service.IntegrationService;
import com.assistivehub.service.UserService;
import com.assistivehub.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/integrations")
@CrossOrigin(origins = "*")
public class IntegrationController {

    @Autowired
    private IntegrationService integrationService;

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
     * 사용자의 모든 연동 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserIntegrations(
            @RequestParam(defaultValue = "false") boolean activeOnly,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            List<IntegratedService> integrations;

            if (activeOnly) {
                integrations = integrationService.getActiveUserIntegrations(userId);
            } else {
                integrations = integrationService.getUserIntegrations(userId);
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
     * 사용자의 연동 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserIntegrationStats(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            Map<String, Object> stats = integrationService.getUserIntegrationStats(userId);

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

    /**
     * 연동 활성화/비활성화
     */
    @PatchMapping("/{integrationId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleIntegration(
            @PathVariable Long integrationId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            IntegratedService integration = integrationService.toggleIntegration(userId, integrationId);

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
     * 연동 삭제
     */
    @DeleteMapping("/{integrationId}")
    public ResponseEntity<Map<String, Object>> deleteIntegration(
            @PathVariable Long integrationId,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getCurrentUserId(httpRequest);
            integrationService.deleteIntegration(userId, integrationId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "연동이 삭제되었습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}