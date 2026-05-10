## BÁO CÁO KẾT QUẢ KHẮC PHỤC RỦI RO SAU CODE REVIEW LẦN 1

### 1. FIX UNAUTHORIZED ACCESS TO /PROFILE ENDPOINT

#### ⚠️ Current Issues

- **Improper Authorization Configuration**  
  Endpoint `/api/v1/users/profile` trước đây được cấu hình `permitAll`, cho phép **unauthenticated users** truy cập vào
  API chứa thông tin cá nhân.

- **Potential NullPointerException Risk**  
  Khi request được gửi từ user chưa authenticate, `Authentication` object có thể là `null`, dẫn đến nguy cơ xảy ra
  `NullPointerException` trong controller/service khi truy xuất thông tin user.

- **Security & Data Leakage Risk**  
  Việc cho phép truy cập public vào endpoint `/profile` có thể dẫn đến rò rỉ thông tin người dùng (data leakage), vi
  phạm nguyên tắc **least privilege** trong bảo mật hệ thống.

---

#### ✅ Proposed Solution

##### 1. Restrict Access to User APIs

- Cập nhật cấu hình authorization để yêu cầu authentication đối với toàn bộ user-related endpoints.

```
.authorizeHttpRequests(auth -> auth
.requestMatchers("/api/v1/users/**").authenticated()
.anyRequest().authenticated()
)
```

- Location: [SecurityConfig.java](../../user-service/src/main/java/com/r2s/user/config/SecurityConfig.java)

---

##### 2. Allow Public Access Only to Authentication APIs

- Chỉ cho phép public access đối với các endpoint phục vụ login/register.

```
.authorizeHttpRequests(auth -> auth
.requestMatchers("/api/v1/auth/**").permitAll()
.anyRequest().authenticated()
```

- Location: [SecurityConfig.java](../../auth-service/src/main/java/com/r2s/auth/config/SecurityConfig.java)

---

#### 🎯 Results Achieved

- Giới hạn truy cập trái phép vào API `/profile`
- Ngăn ngừa lỗi `NullPointerException` do thiếu Authentication
- Tăng cường bảo mật dữ liệu người dùng
- Tuân thủ nguyên tắc phân quyền (Least Privilege)

---

### 2. FIX JWT SECRET HARD-CODE

#### ⚠️ Current Issues

- **Hard-coded Secret in Source Code**  
  JWT secret trước đây được hard-code trực tiếp trong `SecurityConstants`, gây rủi ro bảo mật nghiêm trọng khi source
  code bị truy cập hoặc leak.

- **Poor Secret Management Practice**  
  Việc lưu trữ secret trong source code không tuân theo nguyên tắc **externalized configuration**, gây khó khăn khi thay
  đổi secret giữa các môi trường (dev, staging, production).

- **Security & Maintainability Risk**  
  Hard-coded JWT expiration và secret làm giảm tính linh hoạt khi cấu hình hệ thống và tăng nguy cơ lộ thông tin nhạy
  cảm.

---

#### ✅ Proposed Solution

##### 1. Remove Hard-coded JWT Configuration

- Xóa các giá trị hard-code `JWT_SECRET` và `JWT_EXPIRATION` khỏi `SecurityConstants`.
- Chuyển toàn bộ cấu hình JWT sang external configuration.
- Location:** [SecurityConstants.java](../../core/src/main/java/com/r2s/core/config/SecurityConstants.java)

---

##### 2. Introduce JwtProperties for Centralized Configuration

- Tạo class `JwtProperties` để binding các thuộc tính JWT từ `application.yaml`.

```
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long expiration;

}
```

- Location: [JwtProperties.java](../../core/src/main/java/com/r2s/core/config/JwtProperties.java)

---

##### 3. Enable Configuration Properties

- Enable JwtProperties tại các service để load cấu hình JWT từ environment.

```
@EnableConfigurationProperties(JwtProperties.class)
```

- Location: [UserServiceApplication.java](../../user-service/src/main/java/com/r2s/user/UserServiceApplication.java)
  và [AuthServiceApplication.java](../../auth-service/src/main/java/com/r2s/auth/AuthServiceApplication.java)

##### 4. Externalize JWT Secret via Environment Variables

- Cấu hình JWT secret và expiration trong application.yaml, lấy giá trị từ environment variables thay vì hard-code.

