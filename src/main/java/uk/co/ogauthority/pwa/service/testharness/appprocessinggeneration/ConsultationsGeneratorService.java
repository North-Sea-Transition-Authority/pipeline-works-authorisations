package uk.co.ogauthority.pwa.service.testharness.appprocessinggeneration;

import java.util.Map;
import java.util.stream.StreamSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.repository.appprocessing.consultations.consultees.ConsulteeGroupTeamMemberRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.consultations.AssignResponderService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessUserRetrievalService;

@Service
@Profile("test-harness")
class ConsultationsGeneratorService implements TestHarnessAppProcessingService {

  private final ConsultationRequestService consultationRequestService;
  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final AssignResponderService assignResponderService;
  private final ConsultationResponseService consultationResponseService;
  private final ConsulteeGroupTeamMemberRepository groupTeamMemberRepository;
  private final TestHarnessUserRetrievalService testHarnessUserRetrievalService;


  private static final PwaAppProcessingTask LINKED_APP_PROCESSING_TASK = PwaAppProcessingTask.CONSULTATIONS;

  @Autowired
  public ConsultationsGeneratorService(
      ConsultationRequestService consultationRequestService,
      ConsulteeGroupDetailService consulteeGroupDetailService,
      AssignResponderService assignResponderService,
      ConsultationResponseService consultationResponseService,
      ConsulteeGroupTeamMemberRepository groupTeamMemberRepository,
      TestHarnessUserRetrievalService testHarnessUserRetrievalService) {
    this.consultationRequestService = consultationRequestService;
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.assignResponderService = assignResponderService;
    this.consultationResponseService = consultationResponseService;
    this.groupTeamMemberRepository = groupTeamMemberRepository;
    this.testHarnessUserRetrievalService = testHarnessUserRetrievalService;
  }


  @Override
  public PwaAppProcessingTask getLinkedAppProcessingTask() {
    return LINKED_APP_PROCESSING_TASK;
  }


  @Override
  public void generateAppProcessingTaskData(TestHarnessAppProcessingProperties appProcessingProps) {

    var consulteeMember = getAvailableConsulteeMember();

    createConsultationRequest(appProcessingProps, consulteeMember);
    var consultationRequest = getActiveConsultationRequest(appProcessingProps);

    var consultationResponder = assignResponder(consultationRequest, consulteeMember);
    respondOnConsultationRequest(consultationRequest, consultationResponder);
  }


  private ConsulteeGroupTeamMember getAvailableConsulteeMember() {
    //basing the consultation request on the consultee group of the first consultee member we find with the recipient role
    return StreamSupport.stream(groupTeamMemberRepository.findAll().spliterator(), false)
        .filter(member -> member.getRoles().contains(ConsulteeGroupMemberRole.RECIPIENT))
        .findFirst().orElseThrow(() -> new PwaEntityNotFoundException("Could not find any consultee group member recipients"));
  }

  private ConsultationRequest getActiveConsultationRequest(TestHarnessAppProcessingProperties appProcessingProps) {
    //only 1 request should have been made by the test harness
    return consultationRequestService.getAllOpenRequestsByApplication(appProcessingProps.getPwaApplication())
        .stream().findFirst().orElseThrow(() -> new PwaEntityNotFoundException(String.format(
            "Could not find open consultation request for application with id: %s", appProcessingProps.getPwaApplication().getId())));
  }



  private void createConsultationRequest(TestHarnessAppProcessingProperties appProcessingProps, ConsulteeGroupTeamMember consulteeMember) {

    var consulteeGroupDetail = consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(
        consulteeMember.getConsulteeGroup());

    var form = new ConsultationRequestForm();
    form.setConsulteeGroupSelection(Map.of(String.valueOf(consulteeGroupDetail.getId()), consulteeGroupDetail.getName()));
    form.setDaysToRespond(28);

    consultationRequestService.saveEntitiesAndStartWorkflow(
        form, appProcessingProps.getPwaApplicationDetail(), appProcessingProps.getCaseOfficerAua());
  }


  private ConsulteeGroupTeamMember assignResponder(ConsultationRequest consultationRequest, ConsulteeGroupTeamMember recipient) {

    var responder = groupTeamMemberRepository.findAllByConsulteeGroup(consultationRequest.getConsulteeGroup())
        .stream()
        .filter(member -> member.getRoles().contains(ConsulteeGroupMemberRole.RESPONDER))
        .findFirst().orElseThrow(() -> new PwaEntityNotFoundException(String.format(
            "Could not find any consultee group member responders for consultation request with id: %s", consultationRequest.getId())));

    var form = new AssignResponderForm();
    form.setResponderPersonId(responder.getPerson().getId().asInt());

    assignResponderService.assignResponder(
        form, consultationRequest, testHarnessUserRetrievalService.getWebUserAccount(recipient.getPerson().getId().asInt()));

    return responder;
  }


  private void respondOnConsultationRequest(ConsultationRequest consultationRequest, ConsulteeGroupTeamMember consultationResponder) {

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOptionGroup(ConsultationResponseOptionGroup.CONTENT);
    form.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);
    form.setOption1Description("My response description");
    form.setOption2Description("My response description");

    consultationResponseService.saveResponseAndCompleteWorkflow(
        form, consultationRequest, testHarnessUserRetrievalService.getWebUserAccount(consultationResponder.getPerson().getId().asInt()));
  }


}
