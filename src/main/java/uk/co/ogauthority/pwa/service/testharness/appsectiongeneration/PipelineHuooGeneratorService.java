package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineOption;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineService;

@Service
@Profile("test-harness")
class PipelineHuooGeneratorService implements TestHarnessAppFormService {

  private final PadPipelinesHuooService padPipelinesHuooService;
  private final PickableHuooPipelineService pickableHuooPipelineService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.PIPELINES_HUOO;


  @Autowired
  public PipelineHuooGeneratorService(
      PadPipelinesHuooService padPipelinesHuooService,
      PickableHuooPipelineService pickableHuooPipelineService) {
    this.padPipelinesHuooService = padPipelinesHuooService;
    this.pickableHuooPipelineService = pickableHuooPipelineService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var pwaApplicationDetail = appFormServiceParams.getApplicationDetail();

    HuooRole.stream().forEach(huooRole -> {

      var pickedPipelineStrings = padPipelinesHuooService.getSortedPickablePipelineOptionsForApplicationDetail(
          pwaApplicationDetail, huooRole)
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
