package uk.co.ogauthority.pwa.integration.service.pwaapplications.generic;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.PadCampaignWorksPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossing;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.PadCrossedBlockOwner;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossingOwner;
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.PadDesignOpConditions;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.PadEnvironmentalDecommissioning;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.PadFastTrack;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadLinkedArea;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.PadFluidCompositionInfo;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.Chemical;
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PadPipelineTechInfo;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadFacility;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadLocationDetails;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOption;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.OtherPipelineProperty;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PadPipelineOtherProperties;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadDepositDrawingLink;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadDepositPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Test code only container designed to holder references to each entity linked to a single version of an application.
 */
public class PwaApplicationVersionContainer {
  private final PwaApplicationDetail pwaApplicationDetail;

  private PadProjectInformation padProjectInformation;

  private Map<ApplicationDetailFilePurpose, PadFile> padFiles;

  // assume one
  private Map<HuooRole, ImmutablePair<PadOrganisationRole, PadPipelineOrganisationRoleLink>> huooRolesLookup;

  private PadTechnicalDrawing padTechnicalDrawing;

  private PadTechnicalDrawingLink padTechnicalDrawingLink;

  private SimplePadPipelineContainer simplePadPipelineContainer;

  private PadDepositPipeline padDepositPipeline;

  private PadDepositDrawingLink padDepositDrawingLink;

  private PadCampaignWorksPipeline padCampaignWorksPipeline;

  private PadLocationDetails padLocationDetails;

  private List<PadFacility> padFacilities;

  private List<PadLinkedArea> padLinkedAreas;

  private List<PadCrossedBlockOwner> padCrossedBlockOwners;

  private List<PadPipelineCrossingOwner> padPipelineCrossingOwners;

  private PadEnvironmentalDecommissioning padEnvironmentalDecommissioning;

  private PadCableCrossing padCableCrossing;

  private PadMedianLineAgreement padMedianLineAgreement;

  private PadPipelineTechInfo padPipelineTechInfo;

  private Map<Chemical, PadFluidCompositionInfo> fluidCompositionInfoMap;

  private Map<OtherPipelineProperty, PadPipelineOtherProperties> padPipelineOtherProperties;

  private PadDesignOpConditions padDesignOpConditions;

  private PadFastTrack padFastTrack;

  private PadConfirmationOfOption padConfirmationOfOption;

