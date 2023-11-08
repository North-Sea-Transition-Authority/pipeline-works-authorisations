package uk.co.ogauthority.pwa.features.application.tasks.generaltech;

import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;


@Service
public class PipelineTechInfoMappingService {



  public void mapEntityToForm(PipelineTechInfoForm form, PadPipelineTechInfo entity) {
    form.setEstimatedAssetLife(entity.getEstimatedAssetLife());
    form.setPipelineDesignedToStandards(entity.getPipelineDesignedToStandards());
    if (BooleanUtils.isTrue(entity.getPipelineDesignedToStandards())) {
      form.setPipelineStandardsDescription(entity.getPipelineStandardsDescription());
    }
    form.setCorrosionDescription(entity.getCorrosionDescription());
    form.setPlannedPipelineTieInPoints(entity.getPlannedPipelineTieInPoints());
    if (BooleanUtils.isTrue(entity.getPlannedPipelineTieInPoints())) {
      form.setTieInPointsDescription(entity.getTieInPointsDescription());
    }
  }



  public void mapFormToEntity(PipelineTechInfoForm form, PadPipelineTechInfo entity) {

    entity.setEstimatedAssetLife(form.getEstimatedAssetLife());
    entity.setPipelineDesignedToStandards(form.getPipelineDesignedToStandards());

    entity.setPipelineStandardsDescription(form.getPipelineStandardsDescription());
    // if pipeline standards radio is null or false, null out whatever was entered for description
    Optional.ofNullable(form.getPipelineDesignedToStandards())
        .filter(BooleanUtils::isFalse)
        .ifPresent(b -> entity.setPipelineStandardsDescription(null));

    entity.setCorrosionDescription(form.getCorrosionDescription());
    entity.setPlannedPipelineTieInPoints(form.getPlannedPipelineTieInPoints());

    entity.setTieInPointsDescription(form.getTieInPointsDescription());
    // if tie in points radio is null or false, null out description
    Optional.ofNullable(form.getPlannedPipelineTieInPoints())
        .filter(BooleanUtils::isFalse)
        .ifPresent(b -> entity.setTieInPointsDescription(null));

  }





}

