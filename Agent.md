Project: Distributed API Rate-Limiting & Traffic Throttling Service

In simple words, this project controls how many requests a user or application can send to an API within a certain amount of time.

For example, imagine an application has an API:

GET /products

If one user sends 10 requests per minute, everything is fine.

But if someone sends 10,000 requests per minute, the server may become slow or even stop responding. This can also increase database load and affect other users.

Our project prevents this problem by acting as a security and traffic-control layer between the client and the backend.

1. What problem does the project solve?

Consider an online shopping application.

Many users may simultaneously request:

Login
View Products
Search Products
Add to Cart
Place Order
View Orders

Every action can generate an API request.

Normally:

User → Backend Server → Database

If a user or bot sends too many requests:

User/Bot
   ↓
Thousands of requests
   ↓
Backend Server
   ↓
Database overloaded
   ↓
Slow application / Server failure

This project prevents that situation.

2. What is our solution?

We create a separate API Rate-Limiting Service.

The architecture becomes:

Client
   ↓
API Rate Limiter
   ↓
Check request limit
   ↓
 ┌───────────────┐
 │               │
Allowed        Exceeded
 │               │
 ↓               ↓
Backend       HTTP 429
Server        Too Many Requests

So, before a request reaches the actual application, our service checks:

"Is this client allowed to make another request?"

If yes → request continues.

If no → request is rejected.

3. What is Rate Limiting?

Rate limiting means restricting the number of requests a client can make during a particular time period.

For example, we can configure:

100 requests per minute

That means a client can make up to 100 requests in one minute.

Example

Suppose user ABC has a limit of:

5 requests / minute

The requests are:

Request 1 → Allowed
Request 2 → Allowed
Request 3 → Allowed
Request 4 → Allowed
Request 5 → Allowed
Request 6 → Rejected

The sixth request receives:

HTTP 429 Too Many Requests

After the appropriate amount of time, the user can make requests again.

4. Why do we need this project?

The main purpose is to protect backend applications from:

Excessive API requests
Bots
Traffic spikes
Accidental request flooding
Abuse of APIs
High database load
Performance problems

It helps the backend remain stable and available for genuine users.

5. How does our project identify users?

The system can identify a client using things such as:

API Key

For example:

API-Key: ABC123XYZ

The system recognizes:

ABC123XYZ → Application A

and tracks how many requests that application is making.

It can also use the client IP address where appropriate.

6. Where does Redis come into the project?

Redis is used for fast request tracking.

Suppose a user has made:

80 requests

The rate limiter needs to quickly know how many requests that user has already made.

Instead of repeatedly querying the main PostgreSQL database, we use Redis for this runtime information.

Conceptually:

Client
  ↓
Spring Boot
  ↓
Redis
  ↓
Check request count/state

Redis is suitable because it is very fast and can store temporary rate-limit state.

7. What is PostgreSQL used for?

PostgreSQL is used for permanent application data.

For example, the system can store information about:

Developers
Applications
API Keys
Rate Rules
User Tiers
Audit Information

Example:

Application	Plan	Limit
App A	Free	100/min
App B	Pro	1,000/min
App C	Enterprise	10,000/min

So PostgreSQL manages the configuration and persistent information, while Redis handles fast runtime rate-limiting information.

8. What is Token Bucket?

The project uses the Token Bucket algorithm for rate limiting.

Don't worry about the name. Think of it like a bucket containing tokens.

Suppose:

Bucket capacity = 5 tokens

Each API request consumes one token.

5 tokens
 ↓
Request 1 → 4
Request 2 → 3
Request 3 → 2
Request 4 → 1
Request 5 → 0
Request 6 → ❌ Rejected

Tokens are gradually added back according to the configured refill rate.

This allows the system to control traffic while still allowing a reasonable number of requests.

9. What does Spring Boot do?

The main backend of the project is developed using Java and Spring Boot.

Spring Boot handles things such as:

REST APIs
Authentication
API key management
Rate-limit configuration
Database communication
Redis communication
Request interception
Error handling
Business logic

A Spring MVC HandlerInterceptor can be used to check a request before it reaches the controller.

