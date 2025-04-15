package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.DepositDrawingsService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadDepositDrawing;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class DepositDrawingsGeneratorService implements DocumentSectionGenerator {

  private final DepositDrawingsService depositDrawingsService;
  private final ConsentDocumentImageService consentDocumentImageService;
  private final PermanentDepositService permanentDepositService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public DepositDrawingsGeneratorService(DepositDrawingsService depositDrawingsService,
                                         ConsentDocumentImageService consentDocumentImageService,
                                         PermanentDepositService permanentDepositService,
                                         PadFileManagementService padFileManagementService
  ) {
    this.depositDrawingsService = depositDrawingsService;
    this.consentDocumentImageService = consentDocumentImageService;
    this.permanentDepositService = permanentDepositService;
    this.padFileManagementService = padFileManagementService;
  }

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail,
                                                    DocumentInstance documentInstance,
                                                    DocGenType docGenType) {
    if (!permanentDepositService.permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail)) {
      return null;
    }

    var drawings = depositDrawingsService.getAllDepositDrawingsForDetail(pwaApplicationDetail);

    // short-circuit early if no drawings, nothing to show
    if (drawings.isEmpty()) {
      return null;
    }

    var drawingRefToFileIdMap = drawings.stream()
        .collect(Collectors.toMap(PadDepositDrawing::getReference, d -> String.valueOf(d.getFile().getFileId())));

    var drawingFiles = padFileManagementService.getUploadedFiles(pwaApplicationDetail, FileDocumentType.DEPOSIT_DRAWINGS);

    Map<String, String> fileIdToImgSourceMap = consentDocumentImageService.convertFilesToImageSourceMap(drawingFiles);

    Map<String, Object> modelMap = Map.of(
        "sectionName", DocumentSection.DEPOSIT_DRAWINGS.getDisplayName(),
        "drawingRefToFileIdMap", drawingRefToFileIdMap,
        "fileIdToImgSourceMap", fileIdToImgSourceMap
    );

    return new DocumentSectionData("documents/consents/sections/depositDrawings", modelMap);
  }

}
