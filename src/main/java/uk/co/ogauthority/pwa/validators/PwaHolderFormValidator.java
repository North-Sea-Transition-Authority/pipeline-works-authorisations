package uk.co.ogauthority.pwa.validators;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.internal.PortalOrganisationUnitRepository;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;

@Service
public class PwaHolderFormValidator implements Validator {

  private final PortalOrganisationUnitRepository portalOrganisationUnitRepository;

  @Autowired
  public PwaHolderFormValidator(PortalOrganisationUnitRepository portalOrganisationUnitRepository) {
    this.portalOrganisationUnitRepository = portalOrganisationUnitRepository;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return PwaHolderForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    PwaHolderForm form = (PwaHolderForm) target;

    Optional.ofNullable(form.getHolderOuId()).ifPresentOrElse(
        ouId -> {
          if (portalOrganisationUnitRepository.findById(ouId).isEmpty()) {
            errors.rejectValue("holderOuId", "holderOuId.invalidOrg", "Select a valid organisation");
          }
        },
        () -> errors.rejectValue("holderOuId", "holderOuId.required", "Select an organisation")
    );

  }

}
