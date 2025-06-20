package com.assistivehub.service;

import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;
import java.util.Collections;

@Service
public class OpenAIValidationService {

    private static final String OPENAI_API_BASE_URL = "https://api.openai.com/v1";
    private static final String MODELS_ENDPOINT = "/models";
    private static final int TIMEOUT_SECONDS = 10;

    private final RestTemplate restTemplate;

    public OpenAIValidationService() {
        // 타임아웃 설정을 위한 RequestFactory 구성
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_SECONDS * 1000);
        factory.setReadTimeout(TIMEOUT_SECONDS * 1000);

        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * OpenAI API 키의 유효성을 검증합니다.
     * 
     * @param apiKey 검증할 OpenAI API 키
     * @return OpenAIValidationResult 검증 결과
     */
    public OpenAIValidationResult validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return new OpenAIValidationResult(false, "API 키가 비어있습니다.");
        }

        if (!apiKey.startsWith("sk-")) {
            return new OpenAIValidationResult(false, "유효하지 않은 OpenAI API 키 형식입니다. 'sk-'로 시작해야 합니다.");
        }

        try {
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // OpenAI API 호출 (models 엔드포인트는 가장 가벼운 테스트용)
            String url = OPENAI_API_BASE_URL + MODELS_ENDPOINT;
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            // 성공적으로 응답받았다면 유효한 키
            if (response.getStatusCode() == HttpStatus.OK) {
                return new OpenAIValidationResult(true, "유효한 OpenAI API 키입니다.");
            } else {
                return new OpenAIValidationResult(false, "OpenAI API에서 예상치 못한 응답이 반환되었습니다.");
            }

        } catch (HttpClientErrorException e) {
            // HTTP 4xx 에러 처리
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return new OpenAIValidationResult(false, "유효하지 않은 OpenAI API 키입니다. 키를 확인해주세요.");
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return new OpenAIValidationResult(false, "API 키에 필요한 권한이 없습니다.");
            } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                return new OpenAIValidationResult(false, "API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
            } else {
                return new OpenAIValidationResult(false, "OpenAI API 키 검증 중 오류가 발생했습니다: " + e.getMessage());
            }

        } catch (ResourceAccessException e) {
            // 네트워크 타임아웃 등
            return new OpenAIValidationResult(false, "OpenAI API 연결 시간이 초과되었습니다. 네트워크 연결을 확인해주세요.");

        } catch (Exception e) {
            // 기타 예외
            return new OpenAIValidationResult(false, "OpenAI API 키 검증 중 예상치 못한 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * OpenAI API 키 검증 결과를 담는 클래스
     */
    public static class OpenAIValidationResult {
        private final boolean valid;
        private final String message;

        public OpenAIValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "OpenAIValidationResult{valid=" + valid + ", message='" + message + "'}";
        }
    }
}