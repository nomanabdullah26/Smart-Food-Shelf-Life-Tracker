# Smart Food Shelf Life Tracker

<p align="center">
  <strong>A Smart Food Inventory Management System for Tracking Expiry Dates and Reducing Food Waste</strong>
</p>

<p align="center">
  A Spring Boot web application designed to manage food inventory, monitor shelf life, and generate timely alerts for products approaching expiration.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Spring%20Security-Authentication-green?style=flat-square&logo=springsecurity" alt="Spring Security"/>
  <img src="https://img.shields.io/badge/Thymeleaf-Templates-005F0F?style=flat-square&logo=thymeleaf" alt="Thymeleaf"/>
  <img src="https://img.shields.io/badge/JavaScript-ES6+-yellow?style=flat-square&logo=javascript" alt="JavaScript"/>
</p>

---

## 📌 Overview

**Smart Food Shelf Life Tracker** is a web-based food inventory management application that helps users organize food products, track expiration dates, and receive alerts about items that need attention.

The system combines a Java and Spring Boot backend with a web-based interface to simplify inventory management and support better food storage practices. It is designed to help users identify products approaching their expiry dates and make more informed decisions about food consumption and inventory handling.

## ✨ Key Features

- **Food Inventory Management** — Add, organize, and manage food items.
- **Shelf-Life Monitoring** — Track product expiration dates and identify items approaching expiry.
- **Expiry Alerts** — Manage alerts related to food items that require attention.
- **Barcode-Based Product Lookup** — Support barcode-related product identification and catalog lookup.
- **Product Catalog** — Maintain product information for inventory operations.
- **User Authentication** — Provide login and access-control functionality using Spring Security.
- **Device Management** — Manage device-related information through dedicated application endpoints.
- **REST API** — Expose endpoints for supported application operations.
- **Responsive Web Interface** — Provide a browser-based interface built with HTML, CSS, JavaScript, and Thymeleaf.
- **Scheduled Processing** — Use Spring scheduling for configured background tasks.

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Core programming language |
| Spring Boot | Application development |
| Spring Security | Authentication and access control |
| Spring Data JPA | Data access and persistence |
| Hibernate / JPA | Object-relational mapping |
| Thymeleaf | Server-side HTML rendering |
| HTML5 & CSS3 | Web page structure and styling |
| JavaScript | Client-side interactions |
| Maven | Dependency and build management |
| JUnit / Spring Boot Test | Application testing |

**Note:** The database engine and exact framework versions depend on the project's configuration.

## 🏗️ Application Architecture

The application follows a layered architecture that separates web requests, business logic, and data persistence.

```text
                   ┌─────────────────────────┐
                   │       Web Browser       │
                   │ HTML · CSS · JavaScript │
                   └────────────┬────────────┘
                                │
                   ┌────────────▼────────────┐
                   │ Controllers & REST API  │
                   └────────────┬────────────┘
                                │
                   ┌────────────▼────────────┐
                   │    Service Layer        │
                   │ Business Logic & Alerts │
                   └────────────┬────────────┘
                                │
                   ┌────────────▼────────────┐
                   │ Repository Layer        │
                   │ Spring Data JPA         │
                   └────────────┬────────────┘
                                │
                   ┌────────────▼────────────┐
                   │       Database          │
                   └─────────────────────────┘
```

Spring Security handles application security, while scheduled tasks support configured background processing.

## 📂 Project Structure

