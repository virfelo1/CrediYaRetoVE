# Implementación de Login con Spring Security y JWT

## Resumen de la Implementación

Se ha implementado un sistema de autenticación completo que utiliza Spring Security para la validación de credenciales y JWT para la generación de tokens de autenticación.

## Componentes Principales

### 1. Handler.java - Método loginUser
- **Validación de entrada**: Usa Bean Validation para validar el formato del LoginDTO
- **Búsqueda de usuario**: Utiliza `useCase.findByEmail()` para encontrar el usuario
- **Validación de contraseña**: Usa `PasswordEncoder.matches()` de Spring Security
- **Generación de JWT**: Utiliza `JwtUtil.create()` para generar el token
- **Manejo de errores**: Respuestas HTTP apropiadas para cada caso

### 2. SecurityConfig.java
- **PasswordEncoder**: Configurado con BCrypt para encriptación segura
- **Configuración de endpoints**: Permisos por roles y métodos HTTP
- **Mapeo de roles**: Función para convertir IDs de rol a nombres

### 3. JwtUtil.java
- **Generación de tokens**: Método `create()` para generar JWT
- **Configuración segura**: Algoritmo HMAC256 con clave secreta
- **Expiración**: Tokens válidos por 15 días

## Flujo de Autenticación

### 1. Recepción de Credenciales
```
POST /api/v1/login
{
  "username": "juan@example.com",
  "password": "Contraseña123@"
}
```

### 2. Validación de Entrada
- **Bean Validation**: Verifica formato de email y campos requeridos
- **Logging**: Registra intento de login sin exponer contraseña

### 3. Búsqueda de Usuario
- **Base de datos**: Busca usuario por email
- **Verificación de existencia**: Si no existe, retorna 404

### 4. Validación de Contraseña
- **Spring Security**: Usa `PasswordEncoder.matches()`
- **Comparación segura**: Compara contraseña proporcionada con hash almacenado
- **Resultado**:
  - ✅ **Correcta**: Continúa al siguiente paso
  - ❌ **Incorrecta**: Retorna 401 "Contraseña incorrecta"

### 5. Generación de JWT
- **JwtUtil**: Genera token con claims del usuario
- **Claims incluidos**: username, issuer, issuedAt, expiresAt
- **Respuesta**: Token JWT en texto plano

## Códigos de Respuesta HTTP

| Código | Descripción | Ejemplo de Respuesta |
|--------|-------------|---------------------|
| **200** | Login exitoso | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` |
| **400** | Datos de login inválidos | `{"error": "Datos de login inválidos"}` |
| **401** | Contraseña incorrecta | `{"error": "Contraseña incorrecta"}` |
| **404** | Usuario no encontrado | `{"error": "Usuario no se encuentra registrado"}` |
| **500** | Error interno del servidor | `{"error": "Error interno del servidor"}` |

## Mensajes de Log

### Login Exitoso
```
INFO - LoginDTO recibido para usuario: juan@example.com
INFO - Validación exitosa para username: juan@example.com
INFO - Usuario encontrado para login con ID: 1 y email: juan@example.com
INFO - Usuario con id_rol: 3
DEBUG - Validando contraseña para usuario: juan@example.com
INFO - Usuario logueado con éxito: juan@example.com
DEBUG - Generando token JWT para usuario con ID: 1
INFO - Token JWT generado exitosamente para usuario: juan@example.com con id_rol: 3
INFO - Respuesta de login enviada exitosamente
```

### Contraseña Incorrecta
```
INFO - LoginDTO recibido para usuario: juan@example.com
INFO - Validación exitosa para username: juan@example.com
INFO - Usuario encontrado para login con ID: 1 y email: juan@example.com
INFO - Usuario con id_rol: 3
DEBUG - Validando contraseña para usuario: juan@example.com
WARN - Contraseña incorrecta para usuario: juan@example.com
WARN - Error de contraseña incorrecta en login: Contraseña incorrecta
```

### Usuario No Encontrado
```
INFO - LoginDTO recibido para usuario: inexistente@example.com
INFO - Validación exitosa para username: inexistente@example.com
ERROR - Usuario no encontrado o error en login: User not found
```

## Seguridad Implementada

### 1. Encriptación de Contraseñas
- **Algoritmo**: BCrypt (Spring Security)
- **Salt automático**: Cada contraseña tiene salt único
- **Iteraciones**: Configuración segura por defecto

### 2. Validación Robusta
- **Bean Validation**: Validación de formato de entrada
- **Comparación segura**: `PasswordEncoder.matches()` previene timing attacks
- **Logging seguro**: No expone contraseñas en logs

### 3. Tokens JWT
- **Algoritmo**: HMAC256
- **Clave secreta**: Configurada en JwtUtil
- **Expiración**: 15 días
- **Claims**: Información mínima necesaria

### 4. Manejo de Errores
- **Respuestas específicas**: Cada error tiene su código HTTP apropiado
- **Logging detallado**: Para auditoría y debugging
- **Sin información sensible**: No expone detalles internos

## Uso del Token JWT

### 1. En Requests Posteriores
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. Estructura del Token
```json
{
  "sub": "juan@example.com",
  "iss": "VE",
  "iat": 1640995200,
  "exp": 1642204800
}
```

## Consideraciones de Seguridad

1. **Contraseñas**: Siempre encriptadas con BCrypt
2. **Tokens**: Firmados con HMAC256
3. **Logs**: No contienen información sensible
4. **Validación**: Múltiples capas de validación
5. **Errores**: Respuestas genéricas para evitar información de ataque

## Próximos Pasos Recomendados

1. **Refresh Tokens**: Implementar renovación automática
2. **Rate Limiting**: Limitar intentos de login
3. **Auditoría**: Logging de intentos fallidos
4. **Blacklisting**: Invalidar tokens específicos
5. **MFA**: Autenticación de múltiples factores
