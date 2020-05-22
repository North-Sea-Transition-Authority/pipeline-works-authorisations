package uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;

@Repository
public interface PadTechnicalDrawingRepository extends CrudRepository<PadTechnicalDrawing, Integer> {

  List<PadTechnicalDrawing> getAllByPwaApplicationDetail(PwaApplicationDetail detail);

}