```text
Smart-Food-Shelf-Life-Tracker/
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/com/example/foodtracker/
│   │   │   ├── config/
│   │   │   │   ├── SchedulingConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AlertController.java
│   │   │   │   ├── ApiController.java
│   │   │   │   ├── AppController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── BarcodeController.java
│   │   │   │   ├── DeviceController.java
│   │   │   │   └── FoodController.java
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── FoodTracker2Application.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/styles.css
│   │       │   └── js/app.js
│   │       ├── templates/
│   │       │   ├── app.html
│   │       │   └── login.html
│   │       └── application.properties
│   └── test/
├── .gitignore
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

- Java Development Kit (JDK) 21
- Git
- A compatible database configured for the application

Maven is included through the Maven Wrapper, so a separate Maven installation is generally unnecessary.

### 1. Clone the Repository

```bash
git clone https://github.com/afrin2326/Smart-Food-Shelf-Life-Tracker.git
```

Navigate to the project directory:

```bash
cd Smart-Food-Shelf-Life-Tracker
```

### 2. Configure the Application

Open:

```text
src/main/resources/application.properties
```

Configure the database connection and any required application settings according to your local environment.

**Security reminder:** Never commit actual database passwords, API keys, or other sensitive credentials. Use environment variables or an external configuration mechanism for private values.

### 3. Run the Application

**Windows (PowerShell or Command Prompt):**

```bash
.\mvnw.cmd spring-boot:run
```

**Git Bash / Linux / macOS:**

```bash
./mvnw spring-boot:run
```

If the application starts successfully, open the local address configured for the application in your browser.

```text
http://localhost:8080
```

The default address above assumes that the application uses Spring Boot's standard port. Check `application.properties` if a different port is configured.

### 4. Build the Application

**Windows:**

```bash
.\mvnw.cmd clean package
```

**Git Bash / Linux / macOS:**

```bash
./mvnw clean package
```

### 5. Run Tests

```bash
./mvnw test
```

On Windows, you can use `.\mvnw.cmd test`.

## 🔍 Core Modules

| Module | Responsibility |
|---|---|
| Food Management | Handles food inventory operations |
| Alert Management | Supports food-related alert operations |
| Barcode Service | Handles barcode-related product lookup |
| Product Catalog | Manages catalog information |
| User Management | Supports user-related operations |
| Device Management | Handles device-related functionality |
| Security Configuration | Defines authentication and access-control rules |
| REST API | Provides endpoints for supported application features |

## 🖥️ Application Screenshots

Screenshots of the application can be added here to demonstrate the user interface and key features.

Create a folder named `images` in the repository root and add your screenshots.

For example:

```text
images/
├── login-page.png
├── dashboard.png
└── food-inventory.png
```

Then display them in this section:

```markdown
### Login Page

![Login Page](images/login-page.png)

### Application Dashboard

![Application Dashboard](images/dashboard.png)

### Food Inventory

![Food Inventory](images/food-inventory.png)
```

Replace these example filenames with your actual screenshot filenames.

## 🔐 Security Considerations

- Authentication and authorization are configured through Spring Security.
- Database credentials should be supplied securely.
- Do not publish private tokens, passwords, or production secrets.
- Review access-control rules before deploying the application publicly.

## 🔮 Future Improvements

Potential enhancements include:

- Advanced expiry-date analytics and inventory reports.
- Email or push notifications for approaching expiration dates.
- Improved barcode scanning and product catalog integration.
- Food waste statistics and consumption insights.
- Docker-based deployment and automated testing.

These are potential future enhancements, not claims that the features are already implemented.

## 👥 Authors

This project was developed collaboratively by:

**1. Afrin Binte Amin**

- GitHub: [@afrin2326](https://github.com/afrin2326)

**2. Md Abdullah Al Noman**

- GitHub: [@nomanabdullah26](https://github.com/nomanabdullah26)

## 🤝 Contributions

Contributions, suggestions, and feedback are welcome.

1. Fork the repository.
2. Create a feature branch.
3. Implement and test your changes.
4. Commit your changes with a descriptive message.
5. Open a pull request describing your contribution.

## 📄 License

No license has been specified in this repository yet. Add an appropriate `LICENSE` file before distributing or reusing the project under an open-source license.

---

<p align="center">
  <strong>Smart Food Shelf Life Tracker</strong><br/>
  Building smarter food inventory practices through software.
</p>
