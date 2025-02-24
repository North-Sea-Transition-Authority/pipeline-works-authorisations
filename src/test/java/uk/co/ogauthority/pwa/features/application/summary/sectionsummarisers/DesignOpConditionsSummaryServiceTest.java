package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.DesignOpConditionsView;
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.PadDesignOpConditions;
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.PadDesignOpConditionsService;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class DesignOpConditionsSummaryServiceTest {



  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadDesignOpConditionsService padDesignOpConditionsService;

  private DesignOpConditionsSummaryService designOpConditionsSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {

    designOpConditionsSummaryService = new DesignOpConditionsSummaryService(padDesignOpConditionsService, taskListService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(designOpConditionsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(designOpConditionsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenTaskNotShown() {
    assertThat(designOpConditionsSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void summariseSection_verifyServiceInteractions() {
    var designOpsConditions = new PadDesignOpConditions();
    designOpsConditions.setParent(pwaApplicationDetail);
    var designOpConditionsView = new DesignOpConditionsView(designOpsConditions);
    when(padDesignOpConditionsService.getDesignOpConditionsView(pwaApplicationDetail)).thenReturn(designOpConditionsView);

    var appSummary = designOpConditionsSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(3);
    assertThat(appSummary.getTemplateModel()).contains(entry("designOpConditionsView", designOpConditionsView));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.DESIGN_OP_CONDITIONS.getDisplayName()));
    assertThat(appSummary.getTemplateModel()).contains(entry("unitMeasurements", UnitMeasurement.toMap()));

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.DESIGN_OP_CONDITIONS.getDisplayName(), "#designOpConditionsDetails")
    );

  }


}
