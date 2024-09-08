FROM maven:3.9.8-amazoncorretto-21-debian AS builder

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

#Stage 2: Copy only necessary files
FROM eclipse-temurin:latest

WORKDIR /app

COPY --from=builder /app/app/target/*.jar ./app.jar

CMD ["java", "-jar", "app.jar"]