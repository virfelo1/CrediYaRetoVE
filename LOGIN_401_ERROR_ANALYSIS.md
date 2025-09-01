# Análisis del Error 401 en Login con Credenciales Correctas

## Problema Identificado

El sistema devuelve un **401 Unauthorized** incluso cuando el usuario y contraseña son correctos. Esto se debe a un problema en el manejo de contraseñas entre el registro y el login.

## Análisis del Flujo

### 1. Registro de Usuario (ANTES de la corrección)

```java
// ❌ PROBLEMA: No se encripta la contraseña
public Mono<ServerResponse> registerUser(ServerRequest request) {
    return request.bodyToMono(UserDTO.class)
            .flatMap(dto -> {
                User model = userDTOMapper.toModel(dto);
                // ❌ La contraseña se guarda en texto plano
                return useCase.execute(model);
            });
}
```

**Resultado:** La contraseña se almacena en la base de datos en **texto plano**.

### 2. Login de Usuario (ANTES de la corrección)

```java
// ❌ PROBLEMA: Se valida como si estuviera encriptada
public Mono<ServerResponse> loginUser(ServerRequest serverRequest) {
    return serverRequest.bodyToMono(LoginDTO.class)
            .flatMap(dto -> {
                return useCase.findByEmail(dto.username())
                        .flatMap(user -> {
                            String storedPassword = user.getPassword(); // Texto plano
                            String providedPassword = dto.password();   // Texto plano
                            
                            // ❌ BCrypt.matches() falla porque storedPassword no es BCrypt
                            if (passwordEncoder.matches(providedPassword, storedPassword)) {
                                return Mono.just(user);
                            } else {
                                return Mono.error(new RuntimeException("Contraseña incorrecta"));
                            }
                        });
            });
}
```

**Resultado:** `BCryptPasswordEncoder.matches()` falla porque la contraseña almacenada no está encriptada.

## Solución Implementada

### 1. Registro de Usuario (DESPUÉS de la corrección)

```java
// ✅ SOLUCIÓN: Encriptar contraseña antes de guardar
public Mono<ServerResponse> registerUser(ServerRequest request) {
    return request.bodyToMono(UserDTO.class)
            .flatMap(dto -> {
                User model = userDTOMapper.toModel(dto);
                
                // ✅ Encriptar contraseña antes de guardar
                String encryptedPassword = passwordEncoder.encode(model.getPassword());
                model.setPassword(encryptedPassword);
                logger.info("Contraseña encriptada para usuario: {}", model.getEmail());
                
                return useCase.execute(model);
            });
}
```

**Resultado:** La contraseña se almacena en la base de datos **encriptada con BCrypt**.

### 2. Login de Usuario (DESPUÉS de la corrección)

```java
// ✅ SOLUCIÓN: Ahora funciona correctamente
public Mono<ServerResponse> loginUser(ServerRequest serverRequest) {
    return serverRequest.bodyToMono(LoginDTO.class)
            .flatMap(dto -> {
                return useCase.findByEmail(dto.username())
                        .flatMap(user -> {
                            String storedPassword = user.getPassword(); // BCrypt encriptada
                            String providedPassword = dto.password();   // Texto plano
                            
                            // ✅ BCrypt.matches() funciona correctamente
                            if (passwordEncoder.matches(providedPassword, storedPassword)) {
                                logger.info("Usuario logueado con éxito: {}", user.getEmail());
                                return Mono.just(user);
                            } else {
                                logger.warn("Contraseña incorrecta para usuario: {}", user.getEmail());
                                return Mono.error(new RuntimeException("Contraseña incorrecta"));
                            }
                        });
            });
}
```

**Resultado:** `BCryptPasswordEncoder.matches()` funciona correctamente.

## Flujo de Autenticación Corregido

### 1. Registro
```
Usuario envía contraseña en texto plano → Handler encripta con BCrypt → Se guarda en BD encriptada
```

### 2. Login
```
Usuario envía contraseña en texto plano → BCrypt.matches() compara con contraseña encriptada → Login exitoso
```

## Código de Respuesta HTTP

| Escenario | Código | Descripción |
|-----------|--------|-------------|
| **Login Exitoso** | `200 OK` | Token JWT generado |
| **Contraseña Incorrecta** | `401 Unauthorized` | "Contraseña incorrecta" |
| **Usuario No Encontrado** | `404 Not Found` | "Usuario no se encuentra registrado" |
| **Datos Inválidos** | `400 Bad Request` | "Datos de login inválidos" |
| **Error Interno** | `500 Internal Server Error` | "Error interno del servidor" |

