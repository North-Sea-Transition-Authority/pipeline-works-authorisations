package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView;
import uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateSummaryView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationUpdateSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  private static final PersonId PERSON_ID = new PersonId(1);

  @Mock
  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  @Mock
  private PersonService personService;

  @Mock
  private ApplicationUpdateRequestView applicationUpdateRequestView;

  private ApplicationUpdateSummaryService applicationUpdateSummaryService;

  private PwaApplicationDetail pwaApplicationDetail;

  private Person person;

  @Before
  public void setUp() throws Exception {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    person = PersonTestUtil.createPersonFrom(PERSON_ID);

    when(applicationUpdateRequestView.getResponseByPersonId()).thenReturn(PERSON_ID);

    when(personService.getPersonById(PERSON_ID)).thenReturn(person);

    when(applicationUpdateRequestViewService.getLastRespondedApplicationUpdateView(pwaApplicationDetail))
        .thenReturn(Optional.of(applicationUpdateRequestView));

    applicationUpdateSummaryService = new ApplicationUpdateSummaryService(
        applicationUpdateRequestViewService,
        personService
    );
  }

  @Test
  public void canSummarise_whenPreviousUpdateResponseFound() {
    assertThat(applicationUpdateSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenPreviousUpdateResponseNotFound() {

    when(applicationUpdateRequestViewService.getLastRespondedApplicationUpdateView(pwaApplicationDetail))
        .thenReturn(Optional.empty());
    assertThat(applicationUpdateSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseSection_verifyServiceInterations() {
    var appSummary = applicationUpdateSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);

    verify(personService, times(1)).getPersonById(PERSON_ID);

    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(2);
    assertThat(appSummary.getTemplateModel()).containsKey("appUpdateSummaryView");
    assertThat(appSummary.getTemplateModel()).contains(
        entry("sectionDisplayText", ApplicationUpdateSummaryService.SECTION_NAME)
    );

    var summaryView =(ApplicationUpdateSummaryView) appSummary.getTemplateModel().get("appUpdateSummaryView");
    assertThat(summaryView.getResponseByPersonName()).isEqualTo(person.getFullName());

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(
            ApplicationUpdateSummaryService.SECTION_NAME,
            "#previousAppUpdateSection"
        )
    );


  }
}