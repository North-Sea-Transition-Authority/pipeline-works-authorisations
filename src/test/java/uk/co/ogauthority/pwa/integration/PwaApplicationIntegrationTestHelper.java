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
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.PadCampaignWorkSchedule;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.PadCampaignWorkSchedule_;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.PadCampaignWorksPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.PadCampaignWorksPipeline_;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossing;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.PadCrossedBlock;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.PadCrossedBlockOwner;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossing;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossingOwner;
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.PadDesignOpConditions;
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.PadDesignOpConditions_;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.PadEnvironmentalDecommissioning;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.PadEnvironmentalDecommissioning_;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.PadFastTrack;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.PadFastTrack_;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadField;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadField_;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.PadFluidCompositionInfo;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.PadFluidCompositionInfo_;
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PadPipelineTechInfo;
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PadPipelineTechInfo_;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRole_;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadFacility;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadFacility_;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadLocationDetails;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadLocationDetails_;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadDepositDrawingLink;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadDepositDrawingLink_;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadDepositPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadDepositPipeline_;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadPermanentDeposit;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadPermanentDeposit_;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelineOrganisationRoleLink_;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline_;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData_;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent_;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformation;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformation_;
import uk.co.ogauthority.pwa.integration.service.pwaapplications.generic.PwaApplicationVersionContainer;
import uk.co.ogauthority.pwa.integration.service.pwaapplications.generic.SimplePadPipelineContainer;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.files.PadFile_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadMedianLineAgreement_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCableCrossing_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlockOwner_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlock_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossingOwner_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossing_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.options.PadConfirmationOfOption;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.options.PadConfirmationOfOption_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineOtherProperties;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineOtherProperties_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing_;

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

  public List<PadPipelineCrossingOwner> getPadPipelineCrossingOwners(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadPipelineCrossingOwner> criteriaQuery = cb.createQuery(PadPipelineCrossingOwner.class);
    Root<PadPipelineCrossingOwner> crossingOwnerRoot = criteriaQuery.from(PadPipelineCrossingOwner.class);
    Join<PadPipelineCrossingOwner, PadPipelineCrossing> pipelineCrossingJoin = crossingOwnerRoot.join(
        PadPipelineCrossingOwner_.padPipelineCrossing);
    return entityManager.createQuery(
        criteriaQuery.where(cb.equal(pipelineCrossingJoin.get(PadPipelineCrossing_.pwaApplicationDetail), pwaApplicationDetail))
    ).getResultList();

  }

  public Optional<PadCableCrossing> getPadCableCrossing(PwaApplicationDetail pwaApplicationDetail){
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadCableCrossing> criteriaQuery = cb.createQuery(PadCableCrossing.class);
    Root<PadCableCrossing> cableCrossingRoot = criteriaQuery.from(PadCableCrossing.class);

    return getResultOrEmptyOptional(
        PadCableCrossing.class,
        entityManager.createQuery(criteriaQuery.where(
            cb.equal(cableCrossingRoot.get(PadCableCrossing_.pwaApplicationDetail), pwaApplicationDetail)
        ))
    );
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

  public Optional<PadLocationDetails> getPadLocationDetails(PwaApplicationDetail pwaApplicationDetail) {
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

  public List<PadCrossedBlockOwner> getPadCrossedBlockOwners(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadCrossedBlockOwner> criteriaQuery = cb.createQuery(PadCrossedBlockOwner.class);
    Root<PadCrossedBlockOwner> crossedBlockOwnerRoot = criteriaQuery.from(PadCrossedBlockOwner.class);
    Join<PadCrossedBlockOwner, PadCrossedBlock> crossedBlockJoin = crossedBlockOwnerRoot.join(PadCrossedBlockOwner_.padCrossedBlock);

    return entityManager.createQuery(
        criteriaQuery.where(
            cb.equal(crossedBlockJoin.get(PadCrossedBlock_.pwaApplicationDetail), pwaApplicationDetail)
        )
    ).getResultList();

  }

  private Optional<PadMedianLineAgreement> getPadMedianLineAgreement(PwaApplicationDetail pwaApplicationDetail){
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadMedianLineAgreement> criteriaQuery = cb.createQuery(PadMedianLineAgreement.class);
    Root<PadMedianLineAgreement> medianLineAgreementRoot = criteriaQuery.from(PadMedianLineAgreement.class);

    return getResultOrEmptyOptional(
        PadMedianLineAgreement.class,
        entityManager.createQuery(
            criteriaQuery.where(
                cb.equal(medianLineAgreementRoot.get(PadMedianLineAgreement_.pwaApplicationDetail), pwaApplicationDetail)
            )
        )
    );
  }

  public Optional<PadPipelineTechInfo> getPadPipelineTechInfo(PwaApplicationDetail pwaApplicationDetail){
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadPipelineTechInfo> criteriaQuery = cb.createQuery(PadPipelineTechInfo.class);
    Root<PadPipelineTechInfo> techInfoRoot = criteriaQuery.from(PadPipelineTechInfo.class);

    return getResultOrEmptyOptional(
        PadPipelineTechInfo.class,
        entityManager.createQuery(
            criteriaQuery.where(
                cb.equal(techInfoRoot.get(PadPipelineTechInfo_.pwaApplicationDetail), pwaApplicationDetail)
            )
        )
    );
  }

  public Optional<PadDesignOpConditions> getPadDesignOpConditions(PwaApplicationDetail pwaApplicationDetail){
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadDesignOpConditions> criteriaQuery = cb.createQuery(PadDesignOpConditions.class);
    Root<PadDesignOpConditions> designOpConditionsRoot = criteriaQuery.from(PadDesignOpConditions.class);

    return getResultOrEmptyOptional(
        PadDesignOpConditions.class,
        entityManager.createQuery(
            criteriaQuery.where(
                cb.equal(designOpConditionsRoot.get(PadDesignOpConditions_.pwaApplicationDetail), pwaApplicationDetail)
            )
        )
    );
  }

  public List<PadFluidCompositionInfo> getPadFluidCompositionInfo(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadFluidCompositionInfo> criteriaQuery = cb.createQuery(PadFluidCompositionInfo.class);
    Root<PadFluidCompositionInfo> fluidCompositionInfoRoot = criteriaQuery.from(PadFluidCompositionInfo.class);

    return entityManager.createQuery(
        criteriaQuery.where(
            cb.equal(fluidCompositionInfoRoot.get(PadFluidCompositionInfo_.pwaApplicationDetail), pwaApplicationDetail)
        )
    ).getResultList();

  }

  public List<PadPipelineOtherProperties> getPadPipelineOtherProperties(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadPipelineOtherProperties> criteriaQuery = cb.createQuery(PadPipelineOtherProperties.class);
    Root<PadPipelineOtherProperties> otherPropertiesRoot = criteriaQuery.from(PadPipelineOtherProperties.class);

    return entityManager.createQuery(
        criteriaQuery.where(
            cb.equal(otherPropertiesRoot.get(PadPipelineOtherProperties_.pwaApplicationDetail), pwaApplicationDetail)
        )
    ).getResultList();

  }

  public Optional<PadFastTrack> getPadFastTrack(PwaApplicationDetail pwaApplicationDetail){
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadFastTrack> criteriaQuery = cb.createQuery(PadFastTrack.class);
    Root<PadFastTrack> padFastTrackRoot = criteriaQuery.from(PadFastTrack.class);

    return getResultOrEmptyOptional(
        PadFastTrack.class,
        entityManager.createQuery(
            criteriaQuery.where(
                cb.equal(padFastTrackRoot.get(PadFastTrack_.pwaApplicationDetail), pwaApplicationDetail)
            )
        )
    );
  }

  public Optional<PadConfirmationOfOption> getPadConfirmationOfOption(PwaApplicationDetail pwaApplicationDetail){
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadConfirmationOfOption> criteriaQuery = cb.createQuery(PadConfirmationOfOption.class);
    Root<PadConfirmationOfOption> padConfirmationOfOptionRoot = criteriaQuery.from(PadConfirmationOfOption.class);

    return getResultOrEmptyOptional(
        PadConfirmationOfOption.class,
        entityManager.createQuery(
            criteriaQuery.where(
                cb.equal(padConfirmationOfOptionRoot.get(PadConfirmationOfOption_.pwaApplicationDetail), pwaApplicationDetail)
            )
        )
    );
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
    container.setPadCrossedBlockOwners(getPadCrossedBlockOwners(pwaApplicationDetail));
    container.setPadCableCrossing(getPadCableCrossing(pwaApplicationDetail).orElse(null));
    container.setPadPipelineCrossingOwners(getPadPipelineCrossingOwners(pwaApplicationDetail));
    container.setPadMedianLineAgreement(getPadMedianLineAgreement(pwaApplicationDetail).orElse(null));
    container.setPadPipelineTechInfo(getPadPipelineTechInfo(pwaApplicationDetail).orElse(null));
    container.setPadFluidCompositionInfo(getPadFluidCompositionInfo(pwaApplicationDetail));
    container.setPadPipelineOtherProperties(getPadPipelineOtherProperties(pwaApplicationDetail));
    container.setPadDesignOpConditions(getPadDesignOpConditions(pwaApplicationDetail).orElse(null));
    container.setPadFastTrack(getPadFastTrack(pwaApplicationDetail).orElse(null));
    container.setPadConfirmationOfOption(getPadConfirmationOfOption(pwaApplicationDetail).orElse(null));

    return container;

  }

}
