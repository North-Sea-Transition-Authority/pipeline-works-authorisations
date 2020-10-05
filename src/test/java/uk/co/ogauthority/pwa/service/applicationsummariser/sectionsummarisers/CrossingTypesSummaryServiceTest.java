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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingTypesService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.CrossingTypesView;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class CrossingTypesSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private CrossingTypesService crossingTypesService;

  private CrossingTypesSummaryService crossingTypesSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;


  @Before
  public void setUp() {

    crossingTypesSummaryService = new CrossingTypesSummaryService(crossingTypesService, taskListService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(crossingTypesSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  public void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(crossingTypesSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenTaskNotShown() {
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