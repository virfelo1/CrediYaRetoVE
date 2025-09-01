# Corrección del SecurityConfig para JWT Authentication

## Problema Identificado

El `SecurityConfig.java` tenía un error en la línea 40 con `AuthenticationWebFilter.class` debido a problemas de configuración del filtro JWT y falta de un `ReactiveUserDetailsService`.

## Errores Encontrados

### 1. Import Faltante
```java
// ❌ Faltaba el import correcto
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
```

### 2. Falta de ReactiveUserDetailsService
El `JwtFilter` requiere un `ReactiveUserDetailsService` para cargar los detalles del usuario desde la base de datos.

### 3. Configuración Incompleta
La configuración de seguridad no tenía el `userDetailsService` configurado.

## Solución Implementada

### 1. CustomReactiveUserDetailsService

Se creó un servicio personalizado que implementa `ReactiveUserDetailsService`:

```java
@Service
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {
    
    private final UserUseCase userUseCase;
    
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userUseCase.findByEmail(username)
                .map(this::createUserDetails)
                .onErrorMap(error -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }
    
    private UserDetails createUserDetails(User user) {
        String role = mapRoleIdToRoleName(user.getRol());
        
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
```

### 2. Mapeo de Roles

```java
private String mapRoleIdToRoleName(Integer roleId) {
    switch (roleId) {
        case 1: return "ADMIN";
        case 2: return "ASESOR";
        case 3: return "CLIENTE";
        default: return "CLIENTE";
    }
}
```

### 3. SecurityConfig Actualizado

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomReactiveUserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(JwtFilter jwtFilter, CustomReactiveUserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeExchange(authorize -> authorize
                        .pathMatchers(HttpMethod.POST, "/api/v1/usuarios", "/api/v1/login").permitAll()
                        .pathMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("ADMIN", "CLIENTE")
                        .pathMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .pathMatchers("/api/v1/solicitud").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, AuthenticationWebFilter.class)
                .userDetailsService(userDetailsService)  // ✅ Agregado
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .build();
    }
}
```

## Flujo de Autenticación Completo

### 1. Request Llega con JWT
```
HTTP Request (Authorization: Bearer <token>) → JwtFilter
```

### 2. JwtFilter Valida Token
```java
// Extrae y valida el token
String token = authHeader.substring(7);
if (jwtUtil.isValid(token)) {
    String username = jwtUtil.getUsername(token);
    // Continúa con la autenticación
}
```

### 3. CustomReactiveUserDetailsService
```java
// Busca usuario en la base de datos
return userUseCase.findByEmail(username)
    .map(this::createUserDetails)
    .onErrorMap(error -> new UsernameNotFoundException("Usuario no encontrado"));
```

### 4. Creación de UserDetails
```java
// Crea UserDetails con roles de Spring Security
return User.builder()
    .username(user.getEmail())
    .password(user.getPassword())
    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)))
    .build();
```

### 5. Establecimiento del Contexto de Seguridad
```java
// Establece la autenticación en el contexto reactivo
Authentication authentication = new UsernamePasswordAuthenticationToken(
    userDetails.getUsername(),
    null,
    userDetails.getAuthorities()
);
SecurityContextImpl securityContext = new SecurityContextImpl(authentication);
return chain.filter(exchange)
    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
```

## Configuración de Endpoints

### Endpoints Públicos (Sin Autenticación)
```java
.pathMatchers(HttpMethod.POST, "/api/v1/usuarios", "/api/v1/login").permitAll()
```

### Endpoints con Roles Específicos
```java
.pathMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("ADMIN", "CLIENTE")
.pathMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
.pathMatchers("/api/v1/solicitud").hasRole("ADMIN")
```

### Endpoints Autenticados
```java
.anyExchange().authenticated()
```

## Mapeo de Roles

| ID de Rol | Rol Spring Security | Descripción |
|-----------|-------------------|-------------|
| 1 | `ROLE_ADMIN` | Administrador |
| 2 | `ROLE_ASESOR` | Asesor |
| 3 | `ROLE_CLIENTE` | Cliente (por defecto) |

## Logs de Auditoría

### Autenticación Exitosa
```
DEBUG - JwtFilter: Procesando request para path: /api/v1/protected
DEBUG - JwtFilter: Token encontrado, iniciando validación.
INFO  - JwtFilter: Token JWT válido para usuario: juan@example.com
DEBUG - CustomReactiveUserDetailsService: Buscando usuario por username: juan@example.com
INFO  - CustomReactiveUserDetailsService: Usuario encontrado: juan@example.com con rol: [ROLE_CLIENTE]
```

### Usuario No Encontrado
```
DEBUG - JwtFilter: Token encontrado, iniciando validación.
INFO  - JwtFilter: Token JWT válido para usuario: inexistente@example.com
DEBUG - CustomReactiveUserDetailsService: Buscando usuario por username: inexistente@example.com
WARN  - CustomReactiveUserDetailsService: Usuario no encontrado: inexistente@example.com
```

## Beneficios de la Corrección

### 1. **Autenticación Completa**
- Validación de tokens JWT
- Carga de usuarios desde base de datos
- Mapeo de roles dinámico

### 2. **Seguridad Robusta**
- Contexto de seguridad reactivo
- Manejo de errores graceful
- Logs de auditoría detallados

### 3. **Flexibilidad**
- Roles configurables
- Endpoints con diferentes niveles de acceso
- Fallback para usuarios no encontrados

### 4. **Performance**
- Servicio reactivo no bloqueante
- Carga lazy de UserDetails
- Caching implícito de Spring Security

## Próximos Pasos

### 1. Testing
```bash
# Test de endpoint público
curl -X POST http://localhost:8080/api/v1/login

# Test de endpoint protegido
curl -X GET http://localhost:8080/api/v1/protected \
  -H "Authorization: Bearer <token>"
```

### 2. Monitoreo
- Logs de autenticación
- Métricas de acceso
- Alertas de seguridad

### 3. Mejoras
- Refresh tokens
- Rate limiting
- Blacklisting de tokens
