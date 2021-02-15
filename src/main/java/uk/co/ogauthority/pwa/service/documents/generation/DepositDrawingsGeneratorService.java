package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.DepositDrawingsService;

@Service
public class DepositDrawingsGeneratorService implements DocumentSectionGenerator {

  private final DepositDrawingsService depositDrawingsService;
  private final ConsentDocumentImageService consentDocumentImageService;

  @Autowired
  public DepositDrawingsGeneratorService(DepositDrawingsService depositDrawingsService,
                                         ConsentDocumentImageService consentDocumentImageService) {
    this.depositDrawingsService = depositDrawingsService;
    this.consentDocumentImageService = consentDocumentImageService;
  }

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail,
                                                    DocumentInstance documentInstance) {

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
