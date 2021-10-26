package uk.co.ogauthority.pwa.integration.service.pwaapplications.generic;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.PadEnvironmentalDecommissioning;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.PadFastTrack;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadField;
import uk.co.ogauthority.pwa.model.entity.devuk.PadFacility;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.Chemical;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadLocationDetails;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorksPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCableCrossing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlockOwner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossingOwner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.options.PadConfirmationOfOption;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawingLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadDesignOpConditions;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadFluidCompositionInfo;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineOtherProperties;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineTechInfo;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;

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

  private List<PadField> padFields;

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

  public List<PadField> getPadFields() {
    return padFields;
  }

  public void setPadFields(List<PadField> padFields) {
    this.padFields = padFields;
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
