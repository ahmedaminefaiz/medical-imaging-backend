package com.xeleronai.medicalimagingbackend.service;

import java.util.List;

/**
 * Réponse brute complète de POST /predict (ai-detection-service). `type`
 * vaut toujours "BOX" en v1 ("MASQUE" prévu, non produit) ; on le garde tel
 * quel plutôt que de le typer en enum, pour ne pas avoir à retoucher ce
 * record le jour où la segmentation sera implémentée côté service IA.
 * Type interne, jamais exposé tel quel via l'API.
 */
public record PredictionIA(String type, List<DetectionIA> detections) {
}
