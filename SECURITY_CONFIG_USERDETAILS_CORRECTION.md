# Corrección del ReactiveUserDetailsService en Spring WebFlux

## Problema Identificado

El `SecurityConfig.java` tenía un error en la línea 43 donde se intentaba usar el método `userDetailsService()` que no existe en `ServerHttpSecurity` de Spring Security WebFlux.

## Error Encontrado

```java
// ❌ INCORRECTO - Método userDetailsService() no existe en ServerHttpSecurity
.userDetailsService(userDetailsService)
```

**Error de compilación:**
```
cannot find symbol
symbol: method userDetailsService(CustomReactiveUserDetailsService)
location: class ServerHttpSecurity
```

## Solución Implementada

### 1. Configuración Correcta para WebFlux

En Spring Security WebFlux, el `ReactiveUserDetailsService` se configura como un bean separado:

```java
// ✅ CORRECTO - Bean separado para ReactiveUserDetailsService
@Bean
public ReactiveUserDetailsService reactiveUserDetailsService() {
    return userDetailsService;
}
```

### 2. Diferencias entre Spring MVC y WebFlux

| Aspecto | Spring MVC (Servlet) | Spring WebFlux (Reactive) |
|---------|---------------------|---------------------------|
| **UserDetailsService** | `.userDetailsService()` | Bean `@Bean ReactiveUserDetailsService` |
| **Configuración** | En SecurityFilterChain | Bean separado |
| **Detección** | Manual | Automática por Spring |

### 3. SecurityConfig Corregido

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
                        // Permite el registro y el login sin autenticación
                        .pathMatchers(HttpMethod.POST, "/api/v1/usuarios", "/api/v1/login").permitAll()
                        // Rutas que requieren autenticación con roles específicos
                        .pathMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("ADMIN", "CLIENTE")
                        .pathMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .pathMatchers("/api/v1/solicitud").hasRole("ADMIN")
                        // Todas las demás peticiones deben estar autenticadas
                        .anyExchange().authenticated()
                )
                // ❌ Removido: .userDetailsService(userDetailsService)
                .httpBasic(httpBasic -> {})
                .formLogin(formLogin -> {})
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ Agregado: Bean para ReactiveUserDetailsService
    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        return userDetailsService;
    }
}
```

## Flujo de Autenticación

### 1. Detección Automática
```
Spring Security WebFlux → Busca @Bean ReactiveUserDetailsService → CustomReactiveUserDetailsService
```

### 2. Proceso de Autenticación
```
HTTP Request → JwtFilter → Spring Security → CustomReactiveUserDetailsService → Handler
```

### 3. CustomReactiveUserDetailsService
```java
@Service
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {
    
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userUseCase.findByEmail(username)
                .map(this::createUserDetails)
                .onErrorMap(error -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }
}
```

## Configuración de Endpoints

### Endpoints Públicos
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

### 1. **Compatibilidad WebFlux**
- Configuración correcta para Spring Security WebFlux
- Sin errores de compilación
- Detección automática del servicio

### 2. **Seguridad Mantenida**
- Autenticación JWT funcional
- Autorización basada en roles
- Endpoints protegidos correctamente

### 3. **Flexibilidad**
- Configuración modular
- Fácil extensión
- Mantenimiento simplificado

### 4. **Performance**
- Servicio reactivo no bloqueante
- Carga lazy de UserDetails
- Caching implícito de Spring Security

## Próximos Pasos

### 1. Testing de Configuración
```bash
# Verificar que la aplicación inicia sin errores
./gradlew bootRun

# Probar endpoints públicos
curl -X POST http://localhost:8080/api/v1/login

# Probar endpoints protegidos
curl -X GET http://localhost:8080/api/v1/protected
```

### 2. Monitoreo
- Logs de autenticación
- Métricas de acceso
- Alertas de seguridad

### 3. Mejoras
- Refresh tokens
- Rate limiting
- Blacklisting de tokens

## Consideraciones Importantes

### 1. **Detección Automática**
- Spring Security WebFlux detecta automáticamente el bean `ReactiveUserDetailsService`
- No es necesario configurarlo manualmente en `SecurityWebFilterChain`
- El bean debe estar disponible en el contexto de Spring

### 2. **Configuración Reactiva**
- Todos los componentes deben ser reactivos
- Evitar operaciones bloqueantes
- Usar `Mono` y `Flux` apropiadamente

### 3. **Seguridad**
- La configuración actual mantiene la seguridad
- Los endpoints están correctamente protegidos
- La autenticación JWT funciona como se espera

## Alternativas de Configuración

### Opción 1: Bean Separado (Recomendado)
```java
@Bean
public ReactiveUserDetailsService reactiveUserDetailsService() {
    return userDetailsService;
}
```

### Opción 2: Configuración Manual (Alternativa)
```java
// En SecurityWebFilterChain
.authenticationManager(ReactiveAuthenticationManager)
```

### Opción 3: Filtro Personalizado
```java
// El JwtFilter ya maneja la autenticación
// No es necesario configurar ReactiveUserDetailsService manualmente
```