  public PwaApplicationVersionContainer(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public PadProjectInformation getPadProjectInformation() {
    return padProjectInformation;
  }

  public void setPadProjectInformation(
      PadProjectInformation padProjectInformation) {
    this.padProjectInformation = padProjectInformation;
  }

  public PadFile getPadFile(ApplicationDetailFilePurpose applicationDetailFilePurpose) {
    return this.padFiles.get(applicationDetailFilePurpose);
  }

  public void setPadFiles(Collection<PadFile> padFiles) {
    this.padFiles = padFiles.stream()
        .collect(Collectors.toMap(PadFile::getPurpose, padFile -> padFile));
  }

  public SimplePadPipelineContainer getSimplePadPipelineContainer() {
    return simplePadPipelineContainer;
  }

  public void setSimplePadPipelineContainer(
      SimplePadPipelineContainer simplePadPipelineContainer) {
    this.simplePadPipelineContainer = simplePadPipelineContainer;
  }

  public PadTechnicalDrawing getPadTechnicalDrawing() {
    return padTechnicalDrawing;
  }

  public void setPadTechnicalDrawing(
      PadTechnicalDrawing padTechnicalDrawing) {
    this.padTechnicalDrawing = padTechnicalDrawing;
  }

  public PadTechnicalDrawingLink getPadTechnicalDrawingLink() {
    return padTechnicalDrawingLink;
  }

  public void setPadTechnicalDrawingLink(
      PadTechnicalDrawingLink padTechnicalDrawingLink) {
    this.padTechnicalDrawingLink = padTechnicalDrawingLink;
  }

  public Pair<PadOrganisationRole, PadPipelineOrganisationRoleLink> getHuooRole(HuooRole huooRole) {
    return this.huooRolesLookup.get(huooRole);
  }

  public void setHuooRoles(List<PadPipelineOrganisationRoleLink> pipelineOrgRoleLinks) {
    Map<HuooRole, List<PadPipelineOrganisationRoleLink>> t = pipelineOrgRoleLinks.stream()
        .collect(Collectors.groupingBy(o -> o.getPadOrgRole().getRole()));

    this.huooRolesLookup = new HashMap<>();
    t.forEach((huooRole, padPipelineOrganisationRoleLinkList) -> this.huooRolesLookup.put(
        huooRole,
        ImmutablePair.of(
            padPipelineOrganisationRoleLinkList.get(0).getPadOrgRole(),
            padPipelineOrganisationRoleLinkList.get(0)
        )
    ));
  }

  public PadDepositPipeline getPadDepositPipeline() {
    return padDepositPipeline;
  }

  public void setPadDepositPipeline(
      PadDepositPipeline padDepositPipeline) {
    this.padDepositPipeline = padDepositPipeline;
  }

  public PadCampaignWorksPipeline getPadCampaignWorksPipeline() {
    return padCampaignWorksPipeline;
  }

  public void setPadCampaignWorksPipeline(
      PadCampaignWorksPipeline padCampaignWorksPipeline) {
    this.padCampaignWorksPipeline = padCampaignWorksPipeline;
  }

  public List<PadLinkedArea> getPadFields() {
    return padLinkedAreas;
  }

  public void setPadFields(List<PadLinkedArea> padLinkedAreas) {
    this.padLinkedAreas = padLinkedAreas;
  }

  public PadEnvironmentalDecommissioning getPadEnvironmentalDecommissioning() {
    return padEnvironmentalDecommissioning;
  }

  public void setPadEnvironmentalDecommissioning(
      PadEnvironmentalDecommissioning padEnvironmentalDecommissioning) {
    this.padEnvironmentalDecommissioning = padEnvironmentalDecommissioning;
  }

  public PadDepositDrawingLink getPadDepositDrawingLink() {
    return padDepositDrawingLink;
  }

  public void setPadDepositDrawingLink(
      PadDepositDrawingLink padDepositDrawingLink) {
    this.padDepositDrawingLink = padDepositDrawingLink;
  }

  public PadLocationDetails getPadLocationDetails() {
    return padLocationDetails;
  }

  public void setPadLocationDetails(
      PadLocationDetails padLocationDetails) {
    this.padLocationDetails = padLocationDetails;
  }

  public List<PadFacility> getPadFacilities() {
    return padFacilities;
  }

  public void setPadFacilities(List<PadFacility> padFacilities) {
    this.padFacilities = padFacilities;
  }

  public List<PadCrossedBlockOwner> getPadCrossedBlockOwners() {
    return padCrossedBlockOwners;
  }

  public void setPadCrossedBlockOwners(
      List<PadCrossedBlockOwner> padCrossedBlockOwners) {
    this.padCrossedBlockOwners = padCrossedBlockOwners;
  }

  public PadCableCrossing getPadCableCrossing() {
    return padCableCrossing;
  }

  public void setPadCableCrossing(
      PadCableCrossing padCableCrossing) {
    this.padCableCrossing = padCableCrossing;
  }

  public List<PadPipelineCrossingOwner> getPadPipelineCrossingOwners() {
    return padPipelineCrossingOwners;
  }

  public void setPadPipelineCrossingOwners(
      List<PadPipelineCrossingOwner> padPipelineCrossingOwners) {
    this.padPipelineCrossingOwners = padPipelineCrossingOwners;
  }

  public PadMedianLineAgreement getPadMedianLineAgreement() {
    return padMedianLineAgreement;
  }

  public void setPadMedianLineAgreement(
      PadMedianLineAgreement padMedianLineAgreement) {
    this.padMedianLineAgreement = padMedianLineAgreement;
  }

  public PadPipelineTechInfo getPadPipelineTechInfo() {
    return padPipelineTechInfo;
  }

  public void setPadPipelineTechInfo(
      PadPipelineTechInfo padPipelineTechInfo) {
    this.padPipelineTechInfo = padPipelineTechInfo;
  }

  public void setPadFluidCompositionInfo(List<PadFluidCompositionInfo> padFluidCompositionInfo) {
    this.fluidCompositionInfoMap = padFluidCompositionInfo.stream()
        .collect(
            Collectors.toMap(PadFluidCompositionInfo::getChemicalName,
                fluidCompositionInfo -> fluidCompositionInfo
            )
        );
  }

  public PadFluidCompositionInfo getPadFluidCompositionForChemical(Chemical chemical) {
    return this.fluidCompositionInfoMap.get(chemical);
  }

  public void setPadPipelineOtherProperties(List<PadPipelineOtherProperties> padPipelineOtherProperties) {
    this.padPipelineOtherProperties = padPipelineOtherProperties.stream()
        .collect(Collectors.toMap(
            PadPipelineOtherProperties::getPropertyName,
            pipelineOtherProperties -> pipelineOtherProperties
            )
        );
  }

  public PadPipelineOtherProperties getPadPipelineOtherProperty(OtherPipelineProperty otherPipelineProperty) {
    return this.padPipelineOtherProperties.get(otherPipelineProperty);
  }

  public PadDesignOpConditions getPadDesignOpConditions() {
    return padDesignOpConditions;
  }

  public void setPadDesignOpConditions(PadDesignOpConditions padDesignOpConditions) {
    this.padDesignOpConditions = padDesignOpConditions;
  }

  public PadFastTrack getPadFastTrack() {
    return padFastTrack;
  }

  public void setPadFastTrack(PadFastTrack padFastTrack) {
    this.padFastTrack = padFastTrack;
  }

  public PadConfirmationOfOption getPadConfirmationOfOption() {
    return padConfirmationOfOption;
  }

  public void setPadConfirmationOfOption(
      PadConfirmationOfOption padConfirmationOfOption) {
    this.padConfirmationOfOption = padConfirmationOfOption;
  }
}
