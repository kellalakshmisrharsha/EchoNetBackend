# EchoNet Microservices Application

EchoNet is a scalable, cloud-native social networking platform implemented using microservices architecture. Developed with Spring Boot, Spring Cloud, and reactive programming (Spring WebFlux), it supports modular development and independent deployment of core social features.

---

## Application Overview

The EchoNet system consists of multiple microservices working together to provide social networking functionalities such as user management, post creation, likes, comments, and authentication, all integrated via service discovery and a centralized API Gateway.

### Core Services

- **Eureka Server:** Central service registry that enables dynamic service discovery for all microservices.
- **API Gateway:** Acts as the single entry point for client requests. It handles routing to appropriate backend microservices, load balancing, and applies common cross-cutting concerns like security and rate limiting.
- **Post Service:** Responsible for managing posts—creating, retrieving, liking, commenting, and deleting posts.
- **User Service:** Manages user profiles, authentication states, and user data.
- **Auth Service:** Provides JWT-based authentication and authorization.

---

## API Gateway Functioning

The API Gateway routes client requests to corresponding microservices based on configured routes and service registry metadata:

- **Routing:** Uses Eureka to dynamically discover available service instances. Requests are forwarded to the correct service URL transparently.
- **Security:** Integrates with Auth Service to authenticate users and propagate identity information (like userId, username) to backend services.
- **Aggregation:** Can aggregate responses or orchestrate multiple service calls if needed (extendable design).
- **Cross-cutting:** Implements logging, metrics, and rate limiting for all traffic passing through it.

Clients interact exclusively with the API Gateway, which abstracts away individual microservice details.

---

## Running the Application

Build and run each microservice individually or orchestrate all components using Docker Compose.

---

## Docker Hub Images

All microservices are containerized and published publicly on Docker Hub:

| Service       | Docker Hub Repository                                    |
|---------------|---------------------------------------------------------|
| Post Service  | [echonetpostservice](https://hub.docker.com/r/klsharsha/echonetpostservice)  |
| User Service  | [echonetuserservice](https://hub.docker.com/r/klsharsha/echonetuserservice)  |
| Auth Service  | [echonetauthservice](https://hub.docker.com/r/klsharsha/echonetauthservice)  |
| API Gateway   | [echonetgateway](https://hub.docker.com/r/klsharsha/echonetgateway)          |
| Eureka Server | [echonetserver](https://hub.docker.com/r/klsharsha/echonetserver)            |



---

## Quick Start with Docker Compose


docker-compose up -d

Access services:

- Eureka Server dashboard: [http://localhost:8761](http://localhost:8761)
- API Gateway: [http://localhost:8088](http://localhost:8088) (entry point for all client requests)
- Individual microservices listen on their mapped ports (for development/debugging)

---

## Example API Usage

### Create a Post
curl -X POST http://localhost:8088/posts
-H "Content-Type: application/json"
-H "Authorization: Bearer <your-JWT-token>"
-d '{"content":"Hello, EchoNet!"}'

### Get All Posts

curl http://localhost:8088/posts

### Like a Post

curl -X POST http://localhost:8088/posts/{postId}/like
-H "Authorization: Bearer <your-JWT-token>"

### Add a Comment

curl -X POST http://localhost:8088/posts/comment
-H "Content-Type: application/json"
-H "Authorization: Bearer <your-JWT-token>"
-d '{"postId":123, "content":"Great post!"}'

Replace `<your-JWT-token>` with a valid JWT token issued by the Auth Service.

---

## Architecture Diagram
[Clients] ---> [API Gateway] ---> [Eureka Server]
| |
| --> [Service Registry]
|
|--> [Post Service]
|--> [User Service]
|--> [Auth Service]

- Clients interact only with the API Gateway for simplicity, security, and scalability.
- The API Gateway queries Eureka for service discovery to forward requests dynamically.
- Services communicate and scale independently.

---


