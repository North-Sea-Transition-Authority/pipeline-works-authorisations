package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineOtherProperties;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadPipelineOtherPropertiesRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;


/* Service providing simplified API for Pipelines Other Properties app form */
@Service
public class PadPipelineOtherPropertiesService implements ApplicationFormSectionService {

  private final PadPipelineOtherPropertiesRepository padPipelineOtherPropertiesRepository;
  private final PipelineOtherPropertiesMappingService pipelineOtherPropertiesMappingService;
  private final SpringValidatorAdapter groupValidator;

  @Autowired
  public PadPipelineOtherPropertiesService(
      PadPipelineOtherPropertiesRepository padPipelineOtherPropertiesRepository,
      PipelineOtherPropertiesMappingService pipelineOtherPropertiesMappingService,
      SpringValidatorAdapter groupValidator) {
    this.padPipelineOtherPropertiesRepository = padPipelineOtherPropertiesRepository;
    this.pipelineOtherPropertiesMappingService = pipelineOtherPropertiesMappingService;
    this.groupValidator = groupValidator;
  }


  public PadPipelineOtherProperties getPipelineOtherPropertiesEntity(PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineOtherPropertiesRepository.findByPwaApplicationDetail(pwaApplicationDetail).get();
  }


  // Entity/Form Mapping
  public void mapEntityToForm(PipelineOtherPropertiesForm form, PadPipelineOtherProperties entity) {
    pipelineOtherPropertiesMappingService.mapEntityToForm(form, entity);
  }

  public void saveEntityUsingForm(PipelineOtherPropertiesForm form, PadPipelineOtherProperties entity) {
    pipelineOtherPropertiesMappingService.mapFormToEntity(form, entity);
    padPipelineOtherPropertiesRepository.save(entity);
  }



  // Validation / Checking
  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return true;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult,
                                ValidationType validationType, PwaApplicationDetail pwaApplicationDetail) {
    if (validationType.equals(ValidationType.PARTIAL)) {
      groupValidator.validate(form, bindingResult, PartialValidation.class);
    } else {
      groupValidator.validate(form, bindingResult, FullValidation.class);
      //validator.validate(form, bindingResult);
    }
    return bindingResult;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return true;
  }





}

