package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;

@Service
public class AdmiraltyChartGeneratorService implements DocumentSectionGenerator {

  private final PadFileService padFileService;
  private final ConsentDocumentImageService consentDocumentImageService;

  @Autowired
  public AdmiraltyChartGeneratorService(PadFileService padFileService,
                                        ConsentDocumentImageService consentDocumentImageService) {
    this.padFileService = padFileService;
    this.consentDocumentImageService = consentDocumentImageService;
  }

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail) {

    var admiraltyChartFileId = padFileService
        .getAllByPwaApplicationDetailAndPurpose(pwaApplicationDetail, ApplicationDetailFilePurpose.ADMIRALTY_CHART).stream()
        .filter(pf -> pf.getFileLinkStatus() == ApplicationFileLinkStatus.FULL)
        .map(PadFile::getFileId)
        .findFirst();

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
