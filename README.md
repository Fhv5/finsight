# Finsight

Finsight es un sistema de gestion de finanzas personales diseñado para ofrecer visibilidad sobre tus fondos y ayudarte a alcanzar tus metas de ahorro.

## Stack Tecnologico

- Java 25
- Spring Boot 4.0.5
- PostgreSQL
- Flyway
- Spring Data JPA
- Spring Security (Auth con JWT)
- Springdoc OpenAPI / Swagger (Docs)
- Lombok

## Características Principales

El sistema ofrece los siguientes modulos y funcionalidades:

### Gestion de Cuentas
- Creacion y administracion de cuentas financieras (ej. Efectivo, Tarjeta de Credito, Cuenta Vista).
- Actualizacion automatica del saldo disponible segun las transacciones asociadas.

### Categorias Personalizadas
- Clasificacion de ingresos y gastos de forma especifica por usuario.
- Restricciones para evitar modificaciones de tipo o eliminaciones si existen transacciones o presupuestos asociados.

### Registro de Transacciones
- Registro detallado de ingresos (INGRESO), gastos (GASTO) y transferencias (TRANSFERENCIA) entre cuentas.
- Reversion y recalculacion automatica de saldos al crear, actualizar o eliminar movimientos.

### Presupuestos
- Definicion de limites de gasto mensual por categoria de tipo GASTO.
- Consulta de acumulado mensual de gastos calculados en tiempo real segun la zona horaria del usuario.
- Restricciones de integridad para evitar presupuestos duplicados por categoria.

### Cuentas de ahorro
- Definicion de cuentas de ahorro con una meta asociada.
- Consulta del progreso de cuentas de ahorro.

### Seguridad y Autenticacion
- Registro, inicio de sesion y refresco de tokens JWT.
- Aislamiento de datos por usuario en todos los endpoints de la API.

## Por Implementar

- Reportes periodicos
- Multiples monedas (Actualmente usa un Long generico)

## Configuracion y Ejecucion Local

### Requisitos Previos
- Java 25
- Docker y Docker Compose

### 1. Iniciar Base de Datos
Levante el contenedor de PostgreSQL en segundo plano:
```bash
docker compose up -d
```

### 2. Ejecutar la Aplicacion Spring Boot
Usa el Maven wrapper incluido en el proyecto:
```bash
./mvnw spring-boot:run
```
En Windows puedes usar:
```cmd
mvnw.cmd spring-boot:run
```

### 3. Documentacion de la API
Una vez levantado el servicio, accede a la especificacion OpenAPI e interfaz Swagger interactiva en la siguiente direccion:
```
http://localhost:8080/swagger-ui.html
```
