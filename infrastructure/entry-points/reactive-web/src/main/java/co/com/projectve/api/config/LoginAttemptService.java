package co.com.projectve.api.config;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPTS = Integer.MAX_VALUE; //cambiar aqui el numero maximo de intentos fallidos de password
    // Usamos un ConcurrentHashMap para manejar la concurrencia de forma segura
    private final ConcurrentHashMap<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> blockTimestamp = new ConcurrentHashMap<>();
    private final long BLOCK_DURATION_MINUTES = 15;

    public void loginFailed(String key) {
        attemptsCache.merge(key, 1, Integer::sum);
        if (attemptsCache.get(key) >= MAX_ATTEMPTS) {
            blockUser(key);
        }
    }

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
        blockTimestamp.remove(key);
    }

    public boolean isBlocked(String key) {
        Long blockedTime = blockTimestamp.get(key);
        if (blockedTime != null) {
            // Verifica si el tiempo de bloqueo ha expirado
            long elapsedTime = System.currentTimeMillis() - blockedTime;
            long blockDurationMillis = TimeUnit.MINUTES.toMillis(BLOCK_DURATION_MINUTES);
            if (elapsedTime >= blockDurationMillis) {
                // El bloqueo ha expirado, lo removemos
                loginSucceeded(key);
                return false;
            }
            return true;
        }
        return false;
    }

    private void blockUser(String key) {
        blockTimestamp.put(key, System.currentTimeMillis());
        // Opcional: limpiar los intentos fallidos una vez bloqueado
        attemptsCache.remove(key);
    }
}
