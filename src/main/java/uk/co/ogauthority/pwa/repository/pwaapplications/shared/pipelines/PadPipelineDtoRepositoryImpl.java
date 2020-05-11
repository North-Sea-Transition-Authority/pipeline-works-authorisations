package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines;

import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public class PadPipelineDtoRepositoryImpl implements PadPipelineDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PadPipelineDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<PadPipelineOverviewDto> findAllAsOverviewDtoByPwaApplicationDetail(PwaApplicationDetail detail) {
    return entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineOverviewDto(" +
            "  pp.pwaApplicationDetail" +
            ", pp" +
            ", COUNT(ppi)" +
            ") " +
            "FROM uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline pp " +
            "LEFT JOIN uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent ppi " +
              "ON pp.id = ppi.padPipeline.id " +
            "WHERE pp.pwaApplicationDetail = :detail " +
            "GROUP BY pp.pwaApplicationDetail, pp", PadPipelineOverviewDto.class)
        .setParameter("detail", detail)
        .getResultList();
  }
}
