package uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings;

import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadTechnicalDrawingLinkDtoRepository {

  List<Integer> getLinkedPipelineIdsByDetail(PwaApplicationDetail detail);

}
