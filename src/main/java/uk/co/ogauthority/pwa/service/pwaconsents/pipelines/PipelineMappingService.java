package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineEntity;

@Service
public class PipelineMappingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineMappingService.class);

  public void mapPipelineEntities(PipelineEntity pipelineEntityTo, PipelineEntity pipelineEntityFrom) {

    pipelineEntityTo.setPipelineStatus(pipelineEntityFrom.getPipelineStatus());
    pipelineEntityTo.setPipelineStatusReason(pipelineEntityFrom.getPipelineStatusReason());
    pipelineEntityTo.setPipelineNumber(pipelineEntityFrom.getPipelineNumber());

    if (pipelineEntityFrom.getPipelineType() == null) {
      pipelineEntityTo.setPipelineType(PipelineType.UNKNOWN);
    } else {
      pipelineEntityTo.setPipelineType(pipelineEntityFrom.getPipelineType());
    }

    try {
      pipelineEntityTo.setFromCoordinates(pipelineEntityFrom.getFromCoordinates());
      pipelineEntityTo.setToCoordinates(pipelineEntityFrom.getToCoordinates());
    } catch (NullPointerException npe) {
      LOGGER.debug("PipelineEntity is missing valid coordinates", npe);
    }
    pipelineEntityTo.setFromLocation(pipelineEntityFrom.getFromLocation());
    pipelineEntityTo.setToLocation(pipelineEntityFrom.getToLocation());

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
