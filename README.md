# VidaSalud MS BFF

Backend for Frontend de VidaSalud. Recibe las solicitudes del frontend, valida los JWT emitidos por Microsoft Entra ID y las enruta hacia los microservicios de atenciones y catálogo.

## Responsabilidades

- Validar firma, vigencia, issuer y claims del JWT mediante Spring Security OAuth2 Resource Server.
- Autorizar las solicitudes según roles como `Admin`, `Recepcionista` y `Paciente`.
- Exponer una entrada única para el frontend.
- Enrutar solicitudes hacia appointments y catalog.
- Gestionar CORS para el frontend Angular.

## Tecnologías

- Java 17.
- Spring Boot 3.2.5.
- Spring Web y `RestClient`.
- Spring Security.
- OAuth2 Resource Server con JWT.
- Maven.

## Puerto y endpoints

El BFF utiliza el puerto `8080`.

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/me` | Devuelve información del usuario autenticado. |
| GET | `/api/appointments` | Consulta atenciones a través del microservicio correspondiente. |
| GET | `/api/appointments/{id}` | Consulta una atención por ID. |
| POST | `/api/appointments` | Crea una atención. |
| PUT | `/api/appointments/{id}/status` | Cambia el estado de una atención. |
| GET | `/api/catalog/services` | Consulta el catálogo de prestaciones. |
| POST | `/api/catalog/services` | Crea una prestación, según el rol. |
| PUT | `/api/catalog/services/{id}` | Actualiza una prestación, según el rol. |

Las solicitudes `OPTIONS` se permiten sin JWT porque corresponden al preflight de CORS. Las solicitudes reales permanecen protegidas.

## Configuración

En `src/main/resources/application.yml` se configuran el puerto, el endpoint de claves públicas de Microsoft Entra ID, las URLs de los microservicios y los orígenes permitidos para CORS.

Variables útiles:

```bash
APPOINTMENTS_URL=http://localhost:8081
CATALOG_URL=http://localhost:8082
CORS_ORIGINS=http://localhost:4201
```

En Docker, los servicios pueden utilizar los nombres de contenedor `appointments:8081` y `catalog:8082`.

## Ejecución local

Requisitos: Java 17, Maven y los microservicios appointments y catalog disponibles.

```bash
mvn spring-boot:run
```

Para generar y ejecutar el JAR:

```bash
mvn clean package -DskipTests
java -jar target/bff-1.0.0.jar
```

## Docker

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
# vidasalud-ms-bff
API Backend for Frontend en Spring Boot + Spring Security (Valida JWT de Azure AD y enruta peticiones)
