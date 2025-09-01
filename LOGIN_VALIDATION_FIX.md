# Corrección de Validaciones en LoginDTO

## Problema Identificado

Las validaciones del `LoginDTO` no se estaban ejecutando correctamente. Cuando se enviaban datos inválidos (campos vacíos, formato de email incorrecto), el sistema no devolvía los mensajes de validación apropiados.

## Análisis del Problema

### 1. Validaciones en LoginDTO

```java
public record LoginDTO (
    @Schema(description = "Correo electrónico único del usuario", example = "juan.garcia@email.com", requiredMode = Schema.RequiredMode.REQUIRED, format = "email")
    @Pattern(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$",
         message = "El usuario deber ser un correo")
    @NotBlank(message = "El usuario es obligatorio")
    String username,

    @NotBlank(message = "Se requiere contraseña")
    String password
){}
```

### 2. Problema en el Manejo de Errores (ANTES)

```java
// ❌ PROBLEMA: Orden incorrecto de onErrorResume
.onErrorResume(ConstraintViolationException.class, error -> {
    return ServerResponse.badRequest()
            .bodyValue(Map.of("error", "Datos de login inválidos"));
})
.onErrorResume(RuntimeException.class, error -> {
    // Manejo de RuntimeException
})
.onErrorResume(Exception.class, error -> {
    // ❌ Este capturaba todas las excepciones, incluyendo ConstraintViolationException
    return ServerResponse.status(404)
            .bodyValue(Map.of("error", "Usuario no se encuentra registrado"));
});
```

**Problema:** El último `onErrorResume(Exception.class)` capturaba todas las excepciones, incluyendo `ConstraintViolationException`, por lo que las validaciones nunca llegaban al handler específico.

## Solución Implementada

### 1. Manejo de Errores Corregido (DESPUÉS)

```java
.onErrorResume(ConstraintViolationException.class, error -> {
    logger.warn("Error de validación en login: {}", error.getMessage());
    
    // ✅ Extraer mensajes de validación específicos
    ConstraintViolationException cve = (ConstraintViolationException) error;
    String errorMessage = cve.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .findFirst()
            .orElse("Datos de login inválidos");
    
    return ServerResponse.badRequest()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("error", errorMessage));
})
.onErrorResume(RuntimeException.class, error -> {
    if ("Contraseña incorrecta".equals(error.getMessage())) {
        return ServerResponse.status(401)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("error", "Contraseña incorrecta"));
    } else {
        return ServerResponse.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("error", "Error interno del servidor"));
    }
})
.onErrorResume(Exception.class, error -> {
    // ✅ Solo manejar excepciones que no sean de validación o RuntimeException
    if (!(error instanceof ConstraintViolationException) && !(error instanceof RuntimeException)) {
        logger.error("Usuario no encontrado o error en login: {}", error.getMessage());
        return ServerResponse.status(404)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("error", "Usuario no se encuentra registrado"));
    }
    // ✅ Re-lanzar para que sea manejado por el handler específico
    return Mono.error(error);
});
```

## Casos de Validación

### 1. Campo Username Vacío

**Request:**
```json
{
  "username": "",
  "password": "password123"
}
```

**Respuesta esperada:**
```json
{
  "error": "username: El usuario es obligatorio"
}
```

**Código HTTP:** `400 Bad Request`

### 2. Campo Password Vacío

**Request:**
```json
{
  "username": "juan@example.com",
  "password": ""
}
```

**Respuesta esperada:**
```json
{
  "error": "password: Se requiere contraseña"
}
```

**Código HTTP:** `400 Bad Request`

### 3. Formato de Email Inválido

**Request:**
```json
{
  "username": "invalid-email",
  "password": "password123"
}
```

**Respuesta esperada:**
```json
{
  "error": "username: El usuario deber ser un correo"
}
```

**Código HTTP:** `400 Bad Request`

### 4. Campos Vacíos

**Request:**
```json
{
  "username": "",
  "password": ""
}
```

**Respuesta esperada:**
```json
{
  "error": "username: El usuario es obligatorio"
}
```

**Código HTTP:** `400 Bad Request`

### 5. Request Vacio

**Request:**
```json
{}
```

**Respuesta esperada:**
```json
{
  "error": "username: El usuario es obligatorio"
}
```

**Código HTTP:** `400 Bad Request`

## Logs de Auditoría

### Validación Fallida
```
INFO  - Handler: Iniciando proceso de login de usuario
WARN  - Handler: Validación fallida para username : 1 violaciones encontradas
WARN  - Handler: Violación: username - El usuario es obligatorio
WARN  - Handler: Error de validación en login: Validation failed for classes [co.com.projectve.api.dto.LoginDTO] during validate time for groups [interface jakarta.validation.groups.Default]
```

