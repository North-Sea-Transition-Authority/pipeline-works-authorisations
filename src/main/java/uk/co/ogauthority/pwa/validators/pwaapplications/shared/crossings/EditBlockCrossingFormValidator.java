package uk.co.ogauthority.pwa.validators.pwaapplications.shared.crossings;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.CrossedBlockOwner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.EditBlockCrossingForm;

@Component
public class EditBlockCrossingFormValidator implements Validator {

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Autowired
  public EditBlockCrossingFormValidator(PortalOrganisationsAccessor portalOrganisationsAccessor) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return EditBlockCrossingForm.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (EditBlockCrossingForm) target;

    if (form.getCrossedBlockOwner() != null
        && form.getCrossedBlockOwner().equals(CrossedBlockOwner.PORTAL_ORGANISATION)) {
      ValidationUtils.rejectIfEmpty(
          errors, "blockOwnersOuIdList",
          "blockOwnersOuIdList.required",
          "You must provide a block owner"
      );
    }

    if (form.getCrossedBlockOwner() != null
        && form.getCrossedBlockOwner().equals(CrossedBlockOwner.OTHER_ORGANISATION)) {
      ValidationUtils.rejectIfEmpty(
          errors, "operatorNotFoundFreeTextBox",
          "operatorNotFoundFreeTextBox.required",
          "You must provide a block owner"
      );
    }

    if (form.getBlockOwnersOuIdList() != null && !form.getBlockOwnersOuIdList().isEmpty()) {
      var validOrganisationUnitIdSet = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(
          form.getBlockOwnersOuIdList())
          .stream()
          .map(PortalOrganisationUnit::getOuId)
          .collect(Collectors.toSet());

      for (int i = 0; i < form.getBlockOwnersOuIdList().size(); i++) {
        if (!validOrganisationUnitIdSet.contains(form.getBlockOwnersOuIdList().get(i))) {
          var field = "blockOwnersOuIdList[" + i + "]";
          errors.rejectValue(field, field + ".invalid", "Please provide a valid organisation unit");
        }
      }

    }
  }
}
