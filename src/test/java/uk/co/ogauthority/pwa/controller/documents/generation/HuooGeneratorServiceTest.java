package uk.co.ogauthority.pwa.controller.documents.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDepositTestUtil;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.service.documents.generation.DepositsGeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.HuooGeneratorService;
import uk.co.ogauthority.pwa.service.documents.views.DepositTableRowView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.DepositDrawingsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffableOrgRolePipelineGroup;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.OrganisationRolePipelineGroupView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.PipelineNumbersAndSplits;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories.PipelineAndIdentViewFactory;
import uk.co.ogauthority.pwa.service.pwaconsents.orgrolediffablepipelineservices.AllRoleDiffablePipelineGroupView;
import uk.co.ogauthority.pwa.service.pwaconsents.orgrolediffablepipelineservices.DiffableOrgRolePipelineGroupCreator;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class HuooGeneratorServiceTest {

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  @Mock
  private DiffableOrgRolePipelineGroupCreator diffableOrgRolePipelineGroupCreator;

  private PwaApplicationDetail pwaApplicationDetail;

  private HuooGeneratorService huooGeneratorService;


  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL, 1, 1);
    huooGeneratorService = new HuooGeneratorService(padOrganisationRoleService, diffableOrgRolePipelineGroupCreator);
  }




  private OrganisationRolePipelineGroupView createOrgRolePipelineGroupView() {
    return new OrganisationRolePipelineGroupView(
        HuooType.PORTAL_ORG, null, false, null, null, null, null);
  }


  private DiffableOrgRolePipelineGroup createDiffableOrgRolePipelineGroup() {
    return new DiffableOrgRolePipelineGroup(
        null, null, null, null, null, null, false, List.of());
  }



  @Test
  public void getDocumentSectionData_validSectionName_validGroupViewData() {

    var holderPipelineGroupViews = List.of(createOrgRolePipelineGroupView());
    var userPipelineGroupViews = List.of(createOrgRolePipelineGroupView());
    var operatorPipelineGroupViews = List.of(createOrgRolePipelineGroupView());
    var ownerPipelineGroupViews = List.of(createOrgRolePipelineGroupView());
    var huooRolePipelineGroupsPadView = new AllOrgRolePipelineGroupsView(
        holderPipelineGroupViews, userPipelineGroupViews, operatorPipelineGroupViews, ownerPipelineGroupViews);
    when(padOrganisationRoleService.getAllOrganisationRolePipelineGroupView(pwaApplicationDetail))
        .thenReturn(huooRolePipelineGroupsPadView);


    var allRoleDiffablePipelineGroupView = new AllRoleDiffablePipelineGroupView(
        List.of(createDiffableOrgRolePipelineGroup()), List.of(createDiffableOrgRolePipelineGroup()), List.of(createDiffableOrgRolePipelineGroup()), List.of(createDiffableOrgRolePipelineGroup())
      );

    when(diffableOrgRolePipelineGroupCreator.getAllRoleViewForApp(huooRolePipelineGroupsPadView))
        .thenReturn(allRoleDiffablePipelineGroupView);


    var documentSectionData = huooGeneratorService.getDocumentSectionData(pwaApplicationDetail);
    var allRolePipelineGroupView = (AllRoleDiffablePipelineGroupView) documentSectionData.getTemplateModel().get("allRolePipelineGroupView");
    var sectionName = documentSectionData.getTemplateModel().get("sectionName");

    assertThat(sectionName).isEqualTo(DocumentSection.HUOO.getDisplayName());
    assertThat(allRolePipelineGroupView.getHolderOrgRolePipelineGroups()).hasSize(1);
    assertThat(allRolePipelineGroupView.getUserOrgRolePipelineGroups()).hasSize(1);
    assertThat(allRolePipelineGroupView.getOperatorOrgRolePipelineGroups()).hasSize(1);
    assertThat(allRolePipelineGroupView.getOwnerOrgRolePipelineGroups()).hasSize(1);
  }

}
