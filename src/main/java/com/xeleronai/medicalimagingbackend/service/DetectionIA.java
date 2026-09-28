package com.xeleronai.medicalimagingbackend.service;

/**
 * Une détection brute reçue de ai-detection-service (POST /predict).
 * `coupe` est l'index 0-based dans la série triée par position z — la même
 * base que Image.ordre, donc directement comparable sans décalage.
 * Type interne, jamais exposé tel quel via l'API.
 */
public record DetectionIA(String label, BboxIA bbox, Double confiance, Integer coupe) {
}
