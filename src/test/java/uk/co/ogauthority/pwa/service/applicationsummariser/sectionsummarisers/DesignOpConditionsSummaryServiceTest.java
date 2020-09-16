package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadDesignOpConditions;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.DesignOpConditionsView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadDesignOpConditionsService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class DesignOpConditionsSummaryServiceTest {



  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadDesignOpConditionsService padDesignOpConditionsService;

  private DesignOpConditionsSummaryService designOpConditionsSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {

    designOpConditionsSummaryService = new DesignOpConditionsSummaryService(padDesignOpConditionsService, taskListService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(designOpConditionsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  public void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(designOpConditionsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenTaskNotShown() {
    assertThat(designOpConditionsSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseSection_verifyServiceInteractions() {

    var designOpConditionsView = new DesignOpConditionsView(new PadDesignOpConditions());
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