package uk.co.ogauthority.pwa.service.documents.generation;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@ExtendWith(MockitoExtension.class)
class AdmiraltyChartGeneratorServiceTest {

  @Mock
  private ConsentDocumentImageService consentDocumentImageService;

  @Mock
  private PadFileManagementService padFileManagementService;

  private AdmiraltyChartGeneratorService admiraltyChartGeneratorService;

  private PwaApplicationDetail detail = new PwaApplicationDetail();

  @BeforeEach
  void setUp() {
    admiraltyChartGeneratorService = new AdmiraltyChartGeneratorService(consentDocumentImageService, padFileManagementService);
  }

  @Test
  void getDocumentSectionData_admiraltyChartAvailable() {
    var uploadedFile = new UploadedFile();

    when(padFileManagementService.getUploadedFiles(detail, FileDocumentType.ADMIRALTY_CHART)).thenReturn(List.of(uploadedFile));

    when(consentDocumentImageService.convertFileToImageSource(uploadedFile))
        .thenReturn("fullChartUri");

    var docSectionData = admiraltyChartGeneratorService.getDocumentSectionData(detail, null, DocGenType.PREVIEW);

    assertThat(docSectionData.getTemplatePath()).isEqualTo("documents/consents/sections/admiraltyChart.ftl");
    assertThat(docSectionData.getTemplateModel()).containsOnly(
        entry("sectionName", DocumentSection.ADMIRALTY_CHART.getDisplayName()),
        entry("admiraltyChartImgSource", "fullChartUri")
    );

  }

  @Test
  void getDocumentSectionData_noAdmiraltyChart() {

    when(padFileManagementService.getUploadedFiles(detail, FileDocumentType.ADMIRALTY_CHART)).thenReturn(List.of());

    var docSectionData = admiraltyChartGeneratorService.getDocumentSectionData(detail, null, DocGenType.PREVIEW);

    verify(consentDocumentImageService, times(0)).convertFilesToImageSourceMap(any());

    assertThat(docSectionData).isNull();

  }

}