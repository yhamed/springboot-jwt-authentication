# JWT Authentication & Authorization - Spring boot backend
This application uses Java 17
PostgreSQL DB

# In order to run the app:
## On docker:
* Under classpath of the project in terminal run:
```
docker build -t springboot-jwt .
```
* Under src/main/docker of the project in terminal run:
```
docker-compose up
```
* To stop the backend:
```
docker-compose down
```

## Run Spring Boot application locally

Run postgresql DB docker image locally or change application properties datasource config or reconfigure datasource to point at preferred postgresSQL
If you want to run it locally on your machine run the following command to create a postgres DB image:
* Create & start a postgresSQL DB local container:
```
docker run --name some-postgres -p 32768:5432 -e POSTGRES_PASSWORD=postgres -d postgres
```
* Install dependencies and start the backend application:
```
./mvnw clean install
```
```
./mvnw spring-boot:run
```

## Requirements:
* Install Java 17 JDK
* JAVA_HOME environment variable pointing to the JDK-17
* Install docker for windows.

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

# Notice:
## Database schemas to be reserved:

--------------------------------------------------------------------------------
* Public schema is used for Runtime
* Test schema is used for Integration Tests - **Make sure to change this schema name in application.test.properties & initTestSchema.sql to avoid data loss**