# Moodify Spring Boot Application

## Project Overview
Moodify is a Spring Boot-based application designed to help users track and manage their moods. The application provides RESTful APIs for creating, retrieving, and analyzing mood entries. It is built with a focus on simplicity, scalability, and ease of use.

## Features
- User authentication and management.
- Mood entry creation and retrieval.
- Weekly mood statistics and analysis.
- Database integration for persistent storage.

## Tech Stack
The following technologies are used in this project:

### Backend
- **Java**: The primary programming language.
- **Spring Boot**: Framework for building RESTful APIs.
- **Spring Data JPA**: For database interaction.
- **H2 Database**: In-memory database for development and testing.
- **Flyway**: For database version control and migrations.

### Tools
- **Maven**: Build automation tool.
- **Lombok**: To reduce boilerplate code.
- **JUnit**: For unit testing.

## Installation Guide
Follow these steps to set up and run the application locally:

### Prerequisites
1. Ensure you have the following installed:
   - **Java Development Kit (JDK)** (version 17 or higher).
   - **Maven** (version 3.6 or higher).
2. Clone the repository:
   ```bash
   git clone https://github.com/excotide/moodify-new.git
   ```

### Steps
1. Navigate to the project directory:
   ```bash
   cd moodify-new
   ```
2. Build the project using Maven:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Access the application:
   - The APIs will be available at `http://localhost:8080`.

### Database Configuration
The application uses an in-memory H2 database by default. You can access the H2 console at:
- URL: `http://localhost:8080/h2-console`
- Username: `sa`
- Password: (leave blank)

## Running the Frontend
If the project includes a frontend application, follow these steps to set it up and run:

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install the required dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
4. Access the frontend application:
   - The application will be available at `http://localhost:5173` by default.

## Program Functionality
Moodify provides the following core functionalities:

1. **User Management**:
   - Register and authenticate users.
   - Manage user profiles.

2. **Mood Tracking**:
   - Add mood entries with details such as date, time, and mood description.
   - Retrieve mood entries for specific users.

3. **Statistics and Analysis**:
   - Generate weekly mood statistics.
   - Analyze mood trends over time.

## Contribution
Feel free to fork the repository and submit pull requests. For major changes, please open an issue first to discuss what you would like to change.
