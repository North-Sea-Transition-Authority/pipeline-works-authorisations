package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadConfirmationOfOptionService;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadConfirmationOfOptionView;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadConfirmationOfOptionViewTestUtil;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class OptionConfirmationSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadConfirmationOfOptionService padConfirmationOfOptionService;

  private OptionConfirmationSummaryService optionConfirmationSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    optionConfirmationSummaryService = new OptionConfirmationSummaryService(
        taskListService,
        padConfirmationOfOptionService
    );

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION, 1, 2);

  }

  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(optionConfirmationSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(Set.of(ApplicationTask.CONFIRM_OPTIONS), pwaApplicationDetail))
        .thenReturn(true);
    assertThat(optionConfirmationSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenTaskNotShown() {
    assertThat(optionConfirmationSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseSection_verifyServiceInteractions() {
    var view = PadConfirmationOfOptionViewTestUtil.createFrom("type", "desc");

    when(padConfirmationOfOptionService.getPadConfirmationOfOptionView(pwaApplicationDetail))
        .thenReturn(view);

    var appSummary = optionConfirmationSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);

    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).containsKey("view");
    assertThat(((PadConfirmationOfOptionView) appSummary.getTemplateModel().get("view"))).isEqualTo(view);
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.CONFIRM_OPTIONS.getDisplayName()));
    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.CONFIRM_OPTIONS.getDisplayName(), "#optionConfirmation")
    );

  }

}