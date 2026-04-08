# BÁO CÁO KẾT QUẢ KHẮC PHỤC RỦI RO SAU CODE REVIEW LẦN 1

- Author: Xuân An
- Role: Java Backend Developer (Fresher)
- Date: 07 Apr 2026
- Scope: Fix security & production readiness issues identified during Code Review #1

## TỔNG QUAN

Sau buổi Code Review #1, hệ thống được đánh giá có một số rủi ro liên quan đến:

- Security (Authorization, JWT secret)
- Configuration (DB credentials, logging)
- Exception handling
- Data validation
- Maintainability & performance

## CHI TIẾT KHẮC PHỤC

### 1. Fix unauthorized access to /profile

❌ **Problem**
- Endpoint `/api/v1/users/profile` được truy cập bởi `permitAll`, ảnh hưởng đến khả năng Security khi:
    - Cho phép unauthenticated user truy cập
    - Có thể gây NullPointerException khi Authentication = null
    - Rủi ro data leakage

✅ **Solution**
- Đã thay đổi cấu hình authorization:
    - /api/v1/auth/** → permitAll
    - /api/v1/users/** → authenticated
    - Đảm bảo chỉ user đã login mới truy cập profile API

💻 **Code**
- [user-service/config/SecurityConfig.java](../../user-service/src/main/java/com/r2s/user/config/SecurityConfig.java)

```
.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/users/**").authenticated()
                        .anyRequest().authenticated()
                )
```

- [auth-service/config/SecurityConfig.java](../../auth-service/src/main/java/com/r2s/auth/config/SecurityConfig.java)

```
.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
```

⚠️ **Additional**
- Xử lý exception khi user cố gắng truy cập vào endpoint `/api/v1/users/profile`:
    - Khi chưa login -> authenticationEntryPoint -> Unauthorized (401)
    - Khi đã login, nhưng không có quyền -> accessDeniedHandler -> Access Denied (403)
- [user-service/config/SecurityConfig.java](../../user-service/src/main/java/com/r2s/user/config/SecurityConfig.java) ([auth-service/config/SecurityConfig.java](../../auth-service/src/main/java/com/r2s/auth/config/SecurityConfig.java)
  tương tự)
```
http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/users/**").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
```

---

### 2. Fix JWT Secret Hard-code

❌ **Problem**
- JWT secret được hard-code trực tiếp trong source code, gây rủi ro lộ secret khi source bị truy cập.

✅ **Solution**
- xóa hard-code `JWT_SECRET`, `JWT_EXPIRATION` khỏi SecurityConstants
- Địa nghĩa các properties của JWT tại
  class [JwtProperties](../../core/src/main/java/com/r2s/core/config/JwtProperties.java)
- Enable `@EnableConfigurationProperties(JwtProperties.class)` tại UserServiceApplication và UserServiceApplication
- Externalize secret & expiration qua `application.yml`, và load từ environment variables thay vì hard-code trong source
- Luồng xử lý: environment variables -> application.yaml -> JwtProperties (core module) -> JwtUtil (core module) ->
  auth-service + user-service dùng chung

⚠️ **Additional**
- Tạo .env dùng setup Docker Compose
- Tạo .gitignore chứa .env để tránh leak secret lên github

💻 **Code**
- Trong [auth-service/resources/application.yaml](../../auth-service/src/main/resources/application.yaml) (user-service
  tương tự) chứa:
```
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION}
```

- Trong [JwtUtil](../../core/src/main/java/com/r2s/core/security/JwtUtil.java) get secret qua object jwtProperties:

```
private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        String secretKey = jwtProperties.getSecret();
        if(secretKey == null || secretKey.isEmpty()){
            throw new IllegalStateException("JWT secret key is not configured properly");
        }
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
```
---

### 3. Fix DB credentials & cấu hình production kém

❌ **Problem**
- application.properties của cả auth-service và user-service chứa plaintext password và cấu hình “dev style”:

✅ **Solution**
- Không commit username, password, url của DB lên github
- Setup variables trong edit configurations cho từng service
- Tắt show-sql ở production
- Tại các modules, cập nhật `application.yaml`
```
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```
---

### 4. Fix Exception handling thiếu logging + có thể văng lỗi trong handler

❌ **Problem**
- GlobalExceptionHandler không log exception stacktrace (khó vận hành/forensics).
- Handler validation có thể NPE vì dùng requireNonNull(ex.getBindingResult().getFieldError()):
```
       String msg = Objects
                .requireNonNull(ex.getBindingResult().getFieldError())
                .getDefaultMessage();
```
✅ **Solution**
- 

⚠️ **Additional**

💻 **Code**
---

### Subject Example

❌ **Problem**

✅ **Solution**

⚠️ **Additional**

💻 **Code**
---