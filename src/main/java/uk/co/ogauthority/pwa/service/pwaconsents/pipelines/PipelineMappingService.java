package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

@Service
public class PipelineMappingService {

  public void mapPadPipelineToPipelineDetail(PipelineDetail pipelineDetail, PadPipeline padPipeline) {

    pipelineDetail.setPipelineStatus(padPipeline.getPipelineStatus());
    pipelineDetail.setPipelineStatusReason(padPipeline.getPipelineStatusReason());

    pipelineDetail.setPipelineNumber(padPipeline.getPipelineRef());
    pipelineDetail.setPipelineType(padPipeline.getPipelineType());

    pipelineDetail.setFromLocation(padPipeline.getFromLocation());
    pipelineDetail.setFromCoordinates(padPipeline.getFromCoordinates());
    pipelineDetail.setToLocation(padPipeline.getToLocation());
    pipelineDetail.setToCoordinates(padPipeline.getToCoordinates());

    pipelineDetail.setComponentPartsDesc(padPipeline.getComponentPartsDescription());
    pipelineDetail.setLength(padPipeline.getLength());
    pipelineDetail.setMaxExternalDiameter(padPipeline.getMaxExternalDiameter());
    pipelineDetail.setProductsToBeConveyed(padPipeline.getProductsToBeConveyed());
    pipelineDetail.setTrenchedBuriedFilledFlag(padPipeline.getTrenchedBuriedBackfilled());
    pipelineDetail.setTrenchingMethodsDesc(padPipeline.getTrenchingMethodsDescription());

    pipelineDetail.setPipelineMaterial(padPipeline.getPipelineMaterial());
    pipelineDetail.setOtherPipelineMaterialUsed(padPipeline.getOtherPipelineMaterialUsed());
    pipelineDetail.setPipelineFlexibility(padPipeline.getPipelineFlexibility());
    pipelineDetail.setPipelineDesignLife(padPipeline.getPipelineDesignLife());

    pipelineDetail.setPipelineInBundle(padPipeline.getPipelineInBundle());
    pipelineDetail.setBundleName(padPipeline.getBundleName());

  }

}
