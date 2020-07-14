package uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings;

import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class PadTechnicalDrawingLinkDtoRepositoryImpl implements PadTechnicalDrawingLinkDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PadTechnicalDrawingLinkDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<Integer> getLinkedPipelineIdsByDetail(PwaApplicationDetail detail) {
    return entityManager.createQuery(
        "SELECT ptdl.pipeline.id " +
            "FROM PadTechnicalDrawing ptd " +
            "JOIN PadTechnicalDrawingLink ptdl ON ptd.id = ptdl.technicalDrawing.id " +
            "WHERE ptd.pwaApplicationDetail = :app_detail ", Integer.class)
        .setParameter("app_detail", detail)
        .getResultList();
  }
}
