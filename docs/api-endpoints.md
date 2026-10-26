# Contrato de la API (borrador)

Peticiones que tendrá el aplicativo, con el objeto que recibe el controller y el JSON que responde el servidor. Es un borrador para revisar en equipo: lo marcado como **supuesto** está pendiente de confirmar.

## Convenciones

- Base: `/api`. Todo el cuerpo va en JSON (`Content-Type: application/json`)
- Fechas en ISO-8601: `2026-10-22T14:30:00`
- Los estados viajan como texto: `DISPONIBLE`, `ADOPTADA`, `RECIBIDA`, `EN_REVISION`, `APROBADA`, `RECHAZADA`
- Los campos opcionales sin valor se devuelven como `null`
- Los datos de contacto (`solicitanteCorreo`, `telefono`) solo salen en el detalle de una solicitud para quien tiene permiso, nunca en el catálogo ni en los listados

### Identificación (supuesto)

Sin login real: el cliente envía dos cabeceras y el servidor las usa para autorizar y para llenar `actor` en el historial.

| Cabecera | Valor |
|---|---|
| `X-Rol` | `FUNDACION`, `COORDINADOR` o `SOLICITANTE` |
| `X-Usuario` | Nombre del empleado, o el correo si el rol es `SOLICITANTE` |

Un solicitante es "dueño" de una solicitud cuando `X-Usuario` coincide (sin importar mayúsculas) con `solicitante_correo`.

### Formato de error (todos los endpoints)

```json
{
  "status": 409,
  "error": "Conflict",
  "codigo": "RN-01",
  "mensaje": "Ya tienes una solicitud activa para esta mascota",
  "ruta": "/api/solicitudes-adopcion",
  "fecha": "2026-10-22T14:30:00"
}
```

`codigo` lleva la regla incumplida (`RN-01`, `RN-02`, `RN-05`, `RN-06`) o `VALIDACION`, `NO_ENCONTRADO`, `NO_AUTENTICADO`, `SIN_PERMISO`, `CONFLICTO` (409 que no corresponde a una regla, por ejemplo eliminar una mascota con solicitudes). En un 400 de validación se agrega `"errores": [{ "campo": "...", "mensaje": "..." }]`.

## Resumen

| # | Verbo | Ruta | Quién | Éxito |
|---|---|---|---|---|
| 1 | GET | `/api/mascotas?estado=` | Público | 200 |
| 2 | GET | `/api/mascotas/{id}` | Público | 200 |
| 3 | POST | `/api/mascotas` | FUNDACION | 201 |
| 4 | PUT | `/api/mascotas/{id}` | FUNDACION | 200 |
| 5 | DELETE | `/api/mascotas/{id}` | FUNDACION | 204 |
| 6 | POST | `/api/solicitudes-adopcion` | Público (con datos del solicitante) | 201 |
| 7 | GET | `/api/solicitudes?mascotaId=&estado=` | FUNDACION, COORDINADOR | 200 |
| 8 | GET | `/api/solicitudes/{id}` | Staff o solicitante dueño | 200 |
| 9 | PATCH | `/api/solicitudes/{id}/estado` | FUNDACION, COORDINADOR | 200 |
| 10 | DELETE | `/api/solicitudes/{id}` | Solicitante dueño | 204 |

Los números 1, 4, 6, 8, 9 y 10 son los del enunciado. Los demás (2, 3, 5, 7) son extras para completar el CRUD.

## Mapeo de endpoints a clases DTO

Todas las clases están en el paquete `co.edu.autonoma.adopcionmascotas.dto`. Cualquier error de cualquier endpoint se responde con `ErrorResponse`.

| # | Verbo y ruta | Clase request | Clase response (éxito) | Código |
|---|---|---|---|---|
| 1 | GET `/api/mascotas?estado=` | Ninguna (parámetro `estado`) | `List<MascotaResponse>` | 200 |
| 2 | GET `/api/mascotas/{id}` | Ninguna | `MascotaResponse` | 200 |
| 3 | POST `/api/mascotas` | `MascotaRequest` | `MascotaResponse` | 201 |
| 4 | PUT `/api/mascotas/{id}` | `MascotaRequest` | `MascotaResponse` | 200 |
| 5 | DELETE `/api/mascotas/{id}` | Ninguna | Sin cuerpo | 204 |
| 6 | POST `/api/solicitudes-adopcion` | `SolicitudRequest` | `SolicitudDetalleResponse` | 201 |
| 7 | GET `/api/solicitudes?mascotaId=&estado=` | Ninguna (parámetros `mascotaId` y `estado`) | `List<SolicitudResumenResponse>` | 200 |
| 8 | GET `/api/solicitudes/{id}` | Ninguna | `SolicitudDetalleResponse` | 200 |
| 9 | PATCH `/api/solicitudes/{id}/estado` | `CambioEstadoRequest` | `CambioEstadoResponse` | 200 |
| 10 | DELETE `/api/solicitudes/{id}` | Ninguna | Sin cuerpo | 204 |

## Clases DTO

### Request

