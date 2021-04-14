package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.tasklist;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.SetPipelineReferenceController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.submission.PadPipelineNumberingService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@Service
public class RegulatorPipelineReferenceTaskService {

  private final PadPipelineNumberingService padPipelineNumberingService;
  private final PipelineDetailService pipelineDetailService;

  @Autowired
  public RegulatorPipelineReferenceTaskService(PadPipelineNumberingService padPipelineNumberingService,
                                               PipelineDetailService pipelineDetailService) {
    this.padPipelineNumberingService = padPipelineNumberingService;
    this.pipelineDetailService = pipelineDetailService;
  }

  public boolean pipelineTaskAccessible(Set<PwaApplicationPermission> pwaApplicationPermissionSet,
                                        PadPipeline padPipeline) {
    if (!pwaApplicationPermissionSet.contains(PwaApplicationPermission.SET_PIPELINE_REFERENCE)) {
      return false;
    }

    return !pipelineDetailService.isPipelineConsented(padPipeline.getPipeline());

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
            "Set pipeline reference",
            ReverseRouter.route(on(SetPipelineReferenceController.class).renderSetPipelineReference(
                applicationContext.getApplicationType(),
                applicationContext.getMasterPwaApplicationId(),
                padPipelineTaskListHeader.getPadPipelineId(),
                null)),
            taskIsComplete,
            5)
    );

  }

}
