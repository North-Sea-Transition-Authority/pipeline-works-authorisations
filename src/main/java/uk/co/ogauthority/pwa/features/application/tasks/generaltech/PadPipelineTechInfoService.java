package uk.co.ogauthority.pwa.features.application.tasks.generaltech;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;


/* Service providing simplified API for Pipelines General Technical Information app form */
@Service
public class PadPipelineTechInfoService implements ApplicationFormSectionService {

  private final PadPipelineTechInfoRepository padPipelineTechInfoRepository;
  private final PipelineTechInfoMappingService pipelineTechInfoMappingService;
  private final PipelineTechInfoValidator pipelineTechInfoValidator;
  private final EntityCopyingService entityCopyingService;

  @Autowired
  public PadPipelineTechInfoService(
      PadPipelineTechInfoRepository padPipelineTechInfoRepository,
      PipelineTechInfoMappingService pipelineTechInfoMappingService,
      PipelineTechInfoValidator pipelineTechInfoValidator,
      EntityCopyingService entityCopyingService) {
    this.padPipelineTechInfoRepository = padPipelineTechInfoRepository;
    this.pipelineTechInfoMappingService = pipelineTechInfoMappingService;
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
    return new GeneralTechInfoView(entity.getEstimatedAssetLife(),
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
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    pipelineTechInfoValidator.validate(form, bindingResult, validationType, pwaApplicationDetail.getResourceType());
    return bindingResult;
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

