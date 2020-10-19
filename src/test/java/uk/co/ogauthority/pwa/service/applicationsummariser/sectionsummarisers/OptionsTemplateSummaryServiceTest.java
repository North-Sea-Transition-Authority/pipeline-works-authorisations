package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.pwaapplications.options.OptionsTemplateController;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class OptionsTemplateSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadFileService padFileService;

  private OptionsTemplateSummaryService optionsTemplateSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {

    optionsTemplateSummaryService = new OptionsTemplateSummaryService(
        taskListService,
        padFileService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);

  }

  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(optionsTemplateSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }


  @Test
  public void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(optionsTemplateSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenTaskNotShown() {
    assertThat(optionsTemplateSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseSection_verifyServiceInteractions() {

    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.OPTIONS_TEMPLATE, ApplicationFileLinkStatus.FULL)).thenReturn(
        List.of(
            new UploadedFileView("id", "name", 99L, "desc", Instant.now(), "#")
        ));

    var appSummary = optionsTemplateSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);

    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).containsKey("optionsTemplateFileView");
    assertThat(appSummary.getTemplateModel().get("optionsTemplateFileView") instanceof UploadedFileView).isTrue();
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.OPTIONS_TEMPLATE.getDisplayName()));
    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.OPTIONS_TEMPLATE.getDisplayName(), "#optionsTemplate"));
    assertThat(appSummary.getTemplateModel().get("optionsFileDownloadUrl")).isEqualTo(ReverseRouter
        .route(on(OptionsTemplateController.class)
            .handleDownload(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(), null, null)));

  }

}