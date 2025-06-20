package com.assistivehub.service;

import com.assistivehub.dto.OpenAIKeyRequest;
import com.assistivehub.dto.OpenAIKeyResponse;
import com.assistivehub.dto.OpenAIKeyUpdateRequest;
import com.assistivehub.entity.OpenAIKey;
import com.assistivehub.entity.User;
import com.assistivehub.repository.OpenAIKeyRepository;
import com.assistivehub.repository.UserRepository;
import com.assistivehub.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OpenAIKeyService {

    @Autowired
    private OpenAIKeyRepository openAIKeyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private OpenAIValidationService openAIValidationService;

    private static final int MAX_KEYS_PER_USER = 5;

    /**
     * OpenAI 키 생성 (키 유효성 검증 포함)
     */
    public OpenAIKeyResponse createOpenAIKey(Long userId, OpenAIKeyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 사용자당 키 개수 제한 (최대 5개)
        long totalKeyCount = openAIKeyRepository.countByUser(user);
        if (totalKeyCount >= MAX_KEYS_PER_USER) {
            throw new RuntimeException("사용자당 최대 " + MAX_KEYS_PER_USER + "개의 OpenAI 키만 등록할 수 있습니다.");
        }

        // 1. OpenAI API 키 유효성 검증
        OpenAIValidationService.OpenAIValidationResult validationResult = openAIValidationService
                .validateApiKey(request.getApiKey());

        if (!validationResult.isValid()) {
            throw new RuntimeException("사용할 수 없는 OpenAI API 키입니다: " + validationResult.getMessage());
        }

        // 2. 키 이름 중복 체크
        if (request.getKeyName() != null &&
                openAIKeyRepository.existsByUserAndKeyName(user, request.getKeyName())) {
            throw new RuntimeException("이미 존재하는 키 이름입니다.");
        }

        // 3. 기존 활성 키가 없으면 첫 번째 키를 활성으로 설정
        boolean hasActiveKey = openAIKeyRepository.countByUserAndIsActiveTrue(user) > 0;
        boolean shouldActivate = !hasActiveKey;

        // 4. API 키 암호화
        String encryptedKey = encryptionUtil.encrypt(request.getApiKey());

        // 5. 저장
        OpenAIKey openAIKey = new OpenAIKey(user, encryptedKey, request.getKeyName());
        openAIKey.setIsActive(shouldActivate);
        OpenAIKey savedKey = openAIKeyRepository.save(openAIKey);

        return new OpenAIKeyResponse(savedKey, request.getApiKey());
    }

    /**
     * 사용자의 모든 OpenAI 키 조회
     */
    @Transactional(readOnly = true)
    public List<OpenAIKeyResponse> getUserOpenAIKeys(Long userId, boolean activeOnly) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<OpenAIKey> keys;
        if (activeOnly) {
            keys = openAIKeyRepository.findByUserAndIsActiveTrue(user);
        } else {
            keys = openAIKeyRepository.findByUser(user);
        }

        return keys.stream()
                .map(key -> {
                    String decryptedKey = encryptionUtil.decrypt(key.getEncryptedKey());
                    return new OpenAIKeyResponse(key, decryptedKey);
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 OpenAI 키 조회
     */
    @Transactional(readOnly = true)
    public OpenAIKeyResponse getOpenAIKey(Long userId, Long keyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        OpenAIKey openAIKey = openAIKeyRepository.findByIdAndUser(keyId, user)
                .orElseThrow(() -> new RuntimeException("OpenAI 키를 찾을 수 없습니다."));

        String decryptedKey = encryptionUtil.decrypt(openAIKey.getEncryptedKey());
        return new OpenAIKeyResponse(openAIKey, decryptedKey);
    }

    /**
     * OpenAI 키 수정 (키 유효성 검증 포함)
     */
    public OpenAIKeyResponse updateOpenAIKey(Long userId, Long keyId, OpenAIKeyUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        OpenAIKey openAIKey = openAIKeyRepository.findByIdAndUser(keyId, user)
                .orElseThrow(() -> new RuntimeException("OpenAI 키를 찾을 수 없습니다."));

        // 1. 새로운 API 키가 제공된 경우 유효성 검증
        String decryptedKey;
        if (request.getApiKey() != null && !request.getApiKey().trim().isEmpty()) {
            // OpenAI API 키 유효성 검증
            OpenAIValidationService.OpenAIValidationResult validationResult = openAIValidationService
                    .validateApiKey(request.getApiKey());

            if (!validationResult.isValid()) {
                throw new RuntimeException("사용할 수 없는 OpenAI API 키입니다: " + validationResult.getMessage());
            }

            String encryptedKey = encryptionUtil.encrypt(request.getApiKey());
            openAIKey.setEncryptedKey(encryptedKey);
            decryptedKey = request.getApiKey();
        } else {
            decryptedKey = encryptionUtil.decrypt(openAIKey.getEncryptedKey());
        }

        // 2. 키 이름 중복 체크 (현재 키 제외)
        if (request.getKeyName() != null) {
            Optional<OpenAIKey> existingKey = openAIKeyRepository
                    .findByUserAndKeyNameExcludingId(user, request.getKeyName(), keyId);
            if (existingKey.isPresent()) {
                throw new RuntimeException("이미 존재하는 키 이름입니다.");
            }
            openAIKey.setKeyName(request.getKeyName());
        }

        // 3. 활성 상태 업데이트
        if (request.getIsActive() != null) {
            openAIKey.setIsActive(request.getIsActive());
        }

        OpenAIKey updatedKey = openAIKeyRepository.save(openAIKey);
        return new OpenAIKeyResponse(updatedKey, decryptedKey);
    }

    /**
     * OpenAI 키 삭제 (소프트 삭제 - 비활성화)
     */
    public void deactivateOpenAIKey(Long userId, Long keyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        OpenAIKey openAIKey = openAIKeyRepository.findByIdAndUser(keyId, user)
                .orElseThrow(() -> new RuntimeException("OpenAI 키를 찾을 수 없습니다."));

        openAIKey.setIsActive(false);
        openAIKeyRepository.save(openAIKey);
    }

    /**
     * OpenAI 키 완전 삭제
     */
    public void deleteOpenAIKey(Long userId, Long keyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        OpenAIKey openAIKey = openAIKeyRepository.findByIdAndUser(keyId, user)
                .orElseThrow(() -> new RuntimeException("OpenAI 키를 찾을 수 없습니다."));

        openAIKeyRepository.delete(openAIKey);
    }

    /**
     * 복호화된 OpenAI 키 반환 (실제 사용 시)
     */
    @Transactional(readOnly = true)
    public String getDecryptedApiKey(Long userId, Long keyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        OpenAIKey openAIKey = openAIKeyRepository.findByIdAndUserAndIsActiveTrue(keyId, user)
                .orElseThrow(() -> new RuntimeException("활성화된 OpenAI 키를 찾을 수 없습니다."));

        return encryptionUtil.decrypt(openAIKey.getEncryptedKey());
    }

    /**
     * 사용자의 활성 OpenAI 키 개수 조회
     */
    @Transactional(readOnly = true)
    public long countActiveKeys(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return openAIKeyRepository.countByUserAndIsActiveTrue(user);
    }

    /**
     * 특정 OpenAI 키를 활성화 (다른 키들은 자동 비활성화)
     */
    public OpenAIKeyResponse activateOpenAIKey(Long userId, Long keyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        OpenAIKey targetKey = openAIKeyRepository.findByIdAndUser(keyId, user)
                .orElseThrow(() -> new RuntimeException("OpenAI 키를 찾을 수 없습니다."));

        // 1. 해당 사용자의 모든 키를 비활성화
        List<OpenAIKey> allUserKeys = openAIKeyRepository.findByUser(user);
        for (OpenAIKey key : allUserKeys) {
            key.setIsActive(false);
        }

        // 2. 선택한 키만 활성화
        targetKey.setIsActive(true);

        // 3. 모든 변경사항 저장
        openAIKeyRepository.saveAll(allUserKeys);

        String decryptedKey = encryptionUtil.decrypt(targetKey.getEncryptedKey());
        return new OpenAIKeyResponse(targetKey, decryptedKey);
    }

    /**
     * 현재 활성화된 OpenAI 키 조회
     */
    @Transactional(readOnly = true)
    public OpenAIKeyResponse getActiveOpenAIKey(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<OpenAIKey> activeKeys = openAIKeyRepository.findByUserAndIsActiveTrue(user);

        if (activeKeys.isEmpty()) {
            throw new RuntimeException("활성화된 OpenAI 키가 없습니다.");
        }

        // 활성 키는 1개만 있어야 하지만, 혹시 여러 개가 있다면 첫 번째 반환
        OpenAIKey activeKey = activeKeys.get(0);
        String decryptedKey = encryptionUtil.decrypt(activeKey.getEncryptedKey());
        return new OpenAIKeyResponse(activeKey, decryptedKey);
    }
}