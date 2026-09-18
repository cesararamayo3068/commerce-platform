# ============================================================
# commerce-platform — backend Dockerfile (Etapa 1)
#
# Multi-stage:
#   build   -> maven:3.9-eclipse-temurin-21 (compila el jar)
#   runtime -> eclipse-temurin:21-jre (imagen liviana)
#
# El build NO usa Maven ni Java de Windows: todo ocurre dentro
# del contenedor. La cache de dependencias Maven se guarda en la
# cache de BuildKit (target=/root/.m2), aislada del .m2 global.
# ============================================================

# ---------- STAGE 1: BUILD ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Resolucion de dependencias con cache (BuildKit)
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q dependency:go-offline

# Compilacion del codigo
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests package

# ---------- STAGE 2: RUNTIME ----------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# curl para el healthcheck del compose
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Usuario no root (buena practica)
RUN useradd --create-home --shell /usr/sbin/nologin appuser
USER appuser

COPY --from=build /app/target/commerce-backend-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
