package uk.co.ogauthority.pwa.integration;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import uk.co.ogauthority.pwa.integration.service.pwaapplications.generic.PwaApplicationVersionContainer;
import uk.co.ogauthority.pwa.integration.service.pwaapplications.generic.SimplePadPipelineContainer;
import uk.co.ogauthority.pwa.model.entity.devuk.PadFacility;
import uk.co.ogauthority.pwa.model.entity.devuk.PadFacility_;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField_;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.files.PadFile_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadEnvironmentalDecommissioning;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadEnvironmentalDecommissioning_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadLocationDetails;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadLocationDetails_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorkSchedule;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorkSchedule_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorksPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorksPipeline_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawingLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawingLink_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole_;

/**
 * Helper class containing methods to access form data for pwa application details common across integration tests.
 */
public class PwaApplicationIntegrationTestHelper {

  private final EntityManager entityManager;

  public PwaApplicationIntegrationTestHelper(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public PadProjectInformation getProjInfo(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    CriteriaQuery<PadProjectInformation> criteriaQuery = cb.createQuery(PadProjectInformation.class);
    Root<PadProjectInformation> projInfo = criteriaQuery.from(PadProjectInformation.class);
    return entityManager.createQuery(
        criteriaQuery
            .where(cb.equal(projInfo.get(PadProjectInformation_.pwaApplicationDetail), pwaApplicationDetail))
    ).getSingleResult();
  }

  public List<PadFile> getAllAppDetailPadFiles(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadFile> criteriaQuery = cb.createQuery(PadFile.class);
    Root<PadFile> padFile = criteriaQuery.from(PadFile.class);
    return entityManager.createQuery(
        criteriaQuery.where(cb.equal(padFile.get(PadFile_.pwaApplicationDetail), pwaApplicationDetail))
    ).getResultList();

  }

  public Optional<SimplePadPipelineContainer> getPadPipeline(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadPipelineIdentData> criteriaQuery = cb.createQuery(PadPipelineIdentData.class);
    Root<PadPipelineIdentData> pipelineIdentDataRoot = criteriaQuery.from(PadPipelineIdentData.class);
    Join<PadPipelineIdentData, PadPipelineIdent> identJoin = pipelineIdentDataRoot.join(
        PadPipelineIdentData_.padPipelineIdent);
    Join<PadPipelineIdent, PadPipeline> padPipelineJoin = identJoin.join(PadPipelineIdent_.padPipeline);

    return getResultOrEmptyOptional(
        PadPipelineIdentData.class,
        entityManager.createQuery(
            criteriaQuery.where(cb.equal(padPipelineJoin.get(PadPipeline_.pwaApplicationDetail), pwaApplicationDetail))
        )
    ).map(SimplePadPipelineContainer::new);

  }

  public Optional<PadTechnicalDrawingLink> getPadTechnicalDrawingLink(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadTechnicalDrawingLink> criteriaQuery = cb.createQuery(PadTechnicalDrawingLink.class);
    Root<PadTechnicalDrawingLink> padTechnicalDrawingLinkRoot = criteriaQuery.from(PadTechnicalDrawingLink.class);
    Join<PadTechnicalDrawingLink, PadTechnicalDrawing> technicalDrawingJoin = padTechnicalDrawingLinkRoot
        .join(PadTechnicalDrawingLink_.technicalDrawing);

    return getResultOrEmptyOptional(
        PadTechnicalDrawingLink.class,
        entityManager.createQuery(criteriaQuery.where(
            cb.equal(technicalDrawingJoin.get(PadTechnicalDrawing_.pwaApplicationDetail), pwaApplicationDetail)
        ))
    );
  }

  public List<PadPipelineOrganisationRoleLink> getPadPipelineLinks(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadPipelineOrganisationRoleLink> criteriaQuery = cb.createQuery(
        PadPipelineOrganisationRoleLink.class);
    Root<PadPipelineOrganisationRoleLink> padPipelineOrganisationRoleLinkRoot = criteriaQuery.from(
        PadPipelineOrganisationRoleLink.class);
    Join<PadPipelineOrganisationRoleLink, PadOrganisationRole> organisationRoleJoin = padPipelineOrganisationRoleLinkRoot
        .join(PadPipelineOrganisationRoleLink_.padOrgRole);

    var result = entityManager.createQuery(
        criteriaQuery.where(
            cb.equal(organisationRoleJoin.get(PadOrganisationRole_.pwaApplicationDetail), pwaApplicationDetail)
        )
    ).getResultList();

    return result;

  }

  private <T> Optional<T> getResultOrEmptyOptional(Class<T> clazz, TypedQuery<T> typedQuery) {
    try {
      return Optional.of(typedQuery.getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }


  }

  public Optional<PadDepositPipeline> getPermanentDepositPipeline(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadDepositPipeline> criteriaQuery = cb.createQuery(PadDepositPipeline.class);
    Root<PadDepositPipeline> depositPipelineRoot = criteriaQuery.from(PadDepositPipeline.class);
    Join<PadDepositPipeline, PadPermanentDeposit> permanentDepositJoin = depositPipelineRoot
        .join(PadDepositPipeline_.padPermanentDeposit);

    return getResultOrEmptyOptional(
        PadDepositPipeline.class,
        entityManager.createQuery(
            criteriaQuery.where(
                cb.equal(permanentDepositJoin.get(PadPermanentDeposit_.pwaApplicationDetail), pwaApplicationDetail)
            )
        )
    );

  }

  public Optional<PadCampaignWorksPipeline> getPadCampaignWorksPipeline(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadCampaignWorksPipeline> criteriaQuery = cb.createQuery(PadCampaignWorksPipeline.class);
    Root<PadCampaignWorksPipeline> campaignWorksPipelineRoot = criteriaQuery.from(PadCampaignWorksPipeline.class);
    Join<PadCampaignWorksPipeline, PadCampaignWorkSchedule> workScheduleJoin = campaignWorksPipelineRoot
        .join(PadCampaignWorksPipeline_.padCampaignWorkSchedule);

    return getResultOrEmptyOptional(
        PadCampaignWorksPipeline.class,
        entityManager.createQuery(
            criteriaQuery.where(
                cb.equal(workScheduleJoin.get(PadCampaignWorkSchedule_.pwaApplicationDetail), pwaApplicationDetail)
            )
        )
    );
  }

  public List<PadField> getPadFields(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadField> criteriaQuery = cb.createQuery(PadField.class);
    Root<PadField> padFieldRoot = criteriaQuery.from(PadField.class);

    return entityManager.createQuery(
        criteriaQuery
            .where(cb.equal(padFieldRoot.get(PadField_.pwaApplicationDetail), pwaApplicationDetail))
    ).getResultList();
  }

  public Optional<PadEnvironmentalDecommissioning> getPadEnvironmentalDecommissioning(
      PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadEnvironmentalDecommissioning> criteriaQuery = cb.createQuery(
        PadEnvironmentalDecommissioning.class);
    Root<PadEnvironmentalDecommissioning> decommissioningRoot = criteriaQuery.from(
        PadEnvironmentalDecommissioning.class);

    return getResultOrEmptyOptional(
        PadEnvironmentalDecommissioning.class,
        entityManager.createQuery(
            criteriaQuery
                .where(cb.equal(decommissioningRoot.get(PadEnvironmentalDecommissioning_.pwaApplicationDetail),
                    pwaApplicationDetail))
        )
    );
  }

  public Optional<PadLocationDetails> getPadLocationDetails(
      PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadLocationDetails> criteriaQuery = cb.createQuery(PadLocationDetails.class);
    Root<PadLocationDetails> locationDetailsRoot = criteriaQuery.from(PadLocationDetails.class);

    return getResultOrEmptyOptional(
        PadLocationDetails.class,
        entityManager.createQuery(
            criteriaQuery.where(
                cb.equal(locationDetailsRoot.get(PadLocationDetails_.pwaApplicationDetail), pwaApplicationDetail)
            )
        )
    );
  }

  public List<PadDepositDrawingLink> getPadDepositDrawingLinks(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadDepositDrawingLink> criteriaQuery = cb.createQuery(PadDepositDrawingLink.class);
    Root<PadDepositDrawingLink> depositDrawingLinkRoot = criteriaQuery.from(PadDepositDrawingLink.class);
    Join<PadDepositDrawingLink, PadPermanentDeposit> permanentDepositJoin = depositDrawingLinkRoot.join(
        PadDepositDrawingLink_.PAD_DEPOSIT_DRAWING);

    return entityManager.createQuery(
        criteriaQuery
            .where(cb.equal(permanentDepositJoin.get(PadPermanentDeposit_.pwaApplicationDetail), pwaApplicationDetail))
    ).getResultList();
  }

  public List<PadFacility> getPadFacilities(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadFacility> criteriaQuery = cb.createQuery(PadFacility.class);
    Root<PadFacility> padFacilityRoot = criteriaQuery.from(PadFacility.class);

    return entityManager.createQuery(
        criteriaQuery
            .where(cb.equal(padFacilityRoot.get(PadFacility_.pwaApplicationDetail), pwaApplicationDetail))
    ).getResultList();
  }

  public PwaApplicationVersionContainer getApplicationDetailContainer(PwaApplicationDetail pwaApplicationDetail) {

    var container = new PwaApplicationVersionContainer(pwaApplicationDetail);
    container.setPadProjectInformation(getProjInfo(pwaApplicationDetail));
    container.setPadFiles(getAllAppDetailPadFiles(pwaApplicationDetail));

    container.setSimplePadPipelineContainer(getPadPipeline(pwaApplicationDetail).orElse(null));
    container.setPadDepositDrawingLink(
        getPadDepositDrawingLinks(pwaApplicationDetail)
            .stream()
            .findFirst()
            .orElse(null)
    );
    container.setPadDepositPipeline(getPermanentDepositPipeline(pwaApplicationDetail).orElse(null));
    container.setPadCampaignWorksPipeline(getPadCampaignWorksPipeline(pwaApplicationDetail).orElse(null));
    container.setPadTechnicalDrawingLink(getPadTechnicalDrawingLink(pwaApplicationDetail).orElse(null));
    container.setPadTechnicalDrawing(
        getPadTechnicalDrawingLink(pwaApplicationDetail)
            .map(PadTechnicalDrawingLink::getTechnicalDrawing)
            .orElse(null)
    );

    container.setHuooRoles(getPadPipelineLinks(pwaApplicationDetail));
    container.setPadFields(getPadFields(pwaApplicationDetail));
    container.setPadEnvironmentalDecommissioning(getPadEnvironmentalDecommissioning(pwaApplicationDetail).orElse(null));
    container.setPadFacilities(getPadFacilities(pwaApplicationDetail));
    container.setPadLocationDetails(getPadLocationDetails(pwaApplicationDetail).orElse(null));

    return container;

  }

}
