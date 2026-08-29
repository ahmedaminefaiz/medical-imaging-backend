package com.xeleronai.medicalimagingbackend.repository;

import com.xeleronai.medicalimagingbackend.entity.Examen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamenRepository extends JpaRepository<Examen, Long> {

    @Query("SELECT e FROM Examen e JOIN FETCH e.patient")
    Page<Examen> findAllAvecPatient(Pageable pageable);

    @Query("SELECT e FROM Examen e JOIN FETCH e.patient p WHERE p.mrn = :mrn")
    Page<Examen> findByPatientMrnAvecPatient(@Param("mrn") String mrn, Pageable pageable);
}
