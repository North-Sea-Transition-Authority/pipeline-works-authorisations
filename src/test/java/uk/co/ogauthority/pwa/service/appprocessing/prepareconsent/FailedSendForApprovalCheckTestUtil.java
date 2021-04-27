package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

public final class FailedSendForApprovalCheckTestUtil {

  private FailedSendForApprovalCheckTestUtil(){
    throw new UnsupportedOperationException("no util for you!");
  }

  public static FailedSendForApprovalCheck create(SendConsentForApprovalRequirement sendConsentForApprovalRequirement){
    return new FailedSendForApprovalCheck(sendConsentForApprovalRequirement);

  }
}