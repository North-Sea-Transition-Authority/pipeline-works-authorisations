package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTaskService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingTypesService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.CrossingTypesView;
import uk.co.ogauthority.pwa.service.tasklist.CrossingTaskGeneralPurposeTaskAdapter;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class CrossingTypesSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private CrossingTypesService crossingTypesService;

  @Mock
  private ApplicationTaskService applicationTaskService;
  
  @Captor 
  private  ArgumentCaptor<CrossingTaskGeneralPurposeTaskAdapter> taskAdapterArgumentCaptor;
  private CrossingTypesSummaryService crossingTypesSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;


  @Before
  public void setUp() {

    crossingTypesSummaryService = new CrossingTypesSummaryService(
        crossingTypesService,
        taskListService,
        applicationTaskService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }

  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);

    crossingTypesSummaryService.canSummarise(pwaApplicationDetail);

    verify(taskListService, times(1)).anyTaskShownForApplication(
        Set.of(ApplicationTask.CROSSING_AGREEMENTS), pwaApplicationDetail
    );

    verify(applicationTaskService, times(1)).canShowTask(taskAdapterArgumentCaptor.capture(), eq(pwaApplicationDetail));

    assertThat(taskAdapterArgumentCaptor.getValue().getCrossingAgreementTask()).isEqualTo(CrossingAgreementTask.CROSSING_TYPES);

  }

  @Test
  public void canSummarise_whenHasCrossingsTaskShown_andCrossingTypesSectionShown() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    when(applicationTaskService.canShowTask(any(), any())).thenReturn(true);
    assertThat(crossingTypesSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenHasCrossingsTaskShown_andCrossingTypesSectionNotShown() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    when(applicationTaskService.canShowTask(any(), any())).thenReturn(false);
    assertThat(crossingTypesSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void canSummarise_whenCrossingTaskNotShown() {
    assertThat(crossingTypesSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseSection_verifyServiceInteractions() {

    var crossingTypesView = new CrossingTypesView(null, null, null);
    when(crossingTypesService.getCrossingTypesView(pwaApplicationDetail)).thenReturn(crossingTypesView);

    var appSummary = crossingTypesSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(2);
    assertThat(appSummary.getTemplateModel()).contains(entry("crossingTypesView", crossingTypesView));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", CrossingAgreementTask.CROSSING_TYPES.getDisplayText()));

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(CrossingAgreementTask.CROSSING_TYPES.getDisplayText(), "#crossingTypeDetails")
    );

  }


}