```
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION}
```

- Location:
    - [auth-service/resources/application.yaml](../../auth-service/src/main/resources/application.yaml)
    - [user-service/resources/application.yaml](../../user-service/src/main/resources/application.yaml)

##### 5. Inject JwtProperties into JwtUtil

- JwtUtil sử dụng JwtProperties để lấy secret thay vì sử dụng constant hard-code.

```
private final JwtProperties jwtProperties;

private SecretKey getSigningKey() {

    String secretKey = jwtProperties.getSecret();

    if (secretKey == null || secretKey.isEmpty()) {
        throw new IllegalStateException(
            "JWT secret key is not configured properly"
        );
    }

    byte[] keyBytes = Decoders.BASE64.decode(secretKey);

    return Keys.hmacShaKeyFor(keyBytes);
}
```

- Location: [core/security/JwtUtil.java](core/src/main/java/com/r2s/core/security/JwtUtil.java)

---

##### 6. Secure Secret Management via .env

- Tạo file .env để lưu các biến môi trường phục vụ Docker Compose.
- Thêm .env vào .gitignore để tránh việc commit secret lên repository.

---

#### 🎯 Results Achieved

- Loại bỏ hard-coded JWT secret khỏi source code
- Tăng cường bảo mật thông tin xác thực
- Hỗ trợ cấu hình linh hoạt giữa các môi trường
- Tuân thủ nguyên tắc externalized configuration
- Giảm nguy cơ lộ secret khi chia sẻ source code

---

### 3. FIX DB CREDENTIALS & IMPROVE PRODUCTION CONFIGURATION

#### ⚠️ Current Issues

- **Plaintext Database Credentials in Source Code**  
  `application.properties` của cả `auth-service` và `user-service` trước đây chứa trực tiếp `username`, `password`, và
  `url` của database dưới dạng plaintext, gây rủi ro bảo mật nếu source code bị truy cập hoặc leak.

- **Development-style Configuration in Production**  
  Một số cấu hình mang tính chất development (ví dụ: `show-sql=true`) được bật, làm tăng rủi ro lộ thông tin nhạy cảm
  trong log và ảnh hưởng đến hiệu năng hệ thống.

- **Poor Secret Management Practice**  
  Việc commit thông tin database credentials lên repository không tuân theo nguyên tắc **externalized configuration** và
  **secure credential management**.

---

#### ✅ Proposed Solution

##### 1. Remove Hard-coded Database Credentials

- Xóa toàn bộ thông tin `url`, `username`, `password` khỏi source code.
- Không commit database credentials lên repository.
- Location:
    - [auth-service/resources/application.yaml](../../auth-service/src/main/resources/application.yaml)
    - [user-service/resources/application.yaml](../../user-service/src/main/resources/application.yaml)

---

##### 2. Externalize Database Configuration via Environment Variables

- Cấu hình datasource sử dụng environment variables thay vì hard-code trực tiếp.

```
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

---

### 4. FIX EXCEPTION HANDLING LACKING LOGGING & VALIDATION ERROR LIST NOT YET SUPPORTED

#### ⚠️ Current Issues

- Thiếu logging stacktrace gây khó khăn khi debug
- Structured validation response Không cung cấp thông tin field-level error

#### ✅ Proposed Solution

- Update `GlobalExceptionHandler`, bổ sung logging đầy đủ với stacktrace: `log.warn();`
- Update` GlobalExceptionHandler`, bổ sung xử lý Validate input data (@Valid)

```

// [!] Handle errors Validate input data (@Valid)
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {

        Map<String, String> errors = new HashMap<>();


        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    errors.put(fieldName, error.getDefaultMessage());
        });

        log.warn("[{}] Validation failed for {}: {} -> Path: {}",
                ex.getStatusCode(),
                req.getMethod(),
                errors,
                req.getRequestURI());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                req.getRequestURI(),
                errors);
    }

```

- Update `ApiError` DTO để xử lý Validate input data (@Valid)

```

public class ApiError {
private Instant timestamp;
private int status;
private String error;
private String message;
private String path;
private Map<String, String> fieldErrors;
}

