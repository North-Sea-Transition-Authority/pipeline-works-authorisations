package uk.co.ogauthority.pwa.features.application.tasks.huoo;

import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class PadOrganisationRolesDtoRepositoryImpl implements PadOrganisationRolesDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PadOrganisationRolesDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }


  @Override
  public List<OrganisationRoleInstanceDto> findOrganisationRoleDtoByPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {

    var query = entityManager.createQuery("SELECT " +
            "new uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleInstanceDto( " +
            "  por.organisationUnit.ouId " +
            ", '' " + // migration name will never be available on app roles
            ", por.agreement " +
            ", por.role " +
            ", por.type ) " +
            "FROM PadOrganisationRole por " +
            "WHERE por.pwaApplicationDetail = :detail",
        OrganisationRoleInstanceDto.class)
        .setParameter("detail", pwaApplicationDetail);

    return query.getResultList();
  }

  @Override
  public List<OrganisationPipelineRoleInstanceDto> findActiveOrganisationPipelineRolesByPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    var query = entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto( " +
            "  por.organisationUnit.ouId, " +
            "  por.agreement, " +
            "  por.role, " +
            "  por.type, " +
            "  pporl.pipeline.id, " +
            "  pporl.fromLocation, " +
            "  pporl.fromLocationIdentInclusionMode, " +
            "  pporl.toLocation, " +
            "  pporl.toLocationIdentInclusionMode, " +
            "  pporl.sectionNumber " +
            ") " +
            "FROM PadOrganisationRole por " +
            "LEFT JOIN PadPipelineOrganisationRoleLink pporl ON pporl.padOrgRole = por " +
            "WHERE por.pwaApplicationDetail  = :pwaApplicationDetail ",
        OrganisationPipelineRoleInstanceDto.class)
        .setParameter("pwaApplicationDetail", pwaApplicationDetail);
    return query.getResultList();
  }


}
