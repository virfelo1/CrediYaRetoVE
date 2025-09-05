package co.com.projectve.api.config;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "1a2b3c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r9s0t1u2v3w4x5y6z7a8b9c0d1e2f";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);

    public String create(String username, List<String> roles) { // <-- ¡CAMBIO AQUÍ!
        return JWT.create()
                .withSubject(username)
                .withIssuer("VE")
                .withClaim("roles", roles) // <-- Ahora 'roles' está definido
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)))
                .sign(ALGORITHM);
    }
    public boolean isValid (String jwt){
        try {
            JWT.require(ALGORITHM)
                    .build()
                    .verify(jwt);
            return true;
        } catch (JWTVerificationException e){
            return false;
        }

    }

    public String getUsername(String jwt){
        return JWT.require(ALGORITHM)
                .build()
                .verify(jwt)
                .getSubject();
    }
}