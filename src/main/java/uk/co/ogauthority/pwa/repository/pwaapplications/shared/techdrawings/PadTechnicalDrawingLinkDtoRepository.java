package uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings;

import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineIdDto;

@Repository
public interface PadTechnicalDrawingLinkDtoRepository {

  List<PipelineIdDto> getLinkedPipelineIdsByDetail(PwaApplicationDetail detail);

}
