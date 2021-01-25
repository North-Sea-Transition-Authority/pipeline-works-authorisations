package uk.co.ogauthority.pwa.controller.documents.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.documents.generation.HuooGeneratorService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffableOrgRolePipelineGroup;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.OrganisationRolePipelineGroupView;
import uk.co.ogauthority.pwa.service.pwaconsents.orgrolediffablepipelineservices.AllRoleDiffablePipelineGroupView;
import uk.co.ogauthority.pwa.service.pwaconsents.orgrolediffablepipelineservices.DiffableOrgRolePipelineGroupCreator;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

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
  public void getDocumentSectionData_validSectionName_containsGroupViewData() {

    var huooRolePipelineGroupsPadView = new AllOrgRolePipelineGroupsView(
        List.of(createOrgRolePipelineGroupView()),
        List.of(createOrgRolePipelineGroupView()),
        List.of(createOrgRolePipelineGroupView()),
        List.of(createOrgRolePipelineGroupView()));
    when(padOrganisationRoleService.getAllOrganisationRolePipelineGroupView(pwaApplicationDetail))
        .thenReturn(huooRolePipelineGroupsPadView);

    var allRoleDiffablePipelineGroupView = new AllRoleDiffablePipelineGroupView(
        List.of(createDiffableOrgRolePipelineGroup()),
        List.of(createDiffableOrgRolePipelineGroup()),
        List.of(createDiffableOrgRolePipelineGroup()),
        List.of(createDiffableOrgRolePipelineGroup()));
    when(diffableOrgRolePipelineGroupCreator.getDiffableViewForAllOrgRolePipelineGroupView(huooRolePipelineGroupsPadView))
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
