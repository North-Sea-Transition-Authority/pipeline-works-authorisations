package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval;

public final class FailedSendForApprovalCheckTestUtil {

  private FailedSendForApprovalCheckTestUtil(){
    throw new UnsupportedOperationException("no util for you!");
  }

  public static FailedSendForApprovalCheck create(SendConsentForApprovalRequirement sendConsentForApprovalRequirement){
    return new FailedSendForApprovalCheck(sendConsentForApprovalRequirement);

  }
}