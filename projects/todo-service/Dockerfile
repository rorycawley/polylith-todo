FROM clojure:temurin-21-tools-deps-alpine AS build
WORKDIR /app
COPY . .
WORKDIR /app/projects/todo-service
RUN clojure -T:build uber

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /app/projects/todo-service/target/app.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
