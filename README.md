# StockFlow — Inventory Management System

A backend application built with Java and Spring Boot for managing products and inventory through RESTful APIs, with JWT-based authentication and PostgreSQL database integration.

## Overview

StockFlow is a backend development project focused on building secure REST APIs, managing relational data, and testing application functionality. It demonstrates practical experience with Spring Boot, database integration, authentication, automated testing, and code coverage analysis.

## Features

- **Product Management:** Create, retrieve, update, and delete products.
- **RESTful APIs:** Handle application requests through HTTP endpoints.
- **JWT Authentication:** Generate and validate JSON Web Tokens for authentication.
- **Authorization:** Test access restrictions for different user roles.
- **PostgreSQL Integration:** Persist application data in a relational database.
- **Automated Testing:** Unit tests and integration tests using the project's test suite.
- **Code Coverage:** JaCoCo coverage reporting integrated into the Maven build.

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot | Backend application framework |
| Spring Data JPA | Database access |
| PostgreSQL | Relational database |
| JWT (JJWT) | Token-based authentication |
| Maven | Build and dependency management |
| JUnit | Automated testing |
| JaCoCo | Code coverage analysis |
| Git & GitHub | Version control |

## Prerequisites

Before running StockFlow, install:

- Java Development Kit (JDK) 21
- PostgreSQL
- Git (optional, for cloning the repository)

Maven is available through the project's Maven Wrapper.

## Getting Started

### 1. Clone the repository

Replace the placeholder below with your actual GitHub repository URL.

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd stockflow
```

### 2. Configure PostgreSQL

Create a PostgreSQL database for the application.

```sql
CREATE DATABASE stockflow;
```

Configure the database connection in `src/main/resources/application.properties` using your local PostgreSQL username, password, and database URL.

**Security note:** Never commit real database passwords or JWT secrets to GitHub.

### 3. Configure the JWT secret

The application expects the `STOCKFLOW_JWT_SECRET` environment variable.

Set it in your terminal before starting the application. Use a securely generated Base64-encoded secret containing sufficient key material for your JWT signing algorithm.

PowerShell example:

```powershell
$env:STOCKFLOW_JWT_SECRET = "<YOUR_BASE64_ENCODED_SECRET>"
```

Keep this secret private. Do not paste it into issues, screenshots, or public repositories.

### 4. Run the application

On Windows, use the Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

The default Spring Boot port is commonly `8080`, unless your configuration specifies another port.

## Testing

Run the complete automated test suite:

```powershell
.\mvnw.cmd test
```

Run the full Maven verification build, including packaging and the configured JaCoCo report:

```powershell
.\mvnw.cmd clean verify
```

### Test Coverage

The latest reported JaCoCo results for the measured project code are:

| Metric | Coverage |
|---|---:|
| Instruction coverage | 100% |
| Branch coverage | 100% |
| Line coverage | 100% |
| Methods covered | 86 / 86 |
| Classes covered | 18 / 18 |

Generate the report with:

```powershell
.\mvnw.cmd clean verify
```

Open the generated report:

`target/site/jacoco/index.html`

*Coverage results reflect the reported build and may change as the codebase evolves.*

## API Overview

StockFlow includes product-management endpoints and authentication-related functionality.

The exact HTTP methods, endpoint paths, request formats, and response examples should be documented from the current controller mappings. Add your verified API endpoints here, or link to an exported Postman collection or OpenAPI specification if available.

## Project Structure

```text
stockflow/
├── src/
│   ├── main/
│   │   ├── java/com/anil/stockflow/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── product/
│   │   │   └── service/
│   │   └── resources/
│   └── test/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

*This structure is illustrative; update it to match the actual repository.*

## Learning Outcomes

This project demonstrates practical experience with:

- Building REST APIs using Spring Boot.
- Connecting a Java application to PostgreSQL.
- Implementing JWT token generation and validation.
- Testing authentication and authorization behavior.
- Writing automated unit and integration tests.
- Measuring instruction, line, and branch coverage using JaCoCo.
- Building and packaging a Java application with Maven.

## Future Improvements

Potential enhancements include:

- API documentation using Swagger/OpenAPI.
- Docker-based application and database setup.
- Continuous integration with automated tests.
- Centralized exception handling and request validation.
- Deployment to a cloud hosting platform.

## Author

**Anil Yellapu**

Java Backend Developer | Spring Boot | PostgreSQL | REST APIs

- GitHub: [YellapuHemaVenkataAnil](https://github.com/YellapuHemaVenkataAnil)
- LinkedIn: [anilyellapu](https://www.linkedin.com/in/anilyellapu/)

## License

Add a license file if you intend to distribute this project publicly. Until then, no license should be assumed.
