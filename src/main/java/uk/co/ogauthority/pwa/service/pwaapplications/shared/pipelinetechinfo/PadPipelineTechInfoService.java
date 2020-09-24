package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo;

import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineTechInfo;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineTechInfoForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.GeneralTechInfoView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadPipelineTechInfoRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PipelineTechInfoValidator;


/* Service providing simplified API for Pipelines General Technical Information app form */
@Service
public class PadPipelineTechInfoService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PadPipelineTechInfoService.class);

  private final PadPipelineTechInfoRepository padPipelineTechInfoRepository;
  private final PipelineTechInfoMappingService pipelineTechInfoMappingService;
  private final SpringValidatorAdapter groupValidator;
  private final PipelineTechInfoValidator pipelineTechInfoValidator;
  private final EntityCopyingService entityCopyingService;

  @Autowired
  public PadPipelineTechInfoService(
      PadPipelineTechInfoRepository padPipelineTechInfoRepository,
      PipelineTechInfoMappingService pipelineTechInfoMappingService,
      SpringValidatorAdapter groupValidator,
      PipelineTechInfoValidator pipelineTechInfoValidator,
      EntityCopyingService entityCopyingService) {
    this.padPipelineTechInfoRepository = padPipelineTechInfoRepository;
    this.pipelineTechInfoMappingService = pipelineTechInfoMappingService;
    this.groupValidator = groupValidator;
    this.pipelineTechInfoValidator = pipelineTechInfoValidator;
    this.entityCopyingService = entityCopyingService;
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

  public GeneralTechInfoView getGeneralTechInfoView(PwaApplicationDetail pwaApplicationDetail) {
    var entity = getPipelineTechInfoEntity(pwaApplicationDetail);
    return new GeneralTechInfoView(entity.getEstimatedFieldLife(),
        entity.getPipelineDesignedToStandards(),
        entity.getPipelineStandardsDescription(),
        entity.getCorrosionDescription(),
        entity.getPlannedPipelineTieInPoints(),
        entity.getTieInPointsDescription());
  }



  // Validation / Checking
  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var pipelineTechInfo = getPipelineTechInfoEntity(detail);
    var pipelineTechInfoForm = new PipelineTechInfoForm();
    mapEntityToForm(pipelineTechInfoForm, pipelineTechInfo);
    BindingResult bindingResult = new BeanPropertyBindingResult(pipelineTechInfoForm, "form");
    validate(pipelineTechInfoForm, bindingResult, ValidationType.FULL, detail);

    return !bindingResult.hasErrors();
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

  @Override
  public void cleanupData(PwaApplicationDetail detail) {

    var techInfo = getPipelineTechInfoEntity(detail);

    if (!techInfo.getPipelineDesignedToStandards()) {
      techInfo.setPipelineStandardsDescription(null);
    }

    if (!techInfo.getPlannedPipelineTieInPoints()) {
      techInfo.setTieInPointsDescription(null);
    }

    padPipelineTechInfoRepository.save(techInfo);

  }

  @Transactional
  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    entityCopyingService.duplicateEntityAndSetParent(
        () -> getPipelineTechInfoEntity(fromDetail),
        toDetail,
        PadPipelineTechInfo.class);
  }

}

