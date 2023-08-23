package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeDocumentType;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDate;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeRequest;
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.model.view.publicnotice.PublicNoticeEvent;
import uk.co.ogauthority.pwa.model.view.publicnotice.PublicNoticeEventType;
import uk.co.ogauthority.pwa.model.view.publicnotice.PublicNoticeView;
import uk.co.ogauthority.pwa.util.DateUtils;

public final class PublicNoticeTestUtil {

  private static int VERSION1 = 1;

  private PublicNoticeTestUtil(){}


  public static PublicNotice createDraftPublicNotice(PwaApplication pwaApplication) {
    return new PublicNotice(pwaApplication, PublicNoticeStatus.DRAFT, VERSION1);
  }

  public static PublicNotice createInitialPublicNotice(PwaApplication pwaApplication) {
    return new PublicNotice(pwaApplication, PublicNoticeStatus.MANAGER_APPROVAL, VERSION1);
  }

  public static PublicNotice createApplicantUpdatePublicNotice(PwaApplication pwaApplication) {
    return new PublicNotice(pwaApplication, PublicNoticeStatus.APPLICANT_UPDATE, VERSION1);
  }

  public static PublicNotice createCaseOfficerReviewPublicNotice(PwaApplication pwaApplication) {
    return new PublicNotice(pwaApplication, PublicNoticeStatus.CASE_OFFICER_REVIEW, VERSION1);
  }

  public static PublicNotice createWaitingPublicNotice(PwaApplication pwaApplication) {
    return new PublicNotice(pwaApplication, PublicNoticeStatus.WAITING, VERSION1);
  }

  public static PublicNotice createPublishedPublicNotice(PwaApplication pwaApplication) {
    return new PublicNotice(pwaApplication, PublicNoticeStatus.PUBLISHED, VERSION1);
  }

  public static PublicNotice createEndedPublicNotice(PwaApplication pwaApplication) {
    return new PublicNotice(pwaApplication, PublicNoticeStatus.ENDED, VERSION1);
  }

  public static PublicNotice createPublicNoticeWithStatus(PublicNoticeStatus status) {
    var publicNotice = new PublicNotice();
    publicNotice.setStatus(status);
    publicNotice.setVersion(VERSION1);
    return publicNotice;
  }

  static PublicNotice createWithdrawnPublicNotice(PwaApplication pwaApplication) {
    var publicNotice = new PublicNotice(pwaApplication, PublicNoticeStatus.WITHDRAWN, 10);
    publicNotice.setWithdrawingPersonId(new PersonId(1));
    publicNotice.setWithdrawalReason("my reason");
    publicNotice.setWithdrawalTimestamp(Instant.now());
    return publicNotice;
  }

  static PublicNotice createWithdrawnPublicNotice(PwaApplication pwaApplication,
                                                  Person withdrawingPerson, String reason, Instant withdrawalTimestamp) {
    var publicNotice = new PublicNotice(pwaApplication, PublicNoticeStatus.WITHDRAWN, 10);
    publicNotice.setWithdrawingPersonId(withdrawingPerson.getId());
    publicNotice.setWithdrawalReason(reason);
    publicNotice.setWithdrawalTimestamp(withdrawalTimestamp);
    return publicNotice;
  }

  public static PublicNoticeDocument createArchivedPublicNoticeDocument(PublicNotice publicNotice) {
    return new PublicNoticeDocument(publicNotice, VERSION1, PublicNoticeDocumentType.ARCHIVED, Instant.now());
  }

  public static PublicNoticeDocument createInitialPublicNoticeDocument(PublicNotice publicNotice) {
    return new PublicNoticeDocument(publicNotice, VERSION1, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT, Instant.now());
  }

  public static PublicNoticeDocument createCommentedPublicNoticeDocument(PublicNotice publicNotice) {
    var document = createInitialPublicNoticeDocument(publicNotice);
    document.setComments("comments");
    return document;
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
    publicNoticeRequest.setCreatedTimestamp(Instant.now());
    publicNoticeRequest.setCreatedByPersonId(1);
    return publicNoticeRequest;
  }

  public static PublicNoticeRequest createInitialPublicNoticeRequest(PublicNotice publicNotice) {
    return createInitialPublicNoticeRequest(publicNotice, createDefaultPublicNoticeDraftForm());
  }

  public static PublicNoticeRequest createApprovedPublicNoticeRequest(PublicNotice publicNotice) {
    var publicNoticeRequest = createInitialPublicNoticeRequest(publicNotice, createDefaultPublicNoticeDraftForm());
    publicNoticeRequest.setResponderPersonId(2);
    publicNoticeRequest.setRequestApproved(true);
    publicNoticeRequest.setResponseTimestamp(Instant.now());
    return publicNoticeRequest;
  }

