package uk.co.ogauthority.pwa.service.tasklist;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.BlockCrossingController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.CrossingTypesController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.MedianLineCrossingController;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListSection;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingTypesService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementService;

@Service
public class CrossingAgreementsTaskListService {

  private final BlockCrossingService blockCrossingService;
  private final PadMedianLineAgreementService padMedianLineAgreementService;
  private final CrossingTypesService crossingTypesService;

  @Autowired
  public CrossingAgreementsTaskListService(
      BlockCrossingService blockCrossingService,
      PadMedianLineAgreementService padMedianLineAgreementService,
      CrossingTypesService crossingTypesService) {
    this.blockCrossingService = blockCrossingService;
    this.padMedianLineAgreementService = padMedianLineAgreementService;
    this.crossingTypesService = crossingTypesService;
  }

  public TaskListSection getServiceBean(CrossingAgreementTask task) {
    switch (task) {
      case CROSSING_TYPES:
        return crossingTypesService;
      case MEDIAN_LINE:
        return padMedianLineAgreementService;
      case LICENCE_AND_BLOCK_NUMBERS:
        return blockCrossingService;
      default:
        throw new ActionNotAllowedException("Cannot get service bean for task: " + task.name());
    }
  }

  public String getRoute(PwaApplicationDetail detail, CrossingAgreementTask task) {
    switch (task) {
      case LICENCE_AND_BLOCK_NUMBERS:
        return ReverseRouter.route(on(BlockCrossingController.class).renderAddBlockCrossing(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case MEDIAN_LINE:
        return ReverseRouter.route(on(MedianLineCrossingController.class).renderAddMedianLineForm(
            detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null));
      case CROSSING_TYPES:
        return ReverseRouter.route(on(CrossingTypesController.class).renderForm());
      default:
        throw new ActionNotAllowedException("Cannot get route for task: " + task.name());
    }
  }
}
