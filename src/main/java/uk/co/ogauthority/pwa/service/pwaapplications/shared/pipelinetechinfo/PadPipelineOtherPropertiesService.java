package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyAvailabilityOption;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineOtherProperties;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadPipelineOtherPropertiesRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;


/* Service providing simplified API for Pipelines Other Properties app form */
@Service
public class PadPipelineOtherPropertiesService implements ApplicationFormSectionService {

  private final PadPipelineOtherPropertiesRepository padPipelineOtherPropertiesRepository;
  private final SpringValidatorAdapter groupValidator;

  @Autowired
  public PadPipelineOtherPropertiesService(
      PadPipelineOtherPropertiesRepository padPipelineOtherPropertiesRepository,
      SpringValidatorAdapter groupValidator) {
    this.padPipelineOtherPropertiesRepository = padPipelineOtherPropertiesRepository;
    this.groupValidator = groupValidator;
  }

  // Entity/Form Mapping/Retrieval
  public List<PadPipelineOtherProperties> getPipelineOtherPropertiesEntity(PwaApplicationDetail pwaApplicationDetail) {
    List<PadPipelineOtherProperties> pipelineOtherPropertiesList =
        padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);

    if (pipelineOtherPropertiesList.isEmpty()) {
      for (OtherPipelineProperty property: OtherPipelineProperty.asList()) {
        var padPipelineOtherProperty = new PadPipelineOtherProperties(pwaApplicationDetail, property);
        pipelineOtherPropertiesList.add(padPipelineOtherProperty);
      }
    }

    return pipelineOtherPropertiesList;
  }

  public void mapEntityToForm(PipelineOtherPropertiesForm form, List<PadPipelineOtherProperties> entities) {
    for (PadPipelineOtherProperties entity: entities) {
      var pipelineOtherPropertiesDataForm = new PipelineOtherPropertiesDataForm();
      pipelineOtherPropertiesDataForm.setPropertyAvailabilityOption(entity.getAvailabilityOption());
      if (entity.getAvailabilityOption() != null && entity.getAvailabilityOption().equals(PropertyAvailabilityOption.AVAILABLE)) {
        var minMaxInput = new MinMaxInput(entity.getMinValue(), entity.getMaxValue());
        pipelineOtherPropertiesDataForm.setMinMaxInput(minMaxInput);
      }
      form.addPropertyData(entity.getPropertyName(), pipelineOtherPropertiesDataForm);
    }
  }

  public void saveEntityUsingForm(PipelineOtherPropertiesForm form, List<PadPipelineOtherProperties> entities) {
    for (PadPipelineOtherProperties entity: entities) {
      var pipelineOtherPropertiesDataForm = form.getPropertyDataFormMap().get(entity.getPropertyName());
      entity.setAvailabilityOption(pipelineOtherPropertiesDataForm.getPropertyAvailabilityOption());
      if (pipelineOtherPropertiesDataForm.getPropertyAvailabilityOption() != null
          && pipelineOtherPropertiesDataForm.getPropertyAvailabilityOption().equals(PropertyAvailabilityOption.AVAILABLE)) {
        entity.setMinValue(pipelineOtherPropertiesDataForm.getMinMaxInput().getMinValue());
        entity.setMaxValue(pipelineOtherPropertiesDataForm.getMinMaxInput().getMaxValue());
      }
    }
    padPipelineOtherPropertiesRepository.saveAll(entities);
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

