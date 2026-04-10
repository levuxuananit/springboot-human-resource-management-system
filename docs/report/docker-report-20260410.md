# BÁO CÁO LIFECYCLE KHI RUN VỚI LOCAL / DOCKER

## OVERVIEW

- Mô tả lifecycle khi run LOCAL
- Mô tả lifecycle khi run DOCKER

## LIFECYCLE KHI RUN LOCAL

### 1. Application Startup

- Flow: Run `UserServiceApplication` -> Run `AuthServiceApplication` -> Run `PostgreSQL local`
- Components tham gia:
    - `UserServiceApplication`
    - `AuthServiceApplication`
    - `@SpringBootApplication`
    - `@EnableJpaAuditing`
    - `@EnableConfigurationProperties(JwtProperties.class)`

### 2. Application Startup

- Spring Boot load các cấu hình từ:
    - `application.yaml`
    - `application.properties`
    - `environment variables`

- Bao gồm:
    - JWT configuration
    - Database configuration
    - Pagination configuration
    - Security configuration

### 3. Database Connection Initialization

- Tiến trình khởi tạo và kết nối DB:
    - Spring Boot khởi tạo DataSource
    - Kết nối đến PostgreSQL chạy local
    - Hibernate tạo mapping entity

- Components tham gia:
    - UserRepository
    - User Entity
    - Role Enum
    - PostgreSQL (local instance)

### 4. Security Initialization

- Spring Security được khởi tạo:
    - JWT Filter đăng ký vào filter chain
    - AuthenticationEntryPoint được cấu hình
    - Authorization rules được áp dụng

- Files tham gia:
    - JwtFilter
    - JwtUtil
    - SecurityConfig
    - CustomAuthenticationEntryPoint

### 5. API Request Lifecycle

- Khi client gửi request: Client → Controller → Service → Repository → Database
- Chi tiết:
    - Client gửi HTTP request
    - JWT Filter validate token
    - SecurityContext được thiết lập
    - Controller xử lý request
    - Service thực hiện business logic
    - Repository truy vấn database
    - Response trả về client

### 6. Logging & Exception Handling

- Exception được xử lý bởi `GlobalExceptionHandler`
- Log được ghi thông qua `Slf4j`
- Files tham gia:
    - GlobalExceptionHandler
    - CustomAccessDeniedHandler
    - JwtAuthenticationEntryPoint

## ## LIFECYCLE KHI RUN DOCKER

- Khi chạy hệ thống bằng Docker, toàn bộ các service được container hóa và khởi động thông qua `docker-compose`.

### 1. Docker Compose Initialization

- Developer chạy:

```
docker-compose up --build
```

- Khi chạy hệ thống bằng Docker, toàn bộ các service được container hóa và khởi động thông qua `docker-compose`.

### 1. Docker Compose Initialization

- Developer chạy: `docker-compose up --build`
- Docker Compose đọc cấu hình từ:
    - `docker-compose.yaml `
    - `.env`
- Files tham gia:
    - `docker-compose.yaml`
    - `.env`
    - `auth-service/Dockerfile`
    - `user-service/Dockerfile`
    - `postgres/init.sql`

### 2. Container Build Process

- Docker build image cho từng service:
- Auth Service Image: `auth-service/Dockerfile`
    - Thực hiện:
        - Copy source code
        - Build project bằng Maven
        - Tạo executable `.jar`

- User Service Image: `user-service/Dockerfile`
    - Thực hiện:
        - Build module
        - Tạo runnable container

- PostgreSQL Image
    - Docker pull image: `postgres:latest`
    - Sau đó: `postgres/init.sql` -> được chạy để khởi tạo schema ban đầu.

### 3. Container Startup Order

- Docker Compose khởi động:
    - PostgreSQL container
    - Auth Service container
    - User Service container
- Database phải chạy trước để các service có thể kết nối.

### 4. Environment Variable Injection

- Docker inject biến môi trường từ: `.env`
- Spring Boot load biến này vào:
    - application.yaml
    - JwtProperties
    - Datasource configuration

### 5. Service Communication

- Các container giao tiếp thông qua: `Docker Network`
- Ví dụ:
    - user-service → postgres
    - auth-service → postgres
    - Thông qua hostname: `postgres`

### 6. API Runtime Flow (Inside Container)

- Luồng xử lý request: Client → Docker container → Controller → Service → Repository → PostgreSQL container

### 7. Logging & Monitoring

- Logs được xuất ra:
    - Docker logs
    - stdout
    - stderr

- Developer có thể xem log bằng: `docker-compose logs`