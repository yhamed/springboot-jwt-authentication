# JWT Authentication & Authorization - Spring boot backend


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
## Step 1: Run postgresql DB docker image locally or change application properties datasource config
```
docker run --name some-postgres -p 32768:5432 -e POSTGRES_PASSWORD=postgres -d postgres
```

## Step 2: Run Spring Boot application
```
mvn clean install
mvn spring-boot:run
```
