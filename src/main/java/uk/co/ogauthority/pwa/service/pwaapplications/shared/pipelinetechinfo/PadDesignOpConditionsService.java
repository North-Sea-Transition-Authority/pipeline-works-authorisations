package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadDesignOpConditions;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadDesignOpConditionsRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PadDesignOpConditionsValidator;

/* Service providing simplified API for Technical Information Fluid Composition app form */
@Service
public class PadDesignOpConditionsService implements ApplicationFormSectionService {

  private final PadDesignOpConditionsMappingService padDesignOpConditionsMappingService;
  private final PadDesignOpConditionsRepository padDesignOpConditionsRepository;
  private final PadDesignOpConditionsValidator validator;


  @Autowired
  public PadDesignOpConditionsService(
      PadDesignOpConditionsMappingService padDesignOpConditionsMappingService,
      PadDesignOpConditionsRepository padDesignOpConditionsRepository,
      PadDesignOpConditionsValidator validator) {
    this.padDesignOpConditionsMappingService = padDesignOpConditionsMappingService;
    this.padDesignOpConditionsRepository = padDesignOpConditionsRepository;
    this.validator = validator;
  }


  // Entity/Form  Retrieval/Mapping
  public PadDesignOpConditions getDesignOpConditionsEntity(PwaApplicationDetail pwaApplicationDetail) {
    var designOpConditions = padDesignOpConditionsRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadDesignOpConditions());
    designOpConditions.setPwaApplicationDetail(pwaApplicationDetail);
    return designOpConditions;
  }

  public void mapEntityToForm(DesignOpConditionsForm form, PadDesignOpConditions entity) {
    padDesignOpConditionsMappingService.mapEntityToForm(form, entity);
  }

  public void saveEntityUsingForm(DesignOpConditionsForm form, PadDesignOpConditions entity) {
    padDesignOpConditionsMappingService.mapFormToEntity(form, entity);
    padDesignOpConditionsRepository.save(entity);
  }




  // Validation / Checking
  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return true;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult,
                                ValidationType validationType, PwaApplicationDetail pwaApplicationDetail) {
    if (validationType.equals(ValidationType.FULL)) {
      validator.validate(form, bindingResult, pwaApplicationDetail);
    }
    return bindingResult;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return true;
  }





}

