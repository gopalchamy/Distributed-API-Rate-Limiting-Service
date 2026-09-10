# Distributed API Rate-Limiting & Traffic Throttling Service (NexusLimit)

A high-performance, distributed API traffic-control and rate-limiting gateway built with **Java 23**, **Spring Boot 3**, **Redis Token Bucket (Lua)**, **PostgreSQL / H2**, and a **React Developer Portal**.

---

## Architecture Overview

```
[ Client / Web App / Bot ]
            |
            | HTTP Request (with X-API-KEY or IP)
            v
[ Spring Boot Rate-Limiter Gateway ]
            |
            +---> Intercept Request (HandlerInterceptor)
            |
            +---> Authenticate API-Key & Resolve App Tier / Endpoint Rule
            |
            +---> Execute Atomic Token Bucket Check in Redis (Lua Script)
            |       |
            |       +---> [ Redis ] (Fast distributed token bucket state & TTL)
            |             (Auto-fallback to In-Memory Token Bucket if Redis offline)
            |
      +-----+-----+
      |           |
  [ Allowed ]  [ Exceeded ]
      |           |
      |           +---> Emit HTTP 429 Too Many Requests
      |                 Headers: X-RateLimit-Limit, X-RateLimit-Remaining, Retry-After
      v
[ Forward to Downstream Backend Microservices ]
      |
      v
[ PostgreSQL / H2 Database ] (Applications, API Keys, Rate Rules, Audit Logs)
```

---

## Key Features

1. **Distributed Token Bucket Algorithm**:
   - Atomic evaluation in Redis using Lua scripts (`token_bucket.lua`).
   - Supports smooth traffic shaping, burst tolerance, and continuous mathematical replenishment.
   - Built-in resilient in-memory sliding token bucket fallback if Redis connection is not established.
2. **Standard IETF Rate Limiting Headers**:
   - `X-RateLimit-Limit`: Maximum burst capacity.
   - `X-RateLimit-Remaining`: Tokens remaining in current bucket.
   - `X-RateLimit-Reset`: Time (in seconds) until bucket is fully replenished.
   - `X-RateLimit-Engine`: Active rate limiter engine (`REDIS` or `IN_MEMORY`).
   - `Retry-After`: Minimum wait time before retrying a throttled request.
3. **Multi-Tier Application Management**:
   - **Free Tier**: 20 requests / 60 seconds (burst capacity 20).
   - **Pro Tier**: 120 requests / 60 seconds (burst capacity 120).
   - **Enterprise Tier**: 600 requests / 60 seconds (burst capacity 600).
   - Custom endpoint-level rules (e.g. strict throttling on `POST /api/v1/gateway/orders`).
4. **Developer Portal & Interactive Sandbox**:
   - **Live Token Bucket Animation**: Visual token drainage and replenishment in real-time.
   - **Burst Load Generator**: Fire bursts of 10x, 25x, 50x requests with one click to observe 429 throttling.
   - **Telemetry & Audit Logs**: Full real-time audit log with IP, latency, HTTP status code, and endpoint distribution.
   - **Swagger / OpenAPI Documentation**: Available at `/swagger-ui.html`.

---

## Getting Started

### Prerequisites
- **Java**: JDK 21+ (Java 23 supported)
- **Maven**: 3.9+
- **Node.js**: 18+ and npm

---

### 1. Run Backend Service (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```
- Backend runs on `http://localhost:8080`
- Swagger UI available at `http://localhost:8080/swagger-ui.html`
- Embedded database (H2) and In-Memory Rate Limiter start automatically without any setup needed.
- If Redis is running on `localhost:6379`, distributed Redis Token Bucket mode activates automatically.

---

### 2. Run Developer Portal (React + Vite)

```bash
cd frontend
npm install
npm run dev
```
- Frontend portal runs on `http://localhost:5173`

---

## Pre-Configured Demo Credentials & API Keys

| Plan Tier | Application Name | Sample API Key | Default Limit |
| :--- | :--- | :--- | :--- |
| **Free** | E-Commerce Web Store | `rl_live_ecommerce_demo_key_771` | 20 req / 60s |
| **Pro** | Mobile App API | `rl_live_mobile_app_demo_key_882` | 120 req / 60s |
| **Enterprise** | Partner Logistics | `rl_live_enterprise_demo_key_993` | 600 req / 60s |

**Developer Portal Login:**
- Email: `developer@ratelimiter.io`
- Password: `password123`
*(Or click the "Quick Demo Login" button in the portal)*

---

## Testing Rate Limiting via cURL / CLI

### 1. Normal Request (Allowed - 200 OK)
```bash
curl -i -H "X-API-KEY: rl_live_ecommerce_demo_key_771" http://localhost:8080/api/v1/gateway/products
```

**Response:**
```http
HTTP/1.1 200 OK
X-RateLimit-Limit: 20
X-RateLimit-Remaining: 19
X-RateLimit-Reset: 60
X-RateLimit-Engine: IN_MEMORY
Content-Type: application/json

{
  "status": "SUCCESS",
  "message": "Products retrieved successfully from backend service.",
  "data": [ ... ]
}
```

### 2. Exceeding Rate Limit (Throttled - 429 Too Many Requests)
When sending more requests than the bucket capacity:
```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 20
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 60
Retry-After: 3
Content-Type: application/json

{
  "status": 429,
  "error": "Too Many Requests",
  "message": "API rate limit exceeded. Please throttle your requests or upgrade your plan.",
  "retryAfterSeconds": 3,
  "limit": 20,
  "remaining": 0,
  "engine": "IN_MEMORY",
  "timestamp": 1726000000000
}
```

---

## Running Automated Tests

```bash
cd backend
mvn clean test
```
- Runs unit tests for Token Bucket math, key isolation, burst capacity, and MockMvc integration tests for HTTP 429 gateway responses.