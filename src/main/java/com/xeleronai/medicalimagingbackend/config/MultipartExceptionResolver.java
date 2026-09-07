package com.xeleronai.medicalimagingbackend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * MaxUploadSizeExceededException est levée pendant la résolution du corps
 * multipart, avant que le HandlerMethod (le controller) ne soit déterminé —
 * un @ExceptionHandler de contrôleur ne peut donc jamais la capter. Ce
 * HandlerExceptionResolver est enregistré globalement (pas un
 * @ControllerAdvice) et intercepte cette exception spécifique en amont de
 * DefaultHandlerExceptionResolver, pour logguer la limite réellement
 * configurée et renvoyer un message exploitable côté frontend, plutôt que
 * de laisser passer le dispatch /error générique.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class MultipartExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!(ex instanceof MaxUploadSizeExceededException max)) {
            return null;
        }

        log.warn("Upload rejeté : taille maximale dépassée (limite configurée = {} octets)", max.getMaxUploadSize());

        try {
            response.reset();
            response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Le lot de fichiers est trop volumineux, réessayez avec un lot plus petit.");
        } catch (IOException e) {
            log.warn("Impossible d'écrire la réponse d'erreur 413", e);
        }

        return new ModelAndView();
    }
}
