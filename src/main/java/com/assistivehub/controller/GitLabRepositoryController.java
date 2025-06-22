package com.assistivehub.controller;

import com.assistivehub.entity.User;
import com.assistivehub.service.GitLabRepositoryService;
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
@RequestMapping("/api/repositories/gitlab")
@CrossOrigin(origins = "http://localhost:3000")
public class GitLabRepositoryController {

    @Autowired
    private GitLabRepositoryService gitLabRepositoryService;

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
     * GitLab 프로젝트 상세 정보 조회
     */
    @GetMapping("/{projectId}/details")
    public ResponseEntity<Map<String, Object>> getProjectDetails(
            @PathVariable String projectId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            Map<String, Object> projectDetails = gitLabRepositoryService.fetchProjectDetails(userId, projectId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", projectDetails);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * GitLab 프로젝트 커밋 목록 조회
     */
    @GetMapping("/{projectId}/commits")
    public ResponseEntity<Map<String, Object>> getProjectCommits(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int per_page,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<Map<String, Object>> commits = gitLabRepositoryService.fetchProjectCommits(userId, projectId, page,
                    per_page);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", commits);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * GitLab 프로젝트 기여자 목록 조회
     */
    @GetMapping("/{projectId}/contributors")
    public ResponseEntity<Map<String, Object>> getProjectContributors(
            @PathVariable String projectId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<Map<String, Object>> contributors = gitLabRepositoryService.fetchProjectContributors(userId,
                    projectId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", contributors);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * GitLab 프로젝트 언어 통계 조회
     */
    @GetMapping("/{projectId}/languages")
    public ResponseEntity<Map<String, Object>> getProjectLanguages(
            @PathVariable String projectId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            Map<String, Object> languages = gitLabRepositoryService.fetchProjectLanguages(userId, projectId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", languages);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * GitLab 프로젝트 이슈 목록 조회
     */
    @GetMapping("/{projectId}/issues")
    public ResponseEntity<Map<String, Object>> getProjectIssues(
            @PathVariable String projectId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<Map<String, Object>> issues = gitLabRepositoryService.fetchProjectIssues(userId, projectId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", issues);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * GitLab 프로젝트 머지 리퀘스트 목록 조회
     */
    @GetMapping("/{projectId}/merge-requests")
    public ResponseEntity<Map<String, Object>> getProjectMergeRequests(
            @PathVariable String projectId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<Map<String, Object>> mergeRequests = gitLabRepositoryService.fetchProjectMergeRequests(userId,
                    projectId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", mergeRequests);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}