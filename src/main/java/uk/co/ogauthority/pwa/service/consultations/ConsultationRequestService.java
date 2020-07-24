package uk.co.ogauthority.pwa.service.consultations;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationRequestValidator;

@Service
public class ConsultationRequestService {


  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final ConsultationRequestRepository consultationRequestRepository;
  private final ConsultationRequestValidator consultationRequestValidator;
  private final CamundaWorkflowService camundaWorkflowService;

  @Autowired
  public ConsultationRequestService(
      ConsulteeGroupTeamService consulteeGroupTeamService,
      ConsultationRequestRepository consultationRequestRepository,
      ConsultationRequestValidator consultationRequestValidator,
      CamundaWorkflowService camundaWorkflowService) {
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.consultationRequestRepository = consultationRequestRepository;
    this.consultationRequestValidator = consultationRequestValidator;
    this.camundaWorkflowService = camundaWorkflowService;
  }



  public List<ConsulteeGroupDetail> getConsulteeGroups(AuthenticatedUserAccount user) {
    return consulteeGroupTeamService.getManageableGroupDetailsForUser(user);
  }


  public void saveEntitiesAndStartWorkflow(ConsultationRequestForm form,
                                           PwaApplicationDetail applicationDetail, AuthenticatedUserAccount user) {
    for (var selectedGroupId: form.getConsulteeGroupSelection().keySet()) {
      var consultationRequest = new ConsultationRequest();
      consultationRequest.setConsulteeGroupDetail(
          consulteeGroupTeamService.getConsulteeGroupDetailById(Integer.parseInt(selectedGroupId)));
      consultationRequest.setOtherGroupSelected(false);
      setConsultationRequestInfo(consultationRequest, form, applicationDetail, user);
      consultationRequest = consultationRequestRepository.save(consultationRequest);
      camundaWorkflowService.startWorkflow(consultationRequest);
    }

    if (BooleanUtils.isTrue(form.getOtherGroupSelected())) {
      var consultationRequest = new ConsultationRequest();
      consultationRequest.setOtherGroupSelected(true);
      consultationRequest.setOtherGroupLogin(form.getOtherGroupLogin());
      setConsultationRequestInfo(consultationRequest, form, applicationDetail, user);
      consultationRequestRepository.save(consultationRequest);
      camundaWorkflowService.startWorkflow(consultationRequest);
    }
  }

  private void setConsultationRequestInfo(ConsultationRequest consultationRequest, ConsultationRequestForm form,
                                          PwaApplicationDetail applicationDetail, AuthenticatedUserAccount user) {
    consultationRequest.setPwaApplicationDetail(applicationDetail);
    consultationRequest.setStartTimestamp(Instant.now());
    consultationRequest.setStartedByPersonId(user.getLinkedPerson().getId().asInt());
    consultationRequest.setDeadlineDate(
        consultationRequest.getStartTimestamp().plus(Period.ofDays(form.getDaysToRespond())));
  }



  public BindingResult validate(ConsultationRequestForm form, BindingResult bindingResult,
                                ValidationType validationType, PwaApplicationDetail applicationDetail) {
    consultationRequestValidator.validate(form, bindingResult, this, consulteeGroupTeamService);
    return bindingResult;
  }


  public boolean isConsultationRequestOpen(ConsulteeGroupDetail consulteeGroupDetail) {
    return consultationRequestRepository.findByConsulteeGroupDetail(consulteeGroupDetail).isPresent();
  }



}
