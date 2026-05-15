# Review Google Sheet UAM Theo Tiêu Chí Production

Ngày review: 2026-05-15

Nguồn review:
- Auth service sheet: `gid=0`, 18 test cases
- User service sheet: `gid=936663858`, 25 test cases
- Guideline: `UAM_guideline_v1.2.pdf`

## Kết Luận Nhanh

Bộ test hiện tại đạt mức khá ở tầng unit/API cơ bản: đã có happy path, validation DTO, duplicate username/email, login sai thông tin, RBAC 401/403, mapping DTO không lộ password, profile/update/delete có các case lỗi chính.

Tuy nhiên, nếu review theo tiêu chí production và guideline v1.2 thì bộ test **chưa đạt production-ready**. Các vùng thiếu lớn nằm ở authentication hardening, vòng đời JWT, integration/contract test, profile Docker/Flyway/cache/monitoring/CI, pagination edge cases và audit/logging.

Đề xuất mức đánh giá: **6.5/10**.

## Điểm Tốt

- Auth service có cover registration/login theo guideline: `/register`, `/login`, BCrypt/password encode, duplicate username/email, response integrity.
- Có validation username/password/email/fullName ở mức DTO.
- Login sai username và sai password đều trả thông điệp chung, đúng hướng bảo mật.
- User service có cover role ADMIN vs USER, unauthenticated request, profile isolation, update conflict, delete authorization.
- Có ý thức không trả về trường nhạy cảm như password/secretKey trong `UserResponse`.

## Finding Cần Sửa Trước Khi Chấm Production

### P0 - Chưa đủ test cho JWT/security lifecycle

Sheet Auth chỉ test login thành công và lỗi tạo token, chưa test vòng đời JWT trong production:
- token hết hạn
- token bị sửa signature/claims
- token sai prefix hoặc thiếu `Bearer`
- malformed token
- token của user đã bị disable/deleted
- role trong token không còn khớp DB
- logout/blacklist token nếu hệ thống có refresh token/session revocation

Tác động: production có thể chấp nhận token không hợp lệ, xử lý lỗi security không nhất quán, hoặc không phát hiện regression trong filter/security chain.

Đề xuất bổ sung:
- AUTH-JWT-01 expired token -> 401
- AUTH-JWT-02 tampered token -> 401
- AUTH-JWT-03 malformed Authorization header -> 401
- AUTH-JWT-04 valid token but missing required role -> 403
- AUTH-JWT-05 user disabled/deleted after token issued -> 401/403 theo rule

### P0 - Thiếu rate limit/brute force cho login/register

Guideline yêu cầu JWT authentication và password encryption, nhưng production auth cần có brute-force protection. Sheet chưa có case:
- nhiều lần login sai liên tiếp
- register spam cùng IP/email
- lock/cooldown/captcha/rate-limit response

Tác động: login endpoint dễ bị brute force/credential stuffing.

Đề xuất bổ sung test cho HTTP 429 hoặc cơ chế lock account tạm thời, kèm verify log/audit không lộ password.

### P1 - Thiếu integration test với Spring Security filter chain

Nhiều case ghi "Mock findByUsername", "Mock passwordEncoder", "Mock UserDetails". Cách này tốt cho unit test service, nhưng chưa đủ production vì bug thường nằm ở filter chain, annotation `@PreAuthorize`, exception handler, serialization và validation.

Đề xuất:
- Thêm MockMvc/WebMvcTest cho controller + security.
- Thêm SpringBootTest/Testcontainers PostgreSQL cho register-login-profile flow end-to-end.
- Test request thực sự có header Authorization, JSON body, validation error format.

### P1 - Endpoint trong sheet lệch với guideline, cần xác nhận

Guideline dùng `/auth/register`, `/auth/login`, user-service dùng `/users`, `/users/me`, `PUT /users/me`, `DELETE /users/{username}`. Sheet đang dùng:
- `/api/v1/auth/register`, `/api/v1/auth/login`
- `/api/v1/users/all`, `/api/v1/users/profile`

Đây có thể là convention riêng của project, không sai nếu implementation đã chốt. Nhưng cần ghi rõ API contract, vì review theo guideline sẽ bị coi là mismatch nếu không có quyết định về mapping/versioning.

### P1 - User service pagination/sorting chưa đủ production

User sheet có pagination happy path, empty list, nhưng thiếu edge cases:
- `page < 0`
- `size = 0`, `size` quá lớn
- sort field không hợp lệ
- page vượt quá tổng số page
- default page/size khi không truyền

Tác động: API list user dễ lỗi 500, leak stacktrace, hoặc query quá lớn.

### P1 - Update profile thiếu input boundary và authorization nuance

