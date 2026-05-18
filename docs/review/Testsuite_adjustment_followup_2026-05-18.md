# Rà Soát Kết Quả Điều Chỉnh UAM Google Sheet

Ngày rà soát: 2026-05-18

Baseline đối chiếu:
- `Review/UAM_GoogleSheet_Production_Review_VI_2026-05-15.md`

Nguồn Google Sheet mới:
- Auth service: `gid=405152142`
- User service: `gid=500456332`
- Infra: `gid=895025748`
- Coverage Summary: `gid=609740402`

## Kết Luận

Kết quả điều chỉnh **đã đáp ứng phần lớn các góp ý về thiết kế bộ test** trong bản review ngày 2026-05-15. Bộ test đã được mở rộng từ phạm vi Auth/User API cơ bản sang bộ test có cấu trúc production hơn, gồm:

- 79 test cases tổng cộng
- 27 P0, 41 P1, 11 P2
- Có thêm nhóm Auth JWT Hardening
- Có thêm nhóm Infra cho Config/Profile, Docker, Flyway, Observability, Cache
- Test case ID đã chuẩn hóa, không còn trùng kiểu `TC01`
- Method đã viết chuẩn uppercase: `GET`, `POST`, `PUT`, `DELETE`
- Có thêm cột `Priority`, `Actual Result / Status`, `Notes / Evidence`

Tuy nhiên, nếu hỏi “đã đủ để chốt production-ready chưa?” thì câu trả lời là **chưa**. Lý do chính: một số evidence còn thiếu, và một số expected result vẫn còn dạng lựa chọn “hoặc/tùy config”, nên chưa đủ để kết luận các test đã được thực thi và passed.

Đánh giá cập nhật:
- Mức thiết kế test case: **8.2/10**
- Mức sẵn sàng làm production release gate: **chưa đạt**, cần bổ sung execution result/evidence và chốt expected result duy nhất cho các case còn mơ hồ.

## Đối Chiếu Theo Finding Cũ

| Finding từ review 2026-05-15 | Trạng thái | Nhận xét |
| --- | --- | --- |
| Thiếu JWT negative/security lifecycle | Đã đáp ứng phần lớn | Đã thêm `AUTH-JWT-01` đến `AUTH-JWT-08`: expired, tampered signature, role tamper, missing Bearer, malformed header, missing header, disabled user, missing role. |
| Thiếu brute force/rate limit | Đã bổ sung | Đã có `AUTH-REG-13` và `AUTH-LOGIN-06`. Tuy nhiên expected còn dùng “HTTP 429 hoặc lock/block”, cần chốt một rule chính thức. |
| Thiếu integration test qua Spring Security filter chain | Đáp ứng một phần | Một số note đã nhắc Security filter chain, nhưng chưa có nhóm test rõ ràng cho MockMvc/WebMvcTest/SpringBootTest/Testcontainers. Nên bổ sung loại test và scope execution. |
| Endpoint lệch guideline cần xác nhận | Chưa xử lý rõ | Sheet vẫn dùng `/api/v1/...`. Điều này có thể đúng nếu project đã chốt API versioning, nhưng chưa thấy test/ghi chú xác nhận API contract so với guideline. |
| Pagination/sorting edge cases | Đã đáp ứng phần lớn | Đã thêm `page < 0`, `size = 0`, `size quá lớn`, invalid sort, page vượt tổng số trang. Một số expected còn có lựa chọn, cần chốt convention. |
| Update profile thiếu boundary/security nuance | Đã đáp ứng phần lớn | Đã thêm fullName quá dài, script injection, body rỗng, unknown fields, user A không update user B. Một vài expected còn “400 hoặc sanitized”, “200 hoặc 400 tùy config”. |
| Delete user cần chốt business rule | Đã đáp ứng tốt hơn | Đã chốt self-delete là block `409 Conflict`, delete user không tồn tại là `404`. Transaction rollback vẫn còn expected `500 hoặc 409`, cần chốt theo implementation. |
| Thiếu Docker/Profile/Flyway/Swagger/CI/Monitoring/Cache | Đáp ứng một phần lớn | Đã có Infra tab cho profiles, Docker, Flyway, Observability, Cache. Tuy nhiên chưa thấy Swagger/OpenAPI và CI/CD trong Infra tab mới. |

## Các Điểm Đã Cải Thiện Rõ

### Auth service

Auth tăng lên 29 test cases, gồm:
- Registration: 13 cases
- Login: 8 cases
- JWT hardening: 8 cases

Các điểm tốt:
- Đã thêm request body rỗng `{}` cho register.
- Đã thêm rate limit cho register và brute-force login.
- Đã thêm JWT expired/tampered/malformed/missing Bearer/missing role/disabled user.
- Expected output đã cụ thể hơn trước, có error code cho nhiều case.
- Không còn ID chung chung như `TC01`.

Điểm cần sửa tiếp:
- `AUTH-REG-13`: cần chốt là `429 Too Many Requests` hay block IP/account.
- `AUTH-LOGIN-06`: cần chốt là `429` hay account lock/cooldown.
- `AUTH-JWT-03`, `AUTH-JWT-07`: cần chốt expected là `401` hay `403`.

### User service

