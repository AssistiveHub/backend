package com.assistivehub.repository;

import com.assistivehub.entity.OpenAIKey;
import com.assistivehub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpenAIKeyRepository extends JpaRepository<OpenAIKey, Long> {

    /**
     * 특정 사용자의 모든 OpenAI 키 조회
     */
    List<OpenAIKey> findByUser(User user);

    /**
     * 특정 사용자의 활성 상태인 OpenAI 키만 조회
     */
    List<OpenAIKey> findByUserAndIsActiveTrue(User user);

    /**
     * 특정 사용자의 OpenAI 키를 ID로 조회
     */
    Optional<OpenAIKey> findByIdAndUser(Long id, User user);

    /**
     * 특정 사용자의 활성 상태인 OpenAI 키를 ID로 조회
     */
    Optional<OpenAIKey> findByIdAndUserAndIsActiveTrue(Long id, User user);

    /**
     * 특정 사용자의 OpenAI 키 개수 조회
     */
    long countByUser(User user);

    /**
     * 특정 사용자의 활성 상태인 OpenAI 키 개수 조회
     */
    long countByUserAndIsActiveTrue(User user);

    /**
     * 특정 사용자의 키 이름으로 OpenAI 키 존재 여부 확인
     */
    boolean existsByUserAndKeyName(User user, String keyName);

    /**
     * 특정 사용자의 키 이름으로 OpenAI 키 조회 (수정 시 중복 체크용)
     */
    @Query("SELECT ok FROM OpenAIKey ok WHERE ok.user = :user AND ok.keyName = :keyName AND ok.id != :excludeId")
    Optional<OpenAIKey> findByUserAndKeyNameExcludingId(@Param("user") User user,
            @Param("keyName") String keyName,
            @Param("excludeId") Long excludeId);
}