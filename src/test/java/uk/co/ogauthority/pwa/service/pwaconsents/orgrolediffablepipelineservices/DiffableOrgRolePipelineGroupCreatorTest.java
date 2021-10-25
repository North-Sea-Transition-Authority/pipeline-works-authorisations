package uk.co.ogauthority.pwa.service.pwaconsents.orgrolediffablepipelineservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.OrganisationRolePipelineGroupView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.PipelineNumbersAndSplits;



@RunWith(MockitoJUnitRunner.class)
public class DiffableOrgRolePipelineGroupCreatorTest {

  private DiffableOrgRolePipelineGroupCreator diffableOrgRolePipelineGroupCreator;

  @Before
  public void setUp() {
    diffableOrgRolePipelineGroupCreator = new DiffableOrgRolePipelineGroupCreator();
  }


  @Test
  public void createDiffableView_orgRoleViewHasPortalOrgWithUnitDetail_allPipelineOverrideFlagIsFalse() {

    var portalOrgUnitDetail1 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        PortalOrganisationTestUtils.generateOrganisationUnit(1, "company"), "address", "111");
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

    var diffableOrgRolePipelineGroup = diffableOrgRolePipelineGroupCreator.createDiffableView(orgGroupView, false);

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
  public void createDiffableView_orgRoleViewHasPortalOrgWithUnitDetail_allPipelineOverrideFlagIsTrue() {

    var portalOrgUnitDetail1 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        PortalOrganisationTestUtils.generateOrganisationUnit(1, "company"), "address", "111");
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

    var diffableOrgRolePipelineGroup = diffableOrgRolePipelineGroupCreator.createDiffableView(orgGroupView, true);

    assertThat(diffableOrgRolePipelineGroup.getRoleOwner()).isEqualTo(organisationRoleOwnerDto1);
    assertThat(diffableOrgRolePipelineGroup.getRoleOwnerName().getValue()).isEqualTo(organisationUnitDetail.getCompanyName());
    assertThat(diffableOrgRolePipelineGroup.getCompanyAddress()).isEqualTo(organisationUnitDetail.getCompanyAddress());
    assertThat(diffableOrgRolePipelineGroup.getCompanyNumber()).isEqualTo(organisationUnitDetail.getRegisteredNumber());
    assertThat(diffableOrgRolePipelineGroup.getTreatyAgreementText()).isEqualTo("");
    assertThat(diffableOrgRolePipelineGroup.hasCompanyData()).isTrue();
    assertThat(diffableOrgRolePipelineGroup.isManuallyEnteredName()).isFalse();
    assertThat(diffableOrgRolePipelineGroup.getPipelineAndSplitsList()).isEqualTo(List.of("All pipelines"));
  }


  @Test
  public void createDiffableView_orgRoleViewHasPortalOrgWithNoUnitDetail_allPipelineOverrideFlagIsFalse() {

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

    var diffableOrgRolePipelineGroup = diffableOrgRolePipelineGroupCreator.createDiffableView(orgGroupView, false);

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
  public void createDiffableView_orgRoleViewHasTreaty_allPipelineOverrideFlagIsFalse() {

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

    var diffableOrgRolePipelineGroup = diffableOrgRolePipelineGroupCreator.createDiffableView(orgGroupView, false);

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