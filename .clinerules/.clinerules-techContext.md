# Technical Context: Syndicated Loan Management System

## Technology Stack

### Core Framework
- Spring Boot 3.2.1
- Java 17
- Maven

### Spring Components
1. **Spring Web**
   - RESTful API development
   - MVC architecture
   - HTTP handling

2. **Spring Data JPA**
   - Data persistence
   - Repository pattern
   - Entity management

3. **Spring Validation**
   - Input validation
   - Constraint checking
   - Error handling

4. **Spring AOP**
   - Cross-cutting concerns
   - Logging aspects
   - Performance monitoring

### Database
- H2 Database (In-memory)
- JPA/Hibernate ORM
- SQL-based testing

### Development Tools
- Lombok
- JaCoCo (Code Coverage)
- JUnit (Testing)
- Maven Build Tool

## Configuration

### Server Configuration
```properties
server.port=8080
```

### Database Configuration
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

### JPA Settings
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

### Logging Configuration
```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.com.syndicated_loan=DEBUG
```

### JSON Handling
```properties
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss
spring.jackson.time-zone=UTC
```

## Development Setup

### Prerequisites
1. Java JDK 17
2. Maven 3.x
3. IDE with Spring support
4. Git for version control

### Build Commands
```bash
# Clean and install dependencies
mvn clean install

# Run tests with coverage
mvn test

# Run application
mvn spring-boot:run
```

### Testing Environment
1. JUnit 5 for unit testing
2. H2 in-memory database
3. SQL test data files
4. Builder patterns for test data

## Technical Constraints

### Performance
1. In-memory database limitations
2. JPA N+1 query considerations
3. Transaction boundary management
4. Memory usage for large datasets

### Security
1. No authentication mechanism
2. In-memory database volatility
3. H2 console access control
4. API exposure limitations

### Scalability
1. Single instance deployment
2. In-memory database constraints
3. Stateless application design
4. RESTful architecture limitations

## Monitoring & Debugging

### Available Tools
1. H2 Console
   - Path: /h2-console
   - Credentials: sa / [empty password]
   - Local access only

2. SQL Logging
   - Hibernate SQL debug logging
   - Parameter binding tracing
   - Application-level debugging

3. JaCoCo Coverage Reports
   - Test coverage monitoring
   - Report generation
   - Coverage metrics tracking

### Development Practices
1. Comprehensive logging
2. SQL query monitoring
3. Performance profiling
4. Test-driven development
