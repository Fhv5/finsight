# Finsight

Finsight es un sistema de gestión de finanzas personales digital y interactivo, diseñado para brindarte visibilidad y control absoluto en tiempo real sobre el destino de tu dinero y el estado de tus ahorros.

## El Problema

Frecuentemente las personas pierden el control de su dinero debido a gastos imprevistos y falta de seguimiento, viéndose a menudo obligadas a endeudarse. El seguimiento manual de las finanzas mediante cuadernos es ineficiente, propenso a errores y dificulta el registro rápido de transacciones cotidianas.

## La Solución

Finsight proporciona una plataforma centralizada donde puedes registrar convenientemente tus transacciones (gastos, ingresos y transferencias), asociarlas a cuentas personalizadas, categorizar tus movimientos y visualizar con claridad tu panorama financiero para tomar mejores decisiones.

## Stack Tecnológico

La aplicación está construida sobre un backend sólido aprovechando las herramientas más modernas del ecosistema Java:

- **Lenguaje:** Java 25
- **Framework:** Spring Boot 4.0.5
- **Base de Datos:** PostgreSQL
- **Migraciones:** Flyway
- **Persistencia:** Spring Data JPA
- **Seguridad:** Spring Security (OAuth2)
- **Documentación API:** Springdoc OpenAPI (Swagger)
- **Utilidades:** Lombok

## Características y Hoja de Ruta (Roadmap)

El proyecto está diseñado bajo distintas fases de desarrollo. A continuación se detallan las funcionalidades principales y su estado esperado.

### ✅ Capacidades Principales (Core)
- **Gestión de Cuentas:** Creación de múltiples cuentas personalizadas para organizar el dinero según su origen (ej. Cuenta Vista, Efectivo, Tarjeta de Crédito).
- **Categorías Personalizadas:** Clasificación flexible de ingresos y gastos, específica para cada usuario.
- **Registro de Transacciones:** Gestión completa (CRUD) de ingresos, gastos y transferencias entre cuentas. Actualización automática de saldos.
- **Seguridad y Usuarios:** Autenticación de usuarios para mantener la privacidad y persistencia de sus datos en la nube.

### 🚧 Funcionalidades Próximas (Por implementar)
- **Metas de Ahorro:** Configuración de objetivos de ahorro y seguimiento de abonos hacia esa meta específica.
- **Presupuestos:** Asignación de topes de presupuesto por categoría (diario, semanal, mensual) y sistema de alertas (ej., notificar al llegar al 80% del límite).
- **Panel de Análisis (Insights):** Visualización de gráficos para entender la distribución porcentual de gastos por categoría y curvas de crecimiento de capital a lo largo del tiempo.
- **Informes Mensuales Automáticos:** Generación de resúmenes financieros mensuales y envío automatizado por correo en formatos PDF y CSV.

## Cómo ejecutar el proyecto localmente

1. **Levantar la base de datos:** El proyecto incluye un archivo `docker-compose.yaml` para levantar rápidamente un contenedor de PostgreSQL.
   ```bash
   docker compose up -d
   ```

2. **Ejecutar la aplicación Spring Boot:** Utiliza el archivo *wrapper* de Maven incluido. En la raíz del proyecto, ejecuta:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(Si estás usando Windows, ejecuta `mvnw.cmd spring-boot:run`)*

3. **Acceso a la API:** Una vez iniciada, normalmente podrás revisar los endpoints en la interfaz de Swagger UI (revisa la configuración del puerto en caso de dudas, habitualmente interactuando en `http://localhost:8080/swagger-ui.html`).

## Documentación de Planificación

Para revisar el detalle completo del proyecto, los actores, eventos críticos y especificación de *Historias de Usuario*, puedes consultar el documento inicial en [`docs/PLANNING.md`](./docs/PLANNING.md).
