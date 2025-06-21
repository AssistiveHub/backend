package com.assistivehub.service;

import com.assistivehub.dto.GitHubRepositoryRequest;
import com.assistivehub.dto.GitHubRepositoryResponse;
import com.assistivehub.entity.GitHubRepository;
import com.assistivehub.entity.IntegratedService;
import com.assistivehub.entity.User;
import com.assistivehub.repository.GitHubRepositoryRepository;
import com.assistivehub.repository.IntegratedServiceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class GitHubRepositoryService {

    @Autowired
    private GitHubRepositoryRepository gitHubRepositoryRepository;

    @Autowired
    private IntegratedServiceRepository integratedServiceRepository;

    @Value("${github.client.id}")
    private String githubClientId;

    @Value("${github.client.secret}")
    private String githubClientSecret;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // GitHub URL 패턴 (https://github.com/owner/repo 형태)
    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile("https://github\\.com/([^/]+)/([^/]+)/?");

    public GitHubRepositoryService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 사용자의 모든 GitHub 리포지토리 조회
     */
    @Transactional(readOnly = true)
    public List<GitHubRepositoryResponse> getUserRepositories(Long userId) {
        List<GitHubRepository> repositories = gitHubRepositoryRepository.findByUserId(userId);
        return repositories.stream()
                .map(GitHubRepositoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 GitHub 리포지토리 조회
     */
    @Transactional(readOnly = true)
    public Optional<GitHubRepositoryResponse> getRepository(Long userId, Long repositoryId) {
        Optional<GitHubRepository> repository = gitHubRepositoryRepository.findByUserIdAndRepositoryId(userId,
                repositoryId);
        return repository.map(GitHubRepositoryResponse::fromEntity);
    }

    /**
     * GitHub 리포지토리 추가
     */
    public GitHubRepositoryResponse addRepository(User user, GitHubRepositoryRequest request) {
        // 1. 리포지토리 URL 파싱
        String[] repoInfo = parseRepositoryUrl(request.getRepositoryUrl());
        String owner = repoInfo[0];
        String repoName = repoInfo[1];
        String fullName = owner + "/" + repoName;

        // 2. 중복 확인
        Optional<GitHubRepository> existing = gitHubRepositoryRepository
                .findByUserIdAndRepositoryFullName(user.getId(), fullName);
        if (existing.isPresent()) {
            throw new RuntimeException("이미 추가된 리포지토리입니다: " + fullName);
        }

        // 3. GitHub API로 리포지토리 정보 조회
        RepositoryInfo repoInfo2 = fetchRepositoryInfo(request.getAccessToken(), owner, repoName);

        // 4. GitHub 사용자 정보 조회
        GitHubUserInfo userInfo = fetchGitHubUserInfo(request.getAccessToken());

        // 5. IntegratedService 생성
        IntegratedService integratedService = new IntegratedService();
        integratedService.setUser(user);
        integratedService.setServiceType(IntegratedService.ServiceType.GITHUB);
        integratedService.setServiceName(fullName);
        integratedService.setIsActive(true);
        integratedService.setCreatedAt(LocalDateTime.now());
        integratedService.setUpdatedAt(LocalDateTime.now());

        IntegratedService savedService = integratedServiceRepository.save(integratedService);

        // 6. GitHubRepository 생성
        GitHubRepository repository = new GitHubRepository();
        repository.setIntegratedService(savedService);

        // GitHub 계정 정보
        repository.setGithubUsername(userInfo.getLogin());
        repository.setAccessToken(request.getAccessToken());
        repository.setGithubUserId(userInfo.getId().toString());
        repository.setAvatarUrl(userInfo.getAvatarUrl());

        // 리포지토리 정보
        repository.setRepositoryName(repoName);
        repository.setRepositoryFullName(fullName);
        repository.setRepositoryUrl(request.getRepositoryUrl());
        repository.setRepositoryDescription(repoInfo2.getDescription());
        repository.setRepositoryLanguage(repoInfo2.getLanguage());
        repository.setIsPrivate(repoInfo2.getIsPrivate());
        repository.setDefaultBranch(repoInfo2.getDefaultBranch());

        // 통계 정보
        repository.setStarsCount(repoInfo2.getStarsCount());
        repository.setForksCount(repoInfo2.getForksCount());
        repository.setOpenIssuesCount(repoInfo2.getOpenIssuesCount());

        // 동기화 설정
        repository.setAutoSyncEnabled(request.getAutoSyncEnabled());
        repository.setSyncCommits(request.getSyncCommits());
        repository.setSyncPullRequests(request.getSyncPullRequests());
        repository.setSyncIssues(request.getSyncIssues());
        repository.setSyncReleases(request.getSyncReleases());
        repository.setNotificationEnabled(request.getNotificationEnabled());
        repository.setWebhookEnabled(request.getWebhookEnabled());

        GitHubRepository savedRepository = gitHubRepositoryRepository.save(repository);
        return GitHubRepositoryResponse.fromEntity(savedRepository);
    }

    /**
     * GitHub 리포지토리 제거
     */
    public void removeRepository(Long userId, Long repositoryId) {
        Optional<GitHubRepository> repository = gitHubRepositoryRepository.findByUserIdAndRepositoryId(userId,
                repositoryId);
        if (repository.isPresent()) {
            gitHubRepositoryRepository.delete(repository.get());
        } else {
            throw new RuntimeException("리포지토리를 찾을 수 없습니다.");
        }
    }

    /**
     * 리포지토리 활성화/비활성화 토글
     */
    public GitHubRepositoryResponse toggleRepository(Long userId, Long repositoryId) {
        Optional<GitHubRepository> repositoryOpt = gitHubRepositoryRepository.findByUserIdAndRepositoryId(userId,
                repositoryId);
        if (repositoryOpt.isPresent()) {
            GitHubRepository repository = repositoryOpt.get();
            IntegratedService service = repository.getIntegratedService();
            service.setIsActive(!service.getIsActive());
            service.setUpdatedAt(LocalDateTime.now());

            integratedServiceRepository.save(service);
            return GitHubRepositoryResponse.fromEntity(repository);
        } else {
            throw new RuntimeException("리포지토리를 찾을 수 없습니다.");
        }
    }

    /**
     * GitHub OAuth 코드를 액세스 토큰으로 교환
     */
    public String exchangeCodeForToken(String code) {
        try {
            String response = WebClient.create()
                    .post()
                    .uri("https://github.com/login/oauth/access_token")
                    .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .bodyValue("client_id=" + githubClientId +
                            "&client_secret=" + githubClientSecret +
                            "&code=" + code)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            String accessToken = jsonNode.get("access_token").asText();

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Failed to get access token from GitHub");
            }

            return accessToken;
        } catch (Exception e) {
            throw new RuntimeException("GitHub token exchange failed: " + e.getMessage());
        }
    }

    /**
     * GitHub API로 사용자의 리포지토리 목록 조회 (추가 상태 포함)
     */
    public List<java.util.Map<String, Object>> fetchGitHubRepositoriesWithStatus(String accessToken, Long userId) {
        try {
            // 기존 리포지토리 목록 조회
            List<java.util.Map<String, Object>> repositories = fetchGitHubRepositories(accessToken);

            // 이미 추가된 리포지토리 목록 조회
            List<GitHubRepository> existingRepos = gitHubRepositoryRepository.findByUserId(userId);
            java.util.Set<String> existingRepoUrls = existingRepos.stream()
                    .map(GitHubRepository::getRepositoryUrl)
                    .collect(java.util.stream.Collectors.toSet());

            // 각 리포지토리에 추가 상태 정보 포함
            for (java.util.Map<String, Object> repo : repositories) {
                String htmlUrl = (String) repo.get("html_url");
                boolean isAlreadyAdded = existingRepoUrls.contains(htmlUrl);
                repo.put("is_already_added", isAlreadyAdded);
            }

            return repositories;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch GitHub repositories with status: " + e.getMessage());
        }
    }

    /**
     * GitHub API로 사용자의 리포지토리 목록 조회
     */
    public List<java.util.Map<String, Object>> fetchGitHubRepositories(String accessToken) {
        try {
            String response = webClient.get()
                    .uri("/user/repos?sort=updated&per_page=100")
                    .header(HttpHeaders.AUTHORIZATION, "token " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode repositoriesNode = objectMapper.readTree(response);
            java.util.List<java.util.Map<String, Object>> repositories = new java.util.ArrayList<>();

            for (JsonNode repoNode : repositoriesNode) {
                java.util.Map<String, Object> repo = new java.util.HashMap<>();
                repo.put("id", repoNode.get("id").asLong());
                repo.put("name", repoNode.get("name").asText());
                repo.put("full_name", repoNode.get("full_name").asText());
                repo.put("html_url", repoNode.get("html_url").asText());
                repo.put("description",
                        repoNode.has("description") && !repoNode.get("description").isNull()
                                ? repoNode.get("description").asText()
                                : null);
                repo.put("private", repoNode.get("private").asBoolean());
                repo.put("language",
                        repoNode.has("language") && !repoNode.get("language").isNull()
                                ? repoNode.get("language").asText()
                                : null);
                repo.put("stargazers_count", repoNode.get("stargazers_count").asInt());
                repo.put("updated_at", repoNode.get("updated_at").asText());

                repositories.add(repo);
            }

            return repositories;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch GitHub repositories: " + e.getMessage());
        }
    }

    /**
     * GitHub URL 파싱
     */
    private String[] parseRepositoryUrl(String url) {
        Matcher matcher = GITHUB_URL_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new RuntimeException("유효하지 않은 GitHub 리포지토리 URL입니다: " + url);
        }

        String owner = matcher.group(1);
        String repo = matcher.group(2);

        // .git 확장자 제거
        if (repo.endsWith(".git")) {
            repo = repo.substring(0, repo.length() - 4);
        }

        return new String[] { owner, repo };
    }

    /**
     * GitHub API로 리포지토리 정보 조회
     */
    private RepositoryInfo fetchRepositoryInfo(String accessToken, String owner, String repo) {
        try {
            String response = webClient.get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .header(HttpHeaders.AUTHORIZATION, "token " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);

            RepositoryInfo info = new RepositoryInfo();
            info.setName(jsonNode.get("name").asText());
            info.setFullName(jsonNode.get("full_name").asText());
            info.setDescription(jsonNode.has("description") && !jsonNode.get("description").isNull()
                    ? jsonNode.get("description").asText()
                    : null);
            info.setLanguage(
                    jsonNode.has("language") && !jsonNode.get("language").isNull() ? jsonNode.get("language").asText()
                            : null);
            info.setIsPrivate(jsonNode.get("private").asBoolean());
            info.setDefaultBranch(jsonNode.get("default_branch").asText());
            info.setStarsCount(jsonNode.get("stargazers_count").asInt());
            info.setForksCount(jsonNode.get("forks_count").asInt());
            info.setOpenIssuesCount(jsonNode.get("open_issues_count").asInt());

            return info;

        } catch (Exception e) {
            throw new RuntimeException("리포지토리 정보를 가져올 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * GitHub API로 사용자 정보 조회
     */
    private GitHubUserInfo fetchGitHubUserInfo(String accessToken) {
        try {
            String response = webClient.get()
                    .uri("/user")
                    .header(HttpHeaders.AUTHORIZATION, "token " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);

            GitHubUserInfo info = new GitHubUserInfo();
            info.setId(jsonNode.get("id").asLong());
            info.setLogin(jsonNode.get("login").asText());
            info.setAvatarUrl(jsonNode.get("avatar_url").asText());

            return info;

        } catch (Exception e) {
            throw new RuntimeException("GitHub 사용자 정보를 가져올 수 없습니다: " + e.getMessage());
        }
    }

    // 내부 클래스들
    private static class RepositoryInfo {
        private String name;
        private String fullName;
        private String description;
        private String language;
        private Boolean isPrivate;
        private String defaultBranch;
        private Integer starsCount;
        private Integer forksCount;
        private Integer openIssuesCount;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public Boolean getIsPrivate() {
            return isPrivate;
        }

        public void setIsPrivate(Boolean isPrivate) {
            this.isPrivate = isPrivate;
        }

        public String getDefaultBranch() {
            return defaultBranch;
        }

        public void setDefaultBranch(String defaultBranch) {
            this.defaultBranch = defaultBranch;
        }

        public Integer getStarsCount() {
            return starsCount;
        }

        public void setStarsCount(Integer starsCount) {
            this.starsCount = starsCount;
        }

        public Integer getForksCount() {
            return forksCount;
        }

        public void setForksCount(Integer forksCount) {
            this.forksCount = forksCount;
        }

        public Integer getOpenIssuesCount() {
            return openIssuesCount;
        }

        public void setOpenIssuesCount(Integer openIssuesCount) {
            this.openIssuesCount = openIssuesCount;
        }
    }

    private static class GitHubUserInfo {
        private Long id;
        private String login;
        private String avatarUrl;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}