package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadDesignOpConditions;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.DesignOpConditionsView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadDesignOpConditionsRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PadDesignOpConditionsValidator;

/* Service providing simplified API for Design and Operating Conditions app form */
@Service
public class PadDesignOpConditionsService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PadDesignOpConditionsService.class);

  private final PadDesignOpConditionsMappingService padDesignOpConditionsMappingService;
  private final PadDesignOpConditionsRepository padDesignOpConditionsRepository;
  private final PadDesignOpConditionsValidator validator;
  private final EntityCopyingService entityCopyingService;


  @Autowired
  public PadDesignOpConditionsService(
      PadDesignOpConditionsMappingService padDesignOpConditionsMappingService,
      PadDesignOpConditionsRepository padDesignOpConditionsRepository,
      PadDesignOpConditionsValidator validator,
      EntityCopyingService entityCopyingService) {
    this.padDesignOpConditionsMappingService = padDesignOpConditionsMappingService;
    this.padDesignOpConditionsRepository = padDesignOpConditionsRepository;
    this.validator = validator;
    this.entityCopyingService = entityCopyingService;
  }


  // Entity/Form  Retrieval/Mapping
  public PadDesignOpConditions getDesignOpConditionsEntity(PwaApplicationDetail pwaApplicationDetail) {
    var designOpConditions = padDesignOpConditionsRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadDesignOpConditions(pwaApplicationDetail));
    return designOpConditions;
  }

  public void mapEntityToForm(DesignOpConditionsForm form, PadDesignOpConditions entity) {
    padDesignOpConditionsMappingService.mapEntityToForm(form, entity);
  }

  public void saveEntityUsingForm(DesignOpConditionsForm form, PadDesignOpConditions entity) {
    padDesignOpConditionsMappingService.mapFormToEntity(form, entity);
    padDesignOpConditionsRepository.save(entity);
  }

  public DesignOpConditionsView getDesignOpConditionsView(PwaApplicationDetail pwaApplicationDetail) {
    return new DesignOpConditionsView(getDesignOpConditionsEntity(pwaApplicationDetail));
  }




  // Validation / Checking
  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var designOpConditionsEntity = getDesignOpConditionsEntity(detail);
    var designOpConditionsForm = new DesignOpConditionsForm();
    mapEntityToForm(designOpConditionsForm, designOpConditionsEntity);
    BindingResult bindingResult = new BeanPropertyBindingResult(designOpConditionsForm, "form");
    validate(designOpConditionsForm, bindingResult, ValidationType.FULL, detail);

    return !bindingResult.hasErrors();
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult,
                                ValidationType validationType, PwaApplicationDetail pwaApplicationDetail) {
    validator.validate(form, bindingResult, validationType);
    return bindingResult;
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {

    entityCopyingService.duplicateEntityAndSetParent(
        () -> padDesignOpConditionsRepository.findByPwaApplicationDetail(fromDetail)
        .orElseThrow(() ->
            new PwaEntityNotFoundException("Expected to find Design op conditions but did not. pad_id: " + fromDetail.getId())),
        toDetail,
        PadDesignOpConditions.class
    );

  }
}

