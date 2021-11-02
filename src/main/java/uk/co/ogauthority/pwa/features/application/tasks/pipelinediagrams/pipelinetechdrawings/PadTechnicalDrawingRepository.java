package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadTechnicalDrawingRepository extends CrudRepository<PadTechnicalDrawing, Integer> {

  List<PadTechnicalDrawing> getAllByPwaApplicationDetail(PwaApplicationDetail detail);

  Optional<PadTechnicalDrawing> findByPwaApplicationDetailAndId(PwaApplicationDetail detail, Integer id);

  Optional<PadTechnicalDrawing> findByPwaApplicationDetailAndFile(PwaApplicationDetail detail, PadFile file);

}