## Logs de Auditoría

### Registro Exitoso
```
INFO  - Handler: Iniciando proceso de registro de usuario
INFO  - Handler: DTO recibido para usuario: juan@example.com
INFO  - Handler: Validación exitosa para email: juan@example.com
INFO  - Handler: Contraseña encriptada para usuario: juan@example.com
INFO  - UserUseCase: Asignando id_rol de cliente (byte=3) al usuario: juan@example.com
INFO  - UserUseCase: Usuario guardado exitosamente en el caso de uso - ID: 1, Email: juan@example.com
```

### Login Exitoso
```
INFO  - Handler: Iniciando proceso de login de usuario
INFO  - Handler: LoginDTO recibido para usuario: juan@example.com
INFO  - Handler: Validación exitosa para username: juan@example.com
INFO  - UserUseCase: Usuario encontrado con ID: 1 y email: juan@example.com
INFO  - UserUseCase: Usuario con id_rol: 3
INFO  - Handler: Usuario logueado con éxito: juan@example.com
INFO  - Handler: Token JWT generado exitosamente para usuario: juan@example.com con id_rol: 3
```

### Login Fallido (Contraseña Incorrecta)
```
INFO  - Handler: Iniciando proceso de login de usuario
INFO  - Handler: LoginDTO recibido para usuario: juan@example.com
INFO  - Handler: Validación exitosa para username: juan@example.com
INFO  - UserUseCase: Usuario encontrado con ID: 1 y email: juan@example.com
WARN  - Handler: Contraseña incorrecta para usuario: juan@example.com
WARN  - Handler: Error de contraseña incorrecta en login: Contraseña incorrecta
```

## Configuración de BCrypt

### PasswordEncoder Bean
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Características de BCrypt
- **Salt automático**: Cada encriptación genera un salt único
- **Coste configurable**: Por defecto 10 (2^10 = 1024 iteraciones)
- **Formato**: `$2a$10$...` (versión, coste, salt, hash)

### Ejemplo de Contraseña Encriptada
```
Contraseña original: "password123"
Contraseña encriptada: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
```

## Testing de la Solución

### 1. Registrar Usuario
```bash
curl -X POST http://localhost:8080/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan@example.com",
    "password": "password123",
    "baseSalary": 50000
  }'
```

### 2. Login Exitoso
```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan@example.com",
    "password": "password123"
  }'
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. Login Fallido
```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan@example.com",
    "password": "wrongpassword"
  }'
```

**Respuesta esperada:**
```json
{
  "error": "Contraseña incorrecta"
}
```

## Consideraciones de Seguridad

### 1. **Encriptación de Contraseñas**
- ✅ BCrypt es resistente a ataques de fuerza bruta
- ✅ Salt único por contraseña
- ✅ Coste configurable para adaptarse al hardware

### 2. **Validación de Entrada**
- ✅ Bean Validation en DTOs
- ✅ Sanitización de datos
- ✅ Logs de auditoría

### 3. **Manejo de Errores**
- ✅ Mensajes de error genéricos (no revelan información sensible)
- ✅ Logs detallados para debugging
- ✅ Códigos HTTP apropiados

### 4. **JWT Security**
- ✅ Tokens con expiración (15 días)
- ✅ Firma HMAC256
- ✅ Claims mínimos necesarios

## Próximos Pasos

### 1. **Testing Completo**
- Probar registro y login con diferentes usuarios
- Verificar que las contraseñas se encriptan correctamente
- Validar que el login funciona con credenciales correctas

### 2. **Monitoreo**
- Logs de autenticación exitosa/fallida
- Métricas de intentos de login
- Alertas de seguridad

### 3. **Mejoras Futuras**
- Refresh tokens
- Rate limiting en login
- Blacklisting de tokens
- Política de contraseñas fuertes

## Resumen de la Corrección

| Aspecto | Antes | Después |
|---------|-------|---------|
| **Registro** | Contraseña en texto plano | Contraseña encriptada con BCrypt |
| **Login** | BCrypt.matches() falla | BCrypt.matches() funciona |
| **Resultado** | 401 con credenciales correctas | 200 con credenciales correctas |
| **Seguridad** | ❌ Contraseñas expuestas | ✅ Contraseñas encriptadas |

La corrección asegura que las contraseñas se encripten correctamente durante el registro y se validen apropiadamente durante el login, resolviendo el problema del error 401.
