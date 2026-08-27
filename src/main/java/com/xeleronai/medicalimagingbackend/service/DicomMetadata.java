package com.xeleronai.medicalimagingbackend.service;

import java.time.LocalDate;

/**
 * Métadonnées extraites d'un fichier DICOM. Type interne, jamais exposé tel
 * quel via l'API (voir ExamenUploadResponse / dto/examen).
 */
public record DicomMetadata(
        String mrn,
        String nom,
        LocalDate dateNaissance,
        String sexe,
        LocalDate dateExamen,
        String modalite,
        String type,
        String studyInstanceUid
) {
}
