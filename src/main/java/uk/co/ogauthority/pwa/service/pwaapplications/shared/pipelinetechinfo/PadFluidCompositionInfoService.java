package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadFluidCompositionInfo;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.FluidCompositionInfoForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadFluidCompositionInfoRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.FluidCompositionInfoValidator;


/* Service providing simplified API for Technical Information Fluid Composition app form */
@Service
public class PadFluidCompositionInfoService implements ApplicationFormSectionService {

  private final PadFluidCompositionInfoRepository padFluidCompositionInfoRepository;
  private final SpringValidatorAdapter groupValidator;
  private final FluidCompositionInfoValidator fluidCompositionInfoValidator;

  @Autowired
  public PadFluidCompositionInfoService(
      PadFluidCompositionInfoRepository padFluidCompositionInfoRepository,
      SpringValidatorAdapter groupValidator,
      FluidCompositionInfoValidator fluidCompositionInfoValidator) {
    this.padFluidCompositionInfoRepository = padFluidCompositionInfoRepository;
    this.groupValidator = groupValidator;
    this.fluidCompositionInfoValidator = fluidCompositionInfoValidator;
  }


  // Entity/Form Mapping
  public void mapEntityToForm(FluidCompositionInfoForm form, PadFluidCompositionInfo entity) {

  }

  public void saveEntityUsingForm(FluidCompositionInfoForm form, PadFluidCompositionInfo entity) {

  }



  // Validation / Checking
  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return true;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult,
                                ValidationType validationType, PwaApplicationDetail pwaApplicationDetail) {
    fluidCompositionInfoValidator.validate(form, bindingResult);
    return bindingResult;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return true;
  }





}

