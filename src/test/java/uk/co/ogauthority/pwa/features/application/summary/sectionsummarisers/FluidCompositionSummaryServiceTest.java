package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.FluidCompositionView;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.PadFluidCompositionInfoService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class FluidCompositionSummaryServiceTest {



  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadFluidCompositionInfoService padFluidCompositionInfoService;

  private FluidCompositionSummaryService fluidCompositionSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {

    fluidCompositionSummaryService = new FluidCompositionSummaryService(padFluidCompositionInfoService, taskListService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(fluidCompositionSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(fluidCompositionSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenTaskNotShown() {
    assertThat(fluidCompositionSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void summariseSection_verifyServiceInteractions() {

    var fluidCompositionView = new FluidCompositionView(new LinkedHashMap<>(), new PwaApplicationDetail());
    when(padFluidCompositionInfoService.getFluidCompositionView(pwaApplicationDetail)).thenReturn(fluidCompositionView);

    var appSummary = fluidCompositionSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(2);
    assertThat(appSummary.getTemplateModel()).contains(entry("fluidCompositionView", fluidCompositionView));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.FLUID_COMPOSITION.getDisplayName()));

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.FLUID_COMPOSITION.getDisplayName(), "#fluidCompositionDetails")
    );

  }


}
