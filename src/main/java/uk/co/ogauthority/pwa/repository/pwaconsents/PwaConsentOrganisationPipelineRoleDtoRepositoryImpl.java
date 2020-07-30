package uk.co.ogauthority.pwa.repository.pwaconsents;

import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;

public class PwaConsentOrganisationPipelineRoleDtoRepositoryImpl implements PwaConsentOrganisationPipelineRoleDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PwaConsentOrganisationPipelineRoleDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<OrganisationPipelineRoleInstanceDto> findActiveOrganisationPipelineRolesByMasterPwa(MasterPwa masterPwa) {
    var query = entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleInstanceDto( " +
            "  cor.organisationUnitId, " +
            "  cor.migratedOrganisationName, " +
            "  cor.agreement, " +
            "  cor.role, " +
            "  cor.type, " +
            "  cporl.pipeline.id, " +
            "  cporl.fromLocation, " +
            "  cporl.fromLocationIdentInclusionMode, " +
            "  cporl.toLocation, " +
            "  cporl.toLocationIdentInclusionMode " +
            ") " +
            "FROM PwaConsentPipelineOrganisationRoleLink cporl " +
            "JOIN PwaConsentOrganisationRole cor ON cporl.pwaConsentOrganisationRole = cor " +
            "JOIN PwaConsent pc ON cor.addedByPwaConsent = pc " +
            "WHERE pc.masterPwa  = :masterPwa " +
            "AND cor.endedByPwaConsent IS NULL " +
            "AND cporl.endedByPwaConsent IS NULL ",
        OrganisationPipelineRoleInstanceDto.class)
        .setParameter("masterPwa", masterPwa);
    return query.getResultList();

  }
}
