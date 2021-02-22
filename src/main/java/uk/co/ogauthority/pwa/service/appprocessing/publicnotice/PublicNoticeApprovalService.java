package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeApprovalForm;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeApprovalValidator;

@Service
public class PublicNoticeApprovalService {


  private final PublicNoticeApprovalValidator publicNoticeApprovalValidator;

  @Autowired
  public PublicNoticeApprovalService(
      PublicNoticeApprovalValidator publicNoticeApprovalValidator) {
    this.publicNoticeApprovalValidator = publicNoticeApprovalValidator;
  }




  public void updatePublicNoticeRequest(PublicNoticeApprovalForm form, PwaApplication pwaApplication) {

  }


  public BindingResult validate(PublicNoticeApprovalForm form, BindingResult bindingResult) {
    publicNoticeApprovalValidator.validate(form, bindingResult);
    return bindingResult;
  }






}
