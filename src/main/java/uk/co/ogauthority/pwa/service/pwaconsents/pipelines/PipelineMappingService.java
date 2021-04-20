package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineEntity;

@Service
public class PipelineMappingService {

  public void mapPipelineEntities(PipelineEntity pipelineEntityTo, PipelineEntity pipelineEntityFrom) {

    pipelineEntityTo.setPipelineStatus(pipelineEntityFrom.getPipelineStatus());
    pipelineEntityTo.setPipelineStatusReason(pipelineEntityFrom.getPipelineStatusReason());

    pipelineEntityTo.setPipelineNumber(pipelineEntityFrom.getPipelineNumber());
    pipelineEntityTo.setPipelineType(pipelineEntityFrom.getPipelineType());

    pipelineEntityTo.setFromLocation(pipelineEntityFrom.getFromLocation());
    pipelineEntityTo.setFromCoordinates(pipelineEntityFrom.getFromCoordinates());
    pipelineEntityTo.setToLocation(pipelineEntityFrom.getToLocation());
    pipelineEntityTo.setToCoordinates(pipelineEntityFrom.getToCoordinates());

    pipelineEntityTo.setComponentPartsDescription(pipelineEntityFrom.getComponentPartsDescription());
    pipelineEntityTo.setLength(pipelineEntityFrom.getLength());
    pipelineEntityTo.setMaxExternalDiameter(pipelineEntityFrom.getMaxExternalDiameter());
    pipelineEntityTo.setProductsToBeConveyed(pipelineEntityFrom.getProductsToBeConveyed());
    pipelineEntityTo.setTrenchedBuriedBackfilled(pipelineEntityFrom.getTrenchedBuriedBackfilled());
    pipelineEntityTo.setTrenchingMethodsDescription(pipelineEntityFrom.getTrenchingMethodsDescription());

    pipelineEntityTo.setPipelineMaterial(pipelineEntityFrom.getPipelineMaterial());
    pipelineEntityTo.setOtherPipelineMaterialUsed(pipelineEntityFrom.getOtherPipelineMaterialUsed());
    pipelineEntityTo.setPipelineFlexibility(pipelineEntityFrom.getPipelineFlexibility());
    pipelineEntityTo.setPipelineDesignLife(pipelineEntityFrom.getPipelineDesignLife());

    pipelineEntityTo.setPipelineInBundle(pipelineEntityFrom.getPipelineInBundle());
    pipelineEntityTo.setBundleName(pipelineEntityFrom.getBundleName());

    pipelineEntityTo.setFootnote(pipelineEntityFrom.getFootnote());

  }

}
