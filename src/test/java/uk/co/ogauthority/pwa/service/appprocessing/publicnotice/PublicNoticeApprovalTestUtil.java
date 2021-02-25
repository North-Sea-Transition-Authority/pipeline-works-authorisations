package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeApprovalForm;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationPublicNoticeApprovalResult;

public final class PublicNoticeApprovalTestUtil {


  private PublicNoticeApprovalTestUtil(){}



  static PublicNoticeApprovalForm createApprovedPublicNoticeForm() {
    var publicNoticeApprovalForm = new PublicNoticeApprovalForm();
    publicNoticeApprovalForm.setRequestApproved(PwaApplicationPublicNoticeApprovalResult.REQUEST_APPROVED);
    return publicNoticeApprovalForm;
  }

  static PublicNoticeApprovalForm createRejectedPublicNoticeForm() {
    var publicNoticeApprovalForm = new PublicNoticeApprovalForm();
    publicNoticeApprovalForm.setRequestApproved(PwaApplicationPublicNoticeApprovalResult.REQUEST_REJECTED);
    publicNoticeApprovalForm.setRequestRejectedReason("reason");
    return publicNoticeApprovalForm;
  }








}
