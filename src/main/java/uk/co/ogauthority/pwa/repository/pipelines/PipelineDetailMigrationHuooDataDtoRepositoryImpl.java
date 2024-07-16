package uk.co.ogauthority.pwa.repository.pipelines;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;

public class PipelineDetailMigrationHuooDataDtoRepositoryImpl implements PipelineDetailMigrationHuooDataDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PipelineDetailMigrationHuooDataDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<OrganisationPipelineRoleInstanceDto> findHuooMigrationDataByPipelineDetail(PipelineDetail pipelineDetail) {

    var query = entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto( " +
            "  pdmhd.organisationUnitId, " +
            "  pdmhd.manualOrganisationName, " +
            "  pdmhd.huooRole, " +
            "  pd.pipeline.id " +
            ") " +
            "FROM PipelineDetailMigrationHuooData pdmhd " +
            "JOIN PipelineDetail pd ON pdmhd.pipelineDetail = pd " +
            "WHERE pdmhd.pipelineDetail = :pipelineDetail ",
        OrganisationPipelineRoleInstanceDto.class)
        .setParameter("pipelineDetail", pipelineDetail);
    return query.getResultList();
  }

}
