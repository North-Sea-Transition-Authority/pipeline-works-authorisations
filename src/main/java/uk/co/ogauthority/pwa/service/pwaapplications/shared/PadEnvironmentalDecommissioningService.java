package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Set;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadEnvironmentalDecommissioning;
import uk.co.ogauthority.pwa.model.enums.pwaapplications.shared.EnvDecomQuestion;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.EnvironmentalDecommissioningForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.EnvironmentalDecommissioningView;
import uk.co.ogauthority.pwa.repository.pwaapplications.initial.PadEnvironmentalDecommissioningRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.EnvironmentalDecommissioningValidator;

@Service
public class PadEnvironmentalDecommissioningService implements ApplicationFormSectionService {

  private final PadEnvironmentalDecommissioningRepository padEnvironmentalDecommissioningRepository;
  private final EnvironmentalDecommissioningValidator environmentalDecommissioningValidator;
  private final EntityCopyingService entityCopyingService;

  @Autowired
  public PadEnvironmentalDecommissioningService(PadEnvironmentalDecommissioningRepository padEnvironmentalDecommissioningRepository,
                                                EnvironmentalDecommissioningValidator environmentalDecommissioningValidator,
                                                EntityCopyingService entityCopyingService) {
    this.padEnvironmentalDecommissioningRepository = padEnvironmentalDecommissioningRepository;
    this.environmentalDecommissioningValidator = environmentalDecommissioningValidator;
    this.entityCopyingService = entityCopyingService;
  }

