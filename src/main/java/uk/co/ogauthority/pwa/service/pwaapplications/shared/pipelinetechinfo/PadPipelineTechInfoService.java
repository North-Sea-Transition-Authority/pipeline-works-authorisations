package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineTechInfo;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineTechInfoForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadPipelineTechInfoRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PipelineTechInfoValidator;


/* Service providing simplified API for Pipelines General Technical Information app form */
@Service
public class PadPipelineTechInfoService implements ApplicationFormSectionService {

  private final PadPipelineTechInfoRepository padPipelineTechInfoRepository;
  private final PipelineTechInfoMappingService pipelineTechInfoMappingService;
  private final SpringValidatorAdapter groupValidator;
  private final PipelineTechInfoValidator pipelineTechInfoValidator;

  @Autowired
  public PadPipelineTechInfoService(
      PadPipelineTechInfoRepository padPipelineTechInfoRepository,
      PipelineTechInfoMappingService pipelineTechInfoMappingService,
      SpringValidatorAdapter groupValidator,
      PipelineTechInfoValidator pipelineTechInfoValidator) {
    this.padPipelineTechInfoRepository = padPipelineTechInfoRepository;
    this.pipelineTechInfoMappingService = pipelineTechInfoMappingService;
    this.groupValidator = groupValidator;
    this.pipelineTechInfoValidator = pipelineTechInfoValidator;
  }


  public PadPipelineTechInfo getPipelineTechInfoEntity(PwaApplicationDetail pwaApplicationDetail) {
    var pipelineTechInfo = padPipelineTechInfoRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadPipelineTechInfo());
    pipelineTechInfo.setPwaApplicationDetail(pwaApplicationDetail);
    return pipelineTechInfo;
  }


  // Entity/Form Mapping
  public void mapEntityToForm(PipelineTechInfoForm form, PadPipelineTechInfo entity) {
    pipelineTechInfoMappingService.mapEntityToForm(form, entity);
  }

  public void saveEntityUsingForm(PipelineTechInfoForm form, PadPipelineTechInfo entity) {
    pipelineTechInfoMappingService.mapFormToEntity(form, entity);
    padPipelineTechInfoRepository.save(entity);
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
      pipelineTechInfoValidator.validate(form, bindingResult);
    }
    return bindingResult;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return true;
  }





}

