package com.assistivehub.repository;

import com.assistivehub.entity.GitHubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GitHubRepositoryRepository extends JpaRepository<GitHubRepository, Long> {

    /**
     * 특정 사용자의 모든 깃허브 리포지토리 조회
     */
    @Query("SELECT g FROM GitHubRepository g WHERE g.integratedService.user.id = :userId")
    List<GitHubRepository> findByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 활성 깃허브 리포지토리 조회
     */
    @Query("SELECT g FROM GitHubRepository g WHERE g.integratedService.user.id = :userId AND g.integratedService.isActive = true")
    List<GitHubRepository> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 사용자 ID와 리포지토리 ID로 깃허브 리포지토리 조회
     */
    @Query("SELECT g FROM GitHubRepository g WHERE g.integratedService.user.id = :userId AND g.id = :repositoryId")
    Optional<GitHubRepository> findByUserIdAndRepositoryId(@Param("userId") Long userId,
            @Param("repositoryId") Long repositoryId);

    /**
     * 리포지토리 Full Name으로 조회
     */
    Optional<GitHubRepository> findByRepositoryFullName(String repositoryFullName);

    /**
     * 특정 사용자의 특정 리포지토리 조회 (중복 방지)
     */
    @Query("SELECT g FROM GitHubRepository g WHERE g.integratedService.user.id = :userId AND g.repositoryFullName = :repositoryFullName")
    Optional<GitHubRepository> findByUserIdAndRepositoryFullName(@Param("userId") Long userId,
            @Param("repositoryFullName") String repositoryFullName);

    /**
     * GitHub 사용자 ID로 리포지토리 목록 조회
     */
    List<GitHubRepository> findByGithubUserId(String githubUserId);
}