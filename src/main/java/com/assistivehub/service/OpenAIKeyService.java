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

    /**
     * OpenAI 키 생성
     */
    public OpenAIKeyResponse createOpenAIKey(Long userId, OpenAIKeyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 키 이름 중복 체크
        if (request.getKeyName() != null &&
                openAIKeyRepository.existsByUserAndKeyName(user, request.getKeyName())) {
            throw new RuntimeException("이미 존재하는 키 이름입니다.");
        }

        // API 키 암호화
        String encryptedKey = encryptionUtil.encrypt(request.getApiKey());

        OpenAIKey openAIKey = new OpenAIKey(user, encryptedKey, request.getKeyName());
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
     * OpenAI 키 수정
     */
    public OpenAIKeyResponse updateOpenAIKey(Long userId, Long keyId, OpenAIKeyUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        OpenAIKey openAIKey = openAIKeyRepository.findByIdAndUser(keyId, user)
                .orElseThrow(() -> new RuntimeException("OpenAI 키를 찾을 수 없습니다."));

        // 키 이름 중복 체크 (현재 키 제외)
        if (request.getKeyName() != null) {
            Optional<OpenAIKey> existingKey = openAIKeyRepository
                    .findByUserAndKeyNameExcludingId(user, request.getKeyName(), keyId);
            if (existingKey.isPresent()) {
                throw new RuntimeException("이미 존재하는 키 이름입니다.");
            }
            openAIKey.setKeyName(request.getKeyName());
        }

        // API 키 업데이트 (새 키가 제공된 경우만)
        String decryptedKey;
        if (request.getApiKey() != null && !request.getApiKey().trim().isEmpty()) {
            String encryptedKey = encryptionUtil.encrypt(request.getApiKey());
            openAIKey.setEncryptedKey(encryptedKey);
            decryptedKey = request.getApiKey();
        } else {
            decryptedKey = encryptionUtil.decrypt(openAIKey.getEncryptedKey());
        }

        // 활성 상태 업데이트
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
}