package uk.co.ogauthority.pwa.service.documents.generation;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;

@RunWith(MockitoJUnitRunner.class)
public class AdmiraltyChartGeneratorServiceTest {

  @Mock
  private PadFileService padFileService;

  @Mock
  private ConsentDocumentImageService consentDocumentImageService;

  private AdmiraltyChartGeneratorService admiraltyChartGeneratorService;

  private PadFile chartFile;

  private PwaApplicationDetail detail = new PwaApplicationDetail();

  @Before
  public void setUp() throws Exception {

    admiraltyChartGeneratorService = new AdmiraltyChartGeneratorService(padFileService, consentDocumentImageService);

    chartFile = new PadFile(detail, "id1", ApplicationDetailFilePurpose.ADMIRALTY_CHART, ApplicationFileLinkStatus.FULL);

    when(padFileService.getAllByPwaApplicationDetailAndPurpose(detail, ApplicationDetailFilePurpose.ADMIRALTY_CHART))
        .thenReturn(List.of(chartFile));

    when(consentDocumentImageService.convertFilesToImageSourceMap(Set.of("id1")))
        .thenReturn(Map.of("id1", "fullChartUri"));

  }

  @Test
  public void getDocumentSectionData_admiraltyChartAvailable() {

    var docSectionData = admiraltyChartGeneratorService.getDocumentSectionData(detail, null, DocGenType.PREVIEW);

    verify(consentDocumentImageService, times(1)).convertFilesToImageSourceMap(Set.of("id1"));

    assertThat(docSectionData.getTemplatePath()).isEqualTo("documents/consents/sections/admiraltyChart.ftl");
    assertThat(docSectionData.getTemplateModel()).containsOnly(
        entry("sectionName", DocumentSection.ADMIRALTY_CHART.getDisplayName()),
        entry("admiraltyChartImgSource", "fullChartUri")
    );

  }

  @Test
  public void getDocumentSectionData_noAdmiraltyChart() {

    when(padFileService.getAllByPwaApplicationDetailAndPurpose(detail, ApplicationDetailFilePurpose.ADMIRALTY_CHART))
        .thenReturn(List.of());

    var docSectionData = admiraltyChartGeneratorService.getDocumentSectionData(detail, null, DocGenType.PREVIEW);

    verify(consentDocumentImageService, times(0)).convertFilesToImageSourceMap(Set.of("id1"));

    assertThat(docSectionData).isNull();

  }

}