User tăng lên 35 test cases, gồm:
- List all: 11 cases
- Profile: 5 cases
- Update: 12 cases
- Delete: 7 cases

Các điểm tốt:
- Đã bổ sung pagination boundary: negative page, size zero, size quá lớn, invalid sort, page vượt tổng.
- Đã chốt self-delete: `409 Conflict`, `"Cannot delete your own account"`.
- Đã chốt delete user không tồn tại: `404 Not Found`.
- Đã thêm profile/update isolation.
- Đã thêm XSS/script injection và unknown fields.
- Đã thêm audit log cho delete.

Điểm cần sửa tiếp:
- `USER-LIST-08`: chốt `size=0` là `400` hay page rỗng.
- `USER-LIST-09`: chốt size quá lớn là `400` hay cap về max size.
- `USER-UPDATE-07`: chốt input script là reject `400` hay sanitize.
- `USER-UPDATE-09`: chốt unknown fields là ignore `200` hay reject `400`.
- `USER-UPDATE-12`: endpoint `/profile` nên chỉ update token owner; expected nên viết rõ một hành vi duy nhất.
- `USER-DELETE-06`: chốt rollback lỗi DB trả `500` hay `409`.

### Infra / guideline coverage

Infra có 15 test cases, gồm:
- Config/Profile: 3 cases
- Docker/Compose: 2 cases
- Flyway: 3 cases
- Observability: 4 cases
- Cache: 3 cases

Các điểm tốt:
- Đã cover profile dev/test/prod.
- Đã cover Docker Compose startup.
- Đã cover Flyway từ DB rỗng, chạy lại migration, lỗi migration.
- Đã cover health khi DB up/down, structured log, Prometheus.
- Đã cover cache hit và cache invalidation sau update/delete.

Điểm cần sửa tiếp:
- Chưa thấy test case cho Swagger/OpenAPI.
- Chưa thấy test case cho GitLab CI/CD.
- Docker resilience `DOCKER-02` còn expected “503 hoặc retry đúng”, cần chốt behavior.

## Vấn Đề Còn Tồn Tại Cần Chốt

### 1. Một số expected output vẫn chưa executable

Các case còn expected dạng lựa chọn:
- `AUTH-REG-13`: `HTTP 429` hoặc block IP
- `AUTH-LOGIN-06`: `HTTP 429` hoặc account lock
- `AUTH-JWT-03`: `401` hoặc `403`
- `AUTH-JWT-07`: `401` hoặc `403`
- `USER-LIST-08`: `400` hoặc page rỗng
- `USER-LIST-09`: `400` hoặc capped max size
- `USER-UPDATE-07`: `400` hoặc sanitized output
- `USER-UPDATE-09`: `200` hoặc `400 tùy config`
- `USER-UPDATE-12`: `403` hoặc chỉ update token owner
- `USER-DELETE-06`: `500` hoặc `409`
- `DOCKER-02`: `503` hoặc retry đúng

Production test case nên có expected duy nhất. Nếu có nhiều behavior hợp lệ, cần tách thành decision note hoặc ghi rõ rule đã chốt theo implementation.

### 2. Integration test strategy chưa đủ rõ

Các sheet có nhắc security filter chain, nhưng chưa thấy phân loại rõ:
- Unit test
- Controller test với MockMvc/WebMvcTest
- Integration test với SpringBootTest
- Database integration test với Testcontainers/PostgreSQL
- End-to-end/Postman/Newman test

Đề xuất thêm cột `Test Level` để tránh nhầm giữa mock test và production-like integration test.

### 4. Swagger/OpenAPI và CI/CD vẫn thiếu

Review ngày 2026-05-15 có nhắc guideline yêu cầu Swagger/OpenAPI và GitLab CI/CD. Bản mới có Infra tab tốt hơn, nhưng chưa thấy nhóm:
- `SWAGGER-01`: Swagger UI/OpenAPI docs available.
- `SWAGGER-02`: Security scheme Bearer JWT hiển thị đúng.
- `SWAGGER-03`: API schema/request/response khớp implementation.
- `CI-01`: pipeline build/test/package pass.
- `CI-02`: pipeline chạy unit + integration tests.
- `CI-03`: docker image build/push hoặc scan theo requirement.

## Kết Luận

So với bản review ngày 2026-05-15, bản điều chỉnh ngày 2026-05-18 **đã xử lý khoảng 75-80% góp ý về mặt coverage và cấu trúc test case**.

Các hạng mục đã tốt lên rõ:
- JWT hardening
- brute-force/rate-limit
- pagination boundary
- update/delete business rule
- infra guideline coverage
- ID naming
- priority classification
- coverage summary

Các hạng mục cần hoàn thiện trước khi coi là đạt production:
- Chốt expected result duy nhất cho các case còn “hoặc/tùy config”.
- Bổ sung Swagger/OpenAPI và CI/CD.
- Làm rõ test level, đặc biệt với Spring Security filter chain và Testcontainers.
- Xác nhận API versioning `/api/v1/...` là contract chính thức so với guideline.

Khuyến nghị: **Accept bản điều chỉnh ở mức test design**, nhưng **chưa accept ở mức production release gate** cho đến khi các expected mơ hồ được chốt.

