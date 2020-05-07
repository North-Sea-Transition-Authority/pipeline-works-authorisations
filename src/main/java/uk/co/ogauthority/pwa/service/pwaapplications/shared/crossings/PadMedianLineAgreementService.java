package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.MedianLineAgreementsForm;
import uk.co.ogauthority.pwa.model.tasklist.TaskListLabel;
import uk.co.ogauthority.pwa.model.tasklist.TaskListSection;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadMedianLineAgreementRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.validators.MedianLineAgreementValidator;

@Service
public class PadMedianLineAgreementService implements ApplicationFormSectionService, TaskListSection {

  private final PadMedianLineAgreementRepository padMedianLineAgreementRepository;
  private final MedianLineAgreementValidator medianLineAgreementValidator;
  private final MedianLineCrossingFileService medianLineCrossingFileService;

  @Autowired
  public PadMedianLineAgreementService(
      PadMedianLineAgreementRepository padMedianLineAgreementRepository,
      MedianLineAgreementValidator medianLineAgreementValidator,
      MedianLineCrossingFileService medianLineCrossingFileService) {
    this.padMedianLineAgreementRepository = padMedianLineAgreementRepository;
    this.medianLineAgreementValidator = medianLineAgreementValidator;
    this.medianLineCrossingFileService = medianLineCrossingFileService;
  }

  public PadMedianLineAgreement getMedianLineAgreement(PwaApplicationDetail pwaApplicationDetail) {
    var agreementIfOptionalEmpty = new PadMedianLineAgreement();
    agreementIfOptionalEmpty.setPwaApplicationDetail(pwaApplicationDetail);
    return padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(agreementIfOptionalEmpty);
  }

  @Transactional
  public void save(PadMedianLineAgreement padMedianLineAgreement) {
    padMedianLineAgreementRepository.save(padMedianLineAgreement);
  }

  public void mapEntityToForm(PadMedianLineAgreement padMedianLineAgreement, MedianLineAgreementsForm form) {
    form.setAgreementStatus(padMedianLineAgreement.getAgreementStatus());
    if (padMedianLineAgreement.getAgreementStatus() == MedianLineStatus.NEGOTIATIONS_ONGOING) {
      form.setNegotiatorNameIfOngoing(padMedianLineAgreement.getNegotiatorName());
      form.setNegotiatorEmailIfOngoing(padMedianLineAgreement.getNegotiatorEmail());
    } else if (padMedianLineAgreement.getAgreementStatus() == MedianLineStatus.NEGOTIATIONS_COMPLETED) {
      form.setNegotiatorNameIfCompleted(padMedianLineAgreement.getNegotiatorName());
      form.setNegotiatorEmailIfCompleted(padMedianLineAgreement.getNegotiatorEmail());
    }
  }

  @Transactional
  public void saveEntityUsingForm(PadMedianLineAgreement padMedianLineAgreement, MedianLineAgreementsForm form) {
    padMedianLineAgreement.setAgreementStatus(form.getAgreementStatus());
    if (form.getAgreementStatus() == MedianLineStatus.NEGOTIATIONS_ONGOING) {
      padMedianLineAgreement.setNegotiatorName(form.getNegotiatorNameIfOngoing());
      padMedianLineAgreement.setNegotiatorEmail(form.getNegotiatorEmailIfOngoing());
    } else if (form.getAgreementStatus() == MedianLineStatus.NEGOTIATIONS_COMPLETED) {
      padMedianLineAgreement.setNegotiatorName(form.getNegotiatorNameIfCompleted());
      padMedianLineAgreement.setNegotiatorEmail(form.getNegotiatorEmailIfCompleted());
    } else if (form.getAgreementStatus() == MedianLineStatus.NOT_CROSSED) {
      padMedianLineAgreement.setNegotiatorName(null);
      padMedianLineAgreement.setNegotiatorEmail(null);
    }
    padMedianLineAgreementRepository.save(padMedianLineAgreement);
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var medianLineAgreement = getMedianLineAgreement(detail);
    var form = new MedianLineAgreementsForm();
    mapEntityToForm(medianLineAgreement, form);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    validate(form, bindingResult, ValidationType.FULL, detail);
    if (form.getAgreementStatus() != MedianLineStatus.NOT_CROSSED) {
      if (medianLineCrossingFileService.getFullFileCount(detail) == 0) {
        return false;
      }
      medianLineCrossingFileService.validate(form, bindingResult, ValidationType.FULL, detail);
    }
    return !bindingResult.hasErrors();

  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    if (validationType.equals(ValidationType.FULL)) {
      medianLineAgreementValidator.validate(form, bindingResult, FullValidation.class);
    } else {
      medianLineAgreementValidator.validate(form, bindingResult);
    }
    return bindingResult;
  }

  @Override
  public boolean isTaskListEntryCompleted(PwaApplicationDetail pwaApplicationDetail) {
    return false;
  }

  @Override
  public boolean getCanShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return true;
  }

  @Override
  public List<TaskListLabel> getTaskListLabels(PwaApplicationDetail pwaApplicationDetail) {
    return List.of();
  }
}
