package uk.co.ogauthority.pwa.service.documents.generation;

import com.google.common.base.Stopwatch;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.DepositDrawingsService;

@Service
public class DepositDrawingsGeneratorService implements DocumentSectionGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(DepositDrawingsGeneratorService.class);

  private final DepositDrawingsService depositDrawingsService;
  private final FileUploadService fileUploadService;

  @Autowired
  public DepositDrawingsGeneratorService(DepositDrawingsService depositDrawingsService,
                                         FileUploadService fileUploadService) {
    this.depositDrawingsService = depositDrawingsService;
    this.fileUploadService = fileUploadService;
  }

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail) {

    var drawings = depositDrawingsService.getAllDepositDrawingsForDetail(pwaApplicationDetail);

    var drawingRefToFileIdMap = drawings.stream()
        .collect(Collectors.toMap(PadDepositDrawing::getReference, d -> d.getFile().getFileId()));

    var stopwatch = Stopwatch.createStarted();

    // TODO pick approach PWA-1014
    Map<String, String> fileIdToBase64StringMap = fileUploadService.getFilesByIds(drawingRefToFileIdMap.values())
        .stream()
        .collect(Collectors.toMap(UploadedFile::getFileId, this::convertToBase64String));

    // return fileUploadService.createTempFile(d);

    var elapsedMs = stopwatch.elapsed(TimeUnit.MILLISECONDS);

    LOGGER.info("Base64 conversion completed. Took [{}ms]", elapsedMs);

    // LOGGER.info("Temp file creation completed. Took [{}ms]", elapsedMs);

    Map<String, Object> modelMap = Map.of(
        "sectionName", DocumentSection.DEPOSIT_DRAWINGS.getDisplayName(),
        "drawingRefToFileIdMap", drawingRefToFileIdMap,
        "fileIdToBase64StringMap", fileIdToBase64StringMap
    );

    return new DocumentSectionData("documents/consents/sections/depositDrawings", modelMap);

  }

  private String convertToBase64String(UploadedFile file) {
    try {
      return Base64.encodeBase64String(file.getFileData().getBytes(1, (int) file.getFileData().length()));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
