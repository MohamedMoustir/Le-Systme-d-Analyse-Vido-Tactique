# ⚽ SAVT - Système d'Analyse Vidéo Tactique

![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot)
![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![YOLOv8](https://img.shields.io/badge/AI-YOLOv8-FF9900?style=for-the-badge&logo=ultralytics)
![Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)

## 📖 Overview
**SAVT** is an Enterprise-Level Full-Stack application designed to automate football (soccer) tactical video analysis. By leveraging the power of Artificial Intelligence (YOLOv8) and a robust Java Spring Boot backend, SAVT allows coaches and football analysts to upload match footage, track players in real-time, and generate insightful tactical statistics and radars automatically.

## ✨ Key Features
* **🤖 AI-Powered Analysis:** Automated player detection, tracking, and tactical mapping using Python and YOLOv8.
* **📡 Real-Time Feedback:** Full-duplex WebSocket integration to stream AI processing logs directly to the Angular frontend without blocking the UI.
* **🔒 Enterprise Security:** Stateless JWT authentication, role-based access control (RBAC), and protected Admin/Coach dashboards.
* **💳 Monetization & Subscriptions:** Seamless integration with Stripe API for premium tier management.
* **📊 Interactive Dashboards:** Dynamic timelines, tactical radars, and statistics powered by Angular State Management (Store).

## 🏗️ System Architecture

The project follows a decoupled, highly scalable architecture:

1.  **Frontend (Angular):** Single Page Application (SPA) implementing Smart/Dumb component patterns, state management for a single source of truth, and Route Guards for security.
2.  **Backend (Spring Boot):** N-Tier architecture (Controller, Service, Repository). Implements the DTO pattern to protect database entities, `@Transactional` for ACID compliance, and asynchronous thread management (`@Async`) to handle heavy video processing without bottlenecks.
3.  **AI Engine (Python):** Executed as an independent process via IPC (Inter-Process Communication), generating analytical JSONs mapped back to Java Objects.
4.  **Database:** Spring Data JPA with Hibernate ORM, allowing database-agnostic operations.



## 🛠️ Technology Stack
* **Backend:** Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA, WebSockets, Stripe API.
* **Frontend:** Angular 17+, TypeScript, RxJS, NgRx/Store (State Management), TailwindCSS.
* **AI / Computer Vision:** Python, YOLOv8, OpenCV, Pandas.
* **DevOps / Infrastructure:** Docker, Docker Compose, GitHub Actions (CI/CD).

## 🚀 Getting Started

### Prerequisites
* Java 17+
* Node.js & npm
* Python 3.9+
* Docker & Docker Compose (Optional for containerized run)

### Local Setup
1. **Clone the repository:**
   ```bash
   git clone [https://github.com/yourusername/savt-project.git](https://github.com/yourusername/savt-project.git)
   cd savt-project