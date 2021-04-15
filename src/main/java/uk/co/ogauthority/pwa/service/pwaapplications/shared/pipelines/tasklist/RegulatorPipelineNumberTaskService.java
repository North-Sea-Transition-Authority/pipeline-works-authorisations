package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.tasklist;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.SetPipelineNumberController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.submission.PadPipelineNumberingService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@Service
public class RegulatorPipelineNumberTaskService {

  private final PadPipelineNumberingService padPipelineNumberingService;
  private final PipelineDetailService pipelineDetailService;
  private final SetPipelineNumberFormValidator setPipelineNumberFormValidator;

  @Autowired
  public RegulatorPipelineNumberTaskService(PadPipelineNumberingService padPipelineNumberingService,
                                            PipelineDetailService pipelineDetailService,
                                            SetPipelineNumberFormValidator setPipelineNumberFormValidator) {
    this.padPipelineNumberingService = padPipelineNumberingService;
    this.pipelineDetailService = pipelineDetailService;
    this.setPipelineNumberFormValidator = setPipelineNumberFormValidator;

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
    // TODO PWA-470 make range configurable and enforceable via migration patch.
    return SetPipelineNumberValidationConfig.rangeCreate(
        5000,
        6000
    );
  }

  @Transactional
  public void setPipelineNumber(PadPipeline padPipeline, String pipelineReference) {
    padPipelineNumberingService.setManualPipelineReference(padPipeline, pipelineReference);
  }

  public Range<Integer> getPermittedPipelineNumberRange() {
    return getValidationHint().getPipelineNumberRange();
  }

  Optional<TaskListEntry> getTaskListEntry(PwaApplicationContext applicationContext,
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
