package uk.co.ogauthority.pwa.validators.pwaapplications.shared.crossings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.entity.licence.PearsBlock;
import uk.co.ogauthority.pwa.model.entity.licence.PearsLicence;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.AddBlockCrossingForm;
import uk.co.ogauthority.pwa.service.licence.PearsBlockService;

@Component
public class AddBlockCrossingFormValidator implements Validator {

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
    var form = (AddBlockCrossingForm) target;

    PearsLicence licence;

    if (form.getPickedBlock() != null) {
      var optionalBlock = pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKey(form.getPickedBlock());
      if (optionalBlock.isEmpty()) {
        errors.rejectValue("pickedBlock", "pickedBlock.invalid", "You must pick a valid block");
      }
      licence = optionalBlock.map(PearsBlock::getPearsLicence)
          .orElse(null);
    } else {
      licence = null;
    }

    editBlockCrossingFormValidator.validate(form, errors, licence);
  }
}
