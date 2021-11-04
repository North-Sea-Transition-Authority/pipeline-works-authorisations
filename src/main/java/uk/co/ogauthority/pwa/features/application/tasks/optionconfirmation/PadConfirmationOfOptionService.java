package uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.EntityCopyingException;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class PadConfirmationOfOptionService implements ApplicationFormSectionService {

  private final ApproveOptionsService approveOptionsService;
  private final PadConfirmationOfOptionRepository padConfirmationOfOptionRepository;
  private final ConfirmOptionFormValidator confirmOptionFormValidator;
  private final EntityCopyingService entityCopyingService;

  @Autowired
  public PadConfirmationOfOptionService(ApproveOptionsService approveOptionsService,
                                        PadConfirmationOfOptionRepository padConfirmationOfOptionRepository,
                                        ConfirmOptionFormValidator confirmOptionFormValidator,
                                        EntityCopyingService entityCopyingService) {
    this.approveOptionsService = approveOptionsService;
    this.padConfirmationOfOptionRepository = padConfirmationOfOptionRepository;
    this.confirmOptionFormValidator = confirmOptionFormValidator;
    this.entityCopyingService = entityCopyingService;
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
    confirmOptionForm.setOptionCompletedDescription(null);
    confirmOptionForm.setOtherWorkDescription(null);

    if (ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS.equals(padConfirmationOfOption.getConfirmedOptionType())) {
      confirmOptionForm.setOptionCompletedDescription(padConfirmationOfOption.getChosenOptionDesc());
    } else if (ConfirmedOptionType.WORK_DONE_BUT_NOT_PRESENTED_AS_OPTION.equals(padConfirmationOfOption.getConfirmedOptionType())) {
      confirmOptionForm.setOtherWorkDescription(padConfirmationOfOption.getChosenOptionDesc());
    }

  }

  public void mapFormToEntity(ConfirmOptionForm confirmOptionForm, PadConfirmationOfOption padConfirmationOfOption) {

    padConfirmationOfOption.setConfirmedOptionType(confirmOptionForm.getConfirmedOptionType());
    padConfirmationOfOption.setChosenOptionDesc(null);

    if (ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS.equals(confirmOptionForm.getConfirmedOptionType())) {
      padConfirmationOfOption.setChosenOptionDesc(confirmOptionForm.getOptionCompletedDescription());
    } else if (ConfirmedOptionType.WORK_DONE_BUT_NOT_PRESENTED_AS_OPTION.equals(confirmOptionForm.getConfirmedOptionType())) {
      padConfirmationOfOption.setChosenOptionDesc(confirmOptionForm.getOtherWorkDescription());
    }

  }

  public Optional<PadConfirmationOfOption> findPadConfirmationOfOption(PwaApplicationDetail pwaApplicationDetail) {
    return padConfirmationOfOptionRepository.findByPwaApplicationDetail(pwaApplicationDetail);
  }

  public PadConfirmationOfOption getOrCreatePadConfirmationOfOption(PwaApplicationDetail pwaApplicationDetail) {
    return padConfirmationOfOptionRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadConfirmationOfOption(pwaApplicationDetail));
  }

  public PadConfirmationOfOptionView getPadConfirmationOfOptionView(PwaApplicationDetail pwaApplicationDetail) {

    var confirmation = findPadConfirmationOfOption(pwaApplicationDetail);

    var workType = confirmation
        .map(PadConfirmationOfOption::getConfirmedOptionType)
        .map(ConfirmedOptionType::getDisplayName)
        .orElse(null);

    var workDesc = confirmation
        .map(PadConfirmationOfOption::getChosenOptionDesc)
        .orElse(null);

    return new PadConfirmationOfOptionView(workType, workDesc);

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

  @Transactional
  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    entityCopyingService.duplicateEntityAndSetParent(
        () -> findPadConfirmationOfOption(fromDetail)
            .orElseThrow(() -> new EntityCopyingException("Could not find padConfirmationOfOption for pad.id:" + fromDetail.getId())),
        toDetail,
        PadConfirmationOfOption.class
    );
  }

}
