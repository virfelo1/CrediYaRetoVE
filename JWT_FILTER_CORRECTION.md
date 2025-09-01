# Corrección del JwtFilter para Spring WebFlux

## Problema Identificado

El archivo `JwtFilter.java` estaba extendiendo `OncePerRequestFilter` que es parte del framework **servlet-based** de Spring MVC, pero este proyecto utiliza **Spring WebFlux** (reactive), lo que causaba errores de compatibilidad.

## Error Original

```java
// ❌ INCORRECTO - Servlet-based filter
public class JwtFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        jakarta.servlet.http.HttpServletRequest request, 
        jakarta.servlet.http.HttpServletResponse response, 
        jakarta.servlet.FilterChain filterChain
    ) throws ServletException, IOException {
        // ...
    }
}
```

## Solución Implementada

### 1. Cambio a WebFilter (Reactive)

```java
// ✅ CORRECTO - WebFlux reactive filter
@Component
public class JwtFilter implements WebFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Lógica de filtrado reactiva
    }
}
```

### 2. Diferencias Clave

| Aspecto | Servlet (OncePerRequestFilter) | WebFlux (WebFilter) |
|---------|--------------------------------|---------------------|
| **Framework** | Spring MVC | Spring WebFlux |
| **Paradigma** | Imperativo | Reactivo |
| **Método** | `doFilterInternal()` | `filter()` |
| **Retorno** | `void` | `Mono<Void>` |
| **Parámetros** | `HttpServletRequest/Response` | `ServerWebExchange` |
| **Cadena** | `FilterChain` | `WebFilterChain` |

### 3. Imports Correctos

```java
// ❌ Imports para Servlet
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;

// ✅ Imports para WebFlux
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
```

## Funcionalidad del JwtFilter

### 1. Extracción del Token

```java
String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

if (authHeader != null && authHeader.startsWith("Bearer ")) {
    String token = authHeader.substring(7); // Remover "Bearer "
    // Procesar token...
}
```

### 2. Validación del Token

```java
if (jwtUtil.isValid(token)) {
    String username = jwtUtil.getUsername(token);
    logger.info("JwtFilter: Token JWT válido para usuario: {}", username);
    // Token válido - continuar
} else {
    logger.warn("JwtFilter: Token JWT inválido");
    // Token inválido - continuar sin autenticación
}
```

### 3. Manejo de Errores

```java
try {
    // Validación del token
    return chain.filter(exchange);
} catch (Exception e) {
    logger.warn("JwtFilter: Error al validar token JWT: {}", e.getMessage());
    // Continuar sin autenticación en caso de error
    return chain.filter(exchange);
}
```

## Flujo de Funcionamiento

### 1. Request Llega al Filtro
```
HTTP Request → JwtFilter → Handler → Response
```

### 2. Verificación de Headers
- Busca header `Authorization`
- Verifica formato `Bearer <token>`

### 3. Validación del Token
- Usa `JwtUtil.isValid()` para verificar firma
- Usa `JwtUtil.getUsername()` para extraer usuario
- Logs de auditoría para debugging

### 4. Continuación del Flujo
- Token válido: Continúa con autenticación
- Token inválido: Continúa sin autenticación
- Sin token: Continúa sin autenticación

## Logs de Auditoría

### Token Válido
```
DEBUG - JwtFilter: Procesando request para path: /api/v1/protected
DEBUG - JwtFilter: Token encontrado en header Authorization
INFO  - JwtFilter: Token JWT válido para usuario: juan@example.com
```

### Token Inválido
```
DEBUG - JwtFilter: Procesando request para path: /api/v1/protected
DEBUG - JwtFilter: Token encontrado en header Authorization
WARN  - JwtFilter: Token JWT inválido
```

### Sin Token
```
DEBUG - JwtFilter: Procesando request para path: /api/v1/public
DEBUG - JwtFilter: No se encontró token en header Authorization
```

## Integración con JwtUtil

El `JwtFilter` utiliza los métodos existentes del `JwtUtil`:

### 1. `isValid(String jwt)`
- Verifica la firma del token
- Valida la expiración
- Retorna `true/false`

### 2. `getUsername(String jwt)`
- Extrae el subject del token
- Retorna el email del usuario

## Consideraciones de Seguridad

### 1. Manejo de Errores
- **No bloquea requests**: Continúa el flujo incluso con tokens inválidos
- **Logging detallado**: Para auditoría y debugging
- **Sin información sensible**: No expone detalles internos

### 2. Flexibilidad
- **Endpoints públicos**: Funcionan sin token
- **Endpoints protegidos**: Requieren token válido
- **Fallback graceful**: Continúa sin autenticación si hay errores

### 3. Performance
- **Filtro reactivo**: No bloquea threads
- **Validación eficiente**: Solo cuando hay token
- **Logging condicional**: Solo en niveles DEBUG/INFO

## Próximos Pasos

### 1. Integración con SecurityContext
```java
// Establecer contexto de seguridad
SecurityContextHolder.getContext().setAuthentication(authentication);
```

### 2. Configuración de Endpoints
```java
// En SecurityConfig
.pathMatchers("/api/v1/protected/**").authenticated()
.pathMatchers("/api/v1/public/**").permitAll()
```

### 3. Manejo de Roles
```java
// Extraer roles del token
String role = jwtUtil.getRole(token);
```

## Beneficios de la Corrección

1. **Compatibilidad**: Funciona correctamente con Spring WebFlux
2. **Performance**: Filtro reactivo no bloqueante
3. **Mantenibilidad**: Código más limpio y moderno
4. **Escalabilidad**: Mejor manejo de concurrencia
5. **Debugging**: Logs detallados para troubleshooting
