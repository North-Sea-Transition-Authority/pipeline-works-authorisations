package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.service.diff.DiffService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffableOrgRolePipelineGroup;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.OrganisationRolePipelineGroupView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.PipelineNumbersAndSplits;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;

/**
 * Construct summary of HUOO information for a given application.
 */
@Service
public class HuooSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;
  private final DiffService diffService;

  @Autowired
  public HuooSummaryService(
      TaskListService taskListService,
      PadOrganisationRoleService padOrganisationRoleService,
      PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService,
      DiffService diffService) {
    this.taskListService = taskListService;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
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
        pwaApplicationDetail.getMasterPwaApplication());

    var diffedAllOrgRolePipelineGroups = getDiffedViewUsingSummaryViews(huooRolePipelineGroupsPadView, huooRolePipelineGroupsConsentedView);

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


  public DiffedAllOrgRolePipelineGroups getDiffedViewUsingSummaryViews(AllOrgRolePipelineGroupsView huooRolePipelineGroupsPadView,
                                                                       AllOrgRolePipelineGroupsView huooRolePipelineGroupsConsentedView) {

    var appHolders = huooRolePipelineGroupsPadView.getHolderOrgRolePipelineGroups().stream()
        .map(this::createDiffableView)
        .collect(Collectors.toList());
    var consentedHolders = huooRolePipelineGroupsConsentedView.getHolderOrgRolePipelineGroups().stream()
        .map(this::createDiffableView)
        .collect(Collectors.toList());
    var diffedHolders = diffService.diffComplexLists(appHolders, consentedHolders, this::findOrgRoleOwner, this::findOrgRoleOwner);

    var appUsers = huooRolePipelineGroupsPadView.getUserOrgRolePipelineGroups().stream()
        .map(this::createDiffableView)
        .collect(Collectors.toList());
    var consentedUsers = huooRolePipelineGroupsConsentedView.getUserOrgRolePipelineGroups().stream()
        .map(this::createDiffableView)
        .collect(Collectors.toList());
    var diffedUsers = diffService.diffComplexLists(appUsers, consentedUsers, this::findOrgRoleOwner, this::findOrgRoleOwner);

    var appOperators = huooRolePipelineGroupsPadView.getOperatorOrgRolePipelineGroups().stream()
        .map(this::createDiffableView)
        .collect(Collectors.toList());

    var consentedOperators = huooRolePipelineGroupsConsentedView.getOperatorOrgRolePipelineGroups().stream()
        .map(this::createDiffableView)
        .collect(Collectors.toList());
    var diffedOperators = diffService.diffComplexLists(appOperators, consentedOperators, this::findOrgRoleOwner, this::findOrgRoleOwner);


    var appOwners = huooRolePipelineGroupsPadView.getOwnerOrgRolePipelineGroups().stream()
        .map(this::createDiffableView)
        .collect(Collectors.toList());
    var consentedOwners = huooRolePipelineGroupsConsentedView.getOwnerOrgRolePipelineGroups().stream()
        .map(this::createDiffableView)
        .collect(Collectors.toList());
    var diffedOwners = diffService.diffComplexLists(appOwners, consentedOwners, this::findOrgRoleOwner, this::findOrgRoleOwner);


    return new DiffedAllOrgRolePipelineGroups(
        diffedHolders,
        diffedUsers,
        diffedOperators,
        diffedOwners);
  }


  @VisibleForTesting
  DiffableOrgRolePipelineGroup createDiffableView(OrganisationRolePipelineGroupView orgRolePipelineGroupView) {

    var orgName = orgRolePipelineGroupView.getManuallyEnteredName();
    var hasCompanyData = false;
    var isManuallyEnteredName = false;
    var companyAddress = "";
    var companyNumber = "";
    var treatyAgreementText = "";

    if (orgRolePipelineGroupView.getHuooType() == HuooType.PORTAL_ORG && orgRolePipelineGroupView.getOrgUnitDetailDto() != null) {
      orgName = orgRolePipelineGroupView.getCompanyName();
      hasCompanyData = true;
      var orgUnitDetail = orgRolePipelineGroupView.getOrgUnitDetailDto();
      companyAddress = orgUnitDetail.getCompanyAddress() != null ? orgUnitDetail.getCompanyAddress() : "";
      companyNumber = orgUnitDetail.getRegisteredNumber() != null ? orgUnitDetail.getRegisteredNumber() : "";

    } else if (orgRolePipelineGroupView.getHuooType() == HuooType.PORTAL_ORG && orgRolePipelineGroupView.getOrgUnitDetailDto() == null) {
      orgName = orgRolePipelineGroupView.getManuallyEnteredName();
      isManuallyEnteredName = true;

    } else if (orgRolePipelineGroupView.getHuooType() == HuooType.TREATY_AGREEMENT) {
      orgName = orgRolePipelineGroupView.getTreatyAgreement().getCountry();
      treatyAgreementText = orgRolePipelineGroupView.getTreatyAgreement().getAgreementText();
    }

    var pipelineNumbersAndSplitsStr = orgRolePipelineGroupView.getPipelineNumbersAndSplits()
        .stream()
        .filter(Objects::nonNull)
        .map(PipelineNumbersAndSplits::toString)
        .collect(Collectors.toList());

    return new DiffableOrgRolePipelineGroup(
        orgRolePipelineGroupView.getOrganisationRoleOwner(),
        orgName,
        companyAddress,
        companyNumber,
        treatyAgreementText,
        hasCompanyData,
        isManuallyEnteredName,
        pipelineNumbersAndSplitsStr
    );
  }



  private OrganisationRoleOwnerDto findOrgRoleOwner(DiffableOrgRolePipelineGroup diffableOrgRolePipelineGroup) {
    return diffableOrgRolePipelineGroup.getRoleOwner();
  }



}
