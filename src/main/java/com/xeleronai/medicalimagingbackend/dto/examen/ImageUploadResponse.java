package com.xeleronai.medicalimagingbackend.dto.examen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadResponse {

    private Long imageId;
    private String format;
    private String cheminOriginal;
    private String cheminApercu;
    private Integer ordre;
}
