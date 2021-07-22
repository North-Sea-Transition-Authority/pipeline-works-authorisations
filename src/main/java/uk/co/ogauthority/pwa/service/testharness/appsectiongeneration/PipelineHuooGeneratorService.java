package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableHuooPipelineOption;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableHuooPipelineService;

@Service
@Profile("development")
public class PipelineHuooGeneratorService {

  private final PadPipelinesHuooService padPipelinesHuooService;
  private final PickableHuooPipelineService pickableHuooPipelineService;


  @Autowired
  public PipelineHuooGeneratorService(
      PadPipelinesHuooService padPipelinesHuooService,
      PickableHuooPipelineService pickableHuooPipelineService) {
    this.padPipelinesHuooService = padPipelinesHuooService;
    this.pickableHuooPipelineService = pickableHuooPipelineService;
  }


  public void generatePipelineHuoos(PwaApplicationDetail pwaApplicationDetail) {

    HuooRole.stream().forEach(huooRole -> {

      var pickedPipelineStrings = padPipelinesHuooService.getSortedPickablePipelineOptionsForApplicationDetail(pwaApplicationDetail, huooRole)
          .stream()
          .map(PickableHuooPipelineOption::getPickableString)
          .collect(Collectors.toSet());

      var orgUnitIds = padPipelinesHuooService.getAvailableOrgUnitDetailsForRole(pwaApplicationDetail, huooRole)
          .stream()
          .map(OrganisationUnitDetailDto::getOrgUnitId)
          .collect(Collectors.toSet());

      var pipelineIdentifiers = pickableHuooPipelineService.getPickedPipelinesFromStrings(
          pwaApplicationDetail, huooRole, pickedPipelineStrings);

      padPipelinesHuooService.updatePipelineHuooLinks(
          pwaApplicationDetail, pipelineIdentifiers, huooRole,
          orgUnitIds.stream()
              .map(OrganisationUnitId::new)
              .collect(Collectors.toSet()),
          null);

    });
  }


}
