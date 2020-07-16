package uk.co.ogauthority.pwa.service.pwaapplications.shared.partnerletters;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.validators.partnerletters.PartnerLettersValidator;

/* Service providing simplified API for PartnerLettersService app form */
@Service
public class PadPartnerLettersService implements ApplicationFormSectionService {

  private final PartnerLettersValidator partnerLettersValidator;
  private final PadFileService padFileService;


  @Autowired
  public PadPartnerLettersService(PartnerLettersValidator partnerLettersValidator,
                                  PadFileService padFileService) {
    this.partnerLettersValidator = partnerLettersValidator;
    this.padFileService = padFileService;
  }





  public void mapEntityToForm(PwaApplicationDetail applicationDetail, PartnerLettersForm form) {

  }


  /**
   * From the form extract form data and file data which should be persisted.
   * Any linked files which are not part of official "save" action are discarded.
   */
  @Transactional
  public void saveEntityUsingForm(PwaApplicationDetail applicationDetail, PartnerLettersForm form) {

  }


  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return false;
  }


  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    partnerLettersValidator.validate(form, bindingResult, pwaApplicationDetail);
    return bindingResult;

  }

}
