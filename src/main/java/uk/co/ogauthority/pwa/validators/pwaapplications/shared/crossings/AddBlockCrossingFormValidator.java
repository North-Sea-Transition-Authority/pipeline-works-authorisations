package uk.co.ogauthority.pwa.validators.pwaapplications.shared.crossings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.licence.PearsBlock;
import uk.co.ogauthority.pwa.model.entity.licence.PearsLicence;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.AddBlockCrossingForm;
import uk.co.ogauthority.pwa.service.licence.PearsBlockService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingService;

@Component
public class AddBlockCrossingFormValidator implements SmartValidator {

  private final EditBlockCrossingFormValidator editBlockCrossingFormValidator;
  private final PearsBlockService pearsBlockService;

  @Autowired
  public AddBlockCrossingFormValidator(
      EditBlockCrossingFormValidator editBlockCrossingFormValidator,
      PearsBlockService pearsBlockService) {
    this.editBlockCrossingFormValidator = editBlockCrossingFormValidator;
    this.pearsBlockService = pearsBlockService;
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
    BlockCrossingService blockCrossingService = (BlockCrossingService) validationHints[1];

    if (form.getPickedBlock() != null) {
      var optionalBlock = pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKey(form.getPickedBlock());
      if (optionalBlock.isEmpty()) {
        errors.rejectValue("pickedBlock", "pickedBlock.invalid", "Select a valid block");

      } else if (blockCrossingService.doesBlockExistOnApp(pwaApplicationDetail, form.getPickedBlock())) {
        errors.rejectValue("pickedBlock", "pickedBlock.invalid", "The selected block already exists on this application");
      }

      licence = optionalBlock.map(PearsBlock::getPearsLicence)
          .orElse(null);
    } else {
      licence = null;
    }

    editBlockCrossingFormValidator.validate(form, errors, licence);
  }
}
