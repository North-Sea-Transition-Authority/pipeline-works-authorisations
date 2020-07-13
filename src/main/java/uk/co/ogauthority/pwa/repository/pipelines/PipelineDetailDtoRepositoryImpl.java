package uk.co.ogauthority.pwa.repository.pipelines;

import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public class PipelineDetailDtoRepositoryImpl implements PipelineDetailDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PipelineDetailDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<PipelineBundlePairDto> getBundleNamesByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return entityManager.createQuery("" +
        "SELECT new uk.co.ogauthority.pwa.repository.pipelines.PipelineBundlePairDto(" +
        "pd.pipeline.id, " +
        "pd.bundleName " +
        ") " +
        "FROM PipelineDetail pd " +
        "JOIN PwaConsent pc ON pd.pwaConsent = pc " +
        "WHERE pd.tipFlag = 1 " +
        "AND pc.masterPwa = :master_pwa " +
        "AND pd.bundleName IS NOT NULL ", PipelineBundlePairDto.class)
        .setParameter("master_pwa", pwaApplicationDetail.getMasterPwaApplication())
        .getResultList();
  }

}
