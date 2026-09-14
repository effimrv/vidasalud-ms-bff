# VidaSalud MS BFF

Backend for Frontend de VidaSalud. Recibe las solicitudes del frontend, valida los JWT emitidos por Microsoft Entra ID y las enruta hacia los microservicios de atenciones y catálogo.

## Responsabilidades

- Validar firma, vigencia, issuer y claims del JWT con Spring Security OAuth2 Resource Server.
- Autorizar solicitudes según roles como `Admin`, `Recepcionista` y `Paciente`.
- Exponer una entrada única para el frontend y gestionar CORS.
- Enrutar solicitudes hacia appointments y catalog.

## Tecnologías

- Java 17, Spring Boot 3.2.5, Spring Web, `RestClient`, Spring Security, OAuth2 Resource Server con JWT y Maven.

## Puerto y endpoints

El BFF utiliza el puerto `8080`.

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/me` | Devuelve información del usuario autenticado. |
| GET | `/api/appointments` | Consulta atenciones. |
| GET | `/api/appointments/{id}` | Consulta una atención por ID. |
| POST | `/api/appointments` | Crea una atención. |
| PUT | `/api/appointments/{id}/status` | Cambia el estado. |
| GET | `/api/catalog/services` | Consulta prestaciones. |
| POST / PUT | `/api/catalog/services` | Administra prestaciones según el rol. |

Las solicitudes `OPTIONS` se permiten sin JWT porque corresponden al preflight de CORS. Las solicitudes reales permanecen protegidas.

## Configuración y ejecución

Las variables principales son `APPOINTMENTS_URL`, `CATALOG_URL` y `CORS_ORIGINS`. En Docker se pueden utilizar `appointments:8081` y `catalog:8082`.

```bash
mvn spring-boot:run
mvn clean package -DskipTests
java -jar target/bff-1.0.0.jar
```

```bash
docker build -t bff .
docker run -d --name bff -p 8080:8080 \
  -e APPOINTMENTS_URL=http://appointments:8081 \
  -e CATALOG_URL=http://catalog:8082 \
  -e CORS_ORIGINS=http://localhost:4201 \
  bff
```

## Verificación rápida

```bash
curl -i http://localhost:8080/api/health
```

Para probar rutas protegidas se necesita un token Bearer válido emitido por Microsoft Entra ID.
