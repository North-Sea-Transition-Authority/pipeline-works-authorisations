package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.MedianLineCrossingController;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.MedianLineAgreementView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION
})
public class CrossingAgreementsController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PadMedianLineAgreementService padMedianLineAgreementService;
  private final BlockCrossingService blockCrossingService;
  private final BlockCrossingFileService blockCrossingFileService;

  @Autowired
  public CrossingAgreementsController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadMedianLineAgreementService padMedianLineAgreementService,
      BlockCrossingService blockCrossingService,
      BlockCrossingFileService blockCrossingFileService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padMedianLineAgreementService = padMedianLineAgreementService;
    this.blockCrossingService = blockCrossingService;
    this.blockCrossingFileService = blockCrossingFileService;
  }

  private ModelAndView getCrossingAgreementsModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/overview")
        .addObject("medianLineUrl", ReverseRouter.route(on(MedianLineCrossingController.class)
            .renderMedianLineForm(detail.getPwaApplicationType(), null, null, null)))
        .addObject("blockCrossings", blockCrossingService.getCrossedBlockViews(detail))
        .addObject("blockCrossingFiles", blockCrossingFileService.getBlockCrossingFileViews(detail, ApplicationFileLinkStatus.FULL))
        .addObject("blockCrossingUrlFactory", new BlockCrossingUrlFactory(detail));
    applicationBreadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView, "Crossings");
    return modelAndView;
  }

  @GetMapping
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView renderCrossingAgreementsOverview(@PathVariable("applicationType")
                                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                       PwaApplicationContext applicationContext,
                                                       AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    var modelAndView = getCrossingAgreementsModelAndView(applicationContext.getApplicationDetail());
    var entity = padMedianLineAgreementService.getMedianLineAgreement(detail);
    if (entity.getAgreementStatus() != null) {
      modelAndView.addObject("medianLineAgreementView", new MedianLineAgreementView(
          entity.getAgreementStatus(),
          entity.getNegotiatorName(),
          entity.getNegotiatorEmail()
      ));
    }
    return modelAndView;
  }


}
