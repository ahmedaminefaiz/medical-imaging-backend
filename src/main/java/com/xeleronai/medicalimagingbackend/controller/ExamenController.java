package com.xeleronai.medicalimagingbackend.controller;

import com.xeleronai.medicalimagingbackend.dto.examen.ExamenUploadResponse;
import com.xeleronai.medicalimagingbackend.dto.examen.UploadStandardRequest;
import com.xeleronai.medicalimagingbackend.entity.Utilisateur;
import com.xeleronai.medicalimagingbackend.security.SecurityUtils;
import com.xeleronai.medicalimagingbackend.service.DetectionQueryService;
import com.xeleronai.medicalimagingbackend.service.ExamenQueryService;
import com.xeleronai.medicalimagingbackend.service.ExamenUploadService;
import com.xeleronai.medicalimagingbackend.service.LectureImpossibleException;
import com.xeleronai.medicalimagingbackend.service.RessourceIntrouvableException;
import com.xeleronai.medicalimagingbackend.service.UploadEchoueException;
import com.xeleronai.medicalimagingbackend.service.UploadValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/examens")
@RequiredArgsConstructor
@Validated
@Tag(name = "Examens", description = "Upload et consultation des examens")
public class ExamenController {

    private final ExamenUploadService examenUploadService;
    private final ExamenQueryService examenQueryService;
    private final DetectionQueryService detectionQueryService;
    private final SecurityUtils securityUtils;

    @PostMapping(value = "/upload/standard", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('RADIOLOGUE', 'TECHNICIEN')")
    @Operation(summary = "Upload d'un lot PNG/JPEG avec métadonnées en formulaire")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Examen créé"),
        @ApiResponse(responseCode = "400", description = "Batch invalide"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé"),
        @ApiResponse(responseCode = "503", description = "Échec de stockage")
    })
    public ResponseEntity<?> uploadStandard(@Valid @ModelAttribute UploadStandardRequest request) {
        Utilisateur utilisateurCourant = securityUtils.getUtilisateurCourant();
        ExamenUploadResponse response = examenUploadService.uploadStandard(request, utilisateurCourant);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/upload/dicom", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('RADIOLOGUE', 'TECHNICIEN')")
    @Operation(summary = "Upload d'un lot DICOM, métadonnées extraites automatiquement")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Examen créé"),
        @ApiResponse(responseCode = "400", description = "Batch invalide"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé"),
        @ApiResponse(responseCode = "503", description = "Échec de stockage")
    })
    public ResponseEntity<?> uploadDicom(@RequestParam("files") List<MultipartFile> files) {
        Utilisateur utilisateurCourant = securityUtils.getUtilisateurCourant();
        ExamenUploadResponse response = examenUploadService.uploadDicom(files, utilisateurCourant);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RADIOLOGUE', 'TECHNICIEN', 'ADMIN')")
    @Operation(summary = "Liste paginée des examens")
    public ResponseEntity<?> lister(
            @RequestParam(required = false) String mrn,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(examenQueryService.lister(mrn, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RADIOLOGUE', 'TECHNICIEN', 'ADMIN')")
    @Operation(summary = "Détail d'un examen et de ses images")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        return ResponseEntity.ok(examenQueryService.detail(id));
    }

    @GetMapping("/{examenId}/detections")
    @PreAuthorize("hasAnyRole('RADIOLOGUE', 'TECHNICIEN', 'ADMIN')")
    @Operation(summary = "Liste des détections IA d'un examen (résultats bruts, non validés)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des détections (vide si aucune)"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé"),
        @ApiResponse(responseCode = "404", description = "Examen introuvable")
    })
    public ResponseEntity<?> detections(@PathVariable Long examenId) {
        return ResponseEntity.ok(detectionQueryService.listerParExamen(examenId));
    }

    @GetMapping("/{examenId}/images/{imageId}/apercu")
    @PreAuthorize("hasAnyRole('RADIOLOGUE', 'TECHNICIEN', 'ADMIN')")
    @Operation(summary = "Aperçu PNG d'une image")
    public ResponseEntity<byte[]> apercu(@PathVariable Long examenId, @PathVariable Long imageId) {
        Utilisateur utilisateurCourant = securityUtils.getUtilisateurCourant();
        byte[] contenu = examenQueryService.lireApercu(examenId, imageId, utilisateurCourant);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(contenu);
    }

    @ExceptionHandler(UploadValidationException.class)
    public ResponseEntity<String> handleUploadValidation(UploadValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UploadEchoueException.class)
    public ResponseEntity<String> handleUploadEchoue(UploadEchoueException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Échec du stockage, réessayez plus tard.");
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<String> handleRessourceIntrouvable(RessourceIntrouvableException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(LectureImpossibleException.class)
    public ResponseEntity<String> handleLectureImpossible(LectureImpossibleException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Fichier temporairement indisponible, réessayez plus tard.");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
