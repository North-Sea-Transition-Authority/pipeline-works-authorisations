package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.mh_debug_prototype;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffableOrgRolePipelineGroup;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.OrganisationRolePipelineGroupView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.PipelineNumbersAndSplits;

@Service
public class DiffableOrgRolePipelineGroupCreator {


  private final PadOrganisationRoleService padOrganisationRoleService;

  @Autowired
  public DiffableOrgRolePipelineGroupCreator(
      PadOrganisationRoleService padOrganisationRoleService) {
    this.padOrganisationRoleService = padOrganisationRoleService;
  }

  //TODO PWA-917 -> change so just takes the object as a param and doesnt call out to PadOrganisationRole Service. (more reuseable)
  public AllRoleDiffablePipelineGroupView getAllRoleViewForApp(PwaApplicationDetail pwaApplicationDetail) {

    var aggViewq = padOrganisationRoleService.getAllOrganisationRolePipelineGroupView(pwaApplicationDetail);


    Map<HuooRole, List<DiffableOrgRolePipelineGroup>> viewList = new HashMap<>();

    for (HuooRole role : HuooRole.values()) {
      var roleShowAllPipelineFlag = aggViewq.hasOnlyOneGroupOfPipelineIdentifiersForRole(
          HuooRole.HOLDER);

      var roleViewList = aggViewq.getOrgRolePipelineGroupView(role).stream()
          .map(o -> createDiffableView(o, roleShowAllPipelineFlag))
          .collect(Collectors.toList());

      viewList.put(role, roleViewList);

    }

    return new AllRoleDiffablePipelineGroupView(

        viewList.get(HuooRole.HOLDER),
        viewList.get(HuooRole.USER),
        viewList.get(HuooRole.OPERATOR),
        viewList.get(HuooRole.OWNER)
    );

  }


  // TODO PWA-917 add tests which cover how allPipelineOverrideFlag changes behaviour
  public DiffableOrgRolePipelineGroup createDiffableView(OrganisationRolePipelineGroupView orgRolePipelineGroupView,
                                                         boolean allPipelineOverrideFlag) {

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

    List<String> pipelineNumbersAndSplitsStr;

    if (allPipelineOverrideFlag) {
      pipelineNumbersAndSplitsStr = List.of("All pipelines");
    } else {
      pipelineNumbersAndSplitsStr = orgRolePipelineGroupView.getPipelineNumbersAndSplits()
          .stream()
          .filter(Objects::nonNull)
          .map(PipelineNumbersAndSplits::toString)
          .collect(Collectors.toList());
    }

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
}
