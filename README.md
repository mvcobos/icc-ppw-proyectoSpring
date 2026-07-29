# icc-ppw-proyectoSpring

## Despliegue (Punto 15 y 16)

Backend desplegado como contenedor Docker en Render, con PostgreSQL y
Redis/Key Value como servicios separados (ver `render.yaml`).

### Variables de entorno requeridas

| Variable | Obligatoria en prod | Origen sugerido en Render |
|---|---|---|
| `PORT` | la asigna Render | automática |
| `DB_URL` | sí | manual (`sync: false`), formato `jdbc:postgresql://host/db` |
| `DB_USERNAME` | sí | inyectada desde la base de datos |
| `DB_PASSWORD` | sí | inyectada desde la base de datos |
| `REDIS_URL` | sí | inyectada desde el servicio Key Value |
| `JWT_SECRET` | sí | generada por Render (`generateValue: true`) |
| `JWT_ACCESS_EXPIRATION` | sí | valor fijo (ms) |
| `JWT_REFRESH_EXPIRATION` | sí | valor fijo (ms) |
| `ALLOWED_ORIGINS` | sí | manual, dominios del frontend en prod |
| `SWAGGER_USER` | sí | manual |
| `SWAGGER_PASSWORD` | sí | manual |
| `JAVA_TOOL_OPTIONS` | recomendada | límite de heap para el plan free |

Ver `.env.example` para el listado completo con valores de ejemplo (no
reales). Nunca subir un `.env` con credenciales reales al repositorio.

### Construir la imagen en local

```powershell
docker build -t proyecto-api .
```

### URLs públicas

- API: `<pendiente>`
- Swagger: `<pendiente>/api/swagger-ui.html`
- Actuator Health: `<pendiente>/api/actuator/health`