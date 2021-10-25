package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.model.diff.DiffedField;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.diff.DiffService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffableOrgRolePipelineGroup;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.OrganisationRolePipelineGroupView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.PipelineNumbersAndSplits;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.orgrolediffablepipelineservices.DiffableOrgRolePipelineGroupCreator;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class HuooSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  @Mock
  private PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  @Mock
  private DiffableOrgRolePipelineGroupCreator diffableOrgRolePipelineGroupCreator;

  private DiffService diffService;

  private HuooSummaryService huooSummaryService;

  private PwaApplicationDetail pwaApplicationDetail;
  private static int ORG_1_ID = 1;
  private static int ORG_2_ID = 2;
  private static int ORG_3_ID = 3;
  private static int PIPELINE_ID_1 = 1;
  private static int PIPELINE_ID_2 = 2;
  private static int PIPELINE_ID_3 = 3;

  @Before
  public void setUp() {
    diffService = new DiffService();
    huooSummaryService = new HuooSummaryService(taskListService, padOrganisationRoleService,
        pwaConsentOrganisationRoleService, diffableOrgRolePipelineGroupCreator, diffService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }


  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(huooSummaryService.canSummarise(pwaApplicationDetail)).isTrue();

  }


  @Test
  public void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(any(), eq(pwaApplicationDetail))).thenReturn(true);
    assertThat(huooSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenTaskNotShown() {
    assertThat(huooSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseSection_verifyServiceInteractions() {

    var padView = new AllOrgRolePipelineGroupsView(List.of(), List.of(), List.of(), List.of());
    when(padOrganisationRoleService.getAllOrganisationRolePipelineGroupView(pwaApplicationDetail)).thenReturn(padView);

    var consentedView = new AllOrgRolePipelineGroupsView(List.of(), List.of(), List.of(), List.of());
    when(pwaConsentOrganisationRoleService.getAllOrganisationRolePipelineGroupView(pwaApplicationDetail.getMasterPwa())).thenReturn(consentedView);

    var appSummary = huooSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);
    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(2);
    assertThat(appSummary.getTemplateModel()).containsKey("diffedAllOrgRolePipelineGroups");
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.HUOO.getDisplayName()));

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.HUOO.getDisplayName(), "#huooDetails")
    );

  }


  @Test
  public void getDiffedViewUsingSummaryViews_appRoleOwnerCoversAllApplicationPipelines_andMatchingConsentedRoleOwnerCoversAllConsentedPipelines() {

    var allPipelinesLabelOverride = true;
    var holderApp = createOrgRolePipelineGroupView(ORG_1_ID, PIPELINE_ID_1);
    var appDiffableOrgRolePipelineGroup = createDiffableOrgRolePipelineGroup(holderApp, allPipelinesLabelOverride);
    when(diffableOrgRolePipelineGroupCreator.createDiffableView(holderApp, allPipelinesLabelOverride))
        .thenReturn(appDiffableOrgRolePipelineGroup);

    var holderConsented = createOrgRolePipelineGroupView(ORG_2_ID, PIPELINE_ID_2);
    var consentedDiffableOrgRolePipelineGroup = createDiffableOrgRolePipelineGroup(holderConsented, allPipelinesLabelOverride);
    when(diffableOrgRolePipelineGroupCreator.createDiffableView(holderConsented, allPipelinesLabelOverride))
        .thenReturn(consentedDiffableOrgRolePipelineGroup);


    var padView = new AllOrgRolePipelineGroupsView(List.of(holderApp), List.of(), List.of(), List.of());
    var consentedView = new AllOrgRolePipelineGroupsView(List.of(holderConsented), List.of(), List.of(), List.of());

    var diffedAllOrgRolePipelineGroups = huooSummaryService.getDiffedViewUsingSummaryViews(
        padView, consentedView, HuooSummaryService.PipelineLabelAction.REDUCE_GROUP_TO_ALL_PIPELINES_LABEL_IF_POSSIBLE);

    var diffedHolder = diffedAllOrgRolePipelineGroups.getHolderOrgRolePipelineGroups().get(0);
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_roleOwner");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_roleOwnerName");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_companyNumber");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_companyAddress");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_hasCompanyData");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_treatyAgreementText");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_pipelineAndSplitsList");

    var actualAllPipelinesLabel = (List<DiffedField>) diffedHolder.get("DiffableOrgRolePipelineGroup_pipelineAndSplitsList");
    assertThat(actualAllPipelinesLabel.get(0).getCurrentValue()).isEqualTo("All pipelines");
    verify(diffableOrgRolePipelineGroupCreator, times(1)).createDiffableView(holderApp, allPipelinesLabelOverride);
    verify(diffableOrgRolePipelineGroupCreator, times(1)).createDiffableView(holderConsented, allPipelinesLabelOverride);

  }


  @Test
  public void getDiffedViewUsingSummaryViews_pipelineLabelActionIsReduceToAllPipelines_allPipelinesOverrideFlagTrue() {

    var allPipelinesLabelOverride = true;
    var holderApp = createOrgRolePipelineGroupView(ORG_1_ID, PIPELINE_ID_1);
    var appDiffableOrgRolePipelineGroup = createDiffableOrgRolePipelineGroup(holderApp, allPipelinesLabelOverride);
    when(diffableOrgRolePipelineGroupCreator.createDiffableView(holderApp, allPipelinesLabelOverride))
        .thenReturn(appDiffableOrgRolePipelineGroup);

    var holderConsented = createOrgRolePipelineGroupView(ORG_2_ID, PIPELINE_ID_2);
    var consentedDiffableOrgRolePipelineGroup = createDiffableOrgRolePipelineGroup(holderConsented, allPipelinesLabelOverride);
    when(diffableOrgRolePipelineGroupCreator.createDiffableView(holderConsented, allPipelinesLabelOverride))
        .thenReturn(consentedDiffableOrgRolePipelineGroup);


    var padView = new AllOrgRolePipelineGroupsView(List.of(holderApp), List.of(), List.of(), List.of());
    var consentedView = new AllOrgRolePipelineGroupsView(List.of(holderConsented), List.of(), List.of(), List.of());

    var diffedAllOrgRolePipelineGroups = huooSummaryService.getDiffedViewUsingSummaryViews(
        padView, consentedView, HuooSummaryService.PipelineLabelAction.REDUCE_GROUP_TO_ALL_PIPELINES_LABEL_IF_POSSIBLE);

    var diffedHolder = diffedAllOrgRolePipelineGroups.getHolderOrgRolePipelineGroups().get(0);
    var actualAllPipelinesLabel = (List<DiffedField>) diffedHolder.get("DiffableOrgRolePipelineGroup_pipelineAndSplitsList");
    assertThat(actualAllPipelinesLabel.get(0).getCurrentValue()).isEqualTo("All pipelines");
    verify(diffableOrgRolePipelineGroupCreator, times(1)).createDiffableView(holderApp, allPipelinesLabelOverride);
    verify(diffableOrgRolePipelineGroupCreator, times(1)).createDiffableView(holderConsented, allPipelinesLabelOverride);

  }

  @Test
  public void getDiffedViewUsingSummaryViews_pipelineLabelActionIsShowEveryPipeline_allPipelinesOverrideFlagFalse() {

    var allPipelinesLabelOverride = false;
    var holderApp = createOrgRolePipelineGroupView(ORG_1_ID, PIPELINE_ID_1);
    var appDiffableOrgRolePipelineGroup = createDiffableOrgRolePipelineGroup(holderApp, allPipelinesLabelOverride);
    when(diffableOrgRolePipelineGroupCreator.createDiffableView(holderApp, allPipelinesLabelOverride))
        .thenReturn(appDiffableOrgRolePipelineGroup);

    var holderConsented = createOrgRolePipelineGroupView(ORG_2_ID, PIPELINE_ID_2);
    var consentedDiffableOrgRolePipelineGroup = createDiffableOrgRolePipelineGroup(holderConsented, allPipelinesLabelOverride);
    when(diffableOrgRolePipelineGroupCreator.createDiffableView(holderConsented, allPipelinesLabelOverride))
        .thenReturn(consentedDiffableOrgRolePipelineGroup);


    var padView = new AllOrgRolePipelineGroupsView(List.of(holderApp), List.of(), List.of(), List.of());
    var consentedView = new AllOrgRolePipelineGroupsView(List.of(holderConsented), List.of(), List.of(), List.of());

    huooSummaryService.getDiffedViewUsingSummaryViews(padView, consentedView, HuooSummaryService.PipelineLabelAction.SHOW_EVERY_PIPELINE_WITHIN_GROUP);

    verify(diffableOrgRolePipelineGroupCreator, times(1)).createDiffableView(holderApp, allPipelinesLabelOverride);
    verify(diffableOrgRolePipelineGroupCreator, times(1)).createDiffableView(holderConsented, allPipelinesLabelOverride);
  }


  @Test
  public void getDiffedViewUsingSummaryViews_appAndConsentedRoleOwnersHaveNoPipelines() {

    var allPipelinesLabelOverride = false;
    var holderApp = createOrgRolePipelineGroupViewWithNoPipelines(ORG_1_ID);
    var appDiffableOrgRolePipelineGroup = createDiffableOrgRolePipelineGroup(holderApp, allPipelinesLabelOverride);
    when(diffableOrgRolePipelineGroupCreator.createDiffableView(holderApp, allPipelinesLabelOverride))
        .thenReturn(appDiffableOrgRolePipelineGroup);

    var holderConsented = createOrgRolePipelineGroupViewWithNoPipelines(ORG_2_ID);
    var consentedDiffableOrgRolePipelineGroup = createDiffableOrgRolePipelineGroup(holderConsented, allPipelinesLabelOverride);
    when(diffableOrgRolePipelineGroupCreator.createDiffableView(holderConsented, allPipelinesLabelOverride))
        .thenReturn(consentedDiffableOrgRolePipelineGroup);


    var padView = new AllOrgRolePipelineGroupsView(List.of(holderApp), List.of(), List.of(), List.of());
    var consentedView = new AllOrgRolePipelineGroupsView(List.of(holderConsented), List.of(), List.of(), List.of());

    var diffedAllOrgRolePipelineGroups = huooSummaryService.getDiffedViewUsingSummaryViews(
        padView, consentedView, HuooSummaryService.PipelineLabelAction.REDUCE_GROUP_TO_ALL_PIPELINES_LABEL_IF_POSSIBLE);

    var diffedHolder = diffedAllOrgRolePipelineGroups.getHolderOrgRolePipelineGroups().get(0);
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_roleOwner");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_roleOwnerName");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_companyNumber");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_companyAddress");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_hasCompanyData");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_treatyAgreementText");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_pipelineAndSplitsList");

    verify(diffableOrgRolePipelineGroupCreator, times(1)).createDiffableView(holderApp, allPipelinesLabelOverride);
    verify(diffableOrgRolePipelineGroupCreator, times(1)).createDiffableView(holderConsented, allPipelinesLabelOverride);

  }


  @Test
  public void getDiffedViewUsingSummaryViews_appRoleOwnerCoversAllApplicationPipelines_andMatchingConsentedRoleOwnerDoesNotCoverAllConsentedPipelines() {

    var allPipelinesLabelOverride = false;
    var operatorApp1 = createOrgRolePipelineGroupView(ORG_1_ID, PIPELINE_ID_1);
    var appDiffableOrgRolePipelineGroup = createDiffableOrgRolePipelineGroup(operatorApp1, allPipelinesLabelOverride);
    when(diffableOrgRolePipelineGroupCreator.createDiffableView(operatorApp1, allPipelinesLabelOverride))
        .thenReturn(appDiffableOrgRolePipelineGroup);

    var operatorApp2 = createOrgRolePipelineGroupView(ORG_2_ID, PIPELINE_ID_2);
    var appDiffableOrgRolePipelineGroup2 = createDiffableOrgRolePipelineGroup(operatorApp2, allPipelinesLabelOverride);
    when(diffableOrgRolePipelineGroupCreator.createDiffableView(operatorApp2, allPipelinesLabelOverride))
        .thenReturn(appDiffableOrgRolePipelineGroup2);

    var operatorConsented = createOrgRolePipelineGroupView(ORG_3_ID, PIPELINE_ID_3);
    var consentedDiffableOrgRolePipelineGroup = createDiffableOrgRolePipelineGroup(operatorConsented, allPipelinesLabelOverride);
    when(diffableOrgRolePipelineGroupCreator.createDiffableView(operatorConsented, allPipelinesLabelOverride))
        .thenReturn(consentedDiffableOrgRolePipelineGroup);


    var padView = new AllOrgRolePipelineGroupsView(List.of(), List.of(), List.of(operatorApp1, operatorApp2), List.of());
    var consentedView = new AllOrgRolePipelineGroupsView(List.of(), List.of(), List.of(operatorConsented), List.of());

    var diffedAllOrgRolePipelineGroups = huooSummaryService.getDiffedViewUsingSummaryViews(
        padView, consentedView, HuooSummaryService.PipelineLabelAction.REDUCE_GROUP_TO_ALL_PIPELINES_LABEL_IF_POSSIBLE);

    var diffedHolder = diffedAllOrgRolePipelineGroups.getOperatorOrgRolePipelineGroups().get(0);
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_roleOwner");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_roleOwnerName");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_companyNumber");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_companyAddress");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_hasCompanyData");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_treatyAgreementText");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_pipelineAndSplitsList");

    verify(diffableOrgRolePipelineGroupCreator, times(1)).createDiffableView(operatorApp1, allPipelinesLabelOverride);
    verify(diffableOrgRolePipelineGroupCreator, times(1)).createDiffableView(operatorApp2, allPipelinesLabelOverride);

  }


  private OrganisationRolePipelineGroupView createOrgRolePipelineGroupView(int orgId, int pipelineId) {
    var portalOrgUnitDetail1 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        PortalOrganisationTestUtils.generateOrganisationUnit(orgId, "company" + orgId), "address" + orgId, "11" + orgId);
    var organisationUnitDetail1 = OrganisationUnitDetailDto.from(portalOrgUnitDetail1);
    var organisationRoleOwnerDto1 = OrganisationRoleOwnerDto.fromOrganisationUnitId(new OrganisationUnitId(1));
    var pipelineNumbersAndSplits1 = List.of(new PipelineNumbersAndSplits(new PipelineId(pipelineId), "ppl" + pipelineId, null));
    return new OrganisationRolePipelineGroupView(
        HuooType.PORTAL_ORG, organisationUnitDetail1, false, null, null, organisationRoleOwnerDto1, pipelineNumbersAndSplits1);

  }

  private OrganisationRolePipelineGroupView createOrgRolePipelineGroupViewWithNoPipelines(int orgId) {
    var portalOrgUnitDetail1 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        PortalOrganisationTestUtils.generateOrganisationUnit(orgId, "company" + orgId), "address" + orgId, "11" + orgId);
    var organisationUnitDetail1 = OrganisationUnitDetailDto.from(portalOrgUnitDetail1);
    var organisationRoleOwnerDto1 = OrganisationRoleOwnerDto.fromOrganisationUnitId(new OrganisationUnitId(1));
    return new OrganisationRolePipelineGroupView(
        HuooType.PORTAL_ORG, organisationUnitDetail1, false, null, null, organisationRoleOwnerDto1, List.of());

  }

  private DiffableOrgRolePipelineGroup createDiffableOrgRolePipelineGroup(
      OrganisationRolePipelineGroupView orgRolePipelineGroupView, boolean allPipelinesLabelOverride) {
    return new DiffableOrgRolePipelineGroup(
        orgRolePipelineGroupView.getOrganisationRoleOwner(),
        orgRolePipelineGroupView.getOrgUnitDetailDto().getCompanyName(),
        orgRolePipelineGroupView.getOrgUnitDetailDto().getCompanyAddress(),
        orgRolePipelineGroupView.getOrgUnitDetailDto().getCompanyAddress(),
        false,
        null,
        !orgRolePipelineGroupView.getIsManuallyEnteredName(),
        orgRolePipelineGroupView.getIsManuallyEnteredName(),
        allPipelinesLabelOverride ? List.of("All pipelines") :
            orgRolePipelineGroupView.getPipelineNumbersAndSplits().stream().map(PipelineNumbersAndSplits::toString).collect(Collectors.toList())
    );
  }










}