| Clase | Campos | Usada en |
|---|---|---|
| `MascotaRequest` | `nombre`, `especie`, `raza`, `descripcion`, `imagenUrl` | 3, 4 |
| `SolicitudRequest` | `mascotaId`, `solicitanteNombre`, `solicitanteCorreo`, `telefono` | 6 |
| `CambioEstadoRequest` | `estado` | 9 |

Las validaciones de cada campo están en la ficha de la petición correspondiente y se aplican con Bean Validation (`@Valid`).

### Response

| Clase | Campos | Usada en |
|---|---|---|
| `MascotaResponse` | `id`, `nombre`, `especie`, `raza`, `descripcion`, `estado`, `imagenUrl` | 1, 2, 3, 4 |
| `SolicitudResumenResponse` | `id`, `mascotaId`, `mascotaNombre`, `solicitanteNombre`, `estado`, `creadaEn`, `retiradaEn` | 7 |
| `SolicitudDetalleResponse` | `id`, `mascotaId`, `mascotaNombre`, `solicitanteNombre`, `solicitanteCorreo`, `telefono`, `estado`, `creadaEn`, `retiradaEn`, `historial` (lista de `HistorialResponse`) | 6, 8 |
| `HistorialResponse` | `tipo`, `estadoAnterior`, `estadoNuevo`, `actor`, `fecha` | Dentro de `SolicitudDetalleResponse` |
| `CambioEstadoResponse` | `id`, `estado`, `mascotaId`, `mascotaEstado`, `solicitudesCerradas` (lista de ids) | 9 |
| `ErrorResponse` | `status`, `error`, `codigo`, `mensaje`, `ruta`, `fecha`, `errores` (lista de `ErrorCampo`, solo en errores de validación) | Todos los errores |
| `ErrorCampo` | `campo`, `mensaje` | Dentro de `ErrorResponse` |

`SolicitudResumenResponse` no incluye `solicitanteCorreo` ni `telefono`, por eso se usa en los listados. `SolicitudDetalleResponse` sí los incluye y solo se devuelve a quien tiene permiso.

---

## Mascotas

### 1. GET `/api/mascotas?estado=DISPONIBLE`

Catálogo público. `estado` es opcional (`DISPONIBLE` o `ADOPTADA`); sin él devuelve todas.

- **Objeto al controller:** ninguno, solo el parámetro `estado`
- **Respuesta 200** (lista de `MascotaResponse`):

```json
[
  {
    "id": 1,
    "nombre": "Luna",
    "especie": "Perro",
    "raza": "Criolla",
    "descripcion": "Juguetona y sociable con otros perros",
    "estado": "DISPONIBLE",
    "imagenUrl": "https://ejemplo.com/luna.jpg"
  }
]
```

- **Errores:** 400 si `estado` no es un valor válido

### 2. GET `/api/mascotas/{id}`

- **Objeto al controller:** ninguno, solo `id` en la ruta
- **Respuesta 200:** un `MascotaResponse` (igual al elemento de la lista anterior)
- **Errores:** 404 si no existe

### 3. POST `/api/mascotas`

La fundación publica una mascota. El estado nace como `DISPONIBLE`.

- **Objeto al controller** (`MascotaRequest`):

```json
{
  "nombre": "Nala",
  "especie": "Gato",
  "raza": null,
  "descripcion": "Cariñosa y curiosa",
  "imagenUrl": "https://ejemplo.com/nala.jpg"
}
```

| Campo | Regla |
|---|---|
| `nombre` | Obligatorio, máx. 100 |
| `especie` | Obligatoria, máx. 50 |
| `raza` | Opcional, máx. 100 |
| `descripcion` | Opcional |
| `imagenUrl` | Opcional, debe empezar por `http://` o `https://`, máx. 500 (RN-06) |

- **Respuesta 201** con cabecera `Location: /api/mascotas/{id}` y un `MascotaResponse` con `"estado": "DISPONIBLE"`
- **Errores:** 400 (validación o `RN-06`), 401, 403

### 4. PUT `/api/mascotas/{id}`

Reemplaza la ficha pública completa. Lo que no se envíe en los campos opcionales queda en `null`.

- **Objeto al controller:** el mismo `MascotaRequest` del POST. No se recibe `estado`: solo cambia por RN-03
- **Respuesta 200:** el `MascotaResponse` actualizado
- **Errores:** 400, 401, 403, 404

### 5. DELETE `/api/mascotas/{id}`

**Supuesto:** solo se puede eliminar una mascota sin solicitudes, para no perder la trazabilidad.

- **Objeto al controller:** ninguno
- **Respuesta 204** sin cuerpo
- **Errores:** 401, 403, 404, 409 si ya tiene solicitudes

---

## Solicitudes

### 6. POST `/api/solicitudes-adopcion`

Crea una solicitud en estado `RECIBIDA` y registra en el historial un evento `CREACION`.

- **Objeto al controller** (`SolicitudRequest`):

```json
{
  "mascotaId": 1,
  "solicitanteNombre": "Ana Pérez",
  "solicitanteCorreo": "ana@correo.com",
  "telefono": "3103334455"
}
```

