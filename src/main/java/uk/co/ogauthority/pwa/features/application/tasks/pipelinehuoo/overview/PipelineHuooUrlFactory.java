package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Base64;
import java.util.Collections;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections.controller.SplitPipelineHuooJourneyController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.controller.ModifyPipelineHuooJourneyController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class PipelineHuooUrlFactory {

  private final int applicationId;
  private final PwaApplicationType applicationType;

  public PipelineHuooUrlFactory(int applicationId,
                                PwaApplicationType applicationType) {
    this.applicationId = applicationId;
    this.applicationType = applicationType;
  }

  public String getAddHolderPipelineRoleUrl() {
    return getAddPipelineRoleUrl(HuooRole.HOLDER);
  }

  public String getAddUserPipelineRoleUrl() {
    return getAddPipelineRoleUrl(HuooRole.USER);
  }

  public String getAddOperatorPipelineRoleUrl() {
    return getAddPipelineRoleUrl(HuooRole.OPERATOR);
  }

  public String getAddOwnerPipelineRoleUrl() {
    return getAddPipelineRoleUrl(HuooRole.OWNER);
  }


  private String getAddPipelineRoleUrl(HuooRole huooRole) {
    return ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .renderPipelinesForHuooAssignment(applicationType, applicationId, huooRole, null, null));
  }

  public String getSplitHolderPipelineUrl() {
    return getSplitPipelineRoleUrl(HuooRole.HOLDER);
  }

  public String getSplitUserPipelineUrl() {
    return getSplitPipelineRoleUrl(HuooRole.USER);
  }

  public String getSplitOperatorPipelineUrl() {
    return getSplitPipelineRoleUrl(HuooRole.OPERATOR);
  }

  public String getSplitOwnerPipelineUrl() {
    return getSplitPipelineRoleUrl(HuooRole.OWNER);
  }

  private String getSplitPipelineRoleUrl(HuooRole huooRole) {
    return ReverseRouter.route(on(SplitPipelineHuooJourneyController.class)
        .renderSelectPipelineToSplit(applicationType, applicationId, huooRole, null, null));
  }


  public String changeGroupPipelineOwnersUrl(HuooRole huooRole,
                                             PipelinesAndOrgRoleGroupView pipelinesAndOrgRoleGroupView) {
    return ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroupRouter(
        applicationType,
        applicationId,
        huooRole,
        null,
        ModifyPipelineHuooJourneyController.JourneyPage.ORGANISATION_SELECTION,
        pipelinesAndOrgRoleGroupView.getPipelineIdentifierSet().stream()
            .map(this::getPipelineIdentifierPickableStringAsBase64)
            .collect(Collectors.toSet()),
        pipelinesAndOrgRoleGroupView.getOrganisationIdsOfRoleOwners().stream()
            .map(OrganisationUnitId::asInt)
            .collect(Collectors.toSet()),
        pipelinesAndOrgRoleGroupView.getTreatyAgreementsOfRoleOwners()
    ));
  }

  public String assignUnassignedPipelineOwnersUrl(HuooRole huooRole,
                                                  PipelineHuooRoleSummaryView pipelineHuooRoleSummaryView) {
    return ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroupRouter(
        applicationType,
        applicationId,
        huooRole,
        null,
        ModifyPipelineHuooJourneyController.JourneyPage.PIPELINE_SELECTION,
        pipelineHuooRoleSummaryView.getUnassignedPipelineIds()
            .stream()
            .map(this::getPipelineIdentifierPickableStringAsBase64)
            .collect(Collectors.toSet()),
        Collections.emptySet(),
        Collections.emptySet()
    ));
  }

  private String getPipelineIdentifierPickableStringAsBase64(PipelineIdentifier pipelineIdentifier) {
    var pickableString = PickableHuooPipelineType.createPickableString(pipelineIdentifier);
    // Yuck, workaround as otherise string pickableString is not correctly mapped into Set<String> by controller. dont know why.
    return Base64.getUrlEncoder().encodeToString(pickableString.getBytes());
  }
}
