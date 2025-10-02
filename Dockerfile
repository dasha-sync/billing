FROM eclipse-temurin:21-jdk

WORKDIR /api

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY . .

RUN ./mvnw package -DskipTests

ENV SPRING_PROFILES_ACTIVE=docker

CMD ["java", "-jar", "target/billing-api.jar"]