| Campo | Regla |
|---|---|
| `mascotaId` | Obligatorio |
| `solicitanteNombre` | Obligatorio, máx. 100 |
| `solicitanteCorreo` | Obligatorio, formato de correo, máx. 150 |
| `telefono` | Obligatorio, máx. 30 |

- **Respuesta 201** con `Location: /api/solicitudes/{id}`. Como quien crea es el propio solicitante, se devuelve el detalle completo (`SolicitudDetalleResponse`):

```json
{
  "id": 5,
  "mascotaId": 1,
  "mascotaNombre": "Luna",
  "solicitanteNombre": "Ana Pérez",
  "solicitanteCorreo": "ana@correo.com",
  "telefono": "3103334455",
  "estado": "RECIBIDA",
  "creadaEn": "2026-10-22T14:30:00",
  "retiradaEn": null,
  "historial": [
    {
      "tipo": "CREACION",
      "estadoAnterior": null,
      "estadoNuevo": "RECIBIDA",
      "actor": "ana@correo.com",
      "fecha": "2026-10-22T14:30:00"
    }
  ]
}
```

- **Errores:** 400 (validación), 404 (la mascota no existe), 409 `RN-01` (ya tiene una solicitud activa para esa mascota), 409 `RN-05` (la mascota ya está `ADOPTADA`)

### 7. GET `/api/solicitudes?mascotaId=1&estado=RECIBIDA`

Listado para el personal de la fundación. Ambos filtros son opcionales.

- **Objeto al controller:** ninguno, solo los parámetros
- **Respuesta 200** (lista de `SolicitudResumenResponse`, sin correo ni teléfono):

```json
[
  {
    "id": 5,
    "mascotaId": 1,
    "mascotaNombre": "Luna",
    "solicitanteNombre": "Ana Pérez",
    "estado": "RECIBIDA",
    "creadaEn": "2026-10-22T14:30:00",
    "retiradaEn": null
  }
]
```

- **Errores:** 400 (filtro inválido), 401, 403

### 8. GET `/api/solicitudes/{id}`

Consulta autorizada. El personal ve cualquier solicitud; un solicitante solo la suya, para que nadie vea datos ajenos cambiando el `id` en la URL.

- **Objeto al controller:** ninguno, solo `id`
- **Respuesta 200:** `SolicitudDetalleResponse` (el mismo JSON del punto 6, con el historial completo)
- **Errores:** 401 (faltan cabeceras), 403 (solicitante que no es dueño), 404

### 9. PATCH `/api/solicitudes/{id}/estado`

Mueve la solicitud por el flujo de RN-02.

| Estado actual | Estados permitidos |
|---|---|
| `RECIBIDA` | `EN_REVISION`, `RECHAZADA` |
| `EN_REVISION` | `APROBADA`, `RECHAZADA` |
| `APROBADA`, `RECHAZADA` | Ninguno (estados finales) |

Una solicitud retirada tampoco puede cambiar de estado.

- **Objeto al controller** (`CambioEstadoRequest`):

```json
{
  "estado": "APROBADA"
}
```

- **Respuesta 200** (`CambioEstadoResponse`). Al aprobar, en la misma transacción la mascota pasa a `ADOPTADA` (RN-03) y se cierran las demás solicitudes activas de esa mascota (RN-04):

```json
{
  "id": 5,
  "estado": "APROBADA",
  "mascotaId": 1,
  "mascotaEstado": "ADOPTADA",
  "solicitudesCerradas": [6, 9]
}
```

Si el cambio no es una aprobación, `mascotaEstado` queda como estaba y `solicitudesCerradas` va vacío (`[]`).

- **Historial:** cada cambio agrega un evento `CAMBIO_ESTADO` (actor: el usuario de la cabecera). Cada solicitud cerrada por RN-04 agrega un `CIERRE_AUTOMATICO` con actor `SISTEMA`
- **Errores:** 400 (estado ausente o inválido), 401, 403, 404, 409 `RN-02` (transición no permitida), 409 `RN-05` (la mascota ya fue adoptada por otra solicitud)

### 10. DELETE `/api/solicitudes/{id}`

El solicitante retira su solicitud. No se borra la fila: se llena `retirada_en` y se registra un evento `RETIRO`. Solo se puede mientras esté en `RECIBIDA`, antes de la revisión.

- **Objeto al controller:** ninguno
- **Respuesta 204** sin cuerpo
- **Errores:** 401, 403 (no es dueño o no es solicitante), 404, 409 (ya salió de `RECIBIDA` o ya estaba retirada)

---

## Supuestos a confirmar

1. La identificación por cabeceras `X-Rol` y `X-Usuario` (la rama `feature/login` aún no tiene nada)
2. Que crear una solicitud (6) sea público y no requiera cabeceras, porque el solicitante no tiene cuenta
3. Que el listado (7) quede solo para el personal y los solicitantes no tengan listado propio
4. Que `DELETE /api/mascotas/{id}` exista (5) o se deje fuera para no complicar el alcance
5. Que el historial viaje dentro del detalle de la solicitud y no en un endpoint aparte
6. Formato único de error con el campo `codigo`
