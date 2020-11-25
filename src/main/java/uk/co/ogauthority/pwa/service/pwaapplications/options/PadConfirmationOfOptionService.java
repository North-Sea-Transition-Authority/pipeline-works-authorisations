package uk.co.ogauthority.pwa.service.pwaapplications.options;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.options.PadConfirmationOfOption;
import uk.co.ogauthority.pwa.model.form.pwaapplications.options.ConfirmOptionForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.options.PadConfirmationOfOptionRepository;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.validators.options.ConfirmOptionFormValidator;

@Service
public class PadConfirmationOfOptionService implements ApplicationFormSectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PadConfirmationOfOptionService.class);

  private final ApproveOptionsService approveOptionsService;
  private final PadConfirmationOfOptionRepository padConfirmationOfOptionRepository;
  private final ConfirmOptionFormValidator confirmOptionFormValidator;

  @Autowired
  public PadConfirmationOfOptionService(ApproveOptionsService approveOptionsService,
                                        PadConfirmationOfOptionRepository padConfirmationOfOptionRepository,
                                        ConfirmOptionFormValidator confirmOptionFormValidator) {
    this.approveOptionsService = approveOptionsService;
    this.padConfirmationOfOptionRepository = padConfirmationOfOptionRepository;
    this.confirmOptionFormValidator = confirmOptionFormValidator;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return approveOptionsService.optionsApproved(pwaApplicationDetail.getPwaApplication());
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var padConfirmationOfOption = getOrCreatePadConfirmationOfOption(detail);
    var form = new ConfirmOptionForm();
    mapEntityToForm(form, padConfirmationOfOption);

    BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult = validate(form, bindingResult, ValidationType.FULL, detail);

    return !bindingResult.hasErrors();
  }

  public void mapEntityToForm(ConfirmOptionForm confirmOptionForm, PadConfirmationOfOption padConfirmationOfOption) {

    confirmOptionForm.setConfirmedOptionType(padConfirmationOfOption.getConfirmedOptionType());
    confirmOptionForm.setOptionCompletedDescription(padConfirmationOfOption.getChosenOptionDesc());

  }

  public void mapFormToEntity(ConfirmOptionForm confirmOptionForm, PadConfirmationOfOption padConfirmationOfOption) {

    padConfirmationOfOption.setConfirmedOptionType(confirmOptionForm.getConfirmedOptionType());
    if (ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS.equals(confirmOptionForm.getConfirmedOptionType())) {
      padConfirmationOfOption.setChosenOptionDesc(confirmOptionForm.getOptionCompletedDescription());
    } else {
      padConfirmationOfOption.setChosenOptionDesc(null);
    }

  }

  public Optional<PadConfirmationOfOption> findPadConfirmationOfOption(PwaApplicationDetail pwaApplicationDetail) {
    return padConfirmationOfOptionRepository.findByPwaApplicationDetail(pwaApplicationDetail);
  }

  public PadConfirmationOfOption getOrCreatePadConfirmationOfOption(PwaApplicationDetail pwaApplicationDetail) {
    return padConfirmationOfOptionRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadConfirmationOfOption(pwaApplicationDetail));
  }


  @Transactional
  public void savePadConfirmation(PadConfirmationOfOption padConfirmationOfOption) {
    padConfirmationOfOptionRepository.save(padConfirmationOfOption);
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {

    ValidationUtils.invokeValidator(confirmOptionFormValidator, form, bindingResult, validationType);

    return bindingResult;

  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    // TODO PWa-118
    LOGGER.info("TODO PWA-118 copySectionInformation not implemented");
  }

}
