package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.orgrolediffablepipelineservices.DiffableOrgRolePipelineGroupCreator;

@Service
public class HuooGeneratorService implements DocumentSectionGenerator {


  private final PadOrganisationRoleService padOrganisationRoleService;
  private final DiffableOrgRolePipelineGroupCreator diffableOrgRolePipelineGroupCreator;

  @Autowired
  public HuooGeneratorService(
      PadOrganisationRoleService padOrganisationRoleService,
      DiffableOrgRolePipelineGroupCreator diffableOrgRolePipelineGroupCreator) {
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.diffableOrgRolePipelineGroupCreator = diffableOrgRolePipelineGroupCreator;
  }


  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail) {

    var huooRolePipelineGroupsPadView = padOrganisationRoleService.getAllOrganisationRolePipelineGroupView(pwaApplicationDetail);
    var allRolePipelineGroupView = diffableOrgRolePipelineGroupCreator.getDiffableViewForAllOrgRolePipelineGroupView(
        huooRolePipelineGroupsPadView);

    Map<String, Object> modelMap = Map.of(
        "sectionName", DocumentSection.HUOO.getDisplayName(),
        "allRolePipelineGroupView", allRolePipelineGroupView
    );

    return new DocumentSectionData("documents/consents/sections/huoos", modelMap);
  }

}
