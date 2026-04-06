## Review (theo mức độ ưu tiên Production)

1. **Critical – Hở endpoint + có thể gây lỗi NPE khi chưa auth**
   - `user-service` cấu hình `permitAll` cho toàn bộ `/api/v1/users/**`, trong khi `GET /profile` và `PUT /profile` **không có `@PreAuthorize`** và dùng thẳng `Authentication authentication`:
```1:47:/Users/kyle/Documents/Tmp/trainees/jbe/AnLVX/springboot-human-resource-management-system/user-service/src/main/java/com/r2s/user/config/SecurityConfig.java
29:                .authorizeHttpRequests(auth -> auth
30:                        .requestMatchers("/api/v1/users/**").permitAll()
31:                        .anyRequest().authenticated()
32:                )
```
```1:50:/Users/kyle/Documents/Tmp/trainees/jbe/AnLVX/springboot-human-resource-management-system/user-service/src/main/java/com/r2s/user/controller/UserController.java
29:    @GetMapping("/profile")
30:    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
31:        return ResponseEntity.ok(userService.getUserByUsername(authentication.getName()));
32:    }
...
36:    @PutMapping("/profile")
37:    public ResponseEntity<UserResponse> updateMyProfile(@Valid @RequestBody UpdateUserRequest req, Authentication authentication) {
38:        String username = authentication.getName();
39:        return ResponseEntity.ok(userService.updateUser(username, req));
40:    }
```
   - Production fix gợi ý: đổi `requestMatchers("/api/v1/users/**").authenticated()` (hoặc thêm `@PreAuthorize("isAuthenticated()")` cho `/profile`) để đảm bảo chỉ user đã login mới truy cập.

2. **Critical – JWT secret hard-code (không dùng config properties)**
   - `SecurityConstants.SECRET` bị hard-code, và `JwtUtil` chỉ đọc từ constant này (không dùng `jwt.secret` trong `application.properties`):
```1:27:/Users/kyle/Documents/Tmp/trainees/jbe/AnLVX/springboot-human-resource-management-system/core/src/main/java/com/r2s/core/config/SecurityConstants.java
5:    public static final String TOKEN_PREFIX = "Bearer ";
...
8:    public static final String SECRET = "bXlzZWNyZXRrZXlteXNlY3JldGtleW15c2VjcmV0a2V5MTYwOTIwMjYwMA==";
```
```1:57:/Users/kyle/Documents/Tmp/trainees/jbe/AnLVX/springboot-human-resource-management-system/core/src/main/java/com/r2s/core/security/JwtUtil.java
44:    private SecretKey getSigningKey() {
45:        String secretKey = SecurityConstants.SECRET;
46:        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
47:        return Keys.hmacShaKeyFor(keyBytes);
48:    }
```
   - Production fix: externalize secret/expiration qua `application.yml` + env vars (Docker/K8s), dùng `@Value("${jwt.secret}")` hoặc `@ConfigurationProperties`.

3. **High – DB credentials & cấu hình production kém**
   - `application.properties` của cả `auth-service` và `user-service` chứa plaintext password và cấu hình “dev style”:
```1:26:/Users/kyle/Documents/Tmp/trainees/jbe/AnLVX/springboot-human-resource-management-system/user-service/src/main/resources/application.properties
6:jwt.secret=bXlzZWNyZXRrZXlteXNlY3JldGtleW15c2VjcmV0a2V5MTYwOTIwMjYwMA==
...
10:spring.datasource.url = jdbc:postgresql://localhost:5432/user-access-management?useSSL=false&allowPublicKeyRetrieval=true
11:spring.datasource.username = postgres
12:spring.datasource.password = P@ssw0rd
...
18:spring.jpa.hibernate.ddl-auto = update
25:spring.jpa.show-sql=true
```
   - Production fix:
     - Không commit DB password; dùng env var/secret manager.
     - `ddl-auto=update` nên thay bằng migration tool (Flyway/Liquibase).
     - Tắt `show-sql` ở production.

4. **High – Exception handling thiếu logging + có thể văng lỗi trong handler**
   - `GlobalExceptionHandler` không log exception stacktrace (khó vận hành/forensics).
   - Handler validation có thể NPE vì dùng `requireNonNull(ex.getBindingResult().getFieldError())`:
```1:65:/Users/kyle/Documents/Tmp/trainees/jbe/AnLVX/springboot-human-resource-management-system/core/src/main/java/com/r2s/core/exception/GlobalExceptionHandler.java
54:        String msg = Objects
55:                .requireNonNull(ex.getBindingResult().getFieldError())
56:                .getDefaultMessage();
```
   - Production fix:
     - Log `ex` (ít nhất ở `handleGlobalException` và `handleValidation`).
     - Trả về message fallback khi không có `fieldError`.

5. **Medium – Register không validate unique email (dễ ra 500 do constraint DB)**
   - `AuthService.register` chỉ check `existsByUsername`, không check email unique trong khi `User.email` có `unique=true`. Nếu trùng email sẽ dễ nổ `DataIntegrityViolationException` và rơi vào `handleGlobalException` (500).
   - File: `auth-service/src/main/java/com/r2s/auth/service/AuthService.java`.

6. **Medium – JWT filter “nuốt” lỗi + magic number**
   - `JwtFilter` catch `Exception` rồi `clearContext()` nhưng không log gì; đồng thời dùng `authHeader.substring(7)` (magic number) thay vì `SecurityConstants.TOKEN_PREFIX.length()`.
   - Production fix: log theo mức phù hợp (debug/warn) và dùng `substring(SecurityConstants.TOKEN_PREFIX.length())`.

7. **Medium/Low – API performance & maintainability**
   - `UserService.getAllUsers()` không pagination (dễ chậm/timeout khi dữ liệu lớn).
   - `UserService` có field `PasswordEncoder` nhưng không dùng (trong update không đổi password) -> noise.
   - `SecurityConstants` có nhiều constant validation nhưng không đồng bộ 100% (ví dụ `LoginRequest` dùng message literal).

8. **Potential issue – Auditing field `updatedAt` nullable=false**
   - `User.updatedAt` được khai báo `nullable=false`, nhưng `@LastModifiedDate` chỉ đảm bảo set khi entity được update (tùy provider/cấu hình). Có thể gây lỗi insert nếu `updatedAt` chưa được populate.
   - File: `core/src/main/java/com/r2s/core/entity/User.java`.

## Gợi ý hướng sửa nhanh (ưu tiên theo thứ tự)
- Siết lại authorization cho `user-service`: đổi `permitAll` hoặc thêm `@PreAuthorize("isAuthenticated()")` cho `/profile` (để tránh lộ data/NPE).
- Externalize `jwt.secret` + expiration (đừng hard-code trong `SecurityConstants`).
- Loại plaintext DB credentials khỏi `application.properties`; tắt `show-sql` và dùng migration thay vì `ddl-auto=update`.
- Nâng chất lượng exception handling: log exception, fallback message cho validation.
- Bổ sung unique-email check khi register và mapping lỗi unique constraint sang HTTP `409`.