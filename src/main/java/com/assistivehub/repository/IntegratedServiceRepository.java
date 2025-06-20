package com.assistivehub.repository;

import com.assistivehub.entity.IntegratedService;
import com.assistivehub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegratedServiceRepository extends JpaRepository<IntegratedService, Long> {

        /**
         * 사용자의 모든 연동 서비스 조회
         */
        List<IntegratedService> findByUserOrderByCreatedAtDesc(User user);

        /**
         * 사용자의 활성 연동 서비스 조회
         */
        List<IntegratedService> findByUserAndIsActiveTrueOrderByCreatedAtDesc(User user);

        /**
         * 사용자의 특정 서비스 타입 조회
         */
        List<IntegratedService> findByUserAndServiceTypeOrderByCreatedAtDesc(User user,
                        IntegratedService.ServiceType serviceType);

        /**
         * 사용자의 특정 서비스 타입 중 활성 서비스 조회
         */
        List<IntegratedService> findByUserAndServiceTypeAndIsActiveTrueOrderByCreatedAtDesc(User user,
                        IntegratedService.ServiceType serviceType);

        /**
         * 사용자의 특정 워크스페이스 서비스 조회
         */
        Optional<IntegratedService> findByUserAndServiceTypeAndWorkspaceId(User user,
                        IntegratedService.ServiceType serviceType, String workspaceId);

        /**
         * 사용자 ID와 서비스 ID로 조회
         */
        @Query("SELECT i FROM IntegratedService i WHERE i.user.id = :userId AND i.id = :serviceId")
        Optional<IntegratedService> findByUserIdAndServiceId(@Param("userId") Long userId,
                        @Param("serviceId") Long serviceId);

        /**
         * 사용자의 연동 서비스 개수 조회
         */
        long countByUserAndServiceType(User user, IntegratedService.ServiceType serviceType);

        /**
         * 사용자의 활성 연동 서비스 개수 조회
         */
        long countByUserAndServiceTypeAndIsActiveTrue(User user, IntegratedService.ServiceType serviceType);

        /**
         * 사용자 ID로 연동 서비스 조회
         */
        @Query("SELECT i FROM IntegratedService i WHERE i.user.id = :userId ORDER BY i.createdAt DESC")
        List<IntegratedService> findByUserId(@Param("userId") Long userId);

        /**
         * 사용자 ID로 활성 연동 서비스 조회
         */
        @Query("SELECT i FROM IntegratedService i WHERE i.user.id = :userId AND i.isActive = true ORDER BY i.createdAt DESC")
        List<IntegratedService> findActiveByUserId(@Param("userId") Long userId);

        /**
         * 사용자 ID로 연동 서비스 개수 조회
         */
        @Query("SELECT COUNT(i) FROM IntegratedService i WHERE i.user.id = :userId")
        long countByUserId(@Param("userId") Long userId);

        /**
         * 사용자 ID로 활성 연동 서비스 개수 조회
         */
        @Query("SELECT COUNT(i) FROM IntegratedService i WHERE i.user.id = :userId AND i.isActive = true")
        long countActiveByUserId(@Param("userId") Long userId);

        /**
         * 사용자 ID와 연동 ID로 조회
         */
        @Query("SELECT i FROM IntegratedService i WHERE i.user.id = :userId AND i.id = :integrationId")
        Optional<IntegratedService> findByUserIdAndId(@Param("userId") Long userId,
                        @Param("integrationId") Long integrationId);
}