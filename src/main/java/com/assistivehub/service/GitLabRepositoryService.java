package com.assistivehub.service;

import com.assistivehub.entity.GitLabIntegration;
import com.assistivehub.repository.GitLabIntegrationRepository;
import com.assistivehub.util.EncryptionUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GitLabRepositoryService {

    @Autowired
    private GitLabIntegrationRepository gitLabIntegrationRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    private final ObjectMapper objectMapper;

    public GitLabRepositoryService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 사용자의 GitLab 연동 정보 가져오기
     */
    private GitLabIntegration getUserGitLabIntegration(Long userId) {
        List<GitLabIntegration> integrations = gitLabIntegrationRepository.findActiveByUserId(userId);
        if (integrations.isEmpty()) {
            throw new RuntimeException("GitLab 연동이 설정되지 않았습니다.");
        }
        return integrations.get(0); // 첫 번째 활성 연동 사용
    }

    /**
     * GitLab 프로젝트 상세 정보 조회
     */
    public Map<String, Object> fetchProjectDetails(Long userId, String projectId) {
        try {
            GitLabIntegration integration = getUserGitLabIntegration(userId);
            String accessToken = encryptionUtil.decrypt(integration.getAccessToken());
            String gitlabUrl = integration.getGitlabUrl();

            WebClient webClient = WebClient.builder()
                    .baseUrl(gitlabUrl + "/api/v4")
                    .build();

            String response = webClient.get()
                    .uri("/projects/{id}", projectId)
                    .header("Private-Token", accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode projectJson = objectMapper.readTree(response);

            Map<String, Object> projectDetails = new HashMap<>();
            projectDetails.put("id", projectJson.get("id").asLong());
            projectDetails.put("name", projectJson.get("name").asText());
            projectDetails.put("path_with_namespace", projectJson.get("path_with_namespace").asText());
            projectDetails.put("description", projectJson.has("description") && !projectJson.get("description").isNull()
                    ? projectJson.get("description").asText()
                    : "");
            projectDetails.put("web_url", projectJson.get("web_url").asText());
            projectDetails.put("http_url_to_repo", projectJson.get("http_url_to_repo").asText());
            projectDetails.put("default_branch", projectJson.get("default_branch").asText());
            projectDetails.put("visibility", projectJson.get("visibility").asText());
            projectDetails.put("created_at", projectJson.get("created_at").asText());
            projectDetails.put("last_activity_at", projectJson.get("last_activity_at").asText());
            projectDetails.put("star_count", projectJson.get("star_count").asInt());
            projectDetails.put("forks_count", projectJson.get("forks_count").asInt());
            projectDetails.put("open_issues_count", projectJson.get("open_issues_count").asInt());

            // 네임스페이스 정보
            if (projectJson.has("namespace")) {
                JsonNode namespace = projectJson.get("namespace");
                Map<String, Object> namespaceInfo = new HashMap<>();
                namespaceInfo.put("name", namespace.get("name").asText());
                namespaceInfo.put("path", namespace.get("path").asText());
                namespaceInfo.put("avatar_url", namespace.has("avatar_url") && !namespace.get("avatar_url").isNull()
                        ? namespace.get("avatar_url").asText()
                        : "");
                projectDetails.put("namespace", namespaceInfo);
            }

            return projectDetails;

        } catch (Exception e) {
            throw new RuntimeException("GitLab 프로젝트 정보를 가져올 수 없습니다: " + e.getMessage(), e);
        }
    }

    /**
     * GitLab 프로젝트 커밋 목록 조회
     */
    public List<Map<String, Object>> fetchProjectCommits(Long userId, String projectId, int page, int perPage) {
        try {
            GitLabIntegration integration = getUserGitLabIntegration(userId);
            String accessToken = encryptionUtil.decrypt(integration.getAccessToken());
            String gitlabUrl = integration.getGitlabUrl();

            WebClient webClient = WebClient.builder()
                    .baseUrl(gitlabUrl + "/api/v4")
                    .build();

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projects/{id}/repository/commits")
                            .queryParam("page", page)
                            .queryParam("per_page", perPage)
                            .build(projectId))
                    .header("Private-Token", accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode commitsArray = objectMapper.readTree(response);
            List<Map<String, Object>> commits = new ArrayList<>();

            for (JsonNode commitNode : commitsArray) {
                Map<String, Object> commit = new HashMap<>();
                commit.put("id", commitNode.get("id").asText());
                commit.put("short_id", commitNode.get("short_id").asText());
                commit.put("title", commitNode.get("title").asText());
                commit.put("message", commitNode.get("message").asText());
                commit.put("author_name", commitNode.get("author_name").asText());
                commit.put("author_email", commitNode.get("author_email").asText());
                commit.put("created_at", commitNode.get("created_at").asText());
                commit.put("web_url", commitNode.get("web_url").asText());

                commits.add(commit);
            }

            return commits;

        } catch (Exception e) {
            throw new RuntimeException("GitLab 커밋 목록을 가져올 수 없습니다: " + e.getMessage(), e);
        }
    }

    /**
     * GitLab 프로젝트 기여자 목록 조회
     */
    public List<Map<String, Object>> fetchProjectContributors(Long userId, String projectId) {
        try {
            GitLabIntegration integration = getUserGitLabIntegration(userId);
            String accessToken = encryptionUtil.decrypt(integration.getAccessToken());
            String gitlabUrl = integration.getGitlabUrl();

            WebClient webClient = WebClient.builder()
                    .baseUrl(gitlabUrl + "/api/v4")
                    .build();

            String response = webClient.get()
                    .uri("/projects/{id}/repository/contributors", projectId)
                    .header("Private-Token", accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode contributorsArray = objectMapper.readTree(response);
            List<Map<String, Object>> contributors = new ArrayList<>();

            for (JsonNode contributorNode : contributorsArray) {
                Map<String, Object> contributor = new HashMap<>();
                contributor.put("name", contributorNode.get("name").asText());
                contributor.put("email", contributorNode.get("email").asText());
                contributor.put("commits", contributorNode.get("commits").asInt());
                contributor.put("additions", contributorNode.get("additions").asInt());
                contributor.put("deletions", contributorNode.get("deletions").asInt());

                contributors.add(contributor);
            }

            return contributors;

        } catch (Exception e) {
            throw new RuntimeException("GitLab 기여자 목록을 가져올 수 없습니다: " + e.getMessage(), e);
        }
    }

    /**
     * GitLab 프로젝트 언어 통계 조회
     */
    public Map<String, Object> fetchProjectLanguages(Long userId, String projectId) {
        try {
            GitLabIntegration integration = getUserGitLabIntegration(userId);
            String accessToken = encryptionUtil.decrypt(integration.getAccessToken());
            String gitlabUrl = integration.getGitlabUrl();

            WebClient webClient = WebClient.builder()
                    .baseUrl(gitlabUrl + "/api/v4")
                    .build();

            String response = webClient.get()
                    .uri("/projects/{id}/languages", projectId)
                    .header("Private-Token", accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode languagesJson = objectMapper.readTree(response);

            Map<String, Double> languageData = new HashMap<>();
            languagesJson.fields().forEachRemaining(entry -> {
                languageData.put(entry.getKey(), entry.getValue().asDouble());
            });

            double totalBytes = languageData.values().stream().mapToDouble(Double::doubleValue).sum();

            List<Map<String, Object>> languages = languageData.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .map(entry -> {
                        Map<String, Object> lang = new HashMap<>();
                        lang.put("language", entry.getKey());
                        lang.put("bytes", entry.getValue().longValue());
                        lang.put("percentage", (entry.getValue() / totalBytes) * 100);
                        return lang;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("total_bytes", (long) totalBytes);
            result.put("languages", languages);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("GitLab 언어 통계를 가져올 수 없습니다: " + e.getMessage(), e);
        }
    }

    /**
     * GitLab 프로젝트 이슈 목록 조회
     */
    public List<Map<String, Object>> fetchProjectIssues(Long userId, String projectId) {
        try {
            GitLabIntegration integration = getUserGitLabIntegration(userId);
            String accessToken = encryptionUtil.decrypt(integration.getAccessToken());
            String gitlabUrl = integration.getGitlabUrl();

            WebClient webClient = WebClient.builder()
                    .baseUrl(gitlabUrl + "/api/v4")
                    .build();

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projects/{id}/issues")
                            .queryParam("state", "opened")
                            .queryParam("per_page", "20")
                            .build(projectId))
                    .header("Private-Token", accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode issuesArray = objectMapper.readTree(response);
            List<Map<String, Object>> issues = new ArrayList<>();

            for (JsonNode issueNode : issuesArray) {
                Map<String, Object> issue = new HashMap<>();
                issue.put("id", issueNode.get("id").asLong());
                issue.put("iid", issueNode.get("iid").asInt());
                issue.put("title", issueNode.get("title").asText());
                issue.put("description", issueNode.has("description") && !issueNode.get("description").isNull()
                        ? issueNode.get("description").asText()
                        : "");
                issue.put("state", issueNode.get("state").asText());
                issue.put("created_at", issueNode.get("created_at").asText());
                issue.put("updated_at", issueNode.get("updated_at").asText());
                issue.put("web_url", issueNode.get("web_url").asText());

                // 작성자 정보
                if (issueNode.has("author")) {
                    JsonNode author = issueNode.get("author");
                    Map<String, Object> authorInfo = new HashMap<>();
                    authorInfo.put("name", author.get("name").asText());
                    authorInfo.put("username", author.get("username").asText());
                    authorInfo.put("avatar_url", author.has("avatar_url") && !author.get("avatar_url").isNull()
                            ? author.get("avatar_url").asText()
                            : "");
                    issue.put("author", authorInfo);
                }

                issues.add(issue);
            }

            return issues;

        } catch (Exception e) {
            throw new RuntimeException("GitLab 이슈 목록을 가져올 수 없습니다: " + e.getMessage(), e);
        }
    }

    /**
     * GitLab 프로젝트 머지 리퀘스트 목록 조회
     */
    public List<Map<String, Object>> fetchProjectMergeRequests(Long userId, String projectId) {
        try {
            GitLabIntegration integration = getUserGitLabIntegration(userId);
            String accessToken = encryptionUtil.decrypt(integration.getAccessToken());
            String gitlabUrl = integration.getGitlabUrl();

            WebClient webClient = WebClient.builder()
                    .baseUrl(gitlabUrl + "/api/v4")
                    .build();

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projects/{id}/merge_requests")
                            .queryParam("state", "opened")
                            .queryParam("per_page", "20")
                            .build(projectId))
                    .header("Private-Token", accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode mergeRequestsArray = objectMapper.readTree(response);
            List<Map<String, Object>> mergeRequests = new ArrayList<>();

            for (JsonNode mrNode : mergeRequestsArray) {
                Map<String, Object> mergeRequest = new HashMap<>();
                mergeRequest.put("id", mrNode.get("id").asLong());
                mergeRequest.put("iid", mrNode.get("iid").asInt());
                mergeRequest.put("title", mrNode.get("title").asText());
                mergeRequest.put("description", mrNode.has("description") && !mrNode.get("description").isNull()
                        ? mrNode.get("description").asText()
                        : "");
                mergeRequest.put("state", mrNode.get("state").asText());
                mergeRequest.put("created_at", mrNode.get("created_at").asText());
                mergeRequest.put("updated_at", mrNode.get("updated_at").asText());
                mergeRequest.put("web_url", mrNode.get("web_url").asText());
                mergeRequest.put("source_branch", mrNode.get("source_branch").asText());
                mergeRequest.put("target_branch", mrNode.get("target_branch").asText());

                // 작성자 정보
                if (mrNode.has("author")) {
                    JsonNode author = mrNode.get("author");
                    Map<String, Object> authorInfo = new HashMap<>();
                    authorInfo.put("name", author.get("name").asText());
                    authorInfo.put("username", author.get("username").asText());
                    authorInfo.put("avatar_url", author.has("avatar_url") && !author.get("avatar_url").isNull()
                            ? author.get("avatar_url").asText()
                            : "");
                    mergeRequest.put("author", authorInfo);
                }

                mergeRequests.add(mergeRequest);
            }

            return mergeRequests;

        } catch (Exception e) {
            throw new RuntimeException("GitLab 머지 리퀘스트 목록을 가져올 수 없습니다: " + e.getMessage(), e);
        }
    }
}