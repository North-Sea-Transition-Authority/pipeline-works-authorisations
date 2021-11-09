package uk.co.ogauthority.pwa.features.application.submission;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class PadPipelineNumberingRepositoryImpl implements PadPipelineNumberingRepository {

  private final EntityManager entityManager;

  @Autowired
  public PadPipelineNumberingRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<PadPipeline> getNonConsentedPipelines(PwaApplicationDetail pwaApplicationDetail) {
    return entityManager.createQuery("" +
        "SELECT pp " +
        "FROM PadPipeline pp " +
        "WHERE pp.id NOT IN (" +
        "  SELECT pp2.id " +
        "  FROM PadPipeline pp2 " +
        "  JOIN PipelineDetail pd ON pd.pipeline = pp2.pipeline " +
        ") AND pp.pwaApplicationDetail = :detail ", PadPipeline.class)
        .setParameter("detail", pwaApplicationDetail)
        .getResultList();
  }

  @Override
  public Long getNextPipelineReferenceNumber() {
    return ((BigDecimal) entityManager.createNativeQuery("" +
        "SELECT pipeline_numbering_seq.nextval value " +
        "FROM dual ")
        .getSingleResult()).longValue();
  }


}
