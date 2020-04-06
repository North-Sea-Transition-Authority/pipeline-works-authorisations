package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

/**
 * Interface to ensure common functionality is implemented across all PWA application form section services.
 */
public interface ApplicationFormSectionService {

  /**
   * Whether or not the section associated with the service is complete (all required data entered and valid).
   * @param detail record for application being checked
   * @return true if section is complete, false otherwise
   */
  boolean isComplete(PwaApplicationDetail detail);

  /**
   * Validate the form object associated with the section and return the result.
   * @param form object
   * @param bindingResult obtained by binding request into form
   * @param validationType specifying whether to do full or partial validation
   * @return binding result containing errors if there were validation problems, clean otherwise
   */
  BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType);

}
