package uk.co.ogauthority.pwa.service.asbuilt;

import java.time.Instant;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;
import uk.co.ogauthority.pwa.model.form.asbuilt.AsBuiltNotificationSubmissionForm;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationSubmissionRepository;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
class AsBuiltNotificationSubmissionService {

  private final AsBuiltNotificationSubmissionRepository asBuiltNotificationSubmissionRepository;

  @Autowired
  AsBuiltNotificationSubmissionService(
      AsBuiltNotificationSubmissionRepository asBuiltNotificationSubmissionRepository) {
    this.asBuiltNotificationSubmissionRepository = asBuiltNotificationSubmissionRepository;
  }

  void submitAsBuiltNotification(AsBuiltNotificationGroupPipeline abngPipeline, AsBuiltNotificationSubmissionForm form,
                                 AuthenticatedUserAccount user) {
    var asBuiltSubmission = new AsBuiltNotificationSubmission();
    asBuiltSubmission.setAsBuiltNotificationGroupPipeline(abngPipeline);
    asBuiltSubmission.setSubmittedByPersonId(user.getLinkedPerson().getId());
    mapFormToEntity(form, asBuiltSubmission);
    saveAsBuiltNotificationSubmission(asBuiltSubmission);
  }

  private void saveAsBuiltNotificationSubmission(AsBuiltNotificationSubmission asBuiltNotificationSubmission) {
    asBuiltNotificationSubmissionRepository.save(asBuiltNotificationSubmission);
  }

  private void mapFormToEntity(AsBuiltNotificationSubmissionForm form, AsBuiltNotificationSubmission asBuiltSubmission) {
    asBuiltSubmission.setSubmittedTimestamp(Instant.now());
    asBuiltSubmission.setRegulatorSubmissionReason(form.getOgaSubmissionReason());
    asBuiltSubmission.setAsBuiltNotificationStatus(form.getAsBuiltNotificationStatus());
    mapAsBuiltStatusToEntity(asBuiltSubmission, form);
  }

  private void mapAsBuiltStatusToEntity(AsBuiltNotificationSubmission asBuiltSubmission, AsBuiltNotificationSubmissionForm form) {
    String dateLaidStr = null;
    String dateBroughtIntoUseStr = null;
    switch (form.getAsBuiltNotificationStatus()) {
      case PER_CONSENT:
        dateLaidStr = form.getPerConsentDateLaidTimestampStr();
        dateBroughtIntoUseStr = form.getPerConsentDateBroughtIntoUseTimestampStr();
        break;
      case NOT_PER_CONSENT:
        dateLaidStr = form.getNotPerConsentDateLaidTimestampStr();
        dateBroughtIntoUseStr = form.getNotPerConsentDateBroughtIntoUseTimestampStr();
        break;
      case NOT_LAID_CONSENT_TIMEFRAME:
        dateLaidStr = form.getNotInConsentTimeframeDateLaidTimestampStr();
        break;
      default:
    }

    if (Objects.nonNull(dateLaidStr)) {
      asBuiltSubmission.setDateLaid(DateUtils.datePickerStringToDate(dateLaidStr));
    }
    if (Objects.nonNull(dateBroughtIntoUseStr)) {
      asBuiltSubmission.setDatePipelineBroughtIntoUse(DateUtils.datePickerStringToDate(dateBroughtIntoUseStr));
    }

  }

}
