package com.assistivehub.repository;

import com.assistivehub.entity.UserSetting;
import com.assistivehub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {

    /**
     * 특정 사용자의 설정 조회
     */
    Optional<UserSetting> findByUser(User user);

    /**
     * 사용자 ID로 설정 조회
     */
    Optional<UserSetting> findByUserId(Long userId);

    /**
     * 특정 사용자의 설정 존재 여부 확인
     */
    boolean existsByUser(User user);

    /**
     * 사용자 ID로 설정 존재 여부 확인
     */
    boolean existsByUserId(Long userId);
}