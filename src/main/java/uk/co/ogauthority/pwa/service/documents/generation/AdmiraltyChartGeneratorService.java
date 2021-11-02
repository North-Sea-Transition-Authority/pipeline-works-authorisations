package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartFileService;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class AdmiraltyChartGeneratorService implements DocumentSectionGenerator {

  private final ConsentDocumentImageService consentDocumentImageService;
  private final AdmiraltyChartFileService admiraltyChartFileService;

  @Autowired
  public AdmiraltyChartGeneratorService(AdmiraltyChartFileService admiraltyChartFileService,
                                        ConsentDocumentImageService consentDocumentImageService) {
    this.admiraltyChartFileService = admiraltyChartFileService;
    this.consentDocumentImageService = consentDocumentImageService;
  }

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail,
                                                    DocumentInstance documentInstance,
                                                    DocGenType docGenType) {

    Optional<String> admiraltyChartFileId = admiraltyChartFileService.getAdmiraltyChartFile(pwaApplicationDetail)
        .map(PadFile::getFileId);

    // short-circuit early if no admiralty chart, nothing to show
    if (admiraltyChartFileId.isEmpty()) {
      return null;
    }

    Map<String, String> fileIdToImgSourceMap = admiraltyChartFileId
        .map(fileId -> consentDocumentImageService.convertFilesToImageSourceMap(Set.of(fileId)))
        .orElse(Map.of());

    String imgSource = fileIdToImgSourceMap.getOrDefault(admiraltyChartFileId.orElse(""), "");

    Map<String, Object> modelMap = Map.of(
        "sectionName", DocumentSection.ADMIRALTY_CHART.getDisplayName(),
        "admiraltyChartImgSource", imgSource
    );

    return new DocumentSectionData("documents/consents/sections/admiraltyChart", modelMap);

  }

}
