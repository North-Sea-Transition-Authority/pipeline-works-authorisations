package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.validators.publicnotice.FinalisePublicNoticeValidator;

@Service
public class FinalisePublicNoticeService {


  private final PublicNoticeService publicNoticeService;
  private final FinalisePublicNoticeValidator finalisePublicNoticeValidator;
  private final CamundaWorkflowService camundaWorkflowService;


  @Autowired
  public FinalisePublicNoticeService(
      PublicNoticeService publicNoticeService,
      FinalisePublicNoticeValidator finalisePublicNoticeValidator,
      CamundaWorkflowService camundaWorkflowService) {
    this.publicNoticeService = publicNoticeService;
    this.finalisePublicNoticeValidator = finalisePublicNoticeValidator;
    this.camundaWorkflowService = camundaWorkflowService;
  }




  public boolean publicNoticeCanBeFinalised(PwaApplication pwaApplication) {
    return publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)
        .stream()
        .anyMatch(publicNotice -> publicNotice.getPwaApplication().equals(pwaApplication));
  }


  public BindingResult validate(FinalisePublicNoticeForm form, BindingResult bindingResult) {
    finalisePublicNoticeValidator.validate(form, bindingResult);
    return bindingResult;
  }



  @Transactional
  public void finalisePublicNotice(PwaApplication pwaApplication,
                                   FinalisePublicNoticeForm form,
                                   AuthenticatedUserAccount authenticatedUserAccount) {

    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);


  }


}
