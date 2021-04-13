package uk.co.ogauthority.pwa.repository.pwaconsents;

import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;

public class PwaConsentOrganisationPipelineRoleDtoRepositoryImpl implements PwaConsentOrganisationPipelineRoleDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PwaConsentOrganisationPipelineRoleDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<OrganisationPipelineRoleInstanceDto> findActiveOrganisationPipelineRolesByMasterPwa(MasterPwa masterPwa) {
    var importableHuooPipelineStatus = PipelineStatus.currentStatusSet();

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
  public List<OrganisationPipelineRoleInstanceDto> findActiveOrganisationPipelineRolesByPipelineDetail(
      PipelineDetail pipelineDetail) {

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
            "WHERE pd.id  = :pipelineDetailId " +
            "AND cor.endedByPwaConsent IS NULL " +
            "AND cporl.endedByPwaConsent IS NULL ",
        OrganisationPipelineRoleInstanceDto.class)
        .setParameter("pipelineDetailId", pipelineDetail.getId());
    return query.getResultList();

  }

}