```

#### 🎯 Results Achieved

    - Logging đầy đủ stacktrace.
    - Validation response hỗ trợ nhiều lỗi.

---

### 5. FIX REGISTER NOT VALIDATE UNIQUE EMAIL

#### ⚠️ Current Issues

- `AuthService.register` chỉ check existsByUsername, không check email unique trong khi User.email có unique=true.
- Nếu trùng email sẽ dễ nổ DataIntegrityViolationException và rơi vào handleGlobalException (500).

#### ✅ Proposed Solution

- Update `AuthService.register`, bổ sung check email unique

```

if (repo.existsByEmail(req.getEmail())) {
throw new ConflictException("Email already exists");
}

```

---

### 6. REFACTOR JWT FILTER & EXCEPTION HANDLING + MAGIC NUMBER

#### ⚠️ Current Issues

- Magic Number: Sử dụng `.substring(7)` gây khó bảo trì.
- Silent Failure: Khối `catch (Exception e)` không log lỗi, gây khó khăn cho Monitoring và Debugging.
- Security Risk: Khi JWT lỗi, Filter "nuốt" lỗi và để request trôi vào trạng thái `Anonymous`, dẫn đến lỗi
  `403 Forbidden` mơ hồ thay vì `401 Unauthorized`.

#### ✅ Proposed Solution

##### 1. Refactor Substring

- Dùng `.length()` của prefix giúp code linh hoạt, thay đổi prefix trong config sẽ không làm hỏng logic filter.

```

String token = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length()).trim();

```

##### 2. Categorized Logging

- Phân loại lỗi **JWT Expired, Signature, Malformed** kèm log phù hợp.
- Filter bắt lỗi và "đính kèm" thông báo lỗi vào request.
- `EntryPoint` sẽ lấy thông báo này để trả về cho người dùng.

``` 

catch (ExpiredJwtException e) {
log.warn("JWT expired: {}", e.getMessage());
request.setAttribute("jwt_exception", "Token has expired");
} catch (SignatureException | MalformedJwtException e) {
log.error("Invalid JWT Signature/Format: {}", e.getMessage());
request.setAttribute("jwt_exception", "Invalid token");
} catch (Exception e) {
log.error("Internal Security Error: ", e);
request.setAttribute("jwt_exception", "Authentication failed");
}

```

- Tạo [JwtAuthenticationEntryPoint](../../core/src/main/java/com/r2s/core/exception/JwtAuthenticationEntryPoint.java) để
  trả về phản hồi lỗi chuẩn JSON cho Client

#### 🎯 Results Achieved

- Code linh hoạt, loại bỏ magic number đảm bảo tính ổn định của logic filter.
- Hệ thống phân loại log rõ ràng (WARN/ERROR), hỗ trợ theo dõi và truy vết lỗi nhanh chóng trên Production
- Trả về mã lỗi 401 kèm JSON, thân thiện cho Frontend

___

### 7. ADD PAGINATION TO USER LIST API + REMOVE PASSWORDENCODER

#### ⚠️ Current Issues

- **Missing Pagination in User Listing API**  
  Method `UserService.getAllUsers()` trước đây sử dụng `repo.findAll()` mà không có pagination, dẫn đến việc tải toàn bộ
  dữ liệu user từ database trong một lần request.

- **Performance & Scalability Risk**  
  Khi số lượng user tăng lớn (hàng chục nghìn bản ghi trở lên), API có thể bị chậm, timeout hoặc tiêu tốn nhiều bộ nhớ (
  OOM – Out Of Memory).

- **Uncontrolled Page Size Risk**  
  Không có giới hạn kích thước page mặc định hoặc tối đa, khiến client có thể gửi request với `size` quá lớn, gây ảnh
  hưởng đến hiệu năng hệ thống.

---

#### ✅ Proposed Solution

##### 1. Implement Pagination using Pageable

- Cập nhật method `getAllUsers()` để sử dụng `Pageable` thay vì `findAll()`.

```
public Page<UserResponse> getAllUsers(Pageable pageable) {

    log.info("Fetching users with pagination: page={}, size={}",
            pageable.getPageNumber(),
            pageable.getPageSize());

    return repo
            .findAll(pageable)
            .map(UserResponse::fromEntity);
}
```

Location: [user-service/service/UserService.java](../../user-service/src/main/java/com/r2s/user/service/UserService.java)

---

##### 2. Update Controller to Support Pageable Reques

- Controller nhận `Pageable` từ request parameters `(page, size, sort)`.

```
@GetMapping("/all")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Page<UserResponse>> getAllUsers(
Pageable pageable
) {

    return ResponseEntity.ok(
            userService.getAllUsers(pageable)
    );
}
```

Location: [user-service/controller/UserController.java](../../user-service/src/main/java/com/r2s/user/controller/UserController.java)

---

##### 3. Configure Default & Maximum Page Size

- Thiết lập giới hạn page mặc định và tối đa trong application.yaml để tránh request quá lớn.

```
spring:
  data:
    web:
      pageable:
        default-page-size: 5
        max-page-size: 100
