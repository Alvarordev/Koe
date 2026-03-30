# PRD: Captura Automática de Transacciones Yape

**Feature:** Yape Notification Listener
**Plataforma:** Android (CarWash Pro / Expense Tracker)
**Estado:** Draft
**Última actualización:** 2026-03-23

---

## Resumen

Implementar un servicio que escuche las notificaciones de la app Yape (BCP) para capturar automáticamente transacciones de ingreso y gasto. El usuario activa esta funcionalidad desde Settings a través de un flujo de onboarding de 3 pantallas, donde se explica la feature, se crea una categoría de sistema "Yape", se asigna una cuenta destino, y se otorgan los permisos necesarios.

---

## Problema

El usuario registra manualmente cada transacción realizada por Yape. Esto genera fricción, olvidos, y datos incompletos. Yape es el medio de pago digital más usado en Perú — la mayoría de transacciones diarias pasan por ahí.

## Solución

Un `NotificationListenerService` que intercepta notificaciones de Yape, parsea el monto/tipo/contraparte, y crea transacciones automáticamente en la cuenta y categoría configuradas por el usuario.

---

## Flujo de Usuario

### Punto de entrada

Botón en la pantalla de **Settings**: `"Captura automática Yape"` con un indicador de estado (activo/inactivo).

### Pantalla 1 — Explicación de la feature

- **Contenido:**
  - Ilustración o icono representativo de Yape + la app.
  - Título: "Registra tus Yapes automáticamente"
  - Descripción corta explicando qué hace: "Capturamos tus notificaciones de Yape para registrar ingresos y gastos sin que tengas que hacer nada."
  - Nota de privacidad: "Solo leemos notificaciones de Yape. Todo se queda en tu dispositivo."
- **Acción:** Botón "Siguiente" en la parte inferior.

### Pantalla 2 — Categoría y Cuenta

Esta pantalla tiene dos secciones:

**Sección superior — Categoría "Yape"**

- Se muestra un pill/chip de categoría con el label "Yape" y un ícono representativo.
- Texto explicativo: "Crearemos una categoría de sistema llamada Yape. Todas las transacciones capturadas se asignarán aquí."
- La categoría es de sistema (`is_system = true`), aplica tanto para tipo `expense` como `income`.
- El usuario no puede editar el nombre ni eliminar esta categoría una vez creada.

**Sección inferior — Selección de cuenta**

- Un box/card que dice: "Elige tu cuenta Yape"
- Texto secundario: "¿En qué cuenta registramos las transacciones?"
- Al tocar, se abre un **dialog/bottom sheet** con la lista de cuentas existentes del usuario.
- El usuario selecciona una cuenta. El box muestra la cuenta seleccionada.
- Si no hay cuentas, mostrar opción de crear una (o redirigir a la pantalla de creación de cuentas).

**Acción:** Botón "Siguiente" — habilitado solo cuando se ha seleccionado una cuenta.

### Pantalla 3 — Permiso de notificaciones

- **Contenido:**
  - Texto explicando que necesitamos acceso a notificaciones.
  - "Para funcionar, necesitamos que nos des acceso a tus notificaciones en la configuración de tu dispositivo."
  - Nota: "Android mostrará que podemos leer todas las notificaciones. Solo procesamos las de Yape y descartamos el resto."
- **Acción:** Botón "Activar" → abre `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`.
- Al regresar del sistema, verificar si el permiso fue concedido:
  - **Sí:** Navegar a pantalla de confirmación / volver a Settings con estado activo.
  - **No:** Mostrar mensaje indicando que la feature no funcionará sin el permiso. Permitir reintentar o salir.

### Estado activo en Settings

Una vez completado el setup:

- El botón en Settings cambia a mostrar estado **"Activo"** con un indicador visual (dot verde, ícono check, etc.).
- Al tocar, se abre una pantalla de detalle con:
  - Estado del servicio: "Escuchando notificaciones" / "Servicio detenido".
  - Cuenta asignada (con opción de cambiarla).
  - Última transacción capturada (timestamp + monto + tipo) como prueba de que funciona.
  - Contador de transacciones capturadas hoy / esta semana.
  - Botón para desactivar la feature (revoca el listener, elimina la configuración pero no las transacciones ya creadas).

---

## Modelo de Datos

### Tabla: `categories` (cambios)

Se agrega una categoría de sistema:

