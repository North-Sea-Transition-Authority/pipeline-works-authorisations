package uk.co.ogauthority.pwa.features.application.creation;

import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.INVALID;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.util.Objects;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.form.pwaapplications.ApplicantOrganisationForm;

@Service
public class ApplicantOrganisationFormValidator implements SmartValidator {

  private final ApplicantOrganisationService applicantOrganisationService;

  @Autowired
  public ApplicantOrganisationFormValidator(ApplicantOrganisationService applicantOrganisationService) {
    this.applicantOrganisationService = applicantOrganisationService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(ApplicantOrganisationForm.class);
  }

  @Override
  public void validate(Object target, Errors errors, Object... objects) {

    var form = (ApplicantOrganisationForm) target;
    var masterPwa = (MasterPwa) objects[0];
    var user = (WebUserAccount) objects[1];

    if (form.getApplicantOrganisationOuId() != null) {

      var allowableOrgs = applicantOrganisationService.getPotentialApplicantOrganisations(masterPwa, user);

      boolean selectedOrgIsValid = allowableOrgs.stream()
          .map(PortalOrganisationUnit::getOuId)
          .anyMatch(ouId -> Objects.equals(form.getApplicantOrganisationOuId(), ouId));

      if (!selectedOrgIsValid) {
        errors.rejectValue("applicantOrganisationOuId", INVALID.errorCode("applicantOrganisationOuId"),
            "Select a valid organisation");
      }

    } else {
      errors.rejectValue("applicantOrganisationOuId", REQUIRED.errorCode("applicantOrganisationOuId"), "Select an organisation");
    }

  }

  @Override
  public void validate(Object o, Errors errors) {
    throw new NotImplementedException("Use the other method.");
  }

}
