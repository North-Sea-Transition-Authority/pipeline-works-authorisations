package uk.co.ogauthority.pwa.service.tasklist;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.BlockCrossingController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.CableCrossingController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.CrossingTypesController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.MedianLineCrossingController;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListSection;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingTypesService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadCableCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementService;

@Service
public class CrossingAgreementsTaskListService {

  private final BlockCrossingService blockCrossingService;
  private final PadMedianLineAgreementService padMedianLineAgreementService;
  private final CrossingTypesService crossingTypesService;
  private final PadCableCrossingService padCableCrossingService;

  @Autowired
  public CrossingAgreementsTaskListService(
      BlockCrossingService blockCrossingService,
      PadMedianLineAgreementService padMedianLineAgreementService,
      CrossingTypesService crossingTypesService,
      PadCableCrossingService padCableCrossingService) {
    this.blockCrossingService = blockCrossingService;
    this.padMedianLineAgreementService = padMedianLineAgreementService;
    this.crossingTypesService = crossingTypesService;
    this.padCableCrossingService = padCableCrossingService;
  }

  public TaskListSection getServiceBean(CrossingAgreementTask task) {
    switch (task) {
      case CROSSING_TYPES:
        return crossingTypesService;
      case MEDIAN_LINE:
        return padMedianLineAgreementService;
      case LICENCE_AND_BLOCK_NUMBERS:
        return blockCrossingService;
      case CABLE_CROSSINGS:
        return padCableCrossingService;
      default:
        throw new ActionNotAllowedException("Cannot get service bean for task: " + task.name());
    }
  }

  public String getRoute(PwaApplicationDetail detail, CrossingAgreementTask task) {
    switch (task) {
      case LICENCE_AND_BLOCK_NUMBERS:
        return ReverseRouter.route(on(BlockCrossingController.class).renderBlockCrossingOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case MEDIAN_LINE:
        return ReverseRouter.route(on(MedianLineCrossingController.class).renderMedianLineOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case CROSSING_TYPES:
        return ReverseRouter.route(on(CrossingTypesController.class).renderForm(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case CABLE_CROSSINGS:
        return ReverseRouter.route(on(CableCrossingController.class).renderOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      default:
        throw new ActionNotAllowedException("Cannot get route for task: " + task.name());
    }
  }

  public ModelAndView getOverviewRedirect(PwaApplicationDetail detail, CrossingAgreementTask task) {
    switch (task) {
      case LICENCE_AND_BLOCK_NUMBERS:
        return ReverseRouter.redirect(on(BlockCrossingController.class).renderBlockCrossingOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case MEDIAN_LINE:
        return ReverseRouter.redirect(on(MedianLineCrossingController.class).renderMedianLineOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case CROSSING_TYPES:
        return ReverseRouter.redirect(on(CrossingTypesController.class).renderForm(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case CABLE_CROSSINGS:
        return ReverseRouter.redirect(on(CableCrossingController.class).renderOverview(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      default:
        throw new ActionNotAllowedException("Cannot get route for task: " + task.name());
    }
  }
}
