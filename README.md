# icc-ppw-proyectoSpring

## Despliegue (Punto 15 y 16)

Backend desplegado como contenedor Docker en Render, con PostgreSQL y
Redis/Key Value como servicios separados (ver `render.yaml`).

### Variables de entorno requeridas

Ninguna de las filas siguientes lleva un valor real: son solo nombre,
para qué sirve y de dónde sale en Render. Ver `.env.example` para el
listado completo con valores de ejemplo (no reales). Nunca subir un
`.env` con credenciales reales al repositorio.

| Variable | Para qué sirve | Origen en Render |
|---|---|---|
| `PORT` | puerto en el que escucha el servidor embebido | automática |
| `DB_URL` | cadena de conexión JDBC a PostgreSQL | manual (`sync: false`); Render entrega `postgresql://...`, hay que reescribirla como `jdbc:postgresql://host/db` |
| `DB_USERNAME` | usuario de la base de datos | inyectada desde el recurso de base de datos |
| `DB_PASSWORD` | contraseña de la base de datos | inyectada desde el recurso de base de datos |
| `REDIS_URL` | cadena de conexión a Redis/Key Value (rate limiting y bloqueo de login) | inyectada desde el servicio Key Value |
| `JWT_SECRET` | clave de firma HS384 de los tokens (mínimo 384 bits) | manual (`sync: false`); `generateValue` no garantiza la longitud mínima |
| `JWT_ACCESS_EXPIRATION` | vigencia del access token en ms | valor fijo |
| `JWT_REFRESH_EXPIRATION` | vigencia del refresh token en ms | valor fijo |
| `ALLOWED_ORIGINS` | orígenes permitidos por CORS | manual, dominios del frontend en prod |
| `SWAGGER_USER` | usuario para acceder a Swagger UI en prod | manual |
| `SWAGGER_PASSWORD` | contraseña para acceder a Swagger UI en prod | manual |
| `JAVA_TOOL_OPTIONS` | límites de heap/metaspace de la JVM | recomendada, necesaria en el plan free (512 MB) |

### Construir y correr con Docker en local

```powershell
docker build -t proyecto-api .

docker run --rm -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=prod `
  -e PORT=8080 `
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/academic_events_db" `
  -e DB_USERNAME=ups `
  -e DB_PASSWORD=ups123 `
  -e REDIS_URL="redis://host.docker.internal:6379" `
  -e JWT_SECRET="<clave-local-de-prueba>" `
  -e JWT_ACCESS_EXPIRATION=900000 `
  -e JWT_REFRESH_EXPIRATION=604800000 `
  -e ALLOWED_ORIGINS="http://localhost:5173" `
  -e SWAGGER_USER=evaluador `
  -e SWAGGER_PASSWORD="<clave-local-de-prueba>" `
  proyecto-api
```

`host.docker.internal` apunta a Postgres/Redis levantados con
`docker compose` en el host. Ninguno de los valores de ejemplo de arriba
es un secreto real.

### URLs públicas

- API: https://proyecto-api-2jua.onrender.com/api
- Swagger: https://proyecto-api-2jua.onrender.com/api/swagger-ui/index.html
- Actuator Health: https://proyecto-api-2jua.onrender.com/api/actuator/health

Credenciales de Swagger: usuario `evaluador`, contraseña (entregada por
separado).

> El plan gratuito de Render suspende el servicio tras 15 minutos de
> inactividad. La primera petición tras despertarlo puede tardar hasta
> un minuto en responder. Se recomienda abrir primero la URL de health
> para "despertar" el servicio antes de probar el resto.

> La base de datos gratuita de Render expira 30 días después de su
> creación (creada el 28 de julio de 2026).

### Credenciales de prueba (datos semilla)

No son secretos: vienen de la migración `V1__initial_schema_and_data.sql`.

| Rol | Email | Contraseña |
|---|---|---|
| Organizador | `maria.cordero@academic.test` | `Password123*` |
| Participante | `carlos.velez@academic.test` | `Password123*` |

### Verificación post-despliegue

Una vez que la API tenga URL pública, ejecutar:

```powershell
.\scripts\verify-deploy.ps1 -BaseUrl "https://<tu-app>.onrender.com/api"
```

Recorre health check, login, lectura de un evento, descarga de reporte
PDF (propio y ajeno), bloqueo por intentos fallidos de login y CORS
contra un origen no autorizado, e imprime un resumen de qué pasó y qué
falló. El primer request puede tardar hasta 90 segundos por el arranque
en frío del plan free; el script ya lo contempla.