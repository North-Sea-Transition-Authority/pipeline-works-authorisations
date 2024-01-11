package uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.controller.CableCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.controller.CarbonStorageAreaCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.controller.BlockCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.controller.MedianLineCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.controller.PipelineCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.types.controller.CrossingTypesController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;

@Service
public class CrossingAgreementsTaskListService {

  private final ApplicationContext applicationContext;

  @Autowired
  public CrossingAgreementsTaskListService(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public ApplicationFormSectionService getServiceBean(CrossingAgreementTask task) {
    return applicationContext.getBean(task.getSectionClass());
  }

  public String getRoute(PwaApplicationDetail detail, CrossingAgreementTask task) {
    switch (task) {
      case LICENCE_AND_BLOCKS:
        return ReverseRouter.route(on(BlockCrossingController.class).renderBlockCrossingOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case MEDIAN_LINE:
        return ReverseRouter.route(on(MedianLineCrossingController.class).renderMedianLineOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null));
      case CARBON_STORAGE_AREAS:
        return ReverseRouter.route(on(CarbonStorageAreaCrossingController.class).renderCarbonStorageCrossingOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case CROSSING_TYPES:
        return ReverseRouter.route(on(CrossingTypesController.class).renderForm(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case CABLE_CROSSINGS:
        return ReverseRouter.route(on(CableCrossingController.class).renderOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case PIPELINE_CROSSINGS:
        return ReverseRouter.route(on(PipelineCrossingController.class).renderOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      default:
        throw new ActionNotAllowedException("Cannot get route for task: " + task.name());
    }
  }

  public ModelAndView getOverviewRedirect(PwaApplicationDetail detail, CrossingAgreementTask task) {
    switch (task) {
      case LICENCE_AND_BLOCKS:
        return ReverseRouter.redirect(on(BlockCrossingController.class).renderBlockCrossingOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case CARBON_STORAGE_AREAS:
        return ReverseRouter.redirect(on(CarbonStorageAreaCrossingController.class).renderCarbonStorageCrossingOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case MEDIAN_LINE:
        return ReverseRouter.redirect(on(MedianLineCrossingController.class).renderMedianLineOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null));
      case CROSSING_TYPES:
        return ReverseRouter.redirect(on(CrossingTypesController.class).renderForm(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case CABLE_CROSSINGS:
        return ReverseRouter.redirect(on(CableCrossingController.class).renderOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case PIPELINE_CROSSINGS:
        return ReverseRouter.redirect(on(PipelineCrossingController.class).renderOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      default:
        throw new ActionNotAllowedException("Cannot get route for task: " + task.name());
    }
  }
}
