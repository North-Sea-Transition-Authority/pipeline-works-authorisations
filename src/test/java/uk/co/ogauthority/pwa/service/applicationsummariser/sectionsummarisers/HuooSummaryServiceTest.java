package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.diff.DiffService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.OrganisationRolePipelineGroupView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.PipelineNumbersAndSplits;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
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


  private DiffService diffService;


  private HuooSummaryService huooSummaryService;
  private PwaApplicationDetail pwaApplicationDetail;

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
    when(pwaConsentOrganisationRoleService.getAllOrganisationRolePipelineGroupView(pwaApplicationDetail.getMasterPwaApplication())).thenReturn(consentedView);

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
  public void getDiffedViewUsingSummaryViews() {
    var portalOrgUnitDetail1 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        new PortalOrganisationUnit(1, "company"), "address", "111");
    var organisationUnitDetail1 = OrganisationUnitDetailDto.from(portalOrgUnitDetail1);
    var organisationRoleOwnerDto1 = OrganisationRoleOwnerDto.fromOrganisationUnitId(new OrganisationUnitId(1));
    var pipelineNumbersAndSplits1 = List.of(new PipelineNumbersAndSplits(new PipelineId(1), "ppl1", null));
    var holderApp = new OrganisationRolePipelineGroupView(
        HuooType.PORTAL_ORG, organisationUnitDetail1, false, null, null, organisationRoleOwnerDto1, pipelineNumbersAndSplits1);

    var portalOrgUnitDetail2 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        new PortalOrganisationUnit(2, "company2"), "address2", "112");
    var organisationUnitDetail2 = OrganisationUnitDetailDto.from(portalOrgUnitDetail2);
    var organisationRoleOwnerDto2 = OrganisationRoleOwnerDto.fromOrganisationUnitId(new OrganisationUnitId(2));
    var pipelineNumbersAndSplits2 = List.of(new PipelineNumbersAndSplits(new PipelineId(2), "ppl2", null));
    var holderConsented = new OrganisationRolePipelineGroupView(
        HuooType.PORTAL_ORG, organisationUnitDetail2, false, null, null, organisationRoleOwnerDto2, pipelineNumbersAndSplits2);

    var padView = new AllOrgRolePipelineGroupsView(List.of(holderApp), List.of(), List.of(), List.of());
    var consentedView = new AllOrgRolePipelineGroupsView(List.of(holderConsented), List.of(), List.of(), List.of());

    var diffedAllOrgRolePipelineGroups = huooSummaryService.getDiffedViewUsingSummaryViews(padView, consentedView);

    var diffedHolder = diffedAllOrgRolePipelineGroups.getHolderOrgRolePipelineGroups().get(0);
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_roleOwner");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_roleOwnerName");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_companyNumber");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_companyAddress");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_hasCompanyData");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_treatyAgreementText");
    assertThat(diffedHolder).containsKey("DiffableOrgRolePipelineGroup_pipelineAndSplitsList");

  }


  @Test
  public void createDiffableView_orgRoleViewHasPortalOrgWithUnitDetail() {

    var portalOrgUnitDetail1 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        new PortalOrganisationUnit(1, "company"), "address", "111");
    var organisationUnitDetail = OrganisationUnitDetailDto.from(portalOrgUnitDetail1);
    var organisationRoleOwnerDto1 = OrganisationRoleOwnerDto.fromOrganisationUnitId(new OrganisationUnitId(1));
    var pipelineNumbersAndSplits = List.of(new PipelineNumbersAndSplits(new PipelineId(1), "ppl1", null));

    var orgGroupView = new OrganisationRolePipelineGroupView(
        HuooType.PORTAL_ORG,
        organisationUnitDetail,
        false,
        null,
        null,
        organisationRoleOwnerDto1,
        pipelineNumbersAndSplits
    );

    var diffableOrgRolePipelineGroup = huooSummaryService.createDiffableView(orgGroupView);

    assertThat(diffableOrgRolePipelineGroup.getRoleOwner()).isEqualTo(organisationRoleOwnerDto1);
    assertThat(diffableOrgRolePipelineGroup.getRoleOwnerName().getValue()).isEqualTo(organisationUnitDetail.getCompanyName());
    assertThat(diffableOrgRolePipelineGroup.getCompanyAddress()).isEqualTo(organisationUnitDetail.getCompanyAddress());
    assertThat(diffableOrgRolePipelineGroup.getCompanyNumber()).isEqualTo(organisationUnitDetail.getRegisteredNumber());
    assertThat(diffableOrgRolePipelineGroup.getTreatyAgreementText()).isEqualTo("");
    assertThat(diffableOrgRolePipelineGroup.hasCompanyData()).isTrue();
    assertThat(diffableOrgRolePipelineGroup.isManuallyEnteredName()).isFalse();
    assertThat(diffableOrgRolePipelineGroup.getPipelineAndSplitsList()).isEqualTo(
        pipelineNumbersAndSplits.stream().map(PipelineNumbersAndSplits::toString).collect(Collectors.toList()));
  }


  @Test
  public void createDiffableView_orgRoleViewHasPortalOrgWithNoUnitDetail() {

    var organisationRoleOwnerDto1 = OrganisationRoleOwnerDto.fromOrganisationUnitId(new OrganisationUnitId(1));
    var pipelineNumbersAndSplits = List.of(new PipelineNumbersAndSplits(new PipelineId(1), "ppl1", null));

    var orgGroupView = new OrganisationRolePipelineGroupView(
        HuooType.PORTAL_ORG,
        null,
        false,
        "manual name",
        null,
        organisationRoleOwnerDto1,
        pipelineNumbersAndSplits
    );

    var diffableOrgRolePipelineGroup = huooSummaryService.createDiffableView(orgGroupView);

    assertThat(diffableOrgRolePipelineGroup.getRoleOwner()).isEqualTo(organisationRoleOwnerDto1);
    assertThat(diffableOrgRolePipelineGroup.getRoleOwnerName().getValue()).isEqualTo(orgGroupView.getManuallyEnteredName());
    assertThat(diffableOrgRolePipelineGroup.getCompanyAddress()).isEqualTo("");
    assertThat(diffableOrgRolePipelineGroup.getCompanyNumber()).isEqualTo("");
    assertThat(diffableOrgRolePipelineGroup.getTreatyAgreementText()).isEqualTo("");
    assertThat(diffableOrgRolePipelineGroup.hasCompanyData()).isFalse();
    assertThat(diffableOrgRolePipelineGroup.isManuallyEnteredName()).isTrue();
    assertThat(diffableOrgRolePipelineGroup.getPipelineAndSplitsList()).isEqualTo(
        pipelineNumbersAndSplits.stream().map(PipelineNumbersAndSplits::toString).collect(Collectors.toList()));
  }


  @Test
  public void createDiffableView_orgRoleViewHasTreaty() {

    var organisationRoleOwnerDto1 = OrganisationRoleOwnerDto.fromTreaty(TreatyAgreement.ANY_TREATY_COUNTRY);
    var pipelineNumbersAndSplits = List.of(new PipelineNumbersAndSplits(new PipelineId(1), "ppl1", null));

    var orgGroupView = new OrganisationRolePipelineGroupView(
        HuooType.TREATY_AGREEMENT,
        null,
        false,
        null,
        TreatyAgreement.ANY_TREATY_COUNTRY,
        organisationRoleOwnerDto1,
        pipelineNumbersAndSplits
    );

    var diffableOrgRolePipelineGroup = huooSummaryService.createDiffableView(orgGroupView);

    assertThat(diffableOrgRolePipelineGroup.getRoleOwner()).isEqualTo(organisationRoleOwnerDto1);
    assertThat(diffableOrgRolePipelineGroup.getRoleOwnerName().getValue()).isEqualTo(orgGroupView.getTreatyAgreement().getCountry());
    assertThat(diffableOrgRolePipelineGroup.getCompanyAddress()).isEqualTo("");
    assertThat(diffableOrgRolePipelineGroup.getCompanyNumber()).isEqualTo("");
    assertThat(diffableOrgRolePipelineGroup.getTreatyAgreementText()).isEqualTo(orgGroupView.getTreatyAgreement().getAgreementText());
    assertThat(diffableOrgRolePipelineGroup.hasCompanyData()).isFalse();
    assertThat(diffableOrgRolePipelineGroup.isManuallyEnteredName()).isFalse();
    assertThat(diffableOrgRolePipelineGroup.getPipelineAndSplitsList()).isEqualTo(
        pipelineNumbersAndSplits.stream().map(PipelineNumbersAndSplits::toString).collect(Collectors.toList()));
  }








}