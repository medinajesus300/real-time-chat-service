# Real-Time Chat & Presence Service

A minimal Spring Boot–powered backend with SockJS/STOMP WebSocket support and a static HTML/JS frontend demonstrating:

- **User registration & session-based login**  
- **Real-time chat** (global room) with message persistence in PostgreSQL  
- **Online/offline presence tracking** + “user joined/left” system notices  
- **Message history** (last 100 messages) loaded on connect  

---

##  Features

- **Spring Boot** (Web, WebSocket, Data JPA)  
- **STOMP over SockJS** for reliable real-time messaging  
- **PostgreSQL** to store users & messages  
- **Lombok** to reduce boilerplate in entities & DTOs  
- **Static HTML/CSS/JS** frontend (no heavy frameworks)  
- **Presence listener** broadcasting `/topic/presence` + system notices  
- **Private user queue** (`/user/queue/history`) for history fetch  

---

##  Tech Stack

| Layer         | Technology                                 |
|---------------|--------------------------------------------|
| Backend       | Java 17, Spring Boot 3.x, Spring Data JPA |
| WebSocket     | Spring WebSocket (STOMP + SockJS)          |
| Database      | PostgreSQL 15                              |
| Frontend      | HTML5, CSS3, Vanilla JS (Fetch API)        |
| Build & Dev   | Maven, Lombok, Spring DevTools             |

---

##  Getting Started

### Prerequisites

- Java 17 SDK   
- Maven 3.6+  
- PostgreSQL 13+ (or Docker/Compose)  

### Clone & Configure

```bash
git clone git@github.com:<your-username>/chat-service.git
cd chat-service
