# JWT Authentication & Authorization - Spring boot backend
This application uses Java 17
PostgreSQL DB
## Requirements:

## Configure Spring Datasource, JPA, App properties
```
# datasource and spring data config
spring.datasource.url=jdbc:postgresql://localhost:32768/postgres
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
# logging file - after first run a directory will be created to contain the logs
logging.file.path=LOGS
# jwt config
authorization.app.jwtSecret=secretKey
# will be valid for one day // can be changed to 10000 in order to test the token expiration use case
authorization.app.jwtExpirationMs=86400000
```

# In order to run the app:
## Step 1: Run postgresql DB docker image locally or change application properties datasource config or reconfigure datasource to point at preferred postgresSQL
```
Install Java and have JAVA_HOME pointing to JDK-17
Install docker for windows and run the following command.
docker run --name some-postgres -p 32768:5432 -e POSTGRES_PASSWORD=postgres -d postgres
```

## Step 2: Run Spring Boot application
```
./mvnw clean install
./mvnw spring-boot:run
```
# Notice:
## Database schemas to be reserved:

--------------------------------------------------------------------------------
* First user to signUp will have the Admin role
* Public schema -  by default for Runtime - updatable in application.properties
* Test Schema - for Integration Tests - Make sure to change this schema name in application.test.properties &  initTestSchema.sql to avoid data loss
