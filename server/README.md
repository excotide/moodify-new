# Moodify Project

Moodify is a mood tracking application that allows users to log their moods and track their emotional well-being over time. This project is built using Spring Boot and connects to a Supabase database for data storage.

## Features

- User registration and authentication
- Create, retrieve, update, and delete mood entries
- Track mood changes over time
- Simple and intuitive RESTful API

## Technologies Used

- Spring Boot
- Java
- Supabase (PostgreSQL)
- Maven

## Getting Started

### Prerequisites

- Java 21
- Maven
- Supabase account

### Installation

1. Clone the repository:
   ```
   git clone <repository-url>
   ```

2. Navigate to the project directory:
   ```
   cd moodify-springboot
   ```

3. Update the `application.yml` file with your Supabase database credentials.

4. Run the application:
   ```
   mvn spring-boot:run
   ```

### Run with dev profile (H2)

Untuk pengembangan lokal tanpa Supabase, gunakan profile `dev` yang memakai database H2 in-memory (mode PostgreSQL) dan menonaktifkan Flyway.

- Jalankan langsung (Maven):
  ```powershell
  mvn spring-boot:run -Dspring-boot.run.profiles=dev
  # Jika PowerShell tetap error Unknown lifecycle phase, gunakan versi dengan kutip:
  mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
  ```
  $env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run

- Atau jalankan JAR dengan profile dev:
  ```powershell
  mvn -DskipTests package
  $env:SPRING_PROFILES_ACTIVE = "dev"
  java -jar target\moodify-springboot-1.0-SNAPSHOT.jar
  ```

Catatan profile dev:
- Tidak memerlukan kredensial Supabase.
- Menggunakan H2 in-memory, data akan hilang saat aplikasi restart.
- Flyway dimatikan, Hibernate akan membuat/menyesuaikan tabel untuk pengujian cepat.
- Base URL: http://localhost:8080

### API Endpoints

- **User Registration**
  - `POST /api/users` atau `POST /api/users/register`

- **Mood Entries**
  - `POST /api/mood-entries` - Create a new mood entry
  - `GET /api/mood-entries` - Retrieve all mood entries
  - `GET /api/mood-entries/{id}` - Retrieve a specific mood entry
  - `PUT /api/mood-entries/{id}` - Update a mood entry
  - `DELETE /api/mood-entries/{id}` - Delete a mood entry

## Database Schema

The database schema is initialized using the SQL statements in `src/main/resources/db/migration/V1__init_schema.sql`. Make sure to run the migration to set up the necessary tables.

## Running Tests

To run the tests, use the following command:
```
mvn test
```

## Contributing

Contributions are welcome! Please open an issue or submit a pull request for any enhancements or bug fixes.

## License

This project is licensed under the MIT License. See the LICENSE file for more details.