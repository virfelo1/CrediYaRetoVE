# Corrección del Error 415 UNSUPPORTED_MEDIA_TYPE en Postman

## Problema Identificado

Al enviar peticiones POST desde Postman al endpoint `/api/v1/login`, se recibe un error `415 UNSUPPORTED_MEDIA_TYPE` con el mensaje:

```
"Content type 'text/plain' not supported for bodyType=co.com.projectve.api.dto.LoginDTO"
```

## Análisis del Problema

### 1. Error en Logs
```
ERROR - Handler: Error inesperado en login: 415 UNSUPPORTED_MEDIA_TYPE "Content type 'text/plain' not supported for bodyType=co.com.projectve.api.dto.LoginDTO"
```

### 2. Causa del Problema
- Postman está enviando el Content-Type como `text/plain`
- Spring WebFlux espera `application/json` para deserializar el JSON a `LoginDTO`
- El Content-Type incorrecto impide que Spring procese el body de la petición

## Solución Implementada

### 1. Configuración Correcta en Postman

#### **Headers:**
```
Content-Type: application/json
```

#### **Body (raw JSON):**
```json
{
  "username": "admin@gmail.com",
  "password": "Admin123@"
}
```

### 2. Pasos Detallados en Postman

#### **Paso 1: Crear Nueva Petición**
1. Abrir Postman
2. Crear nueva petición POST
3. URL: `http://localhost:8080/api/v1/login`

#### **Paso 2: Configurar Headers**
1. Ir a la pestaña "Headers"
2. Agregar header:
   - Key: `Content-Type`
   - Value: `application/json`

#### **Paso 3: Configurar Body**
1. Ir a la pestaña "Body"
2. Seleccionar `raw`
3. En el dropdown seleccionar `JSON`
4. Ingresar el JSON:
```json
{
  "username": "admin@gmail.com",
  "password": "Admin123@"
}
```

### 3. Handler de Error Mejorado

Se agregó un handler específico para errores de Content-Type:

```java
.onErrorResume(UnsupportedMediaTypeStatusException.class, error -> {
    logger.warn("Error de Content-Type en login: {}", error.getMessage());
    return ServerResponse.status(415)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("error", "Content-Type debe ser application/json"));
});
```

## Testing de la Solución

### 1. Configuración Correcta

#### **Request (Postman):**
```
Method: POST
URL: http://localhost:8080/api/v1/login
Headers: Content-Type: application/json
Body (raw JSON):
{
  "username": "admin@gmail.com",
  "password": "Admin123@"
}
```

#### **Respuesta Esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### **Código HTTP:** `200 OK`

### 2. Configuración Incorrecta

#### **Request (Postman - Incorrecto):**
```
Method: POST
URL: http://localhost:8080/api/v1/login
Headers: Content-Type: text/plain (o sin Content-Type)
Body (raw):
{
  "username": "admin@gmail.com",
  "password": "Admin123@"
}
```

#### **Respuesta Esperada:**
```json
{
  "error": "Content-Type debe ser application/json"
}
```

#### **Código HTTP:** `415 Unsupported Media Type`

## Logs de Auditoría

### 1. Configuración Correcta
```
INFO - Handler: Iniciando proceso de login de usuario
INFO - Handler: LoginDTO recibido para usuario: admin@gmail.com
INFO - Handler: Validación exitosa para username: admin@gmail.com
INFO - UserUseCase: Usuario encontrado con ID: X y email: admin@gmail.com
INFO - Handler: Usuario logueado con éxito: admin@gmail.com
INFO - Handler: Token JWT generado exitosamente para usuario: admin@gmail.com
```

### 2. Configuración Incorrecta
```
INFO - Handler: Iniciando proceso de login de usuario
WARN - Handler: Error de Content-Type en login: 415 UNSUPPORTED_MEDIA_TYPE "Content type 'text/plain' not supported for bodyType=co.com.projectve.api.dto.LoginDTO"
```

## Alternativas de Testing

### 1. cURL
```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@gmail.com",
    "password": "Admin123@"
  }'
```

### 2. Insomnia
- Configurar Content-Type: `application/json`
- Seleccionar JSON en el body

### 3. Thunder Client (VS Code)
- Headers: `Content-Type: application/json`
- Body: JSON

## Configuración de Spring WebFlux

### 1. Content-Type Esperado
Spring WebFlux espera `application/json` para deserializar automáticamente el JSON a DTOs.

### 2. Validación de Content-Type
```java
// Spring WebFlux automáticamente valida el Content-Type
return serverRequest.bodyToMono(LoginDTO.class)
    // Si Content-Type no es application/json, lanza UnsupportedMediaTypeStatusException
```

### 3. Handler de Error
```java
.onErrorResume(UnsupportedMediaTypeStatusException.class, error -> {
    return ServerResponse.status(415)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("error", "Content-Type debe ser application/json"));
});
```

## Códigos de Respuesta HTTP

| Escenario | Código | Descripción |
|-----------|--------|-------------|
| **Configuración Correcta** | `200 OK` | Token JWT generado |
| **Content-Type Incorrecto** | `415 Unsupported Media Type` | "Content-Type debe ser application/json" |
| **Campos Vacíos** | `400 Bad Request` | Mensaje específico de validación |
| **Contraseña Incorrecta** | `401 Unauthorized` | "Contraseña incorrecta" |
| **Usuario No Encontrado** | `404 Not Found` | "Usuario no se encuentra registrado" |

## Beneficios de la Corrección

### 1. **Mensaje de Error Claro**
- ✅ Feedback específico sobre el problema de Content-Type
- ✅ Instrucciones claras para el usuario
- ✅ Código HTTP apropiado (415)

### 2. **Debugging Facilitado**
- ✅ Logs específicos para errores de Content-Type
- ✅ Identificación rápida del problema
- ✅ Trazabilidad completa

### 3. **Experiencia de Usuario**
- ✅ Mensaje en español
- ✅ Instrucciones claras
- ✅ Código HTTP estándar

## Consideraciones Importantes

### 1. **Content-Type en Postman**
- Siempre configurar `Content-Type: application/json`
- Seleccionar `JSON` en el dropdown del body
- Verificar que no haya espacios extra en el JSON

### 2. **Validación de JSON**
- Usar un validador de JSON para verificar sintaxis
- No incluir comentarios en el JSON
- Usar comillas dobles para strings

### 3. **Testing**
- Probar con diferentes clientes HTTP
- Verificar logs de la aplicación
- Validar códigos de respuesta HTTP

## Próximos Pasos

### 1. **Testing Completo**
- Probar con Postman configurado correctamente
- Verificar que el login funcione con credenciales válidas
- Validar que las validaciones funcionen correctamente

### 2. **Documentación**
- Crear guía de uso para Postman
- Documentar configuración de headers
- Proporcionar ejemplos de requests

### 3. **Monitoreo**
- Logs de errores de Content-Type
- Métricas de requests exitosos/fallidos
- Alertas para patrones de error

## Resumen de la Corrección

| Aspecto | Antes | Después |
|---------|-------|---------|
| **Error 415** | ❌ Mensaje genérico | ✅ Mensaje específico en español |
| **Debugging** | ❌ Difícil identificar problema | ✅ Logs claros y específicos |
| **Experiencia** | ❌ Confuso para el usuario | ✅ Instrucciones claras |
| **Código HTTP** | ❌ 415 sin contexto | ✅ 415 con mensaje explicativo |

La corrección asegura que los errores de Content-Type se manejen apropiadamente y proporcionen feedback útil al usuario para corregir la configuración en Postman.
