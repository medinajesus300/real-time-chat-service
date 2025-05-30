# TODO

## MVP (Must-Have)

- [x] **Database & Entities**
  - Define `User` and `ChatMessage` JPA entities
  - Configure `application.properties` and verify connection to PostgreSQL
- [x] **Repositories**
  - Implement `UserRepository` and `ChatMessageRepository`
- [x] **WebSocket Setup**
  - Create `WebSocketConfig` for STOMP/SockJS endpoints
  - Implement `ChatController` and `ChatService` message flow
- [ ] **Presence Tracking**
  - Build `PresenceEventListener` to broadcast online/offline users
- [x] **Authentication & Registration**
  - Implement `AuthController`, `UserService`, and session-based login/register
- [x] **Frontend Demo**
  - Create `index.html` with SockJS + Stomp.js integration
  - Display message list, input box, and online user sidebar

## Testing

- [ ] **Unit Tests**
  - Write JUnit/Mockito tests for `UserService` and `ChatService`
- [ ] **Integration Tests**
  - Use `WebSocketStompClient` and Testcontainers (PostgreSQL)

## DevOps & CI

- [ ] **Docker**
  - Finalize `Dockerfile` for the Spring Boot app
  - Verify `docker-compose.yml` for Postgres service
- [ ] **CI Pipeline**
  - Add GitHub Actions workflow to build, test, and lint
- [ ] **Documentation**
  - Write `README.md` with setup, build, and run instructions

## Next-Level (Post-MVP)

- [ ] **Distributed Presence**
  - Integrate Redis for scalable presence tracking
- [ ] **Message Broker**
  - Add RabbitMQ/Kafka for decoupled message dispatch
- [ ] **Security Enhancements**
  - Replace session auth with Spring Security + JWT
- [ ] **Frontend Upgrade**
  - Swap minimal JS for Thymeleaf templates or React demo
- [ ] **Performance Testing**
  - Load-test WebSocket throughput (e.g., with Gatling)
