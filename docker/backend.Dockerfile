# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
COPY common-blog/pom.xml common-blog/pom.xml
COPY gateway-blog/pom.xml gateway-blog/pom.xml
COPY user-blog/pom.xml user-blog/pom.xml
COPY article-blog/pom.xml article-blog/pom.xml
COPY ai-blog/pom.xml ai-blog/pom.xml
COPY common-blog common-blog
COPY gateway-blog gateway-blog
COPY user-blog user-blog
COPY article-blog article-blog
COPY ai-blog ai-blog
ARG MODULE=user-blog
RUN mvn -B -pl ${MODULE} -am package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ARG MODULE=user-blog
COPY --from=build /src/${MODULE}/target/${MODULE}-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENTRYPOINT ["java", "-jar", "app.jar"]
