package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentData;

@Service
public class PipelineIdentDataMappingService {

  public void mapPipelineIdentData(PipelineIdentData mapTo,
                                   PipelineIdentData mapFrom) {

    mapTo.setComponentPartsDesc(mapFrom.getComponentPartsDesc());
    mapTo.setExternalDiameter(mapFrom.getExternalDiameter());
    mapTo.setInternalDiameter(mapFrom.getInternalDiameter());
    mapTo.setWallThickness(mapFrom.getWallThickness());
    mapTo.setInsulationCoatingType(mapFrom.getInsulationCoatingType());
    mapTo.setMaop(mapFrom.getMaop());
    mapTo.setProductsToBeConveyed(mapFrom.getProductsToBeConveyed());

    mapTo.setExternalDiameterMultiCore(mapFrom.getExternalDiameterMultiCore());
    mapTo.setInternalDiameterMultiCore(mapFrom.getInternalDiameterMultiCore());
    mapTo.setWallThicknessMultiCore(mapFrom.getWallThicknessMultiCore());
    mapTo.setInsulationCoatingTypeMultiCore(mapFrom.getInsulationCoatingTypeMultiCore());
    mapTo.setMaopMultiCore(mapFrom.getMaopMultiCore());
    mapTo.setProductsToBeConveyedMultiCore(mapFrom.getProductsToBeConveyedMultiCore());

  }

}