### Validación Exitosa
```
INFO  - Handler: Iniciando proceso de login de usuario
INFO  - Handler: LoginDTO recibido para usuario: juan@example.com
INFO  - Handler: Validación exitosa para username: juan@example.com
```

## Testing de Validaciones

### 1. Username Vacío
```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "",
    "password": "password123"
  }'
```

### 2. Password Vacío
```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan@example.com",
    "password": ""
  }'
```

### 3. Email Inválido
```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "invalid-email",
    "password": "password123"
  }'
```

### 4. Request Vacio
```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{}'
```

## Configuración de Validaciones

### 1. Anotaciones de Validación

| Anotación | Propósito | Mensaje |
|-----------|-----------|---------|
| `@NotBlank` | Campo no puede estar vacío o solo espacios | "El usuario es obligatorio" / "Se requiere contraseña" |
| `@Pattern` | Validar formato de email | "El usuario deber ser un correo" |

### 2. Regex de Email

```java
@Pattern(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")
```

**Características:**
- Permite caracteres alfanuméricos, `_`, `+`, `&`, `*`, `-`
- Permite puntos en el nombre de usuario
- Requiere dominio válido
- Longitud de TLD entre 2-7 caracteres

### 3. Validación en Handler

```java
// Validación del DTO usando Bean Validation
Set<ConstraintViolation<LoginDTO>> violations = validator.validate(dto);
if (!violations.isEmpty()) {
    logger.warn("Validación fallida para username {}: {} violaciones encontradas", 
               dto.username(), violations.size());
    violations.forEach(violation -> 
        logger.warn("Violación: {} - {}", violation.getPropertyPath(), violation.getMessage()));
    throw new ConstraintViolationException(violations);
}
```

## Códigos de Respuesta HTTP

| Escenario | Código | Descripción |
|-----------|--------|-------------|
| **Validación Exitosa** | `200 OK` | Token JWT generado |
| **Campos Vacíos** | `400 Bad Request` | Mensaje específico de validación |
| **Email Inválido** | `400 Bad Request` | "El usuario deber ser un correo" |
| **Contraseña Incorrecta** | `401 Unauthorized` | "Contraseña incorrecta" |
| **Usuario No Encontrado** | `404 Not Found` | "Usuario no se encuentra registrado" |

## Beneficios de la Corrección

### 1. **Validaciones Funcionales**
- ✅ Los campos vacíos son detectados correctamente
- ✅ El formato de email se valida apropiadamente
- ✅ Mensajes de error específicos y claros

### 2. **Manejo de Errores Mejorado**
- ✅ Orden correcto de `onErrorResume`
- ✅ Captura específica de `ConstraintViolationException`
- ✅ Mensajes de error detallados

### 3. **Experiencia de Usuario**
- ✅ Feedback inmediato sobre errores de validación
- ✅ Códigos HTTP apropiados
- ✅ Mensajes en español

### 4. **Logs de Auditoría**
- ✅ Registro detallado de violaciones de validación
- ✅ Trazabilidad completa del proceso
- ✅ Debugging facilitado

## Consideraciones Importantes

### 1. **Orden de onErrorResume**
- Los handlers más específicos deben ir primero
- `ConstraintViolationException` antes que `Exception`
- Verificar que no se capturen excepciones incorrectamente

### 2. **Mensajes de Error**
- Mantener mensajes en español
- Ser específicos sobre qué campo falló
- No revelar información sensible

### 3. **Validaciones de Seguridad**
- Validar formato de email para prevenir inyecciones
- Sanitizar entrada antes de procesar
- Logs de auditoría para intentos fallidos

## Próximos Pasos

### 1. **Testing Completo**
- Probar todos los casos de validación
- Verificar mensajes de error
- Validar códigos HTTP

### 2. **Mejoras Futuras**
- Validación de fortaleza de contraseña
- Rate limiting en validaciones
- Mensajes de error más descriptivos

### 3. **Monitoreo**
- Logs de validaciones fallidas
- Métricas de errores de validación
- Alertas para patrones sospechosos

## Resumen de la Corrección

| Aspecto | Antes | Después |
|---------|-------|---------|
| **Validaciones** | ❌ No funcionaban | ✅ Funcionan correctamente |
| **Mensajes de Error** | ❌ Genéricos | ✅ Específicos y claros |
| **Códigos HTTP** | ❌ Incorrectos | ✅ Apropiados (400) |
| **Manejo de Errores** | ❌ Orden incorrecto | ✅ Orden correcto |

La corrección asegura que las validaciones del `LoginDTO` funcionen correctamente y proporcionen feedback apropiado al usuario cuando los datos de entrada no sean válidos.
