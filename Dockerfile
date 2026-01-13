# ===== Stage 1: Build =====
FROM nexus.sogaz.ru/maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Прокидываем настройки Maven для работы через Nexus
COPY settings.xml /root/.m2/settings.xml

# Копируем pom и исходники
COPY pom.xml .
COPY src ./src

# Собираем jar без тестов
RUN mvn clean package -DskipTests -q

# ===== Stage 2: Runtime =====
FROM nexus.sogaz.ru/eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]