  static PublicNoticeView createCommentedPublicNoticeView(PublicNotice publicNotice, PublicNoticeRequest publicNoticeRequest) {
    return new PublicNoticeView(publicNotice.getStatus(), DateUtils.formatDateTime(publicNoticeRequest.getCreatedTimestamp()),
        publicNoticeRequest.getStatus());
  }

  static PublicNoticeView createCommentedPublicNoticeView(PublicNotice publicNotice, PublicNoticeRequest publicNoticeRequest, PublicNoticeDocument publicNoticeDocument) {
    return new PublicNoticeView(publicNotice.getStatus(),
        DateUtils.formatDateTime(publicNoticeRequest.getCreatedTimestamp()), publicNoticeDocument.getComments(),
        null, null, null, null, null, publicNoticeRequest.getStatus(), publicNoticeRequest.getRejectionReason(), null, List.of(),
        null);
  }

  static PublicNoticeView createWithdrawnPublicNoticeView(PublicNotice publicNotice,
                                                          String withdrawingUsername,
                                                          String withdrawalReason,
                                                          PublicNoticeRequest publicNoticeRequest) {
    return new PublicNoticeView(
        publicNotice.getStatus(),
        DateUtils.formatDateTime(publicNoticeRequest.getCreatedTimestamp()),
        null,
        withdrawingUsername,
        DateUtils.formatDate(publicNotice.getWithdrawalTimestamp()),
        withdrawalReason,
        null,
        null,
        publicNoticeRequest.getStatus(),
        publicNoticeRequest.getRejectionReason(),
        null,
        List.of(),
        null);
  }

  static PublicNoticeView createPublishedPublicNoticeView(PublicNotice publicNotice,
                                                          PublicNoticeDate publicNoticeDate,
                                                          PublicNoticeRequest publicNoticeRequest) {
    return new PublicNoticeView(
        publicNotice.getStatus(),
        DateUtils.formatDateTime(publicNoticeRequest.getCreatedTimestamp()),
        null,
        null,
        null,
        null,
        DateUtils.formatDate(publicNoticeDate.getPublicationStartTimestamp()),
        DateUtils.formatDate(publicNoticeDate.getPublicationEndTimestamp()),
        publicNoticeRequest.getStatus(),
        publicNoticeRequest.getRejectionReason(),
        null,
        Stream.of(
            new PublicNoticeEvent()
                .setEventType(PublicNoticeEventType.REQUEST_CREATED)
                .setEventTimestamp(publicNoticeRequest.getCreatedTimestamp())
                .setPersonId(new PersonId(publicNoticeRequest.getCreatedByPersonId()))
                .setComment(publicNoticeRequest.getReasonDescription()),
            new PublicNoticeEvent()
                .setEventType(PublicNoticeEventType.APPROVED)
                .setEventTimestamp(publicNoticeRequest.getResponseTimestamp())
                .setPersonId(new PersonId(publicNoticeRequest.getResponderPersonId())),
            new PublicNoticeEvent()
                .setEventType(PublicNoticeEventType.PUBLISHED)
                .setEventTimestamp(publicNoticeDate.getPublicationStartTimestamp())
                .setPersonId(new PersonId(publicNoticeDate.getCreatedByPersonId()))
        )
            .sorted(Comparator.comparing(PublicNoticeEvent::getEventTimestamp).reversed())
            .collect(Collectors.toList()),
        Map.of(
            new PersonId(publicNoticeRequest.getCreatedByPersonId()), "Person 1",
            new PersonId(publicNoticeRequest.getResponderPersonId()), "Person 2"
        )
    );
  }

  static FinalisePublicNoticeForm createStartBeforeTodayFinalisePublicNoticeForm() {
    var form = new FinalisePublicNoticeForm();
    var lastMonthDate = LocalDate.now().minusMonths(1);
    form.setStartDay(lastMonthDate.getDayOfMonth());
    form.setStartMonth(lastMonthDate.getMonthValue());
    form.setStartYear(lastMonthDate.getYear());
    form.setDaysToBePublishedFor(40);
    return form;
  }

  static FinalisePublicNoticeForm createAfterTodayFinalisePublicNoticeForm() {
    var form = new FinalisePublicNoticeForm();
    var nextMonthDate = LocalDate.now().plusMonths(1);
    form.setStartDay(nextMonthDate.getDayOfMonth());
    form.setStartMonth(nextMonthDate.getMonthValue());
    form.setStartYear(nextMonthDate.getYear());
    form.setDaysToBePublishedFor(40);
    return form;
  }

  public static PublicNoticeDate createLatestPublicNoticeDate(PublicNotice publicNotice) {
    var startDate = LocalDate.now().minusMonths(1);
    return new PublicNoticeDate(
        publicNotice,
        startDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
        startDate.plusDays(28).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        1,
        Instant.now());
  }






}
