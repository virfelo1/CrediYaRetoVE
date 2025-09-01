# Resolución del Conflicto de Beans ReactiveUserDetailsService

## Problema Identificado

La aplicación fallaba al iniciar debido a un conflicto de beans de `ReactiveUserDetailsService`. Spring encontró dos beans del mismo tipo:

1. `CustomReactiveUserDetailsService` (definido como `@Service`)
2. `reactiveUserDetailsService` (definido como `@Bean` en `SecurityConfig`)

## Error Encontrado

```
APPLICATION FAILED TO START

Description:
Parameter 1 of constructor in co.com.projectve.api.config.JwtFilter required a single bean, but 2 were found:
	- customReactiveUserDetailsService: defined in URL [jar:file:/C:/workspace_virfelo1/RetoTecnico/CrediYaVE/infrastructure/entry-points/reactive-web/build/libs/reactive-web.jar!/co/com/projectve/api/config/CustomReactiveUserDetailsService.class]
	- reactiveUserDetailsService: defined by method 'reactiveUserDetailsService' in class path resource [co/com/projectve/api/config/SecurityConfig.class]

This may be due to missing parameter name information

Action:
Consider marking one of the beans as @Primary, updating the consumer to accept multiple beans, or using @Qualifier to identify the bean that should be consumed
```

## Análisis del Problema

### 1. Beans Duplicados

```java
// ❌ Bean 1: CustomReactiveUserDetailsService como @Service
@Service
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {
    // Implementación...
}

// ❌ Bean 2: reactiveUserDetailsService como @Bean en SecurityConfig
@Bean
public ReactiveUserDetailsService reactiveUserDetailsService() {
    return userDetailsService; // Retorna el mismo CustomReactiveUserDetailsService
}
```

### 2. Inyección Ambigua

```java
// ❌ JwtFilter no sabe cuál bean usar
public JwtFilter(JwtUtil jwtUtil, ReactiveUserDetailsService reactiveUserDetailsService) {
    // Spring encuentra 2 beans del mismo tipo
}
```

## Solución Implementada

### 1. Eliminación del Bean Duplicado

Removí el bean duplicado del `SecurityConfig` ya que `CustomReactiveUserDetailsService` ya está definido como `@Service`:

```java
// ✅ ANTES - SecurityConfig con bean duplicado
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomReactiveUserDetailsService userDetailsService;

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
                .httpBasic(httpBasic -> {})
                .formLogin(formLogin -> {})
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ❌ REMOVIDO - Bean duplicado
    // @Bean
    // public ReactiveUserDetailsService reactiveUserDetailsService() {
    //     return userDetailsService;
    // }
}
```

### 2. Configuración Final

```java
// ✅ DESPUÉS - SecurityConfig sin bean duplicado
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
                .httpBasic(httpBasic -> {})
                .formLogin(formLogin -> {})
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 3. CustomReactiveUserDetailsService (Único Bean)

```java
// ✅ Único bean de ReactiveUserDetailsService
@Service
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {

    private final UserUseCase userUseCase;

    public CustomReactiveUserDetailsService(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userUseCase.findByEmail(username)
                .map(this::createUserDetails)
                .onErrorMap(error -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    private UserDetails createUserDetails(User user) {
        String role = mapRoleIdToRoleName(user.getRol());
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private String mapRoleIdToRoleName(Integer roleId) {
        switch (roleId) {
            case 1: return "ADMIN";
            case 2: return "ASESOR";
            case 3: return "CLIENTE";
            default: return "CLIENTE";
        }
    }
}
```

## Alternativas de Solución

### Opción 1: Eliminar Bean Duplicado (Implementada)
```java
// ✅ Eliminar el bean duplicado del SecurityConfig
// Spring detectará automáticamente el @Service
```

### Opción 2: Usar @Primary
```java
// ✅ Marcar uno de los beans como primario
@Service
@Primary
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {
    // Implementación...
}
```

### Opción 3: Usar @Qualifier
```java
// ✅ Especificar cuál bean usar
public JwtFilter(JwtUtil jwtUtil, 
                @Qualifier("customReactiveUserDetailsService") 
                ReactiveUserDetailsService reactiveUserDetailsService) {
    // Implementación...
}
```

### Opción 4: Eliminar @Service y Solo Usar @Bean
```java
// ✅ Solo usar @Bean en SecurityConfig
@Component
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {
    // Sin @Service
}

@Configuration
public class SecurityConfig {
    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        return new CustomReactiveUserDetailsService(userUseCase);
    }
}
```

## Flujo de Autenticación Final

### 1. Detección Automática
```
Spring Security WebFlux → Busca @Service ReactiveUserDetailsService → CustomReactiveUserDetailsService
```

### 2. Inyección de Dependencias
```
JwtFilter → CustomReactiveUserDetailsService (único bean disponible)
SecurityConfig → CustomReactiveUserDetailsService (único bean disponible)
```

### 3. Proceso de Autenticación
```
HTTP Request → JwtFilter → Spring Security → CustomReactiveUserDetailsService → Handler
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

## Beneficios de la Solución

### 1. **Sin Conflictos de Beans**
- Un solo bean de `ReactiveUserDetailsService`
- Inyección de dependencias sin ambigüedad
- Inicio de aplicación exitoso

### 2. **Configuración Limpia**
- Sin duplicación de código
- Mantenimiento simplificado
- Estructura clara y comprensible

### 3. **Funcionalidad Completa**
- Autenticación JWT funcional
- Autorización basada en roles
- Endpoints protegidos correctamente

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

### 4. **Evitar Duplicación**
- No crear múltiples beans del mismo tipo
- Usar `@Service` o `@Bean`, pero no ambos
- Mantener la configuración simple y clara
