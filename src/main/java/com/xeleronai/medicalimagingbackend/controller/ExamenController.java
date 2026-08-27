package com.xeleronai.medicalimagingbackend.controller;

import com.xeleronai.medicalimagingbackend.dto.examen.ExamenUploadResponse;
import com.xeleronai.medicalimagingbackend.dto.examen.UploadStandardRequest;
import com.xeleronai.medicalimagingbackend.entity.Utilisateur;
import com.xeleronai.medicalimagingbackend.security.SecurityUtils;
import com.xeleronai.medicalimagingbackend.service.ExamenUploadService;
import com.xeleronai.medicalimagingbackend.service.UploadEchoueException;
import com.xeleronai.medicalimagingbackend.service.UploadValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/examens")
@RequiredArgsConstructor
@Tag(name = "Examens", description = "Upload et gestion des examens")
public class ExamenController {

    private final ExamenUploadService examenUploadService;
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

    @ExceptionHandler(UploadValidationException.class)
    public ResponseEntity<String> handleUploadValidation(UploadValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UploadEchoueException.class)
    public ResponseEntity<String> handleUploadEchoue(UploadEchoueException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Échec du stockage, réessayez plus tard.");
    }
}
