package uk.co.ogauthority.pwa.service.appprocessing.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.withdraw.WithdrawApplicationForm;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.validators.WithdrawApplicationValidator;

@Service
public class WithdrawApplicationService implements AppProcessingService {

  private final WithdrawApplicationValidator withdrawApplicationValidator;

  @Autowired
  public WithdrawApplicationService(
      WithdrawApplicationValidator withdrawApplicationValidator) {
    this.withdrawApplicationValidator = withdrawApplicationValidator;
  }


  public void withdrawApplication(WithdrawApplicationForm form, PwaApplicationDetail pwaApplicationDetail) {

  }


  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.WITHDRAW_APPLICATION);
  }

  public BindingResult validate(Object form, BindingResult bindingResult,
                                PwaApplicationDetail pwaApplicationDetail) {
    withdrawApplicationValidator.validate(form, bindingResult, pwaApplicationDetail);
    return bindingResult;
  }

}
