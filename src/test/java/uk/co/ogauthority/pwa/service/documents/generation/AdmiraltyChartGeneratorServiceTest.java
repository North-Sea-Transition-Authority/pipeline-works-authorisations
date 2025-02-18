package uk.co.ogauthority.pwa.service.documents.generation;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartFileService;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
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

  private PadFile chartFile;

  private PwaApplicationDetail detail = new PwaApplicationDetail();

  @BeforeEach
  void setUp() {

    admiraltyChartGeneratorService = new AdmiraltyChartGeneratorService(admiraltyChartFileService, consentDocumentImageService);

    chartFile = new PadFile(detail, "id1", ApplicationDetailFilePurpose.ADMIRALTY_CHART, ApplicationFileLinkStatus.FULL);

    when(admiraltyChartFileService.getAdmiraltyChartFile(detail))
        .thenReturn(Optional.of(chartFile));

  }

  @Test
  void getDocumentSectionData_admiraltyChartAvailable() {

    when(consentDocumentImageService.convertFilesToImageSourceMap(Set.of("id1")))
        .thenReturn(Map.of("id1", "fullChartUri"));

    var docSectionData = admiraltyChartGeneratorService.getDocumentSectionData(detail, null, DocGenType.PREVIEW);

    verify(consentDocumentImageService, times(1)).convertFilesToImageSourceMap(Set.of("id1"));

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

    verify(consentDocumentImageService, times(0)).convertFilesToImageSourceMap(Set.of("id1"));

    assertThat(docSectionData).isNull();

  }

}