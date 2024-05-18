FROM openjdk:21 AS build

WORKDIR /app

COPY . .
RUN ./mvnw -DskipTests clean package

FROM openjdk:21 AS RUN

COPY --from=build /app/target/finance_hack-0.0.1-SNAPSHOT.jar /

ENTRYPOINT java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:7777 -jar finance_hack-0.0.1-SNAPSHOT.jar