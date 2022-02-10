package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsBlock;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsBlockService;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsLicence;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Component
public class AddBlockCrossingFormValidator implements SmartValidator {

  private final EditBlockCrossingFormValidator editBlockCrossingFormValidator;
  private final PearsBlockService pearsBlockService;
  private final BlockCrossingService blockCrossingService;

  @Autowired
  public AddBlockCrossingFormValidator(
      EditBlockCrossingFormValidator editBlockCrossingFormValidator,
      PearsBlockService pearsBlockService,
      BlockCrossingService blockCrossingService) {
    this.editBlockCrossingFormValidator = editBlockCrossingFormValidator;
    this.pearsBlockService = pearsBlockService;
    this.blockCrossingService = blockCrossingService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(AddBlockCrossingForm.class);
  }


  @Override
  public void validate(Object target, Errors errors) {
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (AddBlockCrossingForm) target;

    PearsLicence licence;
    PwaApplicationDetail pwaApplicationDetail = (PwaApplicationDetail) validationHints[0];

    if (form.getPickedBlock() != null) {
      var optionalBlock = pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKey(form.getPickedBlock());
      if (optionalBlock.isEmpty()) {
        errors.rejectValue("pickedBlock", "pickedBlock.invalid", "Select a valid block");

      } else if (blockCrossingService.doesBlockExistOnApp(pwaApplicationDetail, optionalBlock.get())) {
        errors.rejectValue("pickedBlock", "pickedBlock" + FieldValidationErrorCodes.NOT_UNIQUE.getCode(),
            "The selected block already exists on this application");
      }

      licence = optionalBlock.map(PearsBlock::getPearsLicence)
          .orElse(null);
    } else {
      licence = null;
    }

    editBlockCrossingFormValidator.validate(form, errors, licence);
  }
}
