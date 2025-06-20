# AssistiveHub Backend

장애인 보조 기술 플랫폼의 백엔드 API 서버입니다.

## 🚀 주요 기능

- **사용자 인증**: JWT 기반 회원가입/로그인
- **OpenAI 키 관리**: 사용자별 OpenAI API 키 암호화 저장 및 관리
- **보안**: AES 대칭키 암호화를 통한 민감정보 보호

## ⚙️ 환경 설정

### 로컬 개발 환경

로컬에서 개발할 때는 `application-local.yml`을 사용합니다:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 운영 환경

운영 환경에서는 다음 환경변수를 설정해야 합니다:

```bash
# 데이터베이스 설정
export DB_URL="jdbc:mariadb://your-database-host:3306/your-database-name"
export DB_USERNAME="your-database-username"
export DB_PASSWORD="your-database-password"

# JWT 설정
export JWT_SECRET="your-jwt-secret-key-should-be-very-long-and-secure"
export JWT_EXPIRATION="86400000"

# 암호화 설정
export ENCRYPTION_SECRET_KEY="your-32-character-encryption-key!@#$"
```

또는 `.env` 파일 생성:

```env
DB_URL=jdbc:mariadb://your-database-host:3306/your-database-name
DB_USERNAME=your-database-username
DB_PASSWORD=your-database-password
JWT_SECRET=your-jwt-secret-key-should-be-very-long-and-secure
JWT_EXPIRATION=86400000
ENCRYPTION_SECRET_KEY=your-32-character-encryption-key!@#$
```

## 🔐 OpenAI 키 관리 API

### 주요 엔드포인트

```bash
# 키 생성
POST /api/openai-keys
Authorization: Bearer {jwt_token}

# 키 목록 조회
GET /api/openai-keys?activeOnly=true
Authorization: Bearer {jwt_token}

# 특정 키 조회
GET /api/openai-keys/{keyId}
Authorization: Bearer {jwt_token}

# 키 수정
PUT /api/openai-keys/{keyId}
Authorization: Bearer {jwt_token}

# 키 삭제
DELETE /api/openai-keys/{keyId}
Authorization: Bearer {jwt_token}

# 복호화된 키 조회 (실제 사용)
GET /api/openai-keys/{keyId}/decrypt
Authorization: Bearer {jwt_token}
```

### 보안 특징

- **AES 대칭키 암호화**: 모든 OpenAI API 키는 암호화되어 데이터베이스에 저장
- **마스킹 처리**: API 응답 시 키의 일부만 표시 (sk-1234\*\*\*\*abcd)
- **사용자별 격리**: 각 사용자는 자신의 키만 접근 가능
- **복호화 가능**: 실제 OpenAI API 호출 시 복호화하여 사용

## 🛠️ 실행 방법

```bash
# 로컬 환경 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는 IDE에서 VM options에 추가:
-Dspring.profiles.active=local
```

## ⚠️ 보안 주의사항

1. **환경변수 관리**: 민감한 정보는 반드시 환경변수로 관리
2. **키 길이**: 암호화 키는 최소 32자 이상 사용
3. **Git 관리**: `.env` 파일이나 실제 키 값들은 절대 Git에 커밋하지 말 것
4. **프로덕션 환경**: 운영 환경에서는 더욱 강력한 암호화 키 사용 권장