```

Location: [user-service/resources/application.yaml](../../user-service/src/main/resources/application.yaml)

---

#### 🎯 Results Achieved

- Ngăn việc tải toàn bộ dữ liệu user trong một request
- Cải thiện hiệu năng và khả năng mở rộng của API
- Giảm nguy cơ timeout và Out Of Memory (OOM)
- Giới hạn kích thước page nhằm bảo vệ hệ thống khỏi request lớn bất thường
- Tuân thủ best practice về pagination trong REST API

---

### 8. FIX POTENTIAL NULL ISSUE IN UPDATED_AT AUDITING FIELD

#### ⚠️ Current Issues

- **Risk of Null Value in updatedAt Field**  
  Trường `updatedAt` trong entity `User` được cấu hình `nullable=false`, nhưng sử dụng `@LastModifiedDate` chỉ đảm bảo
  giá trị được set khi entity được update.

- **Potential Insert Failure Risk**  
  Trong một số trường hợp, khi thực hiện insert entity mới, `updatedAt` có thể chưa được populate kịp thời, dẫn đến lỗi
  database do vi phạm ràng buộc `NOT NULL`.

- **Auditing Dependency Risk**  
  Việc phụ thuộc hoàn toàn vào JPA Auditing mà không có giá trị mặc định có thể gây lỗi khi auditing không hoạt động
  đúng hoặc cấu hình sai.

---

#### ✅ Proposed Solution

##### 1. Add Default Value for Auditing Fields

- Thiết lập giá trị mặc định cho `createdAt` và `updatedAt` nhằm đảm bảo dữ liệu luôn hợp lệ ngay cả khi auditing chưa
  populate giá trị.

```
@CreatedDate
@Column(name = "created_at", nullable = false, updatable = false)
@Builder.Default
private LocalDateTime createdAt = LocalDateTime.now();

@LastModifiedDate
@Column(name = "updated_at", nullable = false)
@Builder.Default
private LocalDateTime updatedAt = LocalDateTime.now();
```

### 9. EXCEPTION HANDLING ARCHITECTURE REFINEMENT

#### ⚠️ Current Issues

- Phản hồi lệch lạc: Lỗi Security `(401/403)` và lỗi Business không cùng định dạng, gây khó cho Frontend.
- Validation chưa tối ưu: Chỉ hiện một lỗi duy nhất cho mỗi ô nhập liệu thay vì liệt kê toàn bộ sai sót.
- Log nhiễu: Lỗi hệ thống và lỗi người dùng bị đánh đồng, gây khó khăn khi cần truy vết sự cố nghiêm trọng.

#### ✅ Proposed Solution

- Đồng nhất hóa: Dùng HandlerExceptionResolver để đưa mọi loại lỗi về một đầu mối xử lý duy nhất.
- Cấu trúc lại DTO: Chuyển `fieldErrors` sang dạng danh sách `(List)` để gom nhóm toàn bộ lỗi Validation.
- Phân cấp Log: `log.warn`: Dành cho lỗi người dùng (4xx), `log.error`: Dành cho lỗi hệ thống (500) kèm đầy đủ Stack
  Trace.

#### 🎯 Results Achieved

- API chuẩn hóa: Mọi lỗi trả về đều chung một cấu trúc JSON, giúp Frontend xử lý nhanh và gọn.
- Cải thiện UX: Người dùng biết toàn bộ các lỗi cần sửa chỉ trong một lần bấm Submit.
- Vận hành hiệu quả: Log hệ thống sạch hơn, tập trung chính xác vào các lỗi cần can thiệp kỹ thuật.

---