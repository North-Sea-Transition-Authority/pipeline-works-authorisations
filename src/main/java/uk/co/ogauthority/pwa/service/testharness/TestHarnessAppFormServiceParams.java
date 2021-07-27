package uk.co.ogauthority.pwa.service.testharness;

import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Wrapper class used to contain the parameters needed for generating app forms used by implementors of TestHarnessAppFormService.
 * This wrapper class is needed to allow calling the same method with different params as not all parameters are always used.
 */
public class TestHarnessAppFormServiceParams {

  private final WebUserAccount user;
  private final PwaApplicationDetail applicationDetail;
  private final int pipelineQuantity;

  TestHarnessAppFormServiceParams(WebUserAccount user,
                                  PwaApplicationDetail applicationDetail,
                                  int pipelineQuantity) {
    this.user = user;
    this.applicationDetail = applicationDetail;
    this.pipelineQuantity = pipelineQuantity;
  }


  public WebUserAccount getUser() {
    return user;
  }

  public PwaApplicationDetail getApplicationDetail() {
    return applicationDetail;
  }

  public int getPipelineQuantity() {
    return pipelineQuantity;
  }


}
