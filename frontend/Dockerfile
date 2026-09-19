# ============================================================
# commerce-platform - frontend Dockerfile
#
# Desarrollo/demo: instala dependencias con package-lock.json
# (build reproducible) y sirve la SPA con el dev server de
# Angular escuchando en 0.0.0.0:4200. No se introduce nginx
# todavia: eso sera parte del deployment/finalizacion.
# ============================================================

FROM node:22

WORKDIR /app

# package-lock.json garantiza una instalacion reproducible.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

EXPOSE 4200

# El dev server debe escuchar en 0.0.0.0 para ser accesible
# desde el host a traves del puerto publicado por Compose.
CMD ["npm", "start", "--", "--host", "0.0.0.0", "--port", "4200"]
