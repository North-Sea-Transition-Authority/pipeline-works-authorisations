package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.controller.PipelinesTaskListController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.util.FlashUtils;

public class PipelineControllerRouteUtils {

  private PipelineControllerRouteUtils() {
    throw new AssertionError();
  }

  @VisibleForTesting
  static final Set<PipelineStatus> disallowedStatuses = Set.of(
      PipelineStatus.NEVER_LAID, PipelineStatus.RETURNED_TO_SHORE
  );

  public static ModelAndView ifAllowedFromOverviewOrRedirect(PwaApplicationContext applicationContext,
                                                             RedirectAttributes redirectAttributes,
                                                             Supplier<ModelAndView> modelAndViewIfAllowed) {
    var padPipeline = applicationContext.getPadPipeline();
    if (!isAccessible(padPipeline.getPipelineStatus())) {
      FlashUtils.error(redirectAttributes, padPipeline.getPipelineRef() + " information cannot be edited");
      return ReverseRouter.redirect(on(PipelinesTaskListController.class)
          .renderPipelinesOverview(applicationContext.getMasterPwaApplicationId(),
              applicationContext.getApplicationType(), null));
    }
    return modelAndViewIfAllowed.get();
  }

  public static ModelAndView ifAllowedFromOverviewOrError(PwaApplicationContext applicationContext,
                                                          Supplier<ModelAndView> modelAndViewIfAllowed) {
    var padPipeline = applicationContext.getPadPipeline();
    if (!isAccessible(padPipeline.getPipelineStatus())) {
      throw new AccessDeniedException(String.format(
          "PadPipeline with ID: [%s] is not allowed to access the endpoint with PipelineStatus of [%s]. Valid statuses are: %s",
          padPipeline.getId(),
          padPipeline.getPipelineStatus().name(),
          PipelineStatus.streamCurrentStatusesInOrder()
              .filter(PipelineControllerRouteUtils::isAccessible)
              .map(Enum::name)
              .collect(Collectors.joining(", "))
      ));
    }
    return modelAndViewIfAllowed.get();
  }

  @VisibleForTesting
  static boolean isAccessible(PipelineStatus pipelineStatus) {
    return !disallowedStatuses.contains(pipelineStatus);
  }

}
