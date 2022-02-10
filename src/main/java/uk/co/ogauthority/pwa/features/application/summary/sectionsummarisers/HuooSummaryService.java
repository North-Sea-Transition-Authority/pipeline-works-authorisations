package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.aggregates.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.diff.DiffService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffableOrgRolePipelineGroup;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.orgrolediffablepipelineservices.DiffableOrgRolePipelineGroupCreator;

/**
 * Construct summary of HUOO information for a given application.
 */
@Service
public class HuooSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;
  private final DiffableOrgRolePipelineGroupCreator diffableOrgRolePipelineGroupCreator;
  private final DiffService diffService;

  @Autowired
  public HuooSummaryService(
      TaskListService taskListService,
      PadOrganisationRoleService padOrganisationRoleService,
      PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService,
      DiffableOrgRolePipelineGroupCreator diffableOrgRolePipelineGroupCreator,
      DiffService diffService) {
    this.taskListService = taskListService;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
    this.diffableOrgRolePipelineGroupCreator = diffableOrgRolePipelineGroupCreator;
    this.diffService = diffService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.HUOO);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var huooRolePipelineGroupsPadView = padOrganisationRoleService.getAllOrganisationRolePipelineGroupView(pwaApplicationDetail);
    var huooRolePipelineGroupsConsentedView = pwaConsentOrganisationRoleService.getAllOrganisationRolePipelineGroupView(
        pwaApplicationDetail.getMasterPwa());

    var diffedAllOrgRolePipelineGroups = getDiffedViewUsingSummaryViews(huooRolePipelineGroupsPadView, huooRolePipelineGroupsConsentedView,
        PipelineLabelAction.REDUCE_GROUP_TO_ALL_PIPELINES_LABEL_IF_POSSIBLE);

    var sectionDisplayText = ApplicationTask.HUOO.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("diffedAllOrgRolePipelineGroups", diffedAllOrgRolePipelineGroups);

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#huooDetails"
        )),
        summaryModel
    );
  }


  private List<Map<String, ?>> getDiffedModelForAppAndConsentForRole(AllOrgRolePipelineGroupsView huooRolePipelineGroupsPadView,
                                                                     AllOrgRolePipelineGroupsView huooRolePipelineGroupsConsentedView,
                                                                     HuooRole role,
                                                                     PipelineLabelAction pipelineLabelAction) {

    //determine weather we need to show 'All pipelines' or each individual pipeline
    boolean allPipelinesLabelOverride = false;
    if (pipelineLabelAction.equals(PipelineLabelAction.REDUCE_GROUP_TO_ALL_PIPELINES_LABEL_IF_POSSIBLE)) {
      var appGroupShowAllPipelineFlag = huooRolePipelineGroupsPadView.hasOnlyOneGroupOfPipelineIdentifiersForRole(role);
      var consentedGroupShowAllPipelineFlag = huooRolePipelineGroupsConsentedView.hasOnlyOneGroupOfPipelineIdentifiersForRole(
          role);

      var appGroupHasPipelines = huooRolePipelineGroupsPadView.getOrgRolePipelineGroupView(role)
          .stream().anyMatch(group -> !group.getPipelineNumbersAndSplits().isEmpty());
      var consentedGroupHasPipelines = huooRolePipelineGroupsConsentedView.getOrgRolePipelineGroupView(role)
          .stream().anyMatch(group -> !group.getPipelineNumbersAndSplits().isEmpty());

      allPipelinesLabelOverride = appGroupHasPipelines && appGroupShowAllPipelineFlag
          && (!consentedGroupHasPipelines || consentedGroupShowAllPipelineFlag);
    }

    //for the given role and for the app & consented versions, extract the org pipeline group view and create a diffable view from it
    boolean finalAllPipelinesLabelOverride = allPipelinesLabelOverride;
    var appDiffableOrgRolePipelineGroup = huooRolePipelineGroupsPadView.getOrgRolePipelineGroupView(role)
        .stream()
        .map(o -> diffableOrgRolePipelineGroupCreator.createDiffableView(o, finalAllPipelinesLabelOverride))
        .collect(Collectors.toList());

    var consentedDiffableOrgRolePipelineGroup = huooRolePipelineGroupsConsentedView.getOrgRolePipelineGroupView(role)
        .stream()
        .map(o -> diffableOrgRolePipelineGroupCreator.createDiffableView(o, finalAllPipelinesLabelOverride))
        .collect(Collectors.toList());

    //diff both the app and consented diffable views
    return diffService.diffComplexLists(
        appDiffableOrgRolePipelineGroup, consentedDiffableOrgRolePipelineGroup, this::findOrgRoleOwner, this::findOrgRoleOwner);
  }


  /**
   * This method takes pad and consented org pipeline groups and diffs them
   * returning each diffed field in a map structure contained within a DiffedAllOrgRolePipelineGroups object.
   * @param huooRolePipelineGroupsPadView the pad version of all the organisation pipeline groups
   * @param huooRolePipelineGroupsConsentedView the consented version of all the organisation pipeline groups
   * @return a diffed version of the pad and consented organisation pipeline groups
   */
  public DiffedAllOrgRolePipelineGroups getDiffedViewUsingSummaryViews(AllOrgRolePipelineGroupsView huooRolePipelineGroupsPadView,
                                                                       AllOrgRolePipelineGroupsView huooRolePipelineGroupsConsentedView,
                                                                       PipelineLabelAction pipelineLabelAction) {

    var diffedHolders = getDiffedModelForAppAndConsentForRole(
        huooRolePipelineGroupsPadView, huooRolePipelineGroupsConsentedView, HuooRole.HOLDER, pipelineLabelAction);
    var diffedUsers = getDiffedModelForAppAndConsentForRole(
        huooRolePipelineGroupsPadView, huooRolePipelineGroupsConsentedView, HuooRole.USER, pipelineLabelAction);
    var diffedOperators = getDiffedModelForAppAndConsentForRole(
        huooRolePipelineGroupsPadView, huooRolePipelineGroupsConsentedView, HuooRole.OPERATOR, pipelineLabelAction);
    var diffedOwners = getDiffedModelForAppAndConsentForRole(
        huooRolePipelineGroupsPadView, huooRolePipelineGroupsConsentedView, HuooRole.OWNER, pipelineLabelAction);

    return new DiffedAllOrgRolePipelineGroups(
        diffedHolders,
        diffedUsers,
        diffedOperators,
        diffedOwners);
  }



  private OrganisationRoleOwnerDto findOrgRoleOwner(DiffableOrgRolePipelineGroup diffableOrgRolePipelineGroup) {
    return diffableOrgRolePipelineGroup.getRoleOwner();
  }


  public enum PipelineLabelAction {
    REDUCE_GROUP_TO_ALL_PIPELINES_LABEL_IF_POSSIBLE,
    SHOW_EVERY_PIPELINE_WITHIN_GROUP
  }



}
