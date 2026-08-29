package com.xeleronai.medicalimagingbackend.repository;

import com.xeleronai.medicalimagingbackend.entity.Image;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findByIdAndExamenId(Long id, Long examenId);
}
