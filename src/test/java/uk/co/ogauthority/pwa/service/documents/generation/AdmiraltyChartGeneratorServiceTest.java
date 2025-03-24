package uk.co.ogauthority.pwa.service.documents.generation;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartFileService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@ExtendWith(MockitoExtension.class)
class AdmiraltyChartGeneratorServiceTest {

  @Mock
  private AdmiraltyChartFileService admiraltyChartFileService;

  @Mock
  private ConsentDocumentImageService consentDocumentImageService;

  private AdmiraltyChartGeneratorService admiraltyChartGeneratorService;

  private UploadedFileView chartFile;

  private PwaApplicationDetail detail = new PwaApplicationDetail();

  private static final String FILE_ID = String.valueOf(UUID.randomUUID());

  @BeforeEach
  void setUp() {

    admiraltyChartGeneratorService = new AdmiraltyChartGeneratorService(admiraltyChartFileService, consentDocumentImageService);

    chartFile = new UploadedFileView(FILE_ID, null, 1L, "admiralty desc", null, null);

    when(admiraltyChartFileService.getAdmiraltyChartFile(detail))
        .thenReturn(Optional.of(chartFile));
  }

  @Test
  void getDocumentSectionData_admiraltyChartAvailable() {

    when(consentDocumentImageService.convertFilesToImageSourceMap(Set.of(FILE_ID)))
        .thenReturn(Map.of(FILE_ID, "fullChartUri"));

    var docSectionData = admiraltyChartGeneratorService.getDocumentSectionData(detail, null, DocGenType.PREVIEW);

    verify(consentDocumentImageService, times(1)).convertFilesToImageSourceMap(Set.of(FILE_ID));

    assertThat(docSectionData.getTemplatePath()).isEqualTo("documents/consents/sections/admiraltyChart.ftl");
    assertThat(docSectionData.getTemplateModel()).containsOnly(
        entry("sectionName", DocumentSection.ADMIRALTY_CHART.getDisplayName()),
        entry("admiraltyChartImgSource", "fullChartUri")
    );

  }

  @Test
  void getDocumentSectionData_noAdmiraltyChart() {

    when(admiraltyChartFileService.getAdmiraltyChartFile(detail))
        .thenReturn(Optional.empty());

    var docSectionData = admiraltyChartGeneratorService.getDocumentSectionData(detail, null, DocGenType.PREVIEW);

    verify(consentDocumentImageService, times(0)).convertFilesToImageSourceMap(Set.of(FILE_ID));

    assertThat(docSectionData).isNull();

  }

}