FROM eclipse-temurin:17-jdk-alpine AS builder

RUN apk add --no-cache curl unzip

RUN curl -fsSL https://dlcdn.apache.org/maven/maven-3/3.9.7/binaries/apache-maven-3.9.7-bin.zip -o maven.zip \
    && unzip maven.zip -d /opt \
    && ln -s /opt/apache-maven-3.9.7 /opt/maven \
    && ln -s /opt/maven/bin/mvn /usr/bin/mvn

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]