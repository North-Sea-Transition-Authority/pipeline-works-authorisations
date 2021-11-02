package uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber.controller.SetPipelineNumberController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.PadPipelineTaskListHeader;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.submission.PadPipelineNumberingService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@Service
public class RegulatorPipelineNumberTaskService {

  private final PadPipelineNumberingService padPipelineNumberingService;
  private final PipelineDetailService pipelineDetailService;
  private final SetPipelineNumberFormValidator setPipelineNumberFormValidator;
  private final PipelineMigrationConfigRepository pipelineMigrationConfigRepository;

  @Autowired
  public RegulatorPipelineNumberTaskService(PadPipelineNumberingService padPipelineNumberingService,
                                            PipelineDetailService pipelineDetailService,
                                            SetPipelineNumberFormValidator setPipelineNumberFormValidator,
                                            PipelineMigrationConfigRepository pipelineMigrationConfigRepository) {
    this.padPipelineNumberingService = padPipelineNumberingService;
    this.pipelineDetailService = pipelineDetailService;
    this.setPipelineNumberFormValidator = setPipelineNumberFormValidator;
    this.pipelineMigrationConfigRepository = pipelineMigrationConfigRepository;
  }

  public boolean pipelineTaskAccessible(Set<PwaApplicationPermission> pwaApplicationPermissionSet,
                                        PadPipeline padPipeline) {
    if (!pwaApplicationPermissionSet.contains(PwaApplicationPermission.SET_PIPELINE_REFERENCE)) {
      return false;
    }

    return !pipelineDetailService.isPipelineConsented(padPipeline.getPipeline());

  }

  public void validateForm(PadPipeline padPipeline, SetPipelineNumberForm form, Errors errors) {
    setPipelineNumberFormValidator.validate(form, errors, padPipeline, getValidationHint());
  }

  private SetPipelineNumberValidationConfig getValidationHint() {
    return IterableUtils.toList(pipelineMigrationConfigRepository.findAll()).stream()
        .findFirst()
        .map(c -> SetPipelineNumberValidationConfig.rangeCreate(
            c.getReservedPipelineNumberMin(),
            c.getReservedPipelineNumberMax()
        ))
        .orElseThrow(() -> new PipelineNumberConfigException("Could not find migration pipeline number config"));

  }

  @Transactional
  public void setPipelineNumber(PadPipeline padPipeline, String pipelineReference) {
    padPipelineNumberingService.setManualPipelineReference(padPipeline, pipelineReference);
  }

  public Range<Integer> getPermittedPipelineNumberRange() {
    return getValidationHint().getPipelineNumberRange();
  }

  public Optional<TaskListEntry> getTaskListEntry(PwaApplicationContext applicationContext,
                                                  PadPipelineTaskListHeader padPipelineTaskListHeader) {

    var padPipeline = padPipelineTaskListHeader.getPadPipeline();

    if (!pipelineTaskAccessible(applicationContext.getPermissions(), padPipeline)) {
      return Optional.empty();
    }

    var taskIsComplete = !padPipelineNumberingService.nonConsentedPadPipelineRequiresFullReference(padPipeline);

    return Optional.of(
        new TaskListEntry(
            "Set pipeline number",
            ReverseRouter.route(on(SetPipelineNumberController.class).renderSetPipelineNumber(
                applicationContext.getApplicationType(),
                applicationContext.getMasterPwaApplicationId(),
                padPipelineTaskListHeader.getPadPipelineId(),
                null, null)),
            taskIsComplete,
            5)
    );
  }

}
