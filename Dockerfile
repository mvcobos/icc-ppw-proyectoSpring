# ---------- Etapa 1: compilación ----------
# Sigue al toolchain declarado en build.gradle.kts de HEAD
# (JavaLanguageVersion.of(22)): verificado con
# `git show HEAD:build.gradle.kts`, no hay skip-worktree activo ni
# diferencia entre working tree y HEAD en este archivo.
FROM eclipse-temurin:22-jdk AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src src

# gradlew editado en Windows llega con saltos de línea CRLF y Linux
# no lo reconoce como script. Se normaliza antes de ejecutarlo.
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# bootJar en vez de build: no genera el jar "plain" ni corre pruebas.
RUN ./gradlew bootJar --no-daemon -x test

# ---------- Etapa 2: ejecución ----------
FROM eclipse-temurin:22-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]