package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import java.util.List;

public class PreSendForApprovalChecksViewTestUtil {

  private PreSendForApprovalChecksViewTestUtil() {
    throw new UnsupportedOperationException("no util for you!");
  }

  public static PreSendForApprovalChecksView createNoFailedChecksView(){
    return new PreSendForApprovalChecksView(List.of(), List.of());
  }

  public static PreSendForApprovalChecksView createFailedChecksView(){
    var failCheck = FailedSendForApprovalCheckTestUtil.create(SendConsentForApprovalRequirement.DOCUMENT_HAS_CLAUSES);
    return new PreSendForApprovalChecksView(List.of(failCheck), List.of());
  }

}