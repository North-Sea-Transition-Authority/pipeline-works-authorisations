package uk.co.ogauthority.pwa.validators.pipelinetechinfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesForm;
import uk.co.ogauthority.pwa.util.ValidatorUtils;


@Service
public class PipelineOtherPropertiesValidator implements SmartValidator {

  private PipelineOtherPropertiesDataValidator pipelineOtherPropertiesDataValidator;

  @Autowired
  public PipelineOtherPropertiesValidator(
      PipelineOtherPropertiesDataValidator pipelineOtherPropertiesDataValidator) {
    this.pipelineOtherPropertiesDataValidator = pipelineOtherPropertiesDataValidator;
  }


  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PipelineOtherPropertiesForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var pipelineOtherPropertiesForm = (PipelineOtherPropertiesForm) target;
    var propertyDataFormMap = pipelineOtherPropertiesForm.getPropertyDataFormMap();
    for (var propertyEntry: propertyDataFormMap.entrySet()) {
      ValidatorUtils.invokeNestedValidator(errors, pipelineOtherPropertiesDataValidator,
          "propertyDataFormMap[" + propertyEntry.getKey() + "]", propertyEntry.getValue(), propertyEntry.getKey());
    }
  }


  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
    validate(o, errors);
  }





}
