# Uni-Kanban

[![Java](https://img.shields.io/badge/Java-21%2B-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Hibernate](https://img.shields.io/badge/Hibernate-ORM%208.x-orange.svg)](https://hibernate.org/orm/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Uni-Kanban** is a modular, multi-user task and workflow management system built following Kanban methodology. The project is implemented using **Java 21**, **Spring Boot**, **Hibernate ORM**, and **PostgreSQL**, featuring team workspace management with flexible roles, an interactive console CLI, and a data import/export subsystem supporting both **JSON** and **Microsoft Excel-optimized CSV**.

---

## 📌 Key Features

### 1. Team & Workspace Management (`UserTeams`)
- Team creation (`Team`) with descriptions and owner assignment.
- Role-based access control (`TeamRole`):
  - `OWNER` — team owner (protected against demoting or removing the sole owner).
  - `ADMIN` — board and member administrator.
  - `MEMBER` — regular team contributor.
  - `VIEWER` — read-only access.
- Bidirectional association between `User` $\leftrightarrow$ `UserTeam` $\leftrightarrow$ `Team` with database-level uniqueness constraints on `(user_id, team_id)`.

### 2. Kanban Board Hierarchy
- **`TaskTable` (Board)** — associated with a specific team or standalone user.
- **`TaskList` (Columns / Swimlanes)** — configurable workflow stages (e.g., *To Do*, *In Progress*, *Done*).
- **`Task` (Cards)** — task title, rich multi-line description, order position, and assignee (`User`).

### 3. Import & Export Subsystem (JSON & Excel CSV)
- **JSON (Hierarchical Backup):**
  - Complete preservation and restoration of the board tree: Board $\rightarrow$ Columns $\rightarrow$ Tasks $\rightarrow$ Assignees.
  - Ideal for full backups, sprint duplication, and cross-environment migration.
- **CSV (Optimized for Microsoft Excel):**
  - Includes **UTF-8 BOM (`\uFEFF`)** prefix so Microsoft Excel instantly opens Cyrillic and unicode text without encoding glitches.
  - Semicolon (`;`) delimiter and RFC-4180 compliant escaping for quotes and multi-line descriptions.
  - Preserves empty columns during both export and import.
  - Automatic delimiter detection (`;` or `,`) when importing files from diverse locales.
  - Resolves task assignees by `@username`.

### 4. Interactive Console CLI & REST API
- Terminal-driven navigation menus:
  - User management and authentication (`User Menu`).
  - Board import and export (`Import / Export Menu`).
- REST Controller endpoint (`/api/kanban/status`) for external integrations.

---

## 🛠 Technology Stack

| Technology | Purpose |
| :--- | :--- |
| **Java 21** | Primary programming language (LTS) |
| **Spring Boot 3.3.4** | Application framework & dependency injection container |
| **Hibernate ORM 8.x** | Object-Relational Mapping (JPA) |
| **PostgreSQL** | Relational database |
| **Apache Commons CSV 1.12.0** | High-performance CSV generation and parsing |
| **Jackson 2.17.x** | JSON serialization and deserialization |
| **Log4j2 / SLF4J** | Structured application logging |
| **Lombok** | Boilerplate reduction (getters, setters, constructors) |
| **JUnit 5 & Mockito** | Unit testing framework |

---

## 📂 Project Structure

```text
Uni-Kanban/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── backend/
│   │   │   │   ├── config/             # Application configuration & .env loader
│   │   │   │   ├── dao/                # Data Access Objects (UserDao, TeamDao, UserTeamDao)
│   │   │   │   ├── models/
│   │   │   │   │   ├── task/           # Task, TaskList, TaskTable entities
│   │   │   │   │   ├── team/           # Team, UserTeam, TeamRole entities
│   │   │   │   │   └── user/           # User entity
│   │   │   │   ├── services/           # Business logic (UserService, TeamService, UserTeamService)
│   │   │   │   │   └── export/         # JSON and CSV export/import service & DTOs
│   │   │   │   ├── ui/                 # Interactive CLI menus (ConsoleMenu, UserMenu, ImportExportMenu)
│   │   │   │   └── utils/              # Hibernate session factory (HibernateFactory)
│   │   │   ├── controller/             # Spring REST controllers
│   │   │   └── launcher/               # Application entry points (Main, CompileLauncher)
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── hibernate.cfg.xml       # Hibernate settings & entity mappings
│   │       └── log4j2.xml              # Logging configuration
│   └── test/java/backend/              # Test suite (JUnit 5 + Mockito)
├── .env.example                        # Template for environment variables
├── pom.xml                             # Maven build and dependencies
└── README.md
```

---

## 🚀 Quick Start

### Prerequisites
- **JDK 21** or later.
- **Maven 3.9+**.
- **PostgreSQL 14+** (or running Docker container).

### 1. Environment Configuration
Create a `.env` file in the project root or configure system environment variables:

```properties
DB_URL=jdbc:postgresql://localhost:5432/kanban
DB_USER=postgres
DB_PASS=your_password
DB_SCHEMA=public
```

### 2. Build & Test
To build the project and execute the test suite:

```bash
mvn clean test
```

### 3. Package & Run
Package the project into an executable JAR:

```bash
mvn clean package
```

Run the application:

```bash
java -jar target/uni-kanban-1.0-SNAPSHOT.jar
```

---

## 💻 CLI Usage

When launched, the main console menu is displayed:

```text
--- CONSOLE MENU ---
1. User Menu
2. Import / Export (JSON & Excel CSV)
3. Exit
```

### User Menu (`User Menu`)
- **User Discovery:** List all registered users, find by UUID, username, or substring query.
- **CRUD Operations:** Register new users, update username/password, and delete accounts.

### Import / Export Menu (`Import / Export Menu`)
1. **Export Board to JSON:** Exports board structure and tasks to a structured `.json` file.
2. **Import Board from JSON:** Recreates a board from a JSON file, optionally associating it with an existing team.
3. **Export Board to CSV (for Excel):** Generates an Excel-ready `.csv` file using UTF-8 BOM encoding and semicolon delimiter.
4. **Import Board from CSV (for Excel):** Parses a tabular CSV file, auto-detecting delimiters and creating columns and tasks accordingly.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
