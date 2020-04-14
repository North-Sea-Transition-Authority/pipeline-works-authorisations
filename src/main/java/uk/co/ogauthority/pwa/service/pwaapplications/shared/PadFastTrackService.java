package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import java.time.LocalDate;
import java.time.ZoneId;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadFastTrack;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.FastTrackForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadFastTrackRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.validators.FastTrackValidator;

@Service
public class PadFastTrackService implements ApplicationFormSectionService {

  private final PadFastTrackRepository padFastTrackRepository;
  private final PadProjectInformationService padProjectInformationService;
  private final PadMedianLineAgreementService padMedianLineAgreementService;
  private final FastTrackValidator fastTrackValidator;
  private final SpringValidatorAdapter groupValidator;

  @Autowired
  public PadFastTrackService(
      PadFastTrackRepository padFastTrackRepository,
      PadProjectInformationService padProjectInformationService,
      PadMedianLineAgreementService padMedianLineAgreementService,
      FastTrackValidator fastTrackValidator,
      SpringValidatorAdapter groupValidator) {
    this.padFastTrackRepository = padFastTrackRepository;
    this.padProjectInformationService = padProjectInformationService;
    this.padMedianLineAgreementService = padMedianLineAgreementService;
    this.fastTrackValidator = fastTrackValidator;
    this.groupValidator = groupValidator;
  }

  @Transactional
  public PadFastTrack save(PadFastTrack padFastTrack) {
    return padFastTrackRepository.save(padFastTrack);
  }

  public PadFastTrack getFastTrackForDraft(PwaApplicationDetail detail) {
    var fastTrackIfOptionalEmpty = new PadFastTrack();
    fastTrackIfOptionalEmpty.setPwaApplicationDetail(detail);
    return padFastTrackRepository.findByPwaApplicationDetail(detail)
        .orElse(fastTrackIfOptionalEmpty);
  }

  public boolean isFastTrackRequired(PwaApplicationDetail detail) {
    var projectInformation = padProjectInformationService.getPadProjectInformationData(detail);
    if (projectInformation.getProposedStartTimestamp() != null) {
      var startDate = LocalDate.ofInstant(projectInformation.getProposedStartTimestamp(), ZoneId.systemDefault());
      var medianLine = padMedianLineAgreementService.getMedianLineAgreement(detail);
      if (medianLine != null) {
        if (medianLine.getAgreementStatus() == null || medianLine.getAgreementStatus() == MedianLineStatus.NOT_CROSSED) {
          return startDate.isBefore(LocalDate.now().plus(detail.getPwaApplicationType().getMinProcessingPeriod()));
        } else {
          return startDate.isBefore(LocalDate.now().plus(detail.getPwaApplicationType().getMaxProcessingPeriod()));
        }
      }
      return startDate.isBefore(LocalDate.now().plus(detail.getPwaApplicationType().getMinProcessingPeriod()));
    }
    return false;
  }

  public void mapEntityToForm(PadFastTrack fastTrack, FastTrackForm form) {
    form.setAvoidEnvironmentalDisaster(fastTrack.getAvoidEnvironmentalDisaster());
    form.setEnvironmentalDisasterReason(fastTrack.getEnvironmentalDisasterReason());
    form.setSavingBarrels(fastTrack.getSavingBarrels());
    form.setSavingBarrelsReason(fastTrack.getSavingBarrelsReason());
    form.setProjectPlanning(fastTrack.getProjectPlanning());
    form.setProjectPlanningReason(fastTrack.getProjectPlanningReason());
    form.setHasOtherReason(fastTrack.getHasOtherReason());
    form.setOtherReason(fastTrack.getOtherReason());
  }

  @Transactional
  public void saveEntityUsingForm(PadFastTrack fastTrack, FastTrackForm form) {
    fastTrack.setAvoidEnvironmentalDisaster(form.getAvoidEnvironmentalDisaster());
    if (BooleanUtils.isTrue(form.getAvoidEnvironmentalDisaster())) {
      fastTrack.setEnvironmentalDisasterReason(form.getEnvironmentalDisasterReason());
    } else {
      fastTrack.setEnvironmentalDisasterReason(null);
    }

    fastTrack.setSavingBarrels(form.getSavingBarrels());
    if (BooleanUtils.isTrue(form.getSavingBarrels())) {
      fastTrack.setSavingBarrelsReason(form.getSavingBarrelsReason());
    } else {
      fastTrack.setSavingBarrelsReason(null);
    }

    fastTrack.setProjectPlanning(form.getProjectPlanning());
    if (BooleanUtils.isTrue(form.getProjectPlanning())) {
      fastTrack.setProjectPlanningReason(form.getProjectPlanningReason());
    } else {
      fastTrack.setProjectPlanningReason(null);
    }

    fastTrack.setHasOtherReason(form.getHasOtherReason());
    if (BooleanUtils.isTrue(form.getHasOtherReason())) {
      fastTrack.setOtherReason(form.getOtherReason());
    } else {
      fastTrack.setOtherReason(null);
    }
    save(fastTrack);
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {

    var fastTrack = getFastTrackForDraft(detail);
    var fastTrackForm = new FastTrackForm();
    mapEntityToForm(fastTrack, fastTrackForm);
    BindingResult bindingResult = new BeanPropertyBindingResult(fastTrackForm, "form");
    fastTrackValidator.validate(fastTrackForm, bindingResult);

    return !bindingResult.hasErrors();

  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {

    if (validationType.equals(ValidationType.PARTIAL)) {
      groupValidator.validate(form, bindingResult, FastTrackForm.Partial.class);
      return bindingResult;
    }

    groupValidator.validate(form, bindingResult, FastTrackForm.Full.class);
    fastTrackValidator.validate(form, bindingResult);
    return bindingResult;

  }
}
