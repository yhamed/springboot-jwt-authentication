FROM openjdk:17
EXPOSE 8080
ARG JAR_FILE=target/authentication-1.0.0.jar
ADD ${JAR_FILE} /usermanagement/app.jar
ENTRYPOINT ["java","-jar","/usermanagement/app.jar"]