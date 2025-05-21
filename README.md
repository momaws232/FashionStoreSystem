<<<<<<< HEAD
# Fashion Store Application

A Java application for managing a fashion store with database persistence.

## Database Setup

The application now uses MySQL for data persistence. Follow these steps to set up the database:

1. Install MySQL Server and MySQL Workbench (optional but recommended for management)

2. Create a database user:
   ```sql
   CREATE USER 'fashionuser'@'localhost' IDENTIFIED BY 'password';
   GRANT ALL PRIVILEGES ON fashionstore.* TO 'fashionuser'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. The application will automatically create the database and tables when it starts.

## Configuration

Database connection parameters can be configured in `src/main/resources/database.properties`:

```properties
# Database Connection Properties
db.url=jdbc:mysql://localhost:3306/fashionstore
db.user=fashionuser
db.password=password
db.driver=com.mysql.cj.jdbc.Driver

# Connection Pool Settings
db.pool.maxSize=10
db.pool.connectionTimeout=30000
```

Modify these settings to match your MySQL installation.

## Running the Application

There are two versions of database connection handling available:

### 1. Simple Connection Pool (Default)

The application now uses a simple connection pool implementation that doesn't require additional JVM arguments.

```bash
mvn clean install
mvn javafx:run
```

### 2. HikariCP Implementation

If you prefer to use the HikariCP connection pool, you'll need to add the following JVM arguments:

```bash
mvn clean install
mvn javafx:run -Djavafx.run.vmArgs="--add-opens java.base/java.sql=ALL-UNNAMED --add-modules java.sql"
```

### Using Command Line

To run the application from the command line:

```bash
# Simple Connection Pool (Default)
java -jar target/javafxproject-1.0-SNAPSHOT.jar

# HikariCP Implementation
java --add-opens java.base/java.sql=ALL-UNNAMED --add-modules java.sql -jar target/javafxproject-1.0-SNAPSHOT.jar
```

### Using an IDE

Import the project into your favorite IDE (Eclipse, IntelliJ IDEA, etc.) and run the `FashionStoreApp` class.

If you want to use the HikariCP implementation, add these VM arguments to your run configuration:

```
--add-opens java.base/java.sql=ALL-UNNAMED --add-modules java.sql
```

In IntelliJ IDEA:
1. Edit Run Configuration
2. Add to VM options: `--add-opens java.base/java.sql=ALL-UNNAMED --add-modules java.sql`

In Eclipse:
1. Run > Run Configurations...
2. Select your run configuration
3. Go to Arguments tab
4. Add to VM arguments: `--add-opens java.base/java.sql=ALL-UNNAMED --add-modules java.sql`

## Dependencies

- JavaFX 17.0.2
- MySQL Connector/J 8.0.28
- HikariCP 5.0.1 (Connection pooling - Optional) 
=======
# FashionStoreSystem

## Overview

FashionStoreSystem is a desktop application designed to streamline the operations of a fashion retail store. Built using Java and JavaFX, it offers an intuitive user interface to manage inventory, process sales, and generate reports.

## Features

- **Inventory Management**: Add, update, and remove fashion items from the store's inventory.
- **Sales Processing**: Handle customer purchases and maintain sales records.
- **Reporting**: Generate detailed reports on sales performance and inventory status.
- **User Authentication**: Secure login system for staff members to ensure data integrity.

## Technologies Used

- **Java**: Core programming language.
- **JavaFX**: For building the graphical user interface.
- **CSS**: Styling the user interface components.

## Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/momaws232/FashionStoreSystem.git
>>>>>>> 0c9dbe63d736e544d0bcc401044da56e8d01eae0
