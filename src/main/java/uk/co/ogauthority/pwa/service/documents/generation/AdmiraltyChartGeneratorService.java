package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class AdmiraltyChartGeneratorService implements DocumentSectionGenerator {

  private final ConsentDocumentImageService consentDocumentImageService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public AdmiraltyChartGeneratorService(ConsentDocumentImageService consentDocumentImageService,
                                        PadFileManagementService padFileManagementService) {
    this.consentDocumentImageService = consentDocumentImageService;
    this.padFileManagementService = padFileManagementService;
  }

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail,
                                                    DocumentInstance documentInstance,
                                                    DocGenType docGenType) {
    var files = padFileManagementService.getUploadedFiles(pwaApplicationDetail, FileDocumentType.ADMIRALTY_CHART);

    // short-circuit early if no admiralty chart, nothing to show
    if (files.isEmpty()) {
      return null;
    }

    Map<String, Object> modelMap = Map.of(
        "sectionName", DocumentSection.ADMIRALTY_CHART.getDisplayName(),
        "admiraltyChartImgSource", consentDocumentImageService.convertFileToImageSource(files.getFirst())
    );

    return new DocumentSectionData("documents/consents/sections/admiraltyChart", modelMap);
  }

}
