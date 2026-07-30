FROM --platform=$BUILDPLATFORM maven:3.9.16-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml /app/pom.xml
RUN mvn dependency:go-offline

ENV DB_URL=jdbc:mysql://localhost:3306/lobos
ENV DB_USERNAME=dummy
ENV DB_PASSWORD=dummy
ENV JWT_SECRET=dummy

COPY src /app/src
RUN mvn install

FROM builder AS prepare-production
RUN mkdir -p target/dependency
WORKDIR /app/target/dependency
RUN jar -xf ../*.jar

FROM gcr.io/distroless/java21-debian13

EXPOSE 8080
VOLUME /tmp
ARG DEPENDENCY=/app/target/dependency
COPY --from=prepare-production ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=prepare-production ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=prepare-production ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java", "-cp", "app:app/lib/*", "id.my.tudemaha.lobos.LobosApplication"]
