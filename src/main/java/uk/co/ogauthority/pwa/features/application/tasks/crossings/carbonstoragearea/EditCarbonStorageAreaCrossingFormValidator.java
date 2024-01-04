package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;

@Component
public class EditCarbonStorageAreaCrossingFormValidator implements Validator {

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Autowired
  public EditCarbonStorageAreaCrossingFormValidator(PortalOrganisationsAccessor portalOrganisationsAccessor) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return EditCarbonStorageAreaCrossingForm.class.isAssignableFrom(clazz);
  }

  @Override
  @Deprecated
  public void validate(Object target, Errors errors) {
    var form = (EditCarbonStorageAreaCrossingForm) target;

    if (form.getCrossingOwner() != null
        && form.getCrossingOwner().equals(CrossingOwner.PORTAL_ORGANISATION)) {
      ValidationUtils.rejectIfEmpty(
          errors, "ownersOuIdList",
          "ownersOuIdList.required",
          "Select an owner"
      );
    }

    if (form.getOwnersOuIdList() != null && !form.getOwnersOuIdList().isEmpty()) {
      var validOrganisationUnitIdSet = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(
          form.getOwnersOuIdList())
          .stream()
          .filter(PortalOrganisationUnit::isActive)
          .map(PortalOrganisationUnit::getOuId)
          .collect(Collectors.toSet());

      for (int i = 0; i < form.getOwnersOuIdList().size(); i++) {
        if (!validOrganisationUnitIdSet.contains(form.getOwnersOuIdList().get(i))) {
          var field = "ownersOuIdList";
          errors.rejectValue(field, field + ".invalid", "Select a valid organisation unit");
        }
      }

    }
  }
}
