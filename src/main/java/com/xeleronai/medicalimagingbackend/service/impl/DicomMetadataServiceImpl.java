package com.xeleronai.medicalimagingbackend.service.impl;

import com.xeleronai.medicalimagingbackend.service.DicomMetadata;
import com.xeleronai.medicalimagingbackend.service.DicomMetadataService;
import com.xeleronai.medicalimagingbackend.service.UploadValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class DicomMetadataServiceImpl implements DicomMetadataService {

    private static final DateTimeFormatter FORMAT_DICOM_DA = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public DicomMetadata extraire(MultipartFile fichierDicom) {
        Attributes attrs;
        try (DicomInputStream dis = new DicomInputStream(fichierDicom.getInputStream())) {
            attrs = dis.readDataset();
        } catch (IOException e) {
            log.warn("Fichier DICOM illisible : {}", fichierDicom.getOriginalFilename());
            throw new UploadValidationException("Fichier DICOM illisible : " + fichierDicom.getOriginalFilename());
        }

        String mrn = attrs.getString(Tag.PatientID);
        if (!StringUtils.hasText(mrn)) {
            throw new UploadValidationException(
                    "Tag PatientID manquant dans le fichier DICOM : " + fichierDicom.getOriginalFilename());
        }

        return new DicomMetadata(
                mrn,
                attrs.getString(Tag.PatientName),
                parseDateDicom(attrs.getString(Tag.PatientBirthDate)),
                attrs.getString(Tag.PatientSex),
                parseDateDicom(attrs.getString(Tag.StudyDate)),
                attrs.getString(Tag.Modality),
                attrs.getString(Tag.StudyDescription),
                attrs.getString(Tag.StudyInstanceUID));
    }

    private LocalDate parseDateDicom(String valeurDA) {
        if (!StringUtils.hasText(valeurDA)) {
            return null;
        }
        try {
            return LocalDate.parse(valeurDA, FORMAT_DICOM_DA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