| Campo | Valor |
|---|---|
| `name` | "Yape" |
| `type` | `both` (o registros duales: uno `expense`, uno `income`) |
| `is_system` | `true` |
| `icon` | `ic_yape` (ícono custom o uno genérico de pagos móviles) |
| `color` | Morado Yape (#6B2D8B) o el que se decida |

> **Decisión de diseño:** Si el schema actual de `categories` solo permite `expense` o `income` como tipo, se crean dos registros con un flag `system_group = "yape"` para vincularlos.

### Tabla: `yape_listener_config` (nueva, local)

Configuración local del listener. No necesita sincronizarse con Supabase.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Int` (PK) | Siempre 1 (singleton) |
| `is_enabled` | `Boolean` | Feature activa o no |
| `account_id` | `String` (FK) | Cuenta destino seleccionada |
| `category_expense_id` | `String` (FK) | ID de la categoría Yape (expense) |
| `category_income_id` | `String` (FK) | ID de la categoría Yape (income) |
| `created_at` | `Long` | Timestamp de activación |

### Tabla: `processed_notifications` (nueva, local)

Para deduplicación. Tabla local en Room.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` (PK, autoincrement) | — |
| `dedup_key` | `String` (UNIQUE) | `"${postTime}_${sbn.key}_${contentHash}"` |
| `amount` | `Double` | Monto parseado |
| `type` | `String` | `"income"` o `"expense"` |
| `processed_at` | `Long` | Timestamp de procesamiento |

> **Limpieza:** Purgar registros con más de 30 días para no crecer indefinidamente.

### Inserción en `transactions`

Cada notificación válida y no duplicada genera un registro en la tabla `transactions` existente:

| Campo | Valor |
|---|---|
| `amount` | Monto parseado de la notificación |
| `type` | `income` o `expense` según el tipo de yapeo |
| `category_id` | `category_expense_id` o `category_income_id` de la config |
| `account_id` | `account_id` de la config |
| `description` | Texto descriptivo: "Yapeo de Juan" / "Pago en Tienda X" |
| `date` | Timestamp de la notificación (`sbn.postTime`) |
| `source` | `"yape_auto"` (para distinguir de transacciones manuales) |

---

## Componentes Técnicos

### 1. `YapeNotificationListenerService`

- Extiende `NotificationListenerService`.
- Filtro: `sbn.packageName == "com.bcp.innovacxion.yapeapp"`.
- Inyección con Hilt (`@AndroidEntryPoint`).
- Delega parsing y persistencia al caso de uso.

### 2. `YapeNotificationParser`

- Object / clase en la capa de dominio.
- Regex para extraer monto (`S/ XX.XX`), tipo (recibido/enviado/pago), y contraparte.
- Retorna `ParsedYapeTransaction?` — null si no se puede parsear (fallo graceful).
- **Requiere validación empírica:** capturar notificaciones reales de Yape para ajustar los patrones.

### 3. `ProcessYapeNotificationUseCase`

- Recibe la notificación parseada.
- Verifica dedup contra `processed_notifications`.
- Lee config de `yape_listener_config`.
- Inserta en `transactions` + registra en `processed_notifications`.

### 4. `YapeListenerConfigRepository`

- CRUD sobre `yape_listener_config` (Room).
- Expone `Flow<YapeListenerConfig?>` para observar estado.

### 5. UI — Setup Flow

- 3 pantallas Compose en un NavGraph dedicado (e.g., `yape_setup_graph`).
- `YapeSetupViewModel` con estado del flujo y lógica de permisos.
- Navegación: `Settings → YapeSetupScreen1 → YapeSetupScreen2 → YapeSetupScreen3 → Settings`.

### 6. UI — Estado activo

- Composable en Settings que observa `YapeListenerConfig`.
- Pantalla de detalle con stats del listener.

---

## Deduplicación — Estrategia Doble Capa

**Capa 1 — Notification-level (técnica):**
- Clave: `"${sbn.postTime}_${sbn.key}_${content.hashCode()}"`.
- Previene reprocesamiento cuando Android rebindea el servicio o la notificación se actualiza.

**Capa 2 — Business-level (lógica):**
- Mismo monto + mismo tipo + misma contraparte + dentro de ventana de 60 segundos = probable duplicado.
- Esto cubre edge cases donde la notificación llega con un `sbn.key` diferente (reinstalación, agrupamiento, etc.).

---

## Resiliencia del Servicio

### Problema: OEMs que matan servicios en background

Xiaomi, Huawei, Oppo, y Samsung tienen gestión agresiva de batería. Esto es crítico para el mercado peruano donde estas marcas tienen alta penetración.

### Soluciones:

1. **Guía in-app:** Si detectamos el OEM (via `Build.MANUFACTURER`), mostrar instrucciones específicas para excluir la app de optimización de batería.
2. **`requestRebind()`:** Si el listener se desconecta, intentar reconectar.
3. **Health check periódico:** Un `WorkManager` job que cada X horas verifica si el listener sigue activo. Si no, notificar al usuario.
4. **Indicador en Settings:** El estado "Servicio detenido" alerta al usuario de que algo pasó.

---

## Parsing de Notificaciones Yape

### Formatos conocidos (requiere validación empírica)

| Tipo | Título (approx) | Texto (approx) |
|---|---|---|
| Ingreso | "Yapeo recibido" | "[Nombre] te envió S/ XX.XX" |
| Gasto (P2P) | "Yapeo realizado" | "Enviaste S/ XX.XX a [Nombre]" |
| Gasto (QR) | "Pago con QR" | "Pagaste S/ XX.XX en [Comercio]" |
| Gasto (servicios) | "Pago de servicio" | "Pagaste S/ XX.XX - [Servicio]" |

### Campos a extraer

- `amount`: Regex `S/\s?([\d,]+\.?\d{0,2})`
- `type`: Basado en keywords del título ("recibido" → income, "realizado"/"pago" → expense)
- `counterparty`: Regex contextual según el tipo
- `description`: Texto original o resumen

### Manejo de formatos desconocidos

- Si el parser no puede extraer el monto → descartar silenciosamente + log analytics.
- Si el parser extrae monto pero no tipo → default a `expense` + flag `needs_review = true` en la transacción.
- **Métricas:** Trackear ratio de parseo exitoso vs fallido para detectar cambios en el formato de Yape.

---

## Consideraciones de Privacidad y Play Store

### Permisos

- `BIND_NOTIFICATION_LISTENER_SERVICE` — requerido.
- Requiere declaración en Play Console (Permissions Declaration Form).

### Justificación ante Google

> "La app es un tracker de gastos personales. Usamos NotificationListenerService exclusivamente para capturar notificaciones de la app Yape (BCP) y registrar automáticamente transacciones financieras. No almacenamos ni transmitimos contenido de notificaciones de otras apps."

### Política de privacidad

Actualizar la política de privacidad para incluir:
- Qué datos se capturan (monto, tipo, contraparte de notificaciones Yape).
- Que no se capturan datos de otras apps.
- Que los datos se almacenan localmente (y en Supabase si hay sync).
- Que el usuario puede desactivar la feature en cualquier momento.

---

## Scope MVP

### Incluido

- [ ] Flujo de setup de 3 pantallas desde Settings.
- [ ] Creación automática de categoría de sistema "Yape".
- [ ] Selector de cuenta destino con dialog.
- [ ] `NotificationListenerService` filtrando por package de Yape.
- [ ] Parser básico para los 3 tipos principales (recibido, enviado P2P, pago QR).
- [ ] Deduplicación doble capa.
- [ ] Indicador de estado en Settings (activo/inactivo/detenido).
- [ ] Pantalla de estado con última transacción capturada.
- [ ] Tabla `processed_notifications` con purga automática a 30 días.
- [ ] Inserción en `transactions` con `source = "yape_auto"`.

### Excluido del MVP

- Soporte para otras apps de pago (Plin, billeteras bancarias).
- Edición de la transacción capturada desde la notificación misma.
- Notificación in-app cada vez que se captura una transacción.
- Configuración remota de patrones de parsing (remote config).
- Sync de `yape_listener_config` a Supabase.
- Detección inteligente de categoría (e.g., si pagas en un restaurante → categoría "Comida").

---

## Riesgos

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Yape cambia el formato de notificaciones | Parser se rompe silenciosamente | Analytics de parse failures + fallback graceful |
| Google rechaza la app en Play Store | No se puede publicar | Justificación clara + política de privacidad actualizada |
| OEMs matan el servicio | Transacciones no se capturan | Guía por OEM + health check + indicador de estado |
| Notificaciones agrupadas | Se pierde detalle individual | Parsear `EXTRA_TEXT_LINES` para bundles |
| Usuario desinstala y reinstala Yape | `sbn.key` cambia | Business-level dedup como segunda capa |

---

## Definición de "Listo"

1. El usuario puede completar el flujo de setup sin errores.
2. Las transacciones de Yape se capturan automáticamente y aparecen en la lista de transacciones.
3. No hay transacciones duplicadas bajo uso normal.
4. El estado del servicio se refleja correctamente en Settings.
5. La feature se puede desactivar limpiamente.
6. La app pasa la validación de permisos de Play Store.
