package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.EnvironmentalDecommissioningView;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentalDecomSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;

  private EnvironmentalDecomSummaryService environmentalDecomSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {

    environmentalDecomSummaryService = new EnvironmentalDecomSummaryService(padEnvironmentalDecommissioningService, taskListService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(environmentalDecomSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  public void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(environmentalDecomSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenTaskNotShown() {
    assertThat(environmentalDecomSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseSection_verifyServiceInteractions() {

    var environmentalDecommView = new EnvironmentalDecommissioningView(
        null, null, null, null, null, null, Set.of(), Set.of());
    when(padEnvironmentalDecommissioningService.getEnvironmentalDecommissioningView(pwaApplicationDetail)).thenReturn(environmentalDecommView);
    when(padEnvironmentalDecommissioningService.getAvailableQuestions(pwaApplicationDetail)).thenCallRealMethod();

    var appSummary = environmentalDecomSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(5);
    assertThat(appSummary.getTemplateModel()).contains(entry("environmentalDecommView", environmentalDecommView));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING.getDisplayName()));
    assertThat(appSummary.getTemplateModel()).containsKey("environmentalConditions");
    assertThat(appSummary.getTemplateModel()).containsKey("decommissioningConditions");
    assertThat(appSummary.getTemplateModel()).containsEntry("availableQuestions", padEnvironmentalDecommissioningService.getAvailableQuestions(pwaApplicationDetail));

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING.getDisplayName(), "#environmentalDecommDetails")
    );

  }


}