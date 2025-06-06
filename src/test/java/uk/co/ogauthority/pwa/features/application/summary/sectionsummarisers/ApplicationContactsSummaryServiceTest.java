package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContact;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.ContactTeamMemberView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationContactsSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PwaContactService pwaContactService;

  private ApplicationContactsSummaryService applicationContactsSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {
    applicationContactsSummaryService = new ApplicationContactsSummaryService(
        taskListService,
        pwaContactService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);


  }

  @Test
  void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(applicationContactsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(applicationContactsSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenTaskNotShown() {
    assertThat(applicationContactsSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void summariseSection_verifyServiceInteractions() {

    when(pwaContactService.getContactsForPwaApplication(pwaApplicationDetail.getPwaApplication())).thenReturn(List.of(new PwaContact()));

    var appSummary = applicationContactsSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);

    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).containsKey("contactTeamMemberViews");
    assertThat(((List<ContactTeamMemberView>) appSummary.getTemplateModel().get("contactTeamMemberViews")).size()).isEqualTo(1);
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.APPLICATION_USERS.getDisplayName()));
    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.APPLICATION_USERS.getDisplayName(), "#appContactDetails")
    );

  }


}