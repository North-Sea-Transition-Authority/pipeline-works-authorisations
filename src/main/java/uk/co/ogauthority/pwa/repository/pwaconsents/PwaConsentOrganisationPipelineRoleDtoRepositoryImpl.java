package uk.co.ogauthority.pwa.repository.pwaconsents;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PhysicalPipelineState;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
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
      List<PwaConsent> pwaConsents, Pipeline pipeline) {

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
            "  cporl.toLocationIdentInclusionMode, " +
            "  cporl.sectionNumber " +
            ") " +
            "FROM PwaConsentPipelineOrganisationRoleLink cporl " +
            "JOIN PwaConsentOrganisationRole cor ON cporl.pwaConsentOrganisationRole = cor " +
            "JOIN PwaConsent pc ON cor.addedByPwaConsent = pc " +
            "WHERE pc.id  in (:pwaConsentIds) " +
            "AND (cor.endedByPwaConsent IS NULL OR cor.endedByPwaConsent NOT IN (:pwaConsentIds)) " +
            "AND (cporl.endedByPwaConsent IS NULL OR cporl.endedByPwaConsent NOT IN (:pwaConsentIds)) " +
            "AND cporl.pipeline = :pipeline ",
        OrganisationPipelineRoleInstanceDto.class)
        .setParameter("pwaConsentIds", pwaConsents.stream().map(PwaConsent::getId).collect(Collectors.toList()))
        .setParameter("pipeline", pipeline);
    return query.getResultList();

  }

}
