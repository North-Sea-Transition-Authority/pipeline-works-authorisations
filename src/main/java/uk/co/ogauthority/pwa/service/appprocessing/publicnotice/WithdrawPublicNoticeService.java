package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.publicnotice.WithdrawPublicNoticeForm;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.validators.publicnotice.WithdrawPublicNoticeValidator;

@Service
public class WithdrawPublicNoticeService {


  private final PublicNoticeService publicNoticeService;
  private final WithdrawPublicNoticeValidator withdrawPublicNoticeValidator;
  private final CamundaWorkflowService camundaWorkflowService;
  private final EmailCaseLinkService emailCaseLinkService;
  private final NotifyService notifyService;


  @Autowired
  public WithdrawPublicNoticeService(
      PublicNoticeService publicNoticeService,
      WithdrawPublicNoticeValidator withdrawPublicNoticeValidator,
      CamundaWorkflowService camundaWorkflowService,
      EmailCaseLinkService emailCaseLinkService, NotifyService notifyService) {
    this.publicNoticeService = publicNoticeService;
    this.withdrawPublicNoticeValidator = withdrawPublicNoticeValidator;
    this.camundaWorkflowService = camundaWorkflowService;
    this.emailCaseLinkService = emailCaseLinkService;
    this.notifyService = notifyService;
  }




  public boolean publicNoticeCanBeWithdrawn(PwaApplication pwaApplication) {
    return publicNoticeService.getOpenPublicNotices()
        .stream()
        .anyMatch(publicNotice -> publicNotice.getPwaApplication().equals(pwaApplication));
  }


  public BindingResult validate(WithdrawPublicNoticeForm form, BindingResult bindingResult) {
    withdrawPublicNoticeValidator.validate(form, bindingResult);
    return bindingResult;
  }



  @Transactional
  public void withdrawPublicNotice(PwaApplication pwaApplication,
                                   WithdrawPublicNoticeForm form,
                                   AuthenticatedUserAccount authenticatedUserAccount) {

    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
  }


}
