package com.xeleronai.medicalimagingbackend.service.impl;

import com.xeleronai.medicalimagingbackend.dto.detection.DetectionResponse;
import com.xeleronai.medicalimagingbackend.repository.DetectionRepository;
import com.xeleronai.medicalimagingbackend.repository.ExamenRepository;
import com.xeleronai.medicalimagingbackend.service.DetectionQueryService;
import com.xeleronai.medicalimagingbackend.service.RessourceIntrouvableException;
import com.xeleronai.medicalimagingbackend.service.mapper.DetectionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DetectionQueryServiceImpl implements DetectionQueryService {

    private final ExamenRepository examenRepository;
    private final DetectionRepository detectionRepository;
    private final DetectionMapper detectionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DetectionResponse> listerParExamen(Long examenId) {
        if (!examenRepository.existsById(examenId)) {
            throw new RessourceIntrouvableException("Examen introuvable");
        }
        return detectionRepository.findByExamenId(examenId).stream()
                .map(detectionMapper::toResponse)
                .toList();
    }
}
