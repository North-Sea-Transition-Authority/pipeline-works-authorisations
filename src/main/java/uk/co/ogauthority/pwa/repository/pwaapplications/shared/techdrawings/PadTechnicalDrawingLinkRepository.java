package uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;

@Repository
public interface PadTechnicalDrawingLinkRepository extends CrudRepository<PadTechnicalDrawingLink, Integer>,
    PadTechnicalDrawingLinkDtoRepository {

  List<PadTechnicalDrawingLink> getAllByTechnicalDrawingIn(List<PadTechnicalDrawing> technicalDrawings);

  List<PadTechnicalDrawingLink> getAllByTechnicalDrawing(PadTechnicalDrawing drawing);

  List<PadTechnicalDrawingLink> getAllByTechnicalDrawing_PwaApplicationDetailAndPipeline(PwaApplicationDetail detail,
                                                                                         PadPipeline pipeline);

  @EntityGraph(attributePaths = {"technicalDrawing"})
  List<PadTechnicalDrawingLink> getAllByTechnicalDrawing_PwaApplicationDetail(PwaApplicationDetail detail);

}
