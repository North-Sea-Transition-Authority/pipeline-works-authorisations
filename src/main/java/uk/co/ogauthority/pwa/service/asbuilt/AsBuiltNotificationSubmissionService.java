package uk.co.ogauthority.pwa.service.asbuilt;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.form.asbuilt.AsBuiltNotificationSubmissionForm;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationSubmissionRepository;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
class AsBuiltNotificationSubmissionService {

  private final AsBuiltNotificationSubmissionRepository asBuiltNotificationSubmissionRepository;
  private final AsBuiltNotificationGroupStatusService asBuiltNotificationGroupStatusService;
  private final AsBuiltPipelineNotificationService asBuiltPipelineNotificationService;

  @Autowired
  AsBuiltNotificationSubmissionService(
      AsBuiltNotificationSubmissionRepository asBuiltNotificationSubmissionRepository,
      AsBuiltNotificationGroupStatusService asBuiltNotificationGroupStatusService,
      AsBuiltPipelineNotificationService asBuiltPipelineNotificationService) {
    this.asBuiltNotificationSubmissionRepository = asBuiltNotificationSubmissionRepository;
    this.asBuiltNotificationGroupStatusService = asBuiltNotificationGroupStatusService;
    this.asBuiltPipelineNotificationService = asBuiltPipelineNotificationService;
  }

  void submitAsBuiltNotification(AsBuiltNotificationGroupPipeline abngPipeline, AsBuiltNotificationSubmissionForm form,
                                 AuthenticatedUserAccount user) {
    var asBuiltSubmission = new AsBuiltNotificationSubmission();
    asBuiltSubmission.setAsBuiltNotificationGroupPipeline(abngPipeline);
    asBuiltSubmission.setSubmittedByPersonId(user.getLinkedPerson().getId());
    mapFormToEntity(form, asBuiltSubmission);
    saveAsBuiltNotificationSubmission(asBuiltSubmission);
    updateAsBuiltGroupStatus(abngPipeline.getAsBuiltNotificationGroup(), user.getLinkedPerson());
  }

  private void updateAsBuiltGroupStatus(AsBuiltNotificationGroup asBuiltNotificationGroup, Person person) {
    var asBuiltGroupPipelines = asBuiltPipelineNotificationService
        .getAllAsBuiltNotificationGroupPipelines(asBuiltNotificationGroup.getId());
    var asBuiltSubmissionsForAsBuiltGroupPipelines = asBuiltNotificationSubmissionRepository
        .findAllByAsBuiltNotificationGroupPipelineIn(asBuiltGroupPipelines);
    var uniqueNotificationGroupPipelineDetailIdsWithSubmission = asBuiltSubmissionsForAsBuiltGroupPipelines.stream()
        .map(asBuiltNotificationSubmission -> asBuiltNotificationSubmission.getAsBuiltNotificationGroupPipeline().getPipelineDetailId())
        .collect(Collectors.toSet());
    var noSubmissionsWithStatusNotProvided = allAsBuiltSubmissionsHaveProvidedStatus(asBuiltGroupPipelines,
        asBuiltSubmissionsForAsBuiltGroupPipelines);

    if (noSubmissionsWithStatusNotProvided
        && asBuiltGroupPipelines.size() == uniqueNotificationGroupPipelineDetailIdsWithSubmission.size()) {
      asBuiltNotificationGroupStatusService.setGroupStatus(asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.COMPLETE, person);
    } else {
      asBuiltNotificationGroupStatusService.setGroupStatus(asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.IN_PROGRESS, person);
    }
  }

  private boolean allAsBuiltSubmissionsHaveProvidedStatus(List<AsBuiltNotificationGroupPipeline> asBuiltNotificationGroupPipelines,
                                                          List<AsBuiltNotificationSubmission> asBuiltNotificationSubmissions) {
    var latestSubmissionsForEachPipeline = asBuiltNotificationGroupPipelines.stream()
        .map(abngPipeline -> asBuiltNotificationSubmissions.stream()
            .filter(submission -> submission.getAsBuiltNotificationGroupPipeline().getPipelineDetailId()
                .equals(abngPipeline.getPipelineDetailId()))
            .max(Comparator.comparing(AsBuiltNotificationSubmission::getSubmittedTimestamp)))
        .collect(Collectors.toList());
    return latestSubmissionsForEachPipeline.stream()
        .allMatch(submission -> submission.map(latestSubmission ->
            latestSubmission.getAsBuiltNotificationStatus() != AsBuiltNotificationStatus.NOT_PROVIDED).orElse(false));
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
