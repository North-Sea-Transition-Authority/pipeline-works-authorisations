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
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class LicenceBlockSummaryServiceTest {



  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private BlockCrossingService blockCrossingService;

  @Mock
  private PadFileService padFileService;

  private LicenceBlockSummaryService licenceBlockSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {

    licenceBlockSummaryService = new LicenceBlockSummaryService(blockCrossingService, padFileService, taskListService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(licenceBlockSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  public void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(licenceBlockSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenTaskNotShown() {
    assertThat(licenceBlockSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseSection_verifyServiceInteractions() {

    var blockCrossingView = new BlockCrossingView(1, null, null, null, null);
    when(blockCrossingService.getCrossedBlockViews(pwaApplicationDetail)).thenReturn(List.of(blockCrossingView));

    var fileView = new UploadedFileView(null, null, 1L, null, null, null);
    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.BLOCK_CROSSINGS,
        ApplicationFileLinkStatus.FULL)).thenReturn(List.of(fileView));

    when(blockCrossingService.isDocumentsRequired(pwaApplicationDetail)).thenReturn(true);

    var appSummary = licenceBlockSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(5);
    assertThat(appSummary.getTemplateModel()).contains(entry("blockCrossingViews", List.of(blockCrossingView)));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", CrossingAgreementTask.LICENCE_AND_BLOCKS.getDisplayText()));
    assertThat(appSummary.getTemplateModel()).contains(entry("blockCrossingFileViews", List.of(fileView)));
    assertThat(appSummary.getTemplateModel()).containsKey("blockCrossingUrlFactory");
    assertThat(appSummary.getTemplateModel()).contains(entry("isDocumentsRequired", true));

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(CrossingAgreementTask.LICENCE_AND_BLOCKS.getDisplayText(), "#licenceBlockDetails")
    );

  }


}