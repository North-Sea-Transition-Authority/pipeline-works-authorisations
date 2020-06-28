package uk.co.ogauthority.pwa.repository.pwaapplications.huoo;

import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class PadOrganisationRolesDtoRepositoryImpl implements PadOrganisationRolesDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PadOrganisationRolesDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }


  @Override
  public List<OrganisationRoleDto> findOrganisationRoleDtoByPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {

    var query = entityManager.createQuery("SELECT " +
            "new uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDto( " +
            "  por.organisationUnit.ouId " +
            ", '' " + // migration name will never be available on app roles
            ", por.role " +
            ", por.type ) " +
            "FROM PadOrganisationRole por " +
            "WHERE por.pwaApplicationDetail = :detail",
        OrganisationRoleDto.class)
        .setParameter("detail", pwaApplicationDetail);

    return query.getResultList();
  }
}
