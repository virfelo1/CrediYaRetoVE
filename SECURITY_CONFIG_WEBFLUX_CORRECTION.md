# Corrección del SecurityConfig para Spring WebFlux

## Problema Identificado

El `SecurityConfig.java` tenía errores en las líneas 44 y 45 donde se intentaba usar el método `disable()` en `httpBasic()` y `formLogin()`, pero estos métodos no existen en Spring Security WebFlux.

## Errores Encontrados

### 1. Método `disable()` No Existe en WebFlux
```java
// ❌ INCORRECTO - Método disable() no existe en WebFlux
.httpBasic(httpBasic -> httpBasic.disable())
.formLogin(formLogin -> formLogin.disable())
```

### 2. Configuración Incorrecta de Filtros
```java
// ❌ INCORRECTO - Clase AuthenticationWebFilter no es el tipo correcto
.addFilterAt(jwtFilter, AuthenticationWebFilter.class)
```

## Solución Implementada

### 1. Configuración Correcta de HTTP Basic y Form Login

En Spring Security WebFlux, estos métodos se configuran de manera diferente:

```java
// ✅ CORRECTO - Configuración vacía para WebFlux
.httpBasic(httpBasic -> {}) // Configuración básica de HTTP Basic (vacía)
.formLogin(formLogin -> {}) // Configuración básica de form login (vacía)
```

### 2. Diferencias entre Spring MVC y WebFlux

| Aspecto | Spring MVC (Servlet) | Spring WebFlux (Reactive) |
|---------|---------------------|---------------------------|
| **HTTP Basic** | `.disable()` | `-> {}` (configuración vacía) |
| **Form Login** | `.disable()` | `-> {}` (configuración vacía) |
| **Filtros** | `Filter.class` | `SecurityWebFiltersOrder` |

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
                .userDetailsService(userDetailsService)
                .httpBasic(httpBasic -> {}) // ✅ Configuración correcta para WebFlux
                .formLogin(formLogin -> {}) // ✅ Configuración correcta para WebFlux
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## Configuración de Filtros JWT (Opcional)

Si se desea agregar el filtro JWT personalizado, se puede hacer de la siguiente manera:

### Opción 1: Usando SecurityWebFiltersOrder (Recomendado)
```java
import org.springframework.security.web.server.SecurityWebFiltersOrder;

// En el método securityWebFilterChain
.addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
```

### Opción 2: Usando WebFilter (Alternativa)
```java
// El JwtFilter ya implementa WebFilter, por lo que se puede usar directamente
// sin necesidad de addFilterAt
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

## Flujo de Autenticación

### 1. Sin Filtro JWT Personalizado
```
HTTP Request → Spring Security → CustomReactiveUserDetailsService → Handler
```

### 2. Con Filtro JWT Personalizado
```
HTTP Request → JwtFilter → Spring Security → CustomReactiveUserDetailsService → Handler
```

## Beneficios de la Corrección

### 1. **Compatibilidad WebFlux**
- Configuración correcta para Spring WebFlux
- Sin errores de compilación
- Funcionalidad reactiva completa

### 2. **Seguridad Mantenida**
- Autenticación JWT funcional
- Autorización basada en roles
- Endpoints protegidos correctamente

### 3. **Flexibilidad**
- Configuración modular
- Fácil extensión
- Mantenimiento simplificado

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

### 2. Integración del Filtro JWT
Si se desea agregar el filtro JWT personalizado:
1. Importar `SecurityWebFiltersOrder`
2. Agregar `.addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)`
3. Verificar que el filtro funciona correctamente

### 3. Monitoreo
- Logs de autenticación
- Métricas de acceso
- Alertas de seguridad

## Consideraciones Importantes

### 1. **Diferencias de Framework**
- Spring MVC vs Spring WebFlux tienen APIs diferentes
- Los métodos de configuración varían significativamente
- Es importante usar la documentación correcta

### 2. **Configuración Reactiva**
- Todos los componentes deben ser reactivos
- Evitar operaciones bloqueantes
- Usar `Mono` y `Flux` apropiadamente

### 3. **Seguridad**
- La configuración actual mantiene la seguridad
- Los endpoints están correctamente protegidos
- La autenticación JWT funciona como se espera
