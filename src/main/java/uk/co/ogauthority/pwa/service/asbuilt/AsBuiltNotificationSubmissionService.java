package uk.co.ogauthority.pwa.service.asbuilt;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.form.asbuilt.AsBuiltNotificationSubmissionForm;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationSubmissionRepository;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
class AsBuiltNotificationSubmissionService {

  private final AsBuiltNotificationSubmissionRepository asBuiltNotificationSubmissionRepository;
  private final AsBuiltNotificationGroupStatusService asBuiltNotificationGroupStatusService;
  private final AsBuiltPipelineNotificationService asBuiltPipelineNotificationService;
  private final AsBuiltNotificationEmailService asBuiltNotificationEmailService;
  private final String ogaConsentsEmail;

  @Autowired
  AsBuiltNotificationSubmissionService(
      AsBuiltNotificationSubmissionRepository asBuiltNotificationSubmissionRepository,
      AsBuiltNotificationGroupStatusService asBuiltNotificationGroupStatusService,
      AsBuiltPipelineNotificationService asBuiltPipelineNotificationService,
      AsBuiltNotificationEmailService asBuiltNotificationEmailService,
      @Value("${oga.consents.email}") String ogaConsentsEmail) {
    this.asBuiltNotificationSubmissionRepository = asBuiltNotificationSubmissionRepository;
    this.asBuiltNotificationGroupStatusService = asBuiltNotificationGroupStatusService;
    this.asBuiltPipelineNotificationService = asBuiltPipelineNotificationService;
    this.asBuiltNotificationEmailService = asBuiltNotificationEmailService;
    this.ogaConsentsEmail = ogaConsentsEmail;
  }

  void submitAsBuiltNotification(AsBuiltNotificationGroupPipeline abngPipeline, AsBuiltNotificationSubmissionForm form,
                                 AuthenticatedUserAccount user) {
    var asBuiltSubmission = new AsBuiltNotificationSubmission();
    asBuiltSubmission.setAsBuiltNotificationGroupPipeline(abngPipeline);
    asBuiltSubmission.setSubmittedByPersonId(user.getLinkedPerson().getId());
    setLatestSubmissionFlagAndUpdateLastSubmission(asBuiltSubmission);
    mapFormToEntity(form, asBuiltSubmission);
    saveAsBuiltNotificationSubmission(asBuiltSubmission);
    updateAsBuiltGroupStatus(abngPipeline.getAsBuiltNotificationGroup(), user.getLinkedPerson());
    if (doesOgaNeedToBeNotified(form)) {
      var pipelineDetail = getPipelineDetail(abngPipeline.getPipelineDetailId());
      notifyOgaIfNotificationNotPerConsent(asBuiltSubmission.getAsBuiltNotificationGroupPipeline().getAsBuiltNotificationGroup(),
          pipelineDetail.getPipelineNumber(), form.getAsBuiltNotificationStatus());
    }
  }

  private void updateAsBuiltGroupStatus(AsBuiltNotificationGroup asBuiltNotificationGroup, Person person) {
    var allAsBuiltGroupPipelines = asBuiltPipelineNotificationService
        .getAllAsBuiltNotificationGroupPipelines(asBuiltNotificationGroup.getId());
    var latestAsBuiltSubmissionsForAsBuiltGroupPipelines = asBuiltNotificationSubmissionRepository
        .findAllByAsBuiltNotificationGroupPipelineInAndTipFlagIsTrue(allAsBuiltGroupPipelines);
    var noSubmissionsWithStatusNotProvided = allAsBuiltSubmissionsHaveProvidedValidStatus(latestAsBuiltSubmissionsForAsBuiltGroupPipelines);
    if (noSubmissionsWithStatusNotProvided && allAsBuiltGroupPipelines.size() == latestAsBuiltSubmissionsForAsBuiltGroupPipelines.size()) {
      asBuiltNotificationGroupStatusService.setGroupStatus(asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.COMPLETE, person);
    } else {
      asBuiltNotificationGroupStatusService.setGroupStatus(asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.IN_PROGRESS, person);
    }
  }

  private boolean allAsBuiltSubmissionsHaveProvidedValidStatus(List<AsBuiltNotificationSubmission> asBuiltNotificationSubmissions) {
    return asBuiltNotificationSubmissions.stream()
        .allMatch(submission -> submission.getAsBuiltNotificationStatus() != AsBuiltNotificationStatus.NOT_PROVIDED);
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

  private PipelineDetail getPipelineDetail(PipelineDetailId pipelineDetailId) {
    return asBuiltPipelineNotificationService.getPipelineDetail(pipelineDetailId.asInt());
  }

  private boolean doesOgaNeedToBeNotified(AsBuiltNotificationSubmissionForm form) {
    return form.getAsBuiltNotificationStatus() != AsBuiltNotificationStatus.PER_CONSENT;
  }

  private void notifyOgaIfNotificationNotPerConsent(AsBuiltNotificationGroup asBuiltNotificationGroup,
                                                    String pipelineNumber, AsBuiltNotificationStatus asBuiltNotificationStatus) {
    asBuiltNotificationEmailService.sendAsBuiltNotificationNotPerConsentEmail(ogaConsentsEmail,
        "Consents team", asBuiltNotificationGroup, pipelineNumber, asBuiltNotificationStatus);
  }

  private void setLatestSubmissionFlagAndUpdateLastSubmission(AsBuiltNotificationSubmission submission) {
    submission.setTipFlag(true);
    var latestSubmissionOptional = asBuiltNotificationSubmissionRepository
        .findByAsBuiltNotificationGroupPipelineAndTipFlagIsTrue(submission.getAsBuiltNotificationGroupPipeline());
    if (latestSubmissionOptional.isPresent()) {
      var latestSubmission = latestSubmissionOptional.get();
      latestSubmission.setTipFlag(false);
      asBuiltNotificationSubmissionRepository.save(latestSubmission);
    }
  }

}
