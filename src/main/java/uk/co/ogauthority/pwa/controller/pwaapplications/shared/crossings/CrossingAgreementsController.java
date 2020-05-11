package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CableCrossingFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.MedianLineCrossingFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadCableCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DEPOSIT_CONSENT
})
public class CrossingAgreementsController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PadMedianLineAgreementService padMedianLineAgreementService;
  private final BlockCrossingService blockCrossingService;
  private final BlockCrossingFileService blockCrossingFileService;
  private final CrossingAgreementsService crossingAgreementsService;
  private final MedianLineCrossingFileService medianLineCrossingFileService;
  private final PadCableCrossingService cableCrossingService;
  private final CableCrossingFileService cableCrossingFileService;

  @Autowired
  public CrossingAgreementsController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadMedianLineAgreementService padMedianLineAgreementService,
      BlockCrossingService blockCrossingService,
      BlockCrossingFileService blockCrossingFileService,
      CrossingAgreementsService crossingAgreementsService,
      MedianLineCrossingFileService medianLineCrossingFileService,
      PadCableCrossingService cableCrossingService,
      CableCrossingFileService cableCrossingFileService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padMedianLineAgreementService = padMedianLineAgreementService;
    this.blockCrossingService = blockCrossingService;
    this.blockCrossingFileService = blockCrossingFileService;
    this.crossingAgreementsService = crossingAgreementsService;
    this.medianLineCrossingFileService = medianLineCrossingFileService;
    this.cableCrossingService = cableCrossingService;
    this.cableCrossingFileService = cableCrossingFileService;
  }

  private ModelAndView getCrossingAgreementsModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/taskList")
        .addObject("tasks", crossingAgreementsService.getTaskListItems(detail));
    applicationBreadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView,
        "Blocks and crossing agreements");
    return modelAndView;
  }

  @GetMapping
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView renderCrossingAgreementsOverview(@PathVariable("applicationType")
                                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                       @PathVariable("applicationId") Integer applicationId,
                                                       PwaApplicationContext applicationContext,
                                                       AuthenticatedUserAccount user) {
    return getCrossingAgreementsModelAndView(applicationContext.getApplicationDetail());
  }


}
