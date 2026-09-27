package com.xeleronai.medicalimagingbackend.service;

import com.xeleronai.medicalimagingbackend.dto.detection.DetectionResponse;
import java.util.List;

public interface DetectionQueryService {

    /**
     * Liste les détections de toutes les images d'un examen, triées par
     * ordre d'image puis par id de détection.
     *
     * @throws RessourceIntrouvableException si l'examen n'existe pas.
     */
    List<DetectionResponse> listerParExamen(Long examenId);
}