  public PadEnvironmentalDecommissioning getEnvDecomData(PwaApplicationDetail pwaApplicationDetail) {
    var adminDetail = padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadEnvironmentalDecommissioning());
    adminDetail.setPwaApplicationDetail(pwaApplicationDetail);
    return adminDetail;
  }

  public Set<EnvDecomQuestion> getAvailableQuestions(PwaApplicationDetail detail) {

    if (detail.getPwaApplicationType() == PwaApplicationType.CAT_2_VARIATION
        || detail.getPwaApplicationType() == PwaApplicationType.DEPOSIT_CONSENT) {
      return Set.of(EnvDecomQuestion.BEIS_EMT_PERMITS);
    } else if (detail.getPwaApplicationType() == PwaApplicationType.DECOMMISSIONING) {
      return EnumSet.complementOf(EnumSet.of(EnvDecomQuestion.DECOMMISSIONING));
    }

    return EnumSet.allOf(EnvDecomQuestion.class);

  }

  private PadEnvironmentalDecommissioning save(PadEnvironmentalDecommissioning padEnvironmentalDecommissioning) {
    return padEnvironmentalDecommissioningRepository.save(padEnvironmentalDecommissioning);
  }

  public void mapEntityToForm(PadEnvironmentalDecommissioning padEnvironmentalDecommissioning, EnvironmentalDecommissioningForm form) {
    form.setEmtHasOutstandingPermits(padEnvironmentalDecommissioning.getEmtHasOutstandingPermits());
    form.setEmtHasSubmittedPermits(padEnvironmentalDecommissioning.getEmtHasSubmittedPermits());
    form.setPermitsSubmitted(padEnvironmentalDecommissioning.getPermitsSubmitted());
    form.setPermitsPendingSubmission(padEnvironmentalDecommissioning.getPermitsPendingSubmission());
    form.setTransboundaryEffect(padEnvironmentalDecommissioning.getTransboundaryEffect());
    form.setEnvironmentalConditions(padEnvironmentalDecommissioning.getEnvironmentalConditions());
    form.setDecommissioningConditions(padEnvironmentalDecommissioning.getDecommissioningConditions());
    if (padEnvironmentalDecommissioning.getEmtSubmissionTimestamp() != null) {
      var localDate = LocalDate.ofInstant(padEnvironmentalDecommissioning.getEmtSubmissionTimestamp(),
          ZoneId.systemDefault());
      form.setEmtSubmissionDay(localDate.getDayOfMonth());
      form.setEmtSubmissionMonth(localDate.getMonthValue());
      form.setEmtSubmissionYear(localDate.getYear());
    }
  }

  @Transactional
  public void saveEntityUsingForm(PadEnvironmentalDecommissioning padEnvironmentalDecommissioning, EnvironmentalDecommissioningForm form) {
    padEnvironmentalDecommissioning.setEmtHasOutstandingPermits(form.getEmtHasOutstandingPermits());
    padEnvironmentalDecommissioning.setEmtHasSubmittedPermits(form.getEmtHasSubmittedPermits());
    padEnvironmentalDecommissioning.setEnvironmentalConditions(form.getEnvironmentalConditions());
    padEnvironmentalDecommissioning.setDecommissioningConditions(form.getDecommissioningConditions());

    if (BooleanUtils.isTrue(form.getEmtHasSubmittedPermits())) {
      padEnvironmentalDecommissioning.setPermitsSubmitted(form.getPermitsSubmitted());
    } else {
      padEnvironmentalDecommissioning.setPermitsSubmitted(null);
    }

    if (BooleanUtils.isTrue(form.getEmtHasOutstandingPermits())) {
      padEnvironmentalDecommissioning.setPermitsPendingSubmission(form.getPermitsPendingSubmission());
      // TODO: PWA-379 - Prevent discard when date is invalid.
      try {
        var localDate = LocalDate.of(
            form.getEmtSubmissionYear(),
            form.getEmtSubmissionMonth(),
            form.getEmtSubmissionDay()
        );
        var instant = Instant.ofEpochSecond(localDate.toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC));
        padEnvironmentalDecommissioning.setEmtSubmissionTimestamp(instant);
      } catch (Exception e) {
        padEnvironmentalDecommissioning.setEmtSubmissionTimestamp(null);
      }
    } else {
      padEnvironmentalDecommissioning.setPermitsPendingSubmission(null);
      padEnvironmentalDecommissioning.setEmtSubmissionTimestamp(null);
    }
    padEnvironmentalDecommissioning.setTransboundaryEffect(form.getTransboundaryEffect());
    save(padEnvironmentalDecommissioning);
  }


  public EnvironmentalDecommissioningView getEnvironmentalDecommissioningView(PwaApplicationDetail pwaApplicationDetail) {

    var padEnvironmentalDecommissioning = getEnvDecomData(pwaApplicationDetail);
    var submissionTimestamp = padEnvironmentalDecommissioning.getEmtSubmissionTimestamp();

    return new EnvironmentalDecommissioningView(
        padEnvironmentalDecommissioning.getTransboundaryEffect(),
        padEnvironmentalDecommissioning.getEmtHasSubmittedPermits(),
        padEnvironmentalDecommissioning.getPermitsSubmitted(),
        padEnvironmentalDecommissioning.getEmtHasOutstandingPermits(),
        padEnvironmentalDecommissioning.getPermitsPendingSubmission(),
        submissionTimestamp != null ? DateUtils.formatDate(submissionTimestamp) : null,
        padEnvironmentalDecommissioning.getEnvironmentalConditions(),
        padEnvironmentalDecommissioning.getDecommissioningConditions()
    );
  }


  @Override
  public boolean isComplete(PwaApplicationDetail detail) {

    PadEnvironmentalDecommissioning environmentalDecommissioning = getEnvDecomData(detail);
    var environmentalDecommissioningForm = new EnvironmentalDecommissioningForm();
    mapEntityToForm(environmentalDecommissioning, environmentalDecommissioningForm);
    BindingResult bindingResult = new BeanPropertyBindingResult(environmentalDecommissioningForm, "form");
    validate(environmentalDecommissioningForm, bindingResult, ValidationType.FULL, detail);

    return !bindingResult.hasErrors();

  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    environmentalDecommissioningValidator.validate(form, bindingResult, pwaApplicationDetail, validationType);
    return bindingResult;
  }

  @Override
  public void cleanupData(PwaApplicationDetail detail) {

    var envDecomData = getEnvDecomData(detail);

    // if not submitted permits, clear permit info
    if (!envDecomData.getEmtHasSubmittedPermits()) {
      envDecomData.setPermitsSubmitted(null);
    }

    // if no outstanding permits, clear outstanding permit info
    if (!envDecomData.getEmtHasOutstandingPermits()) {
      envDecomData.setPermitsPendingSubmission(null);
      envDecomData.setEmtSubmissionTimestamp(null);
    }

    save(envDecomData);

  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    entityCopyingService.duplicateEntityAndSetParent(
        () -> getEnvDecomData(fromDetail),
        toDetail,
        PadEnvironmentalDecommissioning.class

    );
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return !pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION);
  }
}
