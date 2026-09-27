package com.xeleronai.medicalimagingbackend.service.mapper;

import com.xeleronai.medicalimagingbackend.dto.detection.BboxResponse;
import com.xeleronai.medicalimagingbackend.dto.detection.DetectionResponse;
import com.xeleronai.medicalimagingbackend.entity.Detection;
import com.xeleronai.medicalimagingbackend.entity.enums.DetectionTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DetectionMapper {

    @Mapping(target = "imageId", source = "image.id")
    @Mapping(target = "bbox", expression = "java(toBbox(detection))")
    DetectionResponse toResponse(Detection detection);

    /**
     * bbox n'a de sens que pour une détection BOX ; pour une détection
     * MASQUE (non produite en v1, schéma déjà prévu), x/y/largeur/hauteur ne
     * sont jamais renseignés donc bbox reste null.
     */
    default BboxResponse toBbox(Detection detection) {
        if (detection.getType() != DetectionTypeEnum.BOX) {
            return null;
        }
        return BboxResponse.builder()
                .x(detection.getX())
                .y(detection.getY())
                .largeur(detection.getLargeur())
                .hauteur(detection.getHauteur())
                .build();
    }
}
