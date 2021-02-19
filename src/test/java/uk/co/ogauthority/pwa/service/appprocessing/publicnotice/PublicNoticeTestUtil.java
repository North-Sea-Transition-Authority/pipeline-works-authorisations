package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeDocumentType;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.model.view.publicnotice.PublicNoticeView;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.util.DateUtils;

final class PublicNoticeTestUtil {

  private static int VERSION1 = 1;

  private PublicNoticeTestUtil(){}


  static PublicNotice createInitialPublicNotice(PwaApplication pwaApplication) {
    return new PublicNotice(pwaApplication, PublicNoticeStatus.DRAFT, VERSION1);
  }

  static PublicNotice createEndedPublicNotice(PwaApplication pwaApplication) {
    return new PublicNotice(pwaApplication, PublicNoticeStatus.WITHDRAWN, 10);
  }

  static PublicNoticeDocument createInitialPublicNoticeDocument(PublicNotice publicNotice) {
    return new PublicNoticeDocument(publicNotice, VERSION1, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT);
  }

  static AppFile createAppFileForPublicNotice(PwaApplication pwaApplication) {
    return new AppFile(pwaApplication, "fileID1", AppFilePurpose.PUBLIC_NOTICE, ApplicationFileLinkStatus.TEMPORARY);
  }

  static PublicNoticeDraftForm createDefaultPublicNoticeDraftForm() {
    var publicNoticeDraftForm = new PublicNoticeDraftForm();
    publicNoticeDraftForm.setCoverLetterText("my cover letter text");
    publicNoticeDraftForm.setReason(PublicNoticeRequestReason.CONSULTEES_NOT_ALL_CONTENT);
    publicNoticeDraftForm.setReasonDescription("my reason");
    return publicNoticeDraftForm;
  }

  static PublicNoticeDraftForm createDefaultPublicNoticeDraftForm(List<UploadFileWithDescriptionForm> uploadFileWithDescriptionForms) {
    var publicNoticeDraftForm = createDefaultPublicNoticeDraftForm();
    publicNoticeDraftForm.setUploadedFileWithDescriptionForms(uploadFileWithDescriptionForms);
    return publicNoticeDraftForm;
  }

  static PublicNoticeRequest createInitialPublicNoticeRequest(PublicNotice publicNotice, PublicNoticeDraftForm publicNoticeDraftForm) {
    var publicNoticeRequest = new PublicNoticeRequest();
    publicNoticeRequest.setPublicNotice(publicNotice);
    publicNoticeRequest.setCoverLetterText(publicNoticeDraftForm.getCoverLetterText());
    publicNoticeRequest.setStatus(PublicNoticeRequestStatus.WAITING_MANAGER_APPROVAL);
    publicNoticeRequest.setReason(publicNoticeDraftForm.getReason());
    publicNoticeRequest.setReasonDescription(publicNoticeDraftForm.getReasonDescription());
    publicNoticeRequest.setVersion(1);
    publicNoticeRequest.setSubmittedTimestamp(Instant.now());
    publicNoticeRequest.setCreatedByPersonId(1);
    return publicNoticeRequest;
  }

  static PublicNoticeRequest createInitialPublicNoticeRequest(PublicNotice publicNotice) {
    return createInitialPublicNoticeRequest(publicNotice, createDefaultPublicNoticeDraftForm());
  }

  static PublicNoticeView createPublicNoticeView(PublicNotice publicNotice, PublicNoticeRequest publicNoticeRequest) {
    return new PublicNoticeView(publicNotice.getStatus(),
        DateUtils.formatDate(publicNoticeRequest.getSubmittedTimestamp()));
  }







}
