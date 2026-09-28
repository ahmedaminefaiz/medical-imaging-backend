package com.xeleronai.medicalimagingbackend.service;

import com.xeleronai.medicalimagingbackend.entity.Image;
import java.util.List;

/**
 * Persiste le résultat d'un run d'analyse IA. Bean séparé de
 * DetectionAnalyseServiceImpl (voir son javadoc) : ce service est
 * exclusivement transactionnel, jamais mélangé avec un accès MinIO ou un
 * appel HTTP dans la même méthode.
 */
public interface DetectionRunPersistenceService {

    /**
     * Persiste les Detection du run, passe statutAnalyse=TERMINEE, écrit
     * l'AuditLog DETECTION_RUN. Transaction unique : toute erreur pendant
     * la boucle annule l'ensemble (aucune Detection partielle).
     */
    void enregistrerSucces(Long examenId, List<Image> imagesOrdonnees, PredictionIA prediction, Long utilisateurId);

    /**
     * Passe statutAnalyse=ECHOUEE + analyseMessage, écrit l'AuditLog
     * DETECTION_RUN. Ne touche jamais la table detection.
     */
    void enregistrerEchec(Long examenId, String message, Long utilisateurId);
}
