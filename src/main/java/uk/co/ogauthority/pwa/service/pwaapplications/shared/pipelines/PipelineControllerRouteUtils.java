package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.function.Supplier;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelinesController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.util.FlashUtils;

public class PipelineControllerRouteUtils {

  public static ModelAndView ifAllowedFromOverview(PwaApplicationContext applicationContext,
                                                   RedirectAttributes redirectAttributes,
                                                   Supplier<ModelAndView> modelAndViewIfAllowed) {
    var padPipeline = applicationContext.getPadPipeline();
    switch (padPipeline.getPipelineStatus()) {
      case NEVER_LAID:
      case RETURNED_TO_SHORE:
        FlashUtils.error(redirectAttributes, padPipeline.getPipelineRef() + " information cannot be edited");
        return ReverseRouter.redirect(on(PipelinesController.class)
            .renderPipelinesOverview(applicationContext.getMasterPwaApplicationId(),
                applicationContext.getApplicationType(), null));
      default:
        return modelAndViewIfAllowed.get();
    }
  }

}
