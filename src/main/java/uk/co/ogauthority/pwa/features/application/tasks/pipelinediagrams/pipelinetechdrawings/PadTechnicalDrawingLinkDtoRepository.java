package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadTechnicalDrawingLinkDtoRepository {

  List<PadPipelineKeyDto> getLinkedPipelineIdsByDetail(PwaApplicationDetail detail);

}
