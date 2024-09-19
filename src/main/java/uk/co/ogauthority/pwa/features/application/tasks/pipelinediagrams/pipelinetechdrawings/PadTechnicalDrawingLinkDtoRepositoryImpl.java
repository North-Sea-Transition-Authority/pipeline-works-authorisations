package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class PadTechnicalDrawingLinkDtoRepositoryImpl implements PadTechnicalDrawingLinkDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PadTechnicalDrawingLinkDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<PadPipelineKeyDto> getLinkedPipelineIdsByDetail(PwaApplicationDetail detail) {
    return entityManager.createQuery(
        "SELECT new uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadPipelineKeyDto( " +
            "  ptdl.pipeline.pipeline.id " +
            ", ptdl.pipeline.id " +
            ") " +
            "FROM PadTechnicalDrawing ptd " +
            "JOIN PadTechnicalDrawingLink ptdl ON ptd.id = ptdl.technicalDrawing.id " +
            "WHERE ptd.pwaApplicationDetail = :app_detail ", PadPipelineKeyDto.class)
        .setParameter("app_detail", detail)
        .getResultList();
  }
}
