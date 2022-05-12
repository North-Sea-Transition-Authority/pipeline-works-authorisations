package uk.co.ogauthority.pwa.repository.pwaconsents;

import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PhysicalPipelineState;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

public class PwaConsentOrganisationPipelineRoleDtoRepositoryImpl implements PwaConsentOrganisationPipelineRoleDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PwaConsentOrganisationPipelineRoleDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<OrganisationPipelineRoleInstanceDto> findActiveOrganisationPipelineRolesByMasterPwa(MasterPwa masterPwa) {
    var importableHuooPipelineStatus = PipelineStatus.getStatusesWithState(PhysicalPipelineState.ON_SEABED);

    var query = entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto( " +
            "  cor.organisationUnitId, " +
            "  cor.migratedOrganisationName, " +
            "  cor.agreement, " +
            "  cor.role, " +
            "  cor.type, " +
            "  cporl.pipeline.id, " +
            "  cporl.fromLocation, " +
            "  cporl.fromLocationIdentInclusionMode, " +
            "  cporl.toLocation, " +
            "  cporl.toLocationIdentInclusionMode, " +
            "  cporl.sectionNumber " +
            ") " +
            "FROM PwaConsentPipelineOrganisationRoleLink cporl " +
            "JOIN PipelineDetail pd ON cporl.pipeline = pd.pipeline " +
            "JOIN PwaConsentOrganisationRole cor ON cporl.pwaConsentOrganisationRole = cor " +
            "JOIN PwaConsent pc ON cor.addedByPwaConsent = pc " +
            "WHERE pc.masterPwa  = :masterPwa " +
            "AND cor.endedByPwaConsent IS NULL " +
            "AND cporl.endedByPwaConsent IS NULL " +
            "AND pd.tipFlag = TRUE " +
            "AND pd.pipelineStatus IN :importableHuooPipelineStatus ",
        OrganisationPipelineRoleInstanceDto.class)
        .setParameter("masterPwa", masterPwa)
        .setParameter("importableHuooPipelineStatus", importableHuooPipelineStatus);
    return query.getResultList();

  }

  @Override
  public List<OrganisationPipelineRoleInstanceDto> findActiveOrganisationPipelineRolesByPwaConsent(
      Collection<PwaConsent> pwaConsents, Pipeline pipeline) {

    var query = entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto( " +
            "  cor.organisationUnitId, " +
            "  cor.migratedOrganisationName, " +
            "  cor.agreement, " +
            "  cor.role, " +
            "  cor.type, " +
            "  cporl.pipeline.id, " +
            "  cporl.fromLocation, " +
            "  cporl.fromLocationIdentInclusionMode, " +
            "  cporl.toLocation, " +
            "  cporl.toLocationIdentInclusionMode, " +
            "  cporl.sectionNumber " +
            ") " +
            "FROM PwaConsentPipelineOrganisationRoleLink cporl " +
            "JOIN PwaConsentOrganisationRole cor ON cporl.pwaConsentOrganisationRole = cor " +
            "WHERE cporl.addedByPwaConsent IN (:pwaConsents) " +
            "AND (cor.endedByPwaConsent IS NULL OR cor.endedByPwaConsent NOT IN (:pwaConsents)) " +
            "AND (cporl.endedByPwaConsent IS NULL OR cporl.endedByPwaConsent NOT IN (:pwaConsents)) " +
            "AND cporl.pipeline = :pipeline ",
        OrganisationPipelineRoleInstanceDto.class)
        .setParameter("pwaConsents", pwaConsents)
        .setParameter("pipeline", pipeline);
    return query.getResultList();

  }

}
