package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineIdent;

@Service
public class PipelineIdentMappingService {

  public void mapIdent(PipelineIdent mapTo,
                       PipelineIdent mapFrom) {

    mapTo.setIdentNo(mapFrom.getIdentNo());

    mapTo.setFromLocation(mapFrom.getFromLocation());

    Optional.ofNullable(mapFrom.getFromCoordinates())
        .ifPresent(fromCoords -> {
          if (fromCoords.hasValue()) {
            mapTo.setFromCoordinates(mapFrom.getFromCoordinates());
          }
        });

    mapTo.setToLocation(mapFrom.getToLocation());

    Optional.ofNullable(mapFrom.getToCoordinates())
        .ifPresent(toCoords -> {
          if (toCoords.hasValue()) {
            mapTo.setToCoordinates(mapFrom.getToCoordinates());
          }
        });

    mapTo.setLength(mapFrom.getLength());
    mapTo.setDefiningStructure(mapFrom.getIsDefiningStructure());

  }

}
