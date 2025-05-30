package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOptionService;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOptionView;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOptionViewTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class OptionConfirmationSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadConfirmationOfOptionService padConfirmationOfOptionService;

  private OptionConfirmationSummaryService optionConfirmationSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {
    optionConfirmationSummaryService = new OptionConfirmationSummaryService(
        taskListService,
        padConfirmationOfOptionService
    );

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION, 1, 2);

  }

  @Test
  void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(optionConfirmationSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(Set.of(ApplicationTask.CONFIRM_OPTIONS), pwaApplicationDetail))
        .thenReturn(true);
    assertThat(optionConfirmationSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenTaskNotShown() {
    assertThat(optionConfirmationSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void summariseSection_verifyServiceInteractions() {
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