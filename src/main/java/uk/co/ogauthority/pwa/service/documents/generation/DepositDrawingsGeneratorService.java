package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.DepositDrawingsService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadDepositDrawing;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class DepositDrawingsGeneratorService implements DocumentSectionGenerator {

  private final DepositDrawingsService depositDrawingsService;
  private final ConsentDocumentImageService consentDocumentImageService;
  private final PadProjectInformationService padProjectInformationService;

  @Autowired
  public DepositDrawingsGeneratorService(DepositDrawingsService depositDrawingsService,
                                         ConsentDocumentImageService consentDocumentImageService,
                                         PadProjectInformationService padProjectInformationService) {
    this.depositDrawingsService = depositDrawingsService;
    this.consentDocumentImageService = consentDocumentImageService;
    this.padProjectInformationService = padProjectInformationService;
  }

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail,
                                                    DocumentInstance documentInstance,
                                                    DocGenType docGenType) {

    if (!padProjectInformationService.isIncludingPermanentDepositsIn(pwaApplicationDetail)) {
      return null;
    }

    var drawings = depositDrawingsService.getAllDepositDrawingsForDetail(pwaApplicationDetail);

    // short-circuit early if no drawings, nothing to show
    if (drawings.isEmpty()) {
      return null;
    }

    var drawingRefToFileIdMap = drawings.stream()
        .collect(Collectors.toMap(PadDepositDrawing::getReference, d -> d.getFile().getFileId()));

    Map<String, String> fileIdToImgSourceMap = consentDocumentImageService
        .convertFilesToImageSourceMap(new HashSet<>(drawingRefToFileIdMap.values()));

    Map<String, Object> modelMap = Map.of(
        "sectionName", DocumentSection.DEPOSIT_DRAWINGS.getDisplayName(),
        "drawingRefToFileIdMap", drawingRefToFileIdMap,
        "fileIdToImgSourceMap", fileIdToImgSourceMap
    );

    return new DocumentSectionData("documents/consents/sections/depositDrawings", modelMap);

  }

}
