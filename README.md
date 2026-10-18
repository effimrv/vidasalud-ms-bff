# VidaSalud MS BFF

Backend for Frontend de VidaSalud. Recibe las solicitudes del frontend, valida los JWT emitidos por Microsoft Entra ID y las enruta hacia los microservicios de atenciones y catálogo.

Matriz de funcionalidades y documentación del proyecto: [vidasalud-docs](https://github.com/effimrv/vidasalud-docs).

## Responsabilidades

- Validar firma, vigencia, issuer y claims del JWT con Spring Security OAuth2 Resource Server.
- Autorizar solicitudes según los roles `Admin`, `Recepcionista` y `Paciente`. Una cuenta sin roles (registrada por autoservicio) es `Paciente`.
- Limitar a cada paciente a sus propias citas: el BFF fija su identificador (`oid`), nombre y correo desde el token al listar, consultar, crear y cancelar, sin importar lo que envíe el frontend.
- Registrar quién hace cada cambio de estado (paciente, recepción o administración).
- Exponer una entrada única para el frontend y gestionar CORS.
- Enrutar solicitudes hacia appointments y catalog, con timeouts (5 s de conexión, 15 s de respuesta) y propagando el código de error real de cada microservicio (503 si no responde).

## Tecnologías

- Java 17, Spring Boot 3.2.5, Spring Web, `RestClient`, Spring Security, OAuth2 Resource Server con JWT y Maven.

## Puerto y endpoints

El BFF utiliza el puerto `8080`.

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/me` | Nombre para mostrar, nombre completo y roles efectivos del usuario. |
| GET | `/api/appointments` | Atenciones (un paciente recibe solo las suyas). |
| GET | `/api/appointments/{id}` | Detalle de una atención con su historial (un paciente, solo las suyas). |
| GET | `/api/appointments/disponibilidad?servicioId=&fecha=` | Horas disponibles de una prestación en un día. |
| POST | `/api/appointments` | Crea una atención (a nombre del paciente del token). |
| PUT | `/api/appointments/{id}/status` | Cancela una atención: el personal, cualquiera; un paciente, solo las suyas. |
| GET | `/api/catalog/services` | Consulta prestaciones. |
| POST | `/api/catalog/services` | Crea una prestación (solo Admin). |
| PUT | `/api/catalog/services/{id}` | Actualiza una prestación (solo Admin). |
| GET | `/api/info` | Información institucional (pública). |
| PUT | `/api/info` | Actualiza la información institucional (solo Admin). |
| GET | `/api/health` | Estado del servicio (público). |

Las solicitudes `OPTIONS` se permiten sin JWT porque corresponden al preflight de CORS. Las solicitudes reales permanecen protegidas.

## Configuración y ejecución

Las variables principales son `APPOINTMENTS_URL`, `CATALOG_URL` y `CORS_ORIGINS`. En la nube, `CORS_ORIGINS` debe incluir la URL del frontend desplegado (`https://e7u1iiqh4k.execute-api.us-east-1.amazonaws.com`); si un origen no está en la lista, el BFF responde 403 *Invalid CORS request*. En Docker se pueden utilizar `appointments:8081` y `catalog:8082`.

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
