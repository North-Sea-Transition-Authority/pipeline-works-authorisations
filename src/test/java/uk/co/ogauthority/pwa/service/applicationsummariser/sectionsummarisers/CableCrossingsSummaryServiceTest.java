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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCableCrossing;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CableCrossingView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadCableCrossingService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class CableCrossingsSummaryServiceTest {



  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadCableCrossingService padCableCrossingService;

  @Mock
  private PadFileService padFileService;

  private CableCrossingsSummaryService cableCrossingsSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {

    cableCrossingsSummaryService = new CableCrossingsSummaryService(padCableCrossingService, taskListService, padFileService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(cableCrossingsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  public void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(cableCrossingsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenTaskNotShown() {
    assertThat(cableCrossingsSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseSection_verifyServiceInteractions() {

    var cableCrossing = new PadCableCrossing();
    var cableCrossingViews = List.of(new CableCrossingView(cableCrossing));
    when(padCableCrossingService.getCableCrossingViews(pwaApplicationDetail)).thenReturn(cableCrossingViews);

    var fileView = new UploadedFileView(null, null, 1L, null, null, null);
    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.CABLE_CROSSINGS,
        ApplicationFileLinkStatus.FULL)).thenReturn(List.of(fileView));

    var appSummary = cableCrossingsSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(4);
    assertThat(appSummary.getTemplateModel()).contains(entry("cableCrossingViews", cableCrossingViews));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", CrossingAgreementTask.CABLE_CROSSINGS.getDisplayText()));
    assertThat(appSummary.getTemplateModel()).contains(entry("cableCrossingFiles", List.of(fileView)));
    assertThat(appSummary.getTemplateModel()).containsKey("cableCrossingUrlFactory");

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(CrossingAgreementTask.CABLE_CROSSINGS.getDisplayText(), "#cableCrossingDetails")
    );

  }


}