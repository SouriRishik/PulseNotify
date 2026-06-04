# PulseNotify

PulseNotify is an enterprise-grade, highly scalable distributed notification service designed to handle millions of notifications across various channels, including Email, SMS, and Push.

This project is built to demonstrate advanced system design, event-driven architecture, and robust cloud deployment practices suitable for a production environment.

## Tech Stack

**Backend:**
- Java 21 & Spring Boot 3
- PostgreSQL & Spring Data JPA
- Redis (Caching)
- Spring Security (JWT + RBAC)
- AWS SQS & AWS Lambda

**Frontend:**
- React 18 & TypeScript
- Vite
- React Query & Axios

**DevOps & Cloud:**
- Docker & Docker Compose
- AWS RDS, ElastiCache, ECS

## System Architecture & Workflows

### 1. Modular Monolith Architecture
```mermaid
graph TD
    Client[Frontend: React/Vite App] -->|HTTPS| SpringBoot[Spring Boot Application: Modular Monolith]
    
    subgraph Spring Boot
        Auth[Auth Module]
        Template[Template Module]
        Notif_API[Notification Module]
        Analytics[Analytics Module]
        Scheduler[Scheduler Component]
    end

    Auth --> DB[(PostgreSQL)]
    Template --> DB
    Template --> Cache[(Redis Cache)]
    Notif_API --> DB
    Analytics --> DB
    Analytics --> Cache

    Scheduler -->|Poll scheduled & Move to SQS| SQS_Main
    Notif_API -->|Publish| SQS_Main[AWS SQS: Main Queue]
```

### 2. AWS Infrastructure
```mermaid
graph TD
    subgraph AWS Cloud
        Route53[Amazon Route 53] --> ALB[Application Load Balancer]
        ALB --> ECS[Amazon ECS / EC2 Cluster]
        
        subgraph VPC
            ECS -->|React Frontend| Frontend_Containers
            Frontend_Containers --> Backend_Containers
            ECS -->|Spring Boot Backend| Backend_Containers
            Backend_Containers --> RDS[(Amazon RDS - PostgreSQL)]
            Backend_Containers --> ElastiCache[(ElastiCache - Redis)]
        end
        
        Backend_Containers -->|Publish| SQS[Amazon SQS Main Queue]
        SQS --> Lambda[AWS Lambda Consumer]
        Lambda --> SES[Amazon SES: Email]
        Lambda --> SNS[Amazon SNS / Twilio: SMS]
        
        Lambda -->|Max Retries Exceeded| DLQ[SQS Dead Letter Queue]
    end
    
    User[User/Admin] --> Route53
```

### 3. Notification State Machine
```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> QUEUED : Published to SQS
    QUEUED --> PROCESSING : Lambda consumes
    PROCESSING --> SENT : Handed to Provider
    SENT --> DELIVERED : Provider Webhook
    
    PROCESSING --> FAILED : Error
    FAILED --> RETRYING : Under Max Retries
    RETRYING --> PROCESSING : SQS Retry
    
    FAILED --> DLQ : Max Retries Exceeded
```

### 4. Authentication Flows
**User Login**
```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant DB as PostgreSQL
    participant JwtUtils

    Client->>AuthController: POST /api/v1/auth/login {email, password}
    AuthController->>AuthService: authenticateUser()
    AuthService->>DB: findByEmail()
    DB-->>AuthService: User Entity
    AuthService->>AuthService: Verify Password
    AuthService->>JwtUtils: generateJwtToken(UserDetails)
    JwtUtils-->>AuthService: Access Token
    AuthService->>AuthService: createRefreshToken(UserId)
    AuthService->>DB: Save Refresh Token
    AuthService-->>AuthController: JwtResponse (AccessToken, RefreshToken)
    AuthController-->>Client: 200 OK + JwtResponse
```

**Refresh Token**
```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant RefreshTokenService
    participant DB as PostgreSQL
    participant JwtUtils

    Client->>AuthController: POST /api/v1/auth/refresh {refreshToken}
    AuthController->>RefreshTokenService: findByToken(refreshToken)
    RefreshTokenService->>DB: Query Token
    DB-->>RefreshTokenService: RefreshToken Entity
    RefreshTokenService->>RefreshTokenService: verifyExpiration()
    RefreshTokenService->>JwtUtils: generateTokenFromEmail(User.email)
    JwtUtils-->>RefreshTokenService: New Access Token
    RefreshTokenService-->>AuthController: TokenRefreshResponse (New Access Token)
    AuthController-->>Client: 200 OK + TokenRefreshResponse
```
