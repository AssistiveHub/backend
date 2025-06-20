package com.assistivehub.service;

import com.assistivehub.dto.UserSettingRequest;
import com.assistivehub.dto.UserSettingResponse;
import com.assistivehub.entity.User;
import com.assistivehub.entity.UserSetting;
import com.assistivehub.repository.UserRepository;
import com.assistivehub.repository.UserSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserSettingService {

    @Autowired
    private UserSettingRepository userSettingRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 사용자 설정 조회 (없으면 기본 설정 생성)
     */
    @Transactional(readOnly = true)
    public UserSettingResponse getUserSetting(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserSetting userSetting = userSettingRepository.findByUser(user)
                .orElseGet(() -> createDefaultUserSetting(user));

        return new UserSettingResponse(userSetting);
    }

    /**
     * 사용자 설정 업데이트
     */
    public UserSettingResponse updateUserSetting(Long userId, UserSettingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserSetting userSetting = userSettingRepository.findByUser(user)
                .orElseGet(() -> createDefaultUserSetting(user));

        // null이 아닌 값들만 업데이트 (부분 업데이트 지원)
        if (request.getWeeklyWorkLogEnabled() != null) {
            userSetting.setWeeklyWorkLogEnabled(request.getWeeklyWorkLogEnabled());
        }
        if (request.getDailyWorkLogEnabled() != null) {
            userSetting.setDailyWorkLogEnabled(request.getDailyWorkLogEnabled());
        }
        if (request.getFeedbackReviewEnabled() != null) {
            userSetting.setFeedbackReviewEnabled(request.getFeedbackReviewEnabled());
        }
        if (request.getSkillGrowthTrackingEnabled() != null) {
            userSetting.setSkillGrowthTrackingEnabled(request.getSkillGrowthTrackingEnabled());
        }
        if (request.getNotificationManagementEnabled() != null) {
            userSetting.setNotificationManagementEnabled(request.getNotificationManagementEnabled());
        }

        UserSetting updatedSetting = userSettingRepository.save(userSetting);
        return new UserSettingResponse(updatedSetting);
    }

    /**
     * 사용자 설정 초기화 (기본값으로 리셋)
     */
    public UserSettingResponse resetUserSetting(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserSetting userSetting = userSettingRepository.findByUser(user)
                .orElseGet(() -> new UserSetting(user));

        // 모든 설정을 기본값(true)으로 리셋
        userSetting.setWeeklyWorkLogEnabled(true);
        userSetting.setDailyWorkLogEnabled(true);
        userSetting.setFeedbackReviewEnabled(true);
        userSetting.setSkillGrowthTrackingEnabled(true);
        userSetting.setNotificationManagementEnabled(true);

        UserSetting resetSetting = userSettingRepository.save(userSetting);
        return new UserSettingResponse(resetSetting);
    }

    /**
     * 특정 기능만 토글 (on/off 전환)
     */
    public UserSettingResponse toggleSetting(Long userId, String settingName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserSetting userSetting = userSettingRepository.findByUser(user)
                .orElseGet(() -> createDefaultUserSetting(user));

        switch (settingName.toLowerCase()) {
            case "weeklyworklog":
                userSetting.setWeeklyWorkLogEnabled(!userSetting.getWeeklyWorkLogEnabled());
                break;
            case "dailyworklog":
                userSetting.setDailyWorkLogEnabled(!userSetting.getDailyWorkLogEnabled());
                break;
            case "feedbackreview":
                userSetting.setFeedbackReviewEnabled(!userSetting.getFeedbackReviewEnabled());
                break;
            case "skillgrowthtracking":
                userSetting.setSkillGrowthTrackingEnabled(!userSetting.getSkillGrowthTrackingEnabled());
                break;
            case "notificationmanagement":
                userSetting.setNotificationManagementEnabled(!userSetting.getNotificationManagementEnabled());
                break;
            default:
                throw new RuntimeException("유효하지 않은 설정 이름입니다: " + settingName);
        }

        UserSetting toggledSetting = userSettingRepository.save(userSetting);
        return new UserSettingResponse(toggledSetting);
    }

    /**
     * 기본 사용자 설정 생성
     */
    private UserSetting createDefaultUserSetting(User user) {
        UserSetting defaultSetting = new UserSetting(user);
        // 기본값은 엔티티에서 이미 true로 설정되어 있음
        return userSettingRepository.save(defaultSetting);
    }

    /**
     * 사용자 설정 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean hasUserSetting(Long userId) {
        return userSettingRepository.existsByUserId(userId);
    }
}