Conceptually:

HTTP Request
     ↓
HandlerInterceptor
     ↓
Rate Limit Check
     ↓
 ┌─────────────┐
 │             │
Allowed      Blocked
 │             │
 ↓             ↓
Controller    429
10. What does React do?

React is used to create the developer portal/dashboard.

A developer can use the dashboard to:

Register an application
Application Name: Shopping App
Generate an API key
API Key: ABC123XYZ
Configure rate limits

For example:

100 requests/minute
Monitor traffic

The dashboard can display information such as:

Total Requests
Allowed Requests
Rejected Requests
Traffic Trends

So React provides the user interface, while Spring Boot provides the backend functionality.

11. Authentication and security

The project also uses security mechanisms such as:

JWT

JWT can be used for developer/user authentication.

For example:

Login
  ↓
Username + Password
  ↓
JWT Token
  ↓
Access protected APIs
API Keys

API keys identify registered applications when they access APIs.

So there are two different concepts:

JWT → authenticates the dashboard user/developer

API Key → identifies an application making API requests

12. What happens when an API request comes?

Let's take a simple example.

A client sends:

GET /products
API-Key: ABC123
Step 1 — Request reaches our service
Client
 ↓
Spring Boot Rate Limiter
Step 2 — Identify the client

The system checks:

API-Key = ABC123
Step 3 — Get the configured limit

For example:

Limit = 100 requests/minute
Step 4 — Check Redis

The system checks the current rate-limit state.

Step 5 — Make a decision

If the request is within the limit:

ALLOW

Then:

Rate Limiter
     ↓
Backend API
     ↓
Database
     ↓
Response
     ↓
Client

If the limit has been exceeded:

BLOCK

and return:

HTTP 429
Too Many Requests

The request doesn't unnecessarily reach the backend.

13. Complete project flow

The easiest way to remember the whole project is:

             CLIENT
                |
                ↓
       Spring Boot Service
                |
                ↓
       Identify API Key/IP
                |
                ↓
       Check Rate-Limit Rule
                |
                ↓
          Redis / State
                |
          ┌─────┴─────┐
          ↓           ↓
       Allowed      Exceeded
          ↓           ↓
      Backend       HTTP 429
       API        Too Many Requests
          |
          ↓
      Database

And separately:

Developer
    ↓
React Dashboard
    ↓
Spring Boot REST APIs
    ↓
PostgreSQL
14. What are the main technologies?
Technology	Purpose
Java	Main programming language
Spring Boot	Backend application
Spring MVC Interceptor	Intercept and check requests
React.js	Developer dashboard
JavaScript	Frontend functionality
PostgreSQL	Persistent data storage
Redis	Fast rate-limit/runtime state
JPA/Hibernate	Database interaction
JWT	Authentication
API Keys	Application identification
Token Bucket	Rate-limiting algorithm
REST API	Communication between components
Docker	Application/container deployment
Swagger/OpenAPI	API documentation/testing
15. One simple real-world example

Imagine a shopping website gives a free application:

100 API requests per minute

A normal user might make:

20 requests/minute

No problem.

But a bot might make:

5,000 requests/minute

Our system detects that the limit has been exceeded.

Instead of allowing all 5,000 requests to reach the shopping application's backend:

5,000 requests
       ↓
Rate Limiter
       ↓
100 allowed
       ↓
Backend

The remaining requests are rejected with:

429 Too Many Requests

This protects the backend and database.

16. Why is it called "Distributed"?

It is called Distributed API Rate-Limiting because the rate-limiting service is designed to work with applications/microservices that may run across multiple instances or servers.

For example:

             Rate Limiter
                  |
          Shared Redis State
          /       |       \
         /        |        \
   Server 1   Server 2   Server 3

Redis provides shared rate-limit state so that multiple application instances can make consistent decisions instead of each server maintaining a completely separate count.

17. What is the main benefit?

The biggest benefit is:

It protects APIs from excessive traffic while allowing legitimate users to continue using the application normally.

It also makes rate limits centralized and configurable rather than hardcoding different limits inside every microservice.