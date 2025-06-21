package com.assistivehub.controller;

import com.assistivehub.dto.GitHubRepositoryRequest;
import com.assistivehub.dto.GitHubRepositoryResponse;
import com.assistivehub.entity.User;
import com.assistivehub.service.GitHubRepositoryService;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/repositories/github")
@CrossOrigin(origins = "http://localhost:3000")
public class GitHubRepositoryController {

    @Autowired
    private GitHubRepositoryService gitHubRepositoryService;

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
     * 사용자의 모든 GitHub 리포지토리 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserRepositories(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<GitHubRepositoryResponse> repositories = gitHubRepositoryService.getUserRepositories(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", repositories);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 특정 GitHub 리포지토리 조회
     */
    @GetMapping("/{repositoryId}")
    public ResponseEntity<Map<String, Object>> getRepository(
            @PathVariable Long repositoryId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            Optional<GitHubRepositoryResponse> repository = gitHubRepositoryService.getRepository(userId, repositoryId);

            if (repository.isPresent()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", repository.get());
                return ResponseEntity.ok(result);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "리포지토리를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * GitHub 리포지토리 추가
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addRepository(
            @Valid @RequestBody GitHubRepositoryRequest request,
            HttpServletRequest httpRequest) {
        try {
            System.out.println("DEBUG - Add repository request received");
            System.out.println("DEBUG - Repository URL: " + request.getRepositoryUrl());
            System.out.println("DEBUG - Access Token: " + (request.getAccessToken() != null ? "PROVIDED" : "NULL"));

            Long userId = getCurrentUserId(httpRequest);
            System.out.println("DEBUG - User ID: " + userId);

            User user = userService.findById(userId);
            System.out.println("DEBUG - User found: " + user.getEmail());

            GitHubRepositoryResponse repository = gitHubRepositoryService.addRepository(user, request);
            System.out.println("DEBUG - Repository added successfully: " + repository.getRepositoryName());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "GitHub 리포지토리가 성공적으로 추가되었습니다.");
            result.put("data", repository);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("ERROR - Add repository failed: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * GitHub 리포지토리 제거
     */
    @DeleteMapping("/{repositoryId}")
    public ResponseEntity<Map<String, Object>> removeRepository(
            @PathVariable Long repositoryId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            gitHubRepositoryService.removeRepository(userId, repositoryId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "GitHub 리포지토리가 성공적으로 제거되었습니다.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * GitHub 리포지토리 활성화/비활성화 토글
     */
    @PutMapping("/{repositoryId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleRepository(
            @PathVariable Long repositoryId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            GitHubRepositoryResponse repository = gitHubRepositoryService.toggleRepository(userId, repositoryId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "리포지토리 상태가 변경되었습니다.");
            result.put("data", repository);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/exchange-token")
    public ResponseEntity<Map<String, Object>> exchangeToken(@RequestBody Map<String, String> request) {
        String authorizationCode = request.get("code");
        if (authorizationCode == null || authorizationCode.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Authorization code is required");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            String accessToken = gitHubRepositoryService.exchangeCodeForToken(authorizationCode);

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("access_token", accessToken);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Token exchanged successfully");
            result.put("data", tokenData);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/repositories")
    public ResponseEntity<Map<String, Object>> getRepositoriesFromGitHub(
            @RequestParam String accessToken,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<Map<String, Object>> repositories = gitHubRepositoryService
                    .fetchGitHubRepositoriesWithStatus(accessToken, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", repositories);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}