package com.xeleronai.medicalimagingbackend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.xeleronai.medicalimagingbackend.entity.Patient;
import com.xeleronai.medicalimagingbackend.repository.PatientRepository;
import com.xeleronai.medicalimagingbackend.service.PatientLookupService;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test d'intégration contre la vraie base Postgres locale (medimg-pg) — pas
 * de H2, cohérent avec MedicalImagingBackendApplicationTests, seul précédent
 * du projet.
 */
@SpringBootTest
class PatientLookupServiceImplTest {

    @Autowired
    private PatientLookupService patientLookupService;

    @Autowired
    private PatientRepository patientRepository;

    private String mrnDeTest;

    @AfterEach
    void nettoyer() {
        if (mrnDeTest != null) {
            patientRepository.findByMrn(mrnDeTest).ifPresent(patientRepository::delete);
        }
    }

    @Test
    void mrn_inexistant_cree_un_nouveau_patient() {
        mrnDeTest = "TEST-MRN-" + UUID.randomUUID();

        Patient patient = patientLookupService.trouverOuCreerPatient(mrnDeTest, "Dupont", null, "M");

        assertThat(patient.getId()).isNotNull();
        assertThat(patient.getMrn()).isEqualTo(mrnDeTest);
        assertThat(patient.getNom()).isEqualTo("Dupont");
    }

    @Test
    void mrn_existant_est_reutilise_sans_ecrasement() {
        mrnDeTest = "TEST-MRN-" + UUID.randomUUID();

        Patient premier = patientLookupService.trouverOuCreerPatient(mrnDeTest, "Dupont", null, "M");
        Patient second = patientLookupService.trouverOuCreerPatient(mrnDeTest, "AUTRE_NOM_IGNORE", null, "F");

        assertThat(second.getId()).isEqualTo(premier.getId());
        assertThat(second.getNom()).isEqualTo("Dupont");
        assertThat(second.getSexe()).isEqualTo("M");

        assertThat(patientRepository.findByMrn(mrnDeTest)).hasValueSatisfying(
                p -> assertThat(p.getNom()).isEqualTo("Dupont"));
    }
}