Hiện có email invalid, fullName blank, duplicate email. Còn thiếu:
- fullName quá dài, ký tự đặc biệt, trim whitespace
- email uppercase/lowercase normalization
- request body rỗng/null
- unknown fields trong JSON
- user A không được update/read profile user B
- concurrent update/lost update nếu có versioning

### P1 - Delete user cần chốt business rule và error semantics

Case "Admin tự xóa mình" đang ghi "HTTP 204 hoặc chặn tùy logic" là chưa đủ để thành test case executable. Case transactional ghi expected `HTTP 500, dữ liệu không bị xóa` cũng cần làm rõ trigger lỗi và verify rollback.

Đề xuất:
- Chốt rule self-delete: allow hoặc block 400/409.
- Nếu delete user không tồn tại, chốt 404 hay idempotent 204.
- Test rollback bằng lỗi có kiểm soát, không assert chung chung 500 nếu có global exception contract.

### P1 - Thiếu các phần guideline ngoài API core

Guideline v1.2 yêu cầu thêm:
- multi-profile config: `application-dev.yaml`, `application-prod.yaml`, `application-test.yaml`
- Dockerfile + Docker Compose
- Flyway migration
- Swagger/OpenAPI
- unit test service/controller
- GitLab CI/CD
- monitoring/logging với Actuator/Prometheus/Logback
- caching Caffeine/Redis

Sheet hiện tại mới review Auth/User API logic, chưa có test/acceptance criteria cho các phần trên.

Đề xuất thêm nhóm test:
- CONFIG: profile dev/test/prod/docker load đúng datasource/port/secrets
- DOCKER: `docker-compose up` start đủ auth/user/postgres/redis
- FLYWAY: migration từ DB rỗng, repeat migration, failed migration rollback behavior
- SWAGGER: OpenAPI expose đúng endpoint/schema/security
- CI: pipeline build/test/package/docker pass
- OBS: actuator health/readiness, structured logs, no sensitive logs
- CACHE: cache hit/miss, invalidation after update/delete

## Finding Nhỏ Hơn

- Test case ID trong User sheet bị lặp lại theo từng nhóm (`TC01`, `TC02`...). Nên đổi thành `USER-LIST-01`, `USER-PROFILE-01`, `USER-UPDATE-01`, `USER-DELETE-01` để trace bug/deployment rõ hơn.
- Method nên viết chuẩn HTTP uppercase: `POST`, `GET`, `PUT`, `DELETE`.
- Cột `Result` đang trống toàn bộ, nên tách thành `Actual Result`, `Status`, `Evidence`, `Executed By`, `Executed At`.
- Một số expected output còn mơ hồ: "HTTP 500 hoặc ngoại lệ JWT tương ứng", "HTTP 204 hoặc chặn tùy logic". Production test nên có expected duy nhất.
- Test log "Fetching users..." không nên là acceptance production nếu chỉ check console text. Nên test structured log/event/audit behavior và đảm bảo không log PII/secret.

## Checklist Bổ Sung Ưu Tiên

1. Thêm JWT negative tests: expired, tampered, malformed, missing Bearer, role mismatch.
2. Thêm brute-force/rate-limit tests cho login/register.
3. Thêm integration tests qua Spring Security filter chain thay vì chỉ mock service.
4. Chốt API contract endpoint/versioning so với guideline.
5. Bổ sung pagination boundary và request body malformed/null.
6. Chốt delete self/non-existing semantics.
7. Thêm guideline coverage cho Docker, profiles, Flyway, Swagger, CI/CD, monitoring/logging, cache.
8. Chuẩn hóa ID/status/evidence trong sheet để dùng được như test management artifact.

## Gợi Ý Thêm Test Case Mẫu

| Area | Test case | Expected |
| --- | --- | --- |
| Auth/JWT | Expired access token gọi `/api/v1/users/profile` | 401, error code `TOKEN_EXPIRED` |
| Auth/JWT | Token bị sửa role từ USER sang ADMIN | 401 hoặc 403, không truy cập được admin API |
| Auth/Login | 5 lần sai password trong 1 phút | 429 hoặc account cooldown |
| Auth/Register | Body rỗng `{}` | 400, list validation errors đầy đủ |
| User/List | `page=-1`, `size=10000` | 400, không query quá lớn |
| User/Profile | User A token không đọc/cập nhật được User B | 403/404 theo rule, không leak data |
| User/Delete | Admin tự xóa mình | Expected duy nhất theo BA: block 409 hoặc allow 204 |
| Flyway | Start service với DB rỗng | Migration pass, schema đúng |
| Docker | `docker-compose up` | Auth/user/postgres/redis healthy |
| Observability | Health/readiness endpoint | 200 khi dependency ready, 503 khi DB down |

