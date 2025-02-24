package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.LocationDetailsUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadLocationDetailsService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class LocationDetailsSummaryServiceTest {



  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadLocationDetailsService padLocationDetailsService;

  private LocationDetailsSummaryService locationDetailsSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {

    locationDetailsSummaryService = new LocationDetailsSummaryService(padLocationDetailsService, taskListService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(locationDetailsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(locationDetailsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenTaskNotShown() {
    assertThat(locationDetailsSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void summariseSection_verifyServiceInteractions() {

    var locationDetailsView = LocationDetailsSummaryServiceTestUtil.createLocationDetailsView();
    when(padLocationDetailsService.getLocationDetailsView(pwaApplicationDetail)).thenReturn(locationDetailsView);

    when(padLocationDetailsService.getRequiredQuestions(pwaApplicationDetail)).thenReturn(Set.of());

    var appSummary = locationDetailsSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(4);
    assertThat(appSummary.getTemplateModel()).contains(entry("locationDetailsView", locationDetailsView));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.LOCATION_DETAILS.getDisplayName()));
    assertThat(appSummary.getTemplateModel()).contains(entry("locationDetailsUrlFactory", new LocationDetailsUrlFactory(pwaApplicationDetail)));
    assertThat(appSummary.getTemplateModel()).contains(entry("requiredQuestions", Set.of()));


    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.LOCATION_DETAILS.getDisplayName(), "#locationDetails")
    );

  }


}
