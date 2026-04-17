# kitaplik-online

A microservices-based online library management system built with Spring Cloud. The project is split into small independent services that communicate through an API gateway and discover each other via Eureka. I built this to get hands-on experience with common Spring Cloud patterns like service discovery, centralized configuration, Feign clients, and circuit breakers.

## Architecture

```
               Client
                 |
           [API Gateway :8889]
            /           \
  [book-service :9998]  [library-service :8090]
            \           /
           [Eureka Server :8761]
                 |
          [Config Server :8888]
```

| Service | Port | Responsibility |
|---------|------|----------------|
| eureka-server | 8761 | Service registry — all services register here |
| config-server | 8888 | Centralized configuration pulled from a Git repo |
| gateway | 8889 | Single entry point, routes requests to the right service |
| book-service | 9998 | Manages the book catalog (add, list, find by ISBN or ID) |
| library-service | 8090 | Manages libraries — create a library, add books, list all libraries |

The library-service calls the book-service internally using a **Feign client**. Both calls are wrapped with **Resilience4j circuit breakers** so the library-service returns a fallback response instead of failing when the book-service is down.

Distributed tracing is set up with **Zipkin** (expected at `http://localhost:9411`).

## Tech Stack

- Java / Spring Boot
- Spring Cloud Netflix Eureka (service discovery)
- Spring Cloud Config (centralized configuration via Git)
- Spring Cloud Gateway (reactive API gateway)
- OpenFeign (declarative HTTP client)
- Resilience4j (circuit breaker pattern)
- Spring Cloud Sleuth + Zipkin (distributed tracing)
- H2 (in-memory database for both services in dev)

## Running the Project

Services need to start in a specific order because they depend on each other.

**1. Start the config server first:**
```bash
cd config-server
./mvnw spring-boot:run
```

**2. Start Eureka:**
```bash
cd eureka-server
./mvnw spring-boot:run
```

**3. Start the gateway and application services (order doesn't matter between these):**
```bash
cd gateway
./mvnw spring-boot:run

cd book-service
./mvnw spring-boot:run

cd library-service
./mvnw spring-boot:run
```

Once everything is up, all requests go through the gateway at `http://localhost:8889`.

You can check the Eureka dashboard to see which services have registered: `http://localhost:8761`

## API Overview

| Method | Path | Service | Description |
|--------|------|---------|-------------|
| POST | `/v1/book` | book-service | Add a new book |
| GET | `/v1/book` | book-service | List all books |
| GET | `/v1/book/isbn/{isbn}` | book-service | Find book by ISBN |
| GET | `/v1/book/book/{id}` | book-service | Find book by ID |
| POST | `/v1/library` | library-service | Create a new library |
| GET | `/v1/library` | library-service | List all libraries |
| GET | `/v1/library/{id}` | library-service | Get library with its books |
| PUT | `/v1/library` | library-service | Add a book to a library |

## Configuration

The config-server pulls configuration from the `config` directory in this repository. The library-service uses `@RefreshScope` so its config can be updated at runtime via the `/actuator/refresh` endpoint without a restart.
