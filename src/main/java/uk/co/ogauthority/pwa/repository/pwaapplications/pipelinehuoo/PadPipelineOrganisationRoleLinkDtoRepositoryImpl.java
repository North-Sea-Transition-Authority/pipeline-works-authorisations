package uk.co.ogauthority.pwa.repository.pwaapplications.pipelinehuoo;

import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

class PadPipelineOrganisationRoleLinkDtoRepositoryImpl implements PadPipelineOrganisationRoleLinkDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PadPipelineOrganisationRoleLinkDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<OrganisationPipelineRoleDto> findOrganisationPipelineRoleDtoByPwaApplicationDetail(
      PwaApplicationDetail applicationDetail) {
    var query = entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleDto( " +
            "  por.organisationUnit.ouId, " +
            "  '', " + // empty migrated name as for apps only valid org unit roles exist
            "  por.role, " +
            "  por.type," +
            "  pporl.pipeline.id" +
            ") " +
            "FROM PadPipelineOrganisationRoleLink pporl " +
            "JOIN PadOrganisationRole por ON pporl.padOrgRole = por " +
            "JOIN PwaApplicationDetail pad ON por.pwaApplicationDetail = pad " +
            "WHERE por.pwaApplicationDetail  = :applicationDetail ",
        OrganisationPipelineRoleDto.class)
        .setParameter("applicationDetail", applicationDetail);
    return query.getResultList();
  }


}
