package com.xeleronai.medicalimagingbackend.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Vérifie l'application de @PreAuthorize sur ExamenController (voir
 * @EnableMethodSecurity ajoutée à SecurityConfig pour ce ticket — sans elle,
 * ces deux tests échoueraient silencieusement en laissant passer ADMIN).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ExamenControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sansAuthentification_retourne401() throws Exception {
        mockMvc.perform(post("/api/v1/examens/upload/dicom"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roleAdmin_retourne403() throws Exception {
        // Requête multipart valide avec un fichier bidon : il faut que le
        // binding de @RequestParam("files") réussisse pour que l'exécution
        // atteigne @PreAuthorize (le binding d'arguments a lieu avant que le
        // proxy AOP de @PreAuthorize ne s'exécute) — sinon Spring MVC
        // répondrait 415/400 avant même d'atteindre le contrôle de rôle.
        MockMultipartFile fichier = new MockMultipartFile("files", "a.dcm", "application/dicom", new byte[] {1});
        mockMvc.perform(multipart("/api/v1/examens/upload/dicom")
                        .file(fichier)
                        .with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }
}
