package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineTechInfo;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineTechInfoForm;


@Service
public class PipelineTechInfoMappingService {



  public void mapEntityToForm(PipelineTechInfoForm form, PadPipelineTechInfo entity) {
    form.setEstimatedFieldLife(entity.getEstimatedFieldLife());
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
    entity.setEstimatedFieldLife(form.getEstimatedFieldLife());
    entity.setPipelineDesignedToStandards(form.getPipelineDesignedToStandards());
    if (form.getPipelineDesignedToStandards()) {
      entity.setPipelineStandardsDescription(form.getPipelineStandardsDescription());
    }
    entity.setCorrosionDescription(form.getCorrosionDescription());
    entity.setPlannedPipelineTieInPoints(form.getPlannedPipelineTieInPoints());
    if (form.getPlannedPipelineTieInPoints()) {
      entity.setTieInPointsDescription(form.getTieInPointsDescription());
    }
  }





}

