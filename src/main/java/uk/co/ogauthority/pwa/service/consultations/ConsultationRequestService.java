package uk.co.ogauthority.pwa.service.consultations;

import java.time.Instant;
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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationRequestValidationHints;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationRequestValidator;

@Service
public class ConsultationRequestService {

  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final ConsultationRequestRepository consultationRequestRepository;
  private final ConsultationRequestValidator consultationRequestValidator;
  private final CamundaWorkflowService camundaWorkflowService;

  @Autowired
  public ConsultationRequestService(
      ConsulteeGroupDetailService consulteeGroupDetailService,
      ConsultationRequestRepository consultationRequestRepository,
      ConsultationRequestValidator consultationRequestValidator,
      CamundaWorkflowService camundaWorkflowService) {
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.consultationRequestRepository = consultationRequestRepository;
    this.consultationRequestValidator = consultationRequestValidator;
    this.camundaWorkflowService = camundaWorkflowService;
  }



  public List<ConsulteeGroupDetail> getConsulteeGroups(AuthenticatedUserAccount user) {
    return consulteeGroupDetailService.getAllConsulteeGroupDetails();
  }


  public void saveEntitiesAndStartWorkflow(ConsultationRequestForm form,
                                           PwaApplicationDetail applicationDetail, AuthenticatedUserAccount user) {
    for (var selectedGroupId: form.getConsulteeGroupSelection().keySet()) {
      var consultationRequest = new ConsultationRequest();
      consultationRequest.setConsulteeGroup(
          consulteeGroupDetailService.getConsulteeGroupDetailById(Integer.parseInt(selectedGroupId)).getConsulteeGroup());
      consultationRequest.setPwaApplication(applicationDetail.getPwaApplication());
      consultationRequest.setStartTimestamp(Instant.now());
      consultationRequest.setStartedByPersonId(user.getLinkedPerson().getId().asInt());
      consultationRequest.setDeadlineDate(
          consultationRequest.getStartTimestamp().plus(Period.ofDays(form.getDaysToRespond())));

      consultationRequest = consultationRequestRepository.save(consultationRequest);
      camundaWorkflowService.startWorkflow(consultationRequest);
    }
  }


  public void rebindFormCheckboxes(ConsultationRequestForm form) {
    for (var entry: form.getConsulteeGroupSelection().entrySet()) {
      entry.setValue("true");
    }
  }



  public BindingResult validate(ConsultationRequestForm form, BindingResult bindingResult, PwaApplication pwaApplication) {
    consultationRequestValidator.validate(form, bindingResult,
        new ConsultationRequestValidationHints(this, consulteeGroupDetailService, pwaApplication));
    return bindingResult;
  }


  public boolean isConsultationRequestOpen(ConsulteeGroup consulteeGroup, PwaApplication pwaApplication) {
    return consultationRequestRepository.findByConsulteeGroupAndPwaApplication(consulteeGroup, pwaApplication).isPresent();
  }



}
