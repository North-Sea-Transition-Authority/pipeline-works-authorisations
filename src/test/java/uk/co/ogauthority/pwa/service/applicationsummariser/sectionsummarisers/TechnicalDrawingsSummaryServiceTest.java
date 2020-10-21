package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiraltyChartFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiraltyChartUrlFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.UmbilicalCrossSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.UmbilicalCrossSectionUrlFactory;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class TechnicalDrawingsSummaryServiceTest {



  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadFileService padFileService;

  @Mock
  private AdmiraltyChartFileService admiraltyChartFileService;

  @Mock
  private UmbilicalCrossSectionService umbilicalCrossSectionService;

  private TechnicalDrawingsSummaryService technicalDrawingsSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {

    technicalDrawingsSummaryService = new TechnicalDrawingsSummaryService(taskListService, padFileService, admiraltyChartFileService, umbilicalCrossSectionService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    when(admiraltyChartFileService.canUploadDocuments(pwaApplicationDetail)).thenReturn(true);

    assertThat(technicalDrawingsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }

  @Test
  public void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    when(umbilicalCrossSectionService.canUploadDocuments(pwaApplicationDetail)).thenReturn(true);
    assertThat(technicalDrawingsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenTaskNotShown() {
    assertThat(technicalDrawingsSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }



  @Test
  public void summariseSection_verifyServiceInteractions() {

    var admiraltyChartFileViews = List.of(new UploadedFileView("", "", 1L, null, null, null));
    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.ADMIRALTY_CHART, ApplicationFileLinkStatus.FULL))
        .thenReturn(admiraltyChartFileViews);

    var umbilicalFileViews = List.of(new UploadedFileView("", "", 1L, null, null, null));
    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.UMBILICAL_CROSS_SECTION, ApplicationFileLinkStatus.FULL))
        .thenReturn(umbilicalFileViews);

    var canShowAdmiraltyChart = true;
    when(admiraltyChartFileService.canUploadDocuments(pwaApplicationDetail)).thenReturn(canShowAdmiraltyChart);

    var canShowUmbilicalCrossSection = true;
    when(umbilicalCrossSectionService.canUploadDocuments(pwaApplicationDetail)).thenReturn(canShowUmbilicalCrossSection);

    var appSummary = technicalDrawingsSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(7);
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", "Other diagrams"));
    assertThat(appSummary.getTemplateModel()).contains(entry("admiraltyChartFileViews", admiraltyChartFileViews));
    assertThat(appSummary.getTemplateModel()).contains(entry("admiraltyChartUrlFactory", new AdmiraltyChartUrlFactory(pwaApplicationDetail)));
    assertThat(appSummary.getTemplateModel()).contains(entry("umbilicalFileViews", umbilicalFileViews));
    assertThat(appSummary.getTemplateModel()).contains(entry("umbilicalUrlFactory", new UmbilicalCrossSectionUrlFactory(pwaApplicationDetail)));
    assertThat(appSummary.getTemplateModel()).contains(entry("canShowAdmiraltyChart", canShowAdmiraltyChart));
    assertThat(appSummary.getTemplateModel()).contains(entry("canShowUmbilicalCrossSection", canShowUmbilicalCrossSection));


    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink("Other diagrams", "#technicalDrawings")
    );

  }


}