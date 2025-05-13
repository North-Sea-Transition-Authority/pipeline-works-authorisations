package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appprocessinggeneration;

import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.TestHarnessUserRetrievalService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseDataForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.consultations.AssignResponderService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.UserTeamRolesView;

@Service
@Profile("test-harness")
class ConsultationsGeneratorService implements TestHarnessAppProcessingService {

  private final ConsultationRequestService consultationRequestService;
  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final AssignResponderService assignResponderService;
  private final ConsultationResponseService consultationResponseService;
  private final TestHarnessUserRetrievalService testHarnessUserRetrievalService;


  private static final PwaAppProcessingTask LINKED_APP_PROCESSING_TASK = PwaAppProcessingTask.CONSULTATIONS;
  private final TeamQueryService teamQueryService;
  private final UserAccountService userAccountService;

  @Autowired
  ConsultationsGeneratorService(
      ConsultationRequestService consultationRequestService,
      ConsulteeGroupDetailService consulteeGroupDetailService,
      AssignResponderService assignResponderService,
      ConsultationResponseService consultationResponseService,
      TestHarnessUserRetrievalService testHarnessUserRetrievalService,
      TeamQueryService teamQueryService,
      UserAccountService userAccountService) {
    this.consultationRequestService = consultationRequestService;
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.assignResponderService = assignResponderService;
    this.consultationResponseService = consultationResponseService;
    this.testHarnessUserRetrievalService = testHarnessUserRetrievalService;
    this.teamQueryService = teamQueryService;
    this.userAccountService = userAccountService;
  }

  @Override
  public PwaAppProcessingTask getLinkedAppProcessingTask() {
    return LINKED_APP_PROCESSING_TASK;
  }


  @Override
  public void generateAppProcessingTaskData(TestHarnessAppProcessingProperties appProcessingProps) {

    var consulteeMember = getAvailableConsulteeMember();

    var consulteeGroupId = Integer.valueOf(consulteeMember.teamScopeId());
    var consulteeGroupDetail = consulteeGroupDetailService.getConsulteeGroupDetailByGroupIdAndTipFlagIsTrue(consulteeGroupId);

    createConsultationRequest(appProcessingProps, consulteeGroupDetail.getConsulteeGroup());
    var consultationRequest = getActiveConsultationRequest(appProcessingProps);

    WebUserAccount assigningUser = testHarnessUserRetrievalService.getWebUserAccount(consulteeMember.wuaId().intValue());
    var consultationResponderUser = assignResponder(consultationRequest, assigningUser);

    respondOnConsultationRequest(consultationRequest, consultationResponderUser);
  }

  UserTeamRolesView getAvailableConsulteeMember() {
    //basing the consultation request on the consultee group of the first consultee member we find with the recipient role
    return teamQueryService.getUsersOfTeamTypeWithRoleIn(TeamType.CONSULTEE, Set.of(Role.RECIPIENT)).stream()
        .findFirst()
        .orElseThrow(() -> new PwaEntityNotFoundException("Could not find any consultee group member recipients"));
  }

  ConsultationRequest getActiveConsultationRequest(TestHarnessAppProcessingProperties appProcessingProps) {
    //only 1 request should have been made by the test harness
    return consultationRequestService.getAllOpenRequestsByApplication(appProcessingProps.getPwaApplication())
        .stream().findFirst().orElseThrow(() -> new PwaEntityNotFoundException(String.format(
            "Could not find open consultation request for application with id: %s", appProcessingProps.getPwaApplication().getId())));
  }

  void createConsultationRequest(TestHarnessAppProcessingProperties appProcessingProps, ConsulteeGroup consulteeGroup) {

    var consulteeGroupDetail = consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consulteeGroup);

    var form = new ConsultationRequestForm();
    form.setConsulteeGroupSelection(Map.of(String.valueOf(consulteeGroupDetail.getId()), consulteeGroupDetail.getName()));
    form.setDaysToRespond(28);

    consultationRequestService.saveEntitiesAndStartWorkflow(
        form, appProcessingProps.getPwaApplicationDetail(), appProcessingProps.getCaseOfficerAua());
  }

  WebUserAccount assignResponder(ConsultationRequest consultationRequest, WebUserAccount assigningUser) {

    var teamType = TeamType.CONSULTEE;
    var teamScopeReference = TeamScopeReference.from(consultationRequest.getConsulteeGroup().getId(), teamType);
    var responderUser = teamQueryService.getUsersOfScopedTeamWithRoleIn(teamType, teamScopeReference, Set.of(Role.RESPONDER))
        .stream()
        .findFirst()
        .map(teamMemberView -> userAccountService.getWebUserAccount(teamMemberView.wuaId().intValue()))
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format(
            "Could not find any consultee group member responders for consultation request with id: %s", consultationRequest.getId())));

    var form = new AssignResponderForm();
    form.setResponderPersonId(responderUser.getLinkedPerson().getId().asInt());

    assignResponderService.assignResponder(form, consultationRequest, assigningUser);

    return responderUser;
  }

  void respondOnConsultationRequest(ConsultationRequest consultationRequest, WebUserAccount respondingUser) {

    var dataForm = new ConsultationResponseDataForm();
    dataForm.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);
    dataForm.setOption1Description("My response description");
    dataForm.setOption2Description("My response description");
    dataForm.setOption3Description("My response description");

    var form = new ConsultationResponseForm();
    form.setResponseDataForms(Map.of(ConsultationResponseOptionGroup.CONTENT, dataForm));

    consultationResponseService.saveResponseAndCompleteWorkflow(
        form, consultationRequest, respondingUser);
  }
}
