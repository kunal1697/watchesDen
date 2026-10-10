# One Dockerfile for every Spring Boot module. Build with:
#   docker build --build-arg SERVICE=user-service -t aeon/user-service ./backend
ARG SERVICE

# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
ARG SERVICE
WORKDIR /src

# Resolve dependencies first so they are cached between source-only changes.
COPY pom.xml .
COPY common/pom.xml common/
COPY discovery-server/pom.xml discovery-server/
COPY api-gateway/pom.xml api-gateway/
COPY user-service/pom.xml user-service/
COPY product-service/pom.xml product-service/
COPY order-service/pom.xml order-service/
RUN mvn -B -q -pl ${SERVICE} -am dependency:go-offline -DskipTests || true

COPY . .
RUN mvn -B -q -pl ${SERVICE} -am package -DskipTests \
 && cp ${SERVICE}/target/${SERVICE}-*.jar /app.jar

# ---------- runtime ----------
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S aeon && adduser -S aeon -G aeon
USER aeon
WORKDIR /app
COPY --from=build /app.jar app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
