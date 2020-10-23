package uk.co.ogauthority.pwa.service.appprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationInvolvementServiceTest {

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private UserTypeService userTypeService;

  private ApplicationInvolvementService applicationInvolvementService;

  private PwaApplication application;
  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {

    applicationInvolvementService = new ApplicationInvolvementService(consulteeGroupTeamService, pwaContactService, consultationRequestService, camundaWorkflowService, userTypeService);

    application = new PwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, new Person(1, null, null, null, null)), Set.of());

  }

  @Test
  public void getApplicationInvolvementDto_industryUser_isContact_onlyRelevantInteractionsAndDataPopulated() {

    when(userTypeService.getUserType(user)).thenReturn(UserType.INDUSTRY);
    when(pwaContactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of(PwaContactRole.PREPARER));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    verifyNoInteractions(consulteeGroupTeamService, camundaWorkflowService, consultationRequestService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).containsExactly(PwaContactRole.PREPARER);
    assertThat(involvement.getConsulteeRoles()).isEmpty();
    assertThat(involvement.isCaseOfficerStageAndUserAssigned()).isFalse();

  }

  @Test
  public void getApplicationInvolvementDto_industryUser_notContact_onlyRelevantInteractionsAndDataPopulated() {

    when(userTypeService.getUserType(user)).thenReturn(UserType.INDUSTRY);
    when(pwaContactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of());

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    verifyNoInteractions(consulteeGroupTeamService, camundaWorkflowService, consultationRequestService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsulteeRoles()).isEmpty();
    assertThat(involvement.isCaseOfficerStageAndUserAssigned()).isFalse();

  }

  @Test
  public void getApplicationInvolvementDto_regulatorUser_assignedCo_onlyRelevantInteractionsAndDataPopulated() {

    when(userTypeService.getUserType(user)).thenReturn(UserType.OGA);
    when(camundaWorkflowService.getAssignedPersonId(any())).thenReturn(Optional.of(new PersonId(1)));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    verifyNoInteractions(consulteeGroupTeamService, pwaContactService, consultationRequestService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsulteeRoles()).isEmpty();
    assertThat(involvement.isCaseOfficerStageAndUserAssigned()).isTrue();

  }

  @Test
  public void getApplicationInvolvementDto_regulatorUser_notAssignedCo_onlyRelevantInteractionsAndDataPopulated() {

    when(userTypeService.getUserType(user)).thenReturn(UserType.OGA);
    when(camundaWorkflowService.getAssignedPersonId(any())).thenReturn(Optional.empty());

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    verifyNoInteractions(consulteeGroupTeamService, pwaContactService, consultationRequestService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsulteeRoles()).isEmpty();
    assertThat(involvement.isCaseOfficerStageAndUserAssigned()).isFalse();

  }

  @Test
  public void getApplicationInvolvementDto_consulteeUser_notConsulted_onlyRelevantInteractionsAndDataPopulated() {

    var groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("name", "abb");

    when(userTypeService.getUserType(user)).thenReturn(UserType.CONSULTEE);
    when(consulteeGroupTeamService.getTeamMemberByPerson(any())).thenReturn(Optional.of(
        new ConsulteeGroupTeamMember(
            groupDetail.getConsulteeGroup(),
            new Person(2, null, null, null, null),
            Set.of(ConsulteeGroupMemberRole.RECIPIENT))
    ));
    when(consultationRequestService.getAllRequestsByApplication(application)).thenReturn(List.of());

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    verifyNoInteractions(camundaWorkflowService, pwaContactService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsulteeRoles()).isEmpty();
    assertThat(involvement.isCaseOfficerStageAndUserAssigned()).isFalse();

  }

  @Test
  public void getApplicationInvolvementDto_consulteeUser_wasConsulted_onlyRelevantInteractionsAndDataPopulated() {

    var groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("name", "abb");

    when(userTypeService.getUserType(user)).thenReturn(UserType.CONSULTEE);
    when(consulteeGroupTeamService.getTeamMemberByPerson(any())).thenReturn(Optional.of(
        new ConsulteeGroupTeamMember(
            groupDetail.getConsulteeGroup(),
            new Person(2, null, null, null, null),
            Set.of(ConsulteeGroupMemberRole.RECIPIENT))
    ));

    var request = new ConsultationRequest();
    request.setConsulteeGroup(groupDetail.getConsulteeGroup());

    when(consultationRequestService.getAllRequestsByApplication(application)).thenReturn(List.of(request));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    verifyNoInteractions(camundaWorkflowService, pwaContactService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsulteeRoles()).containsExactly(ConsulteeGroupMemberRole.RECIPIENT);
    assertThat(involvement.isCaseOfficerStageAndUserAssigned()).isFalse();

  }



}
