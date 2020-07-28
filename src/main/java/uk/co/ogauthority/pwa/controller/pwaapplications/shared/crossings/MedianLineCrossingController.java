package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.enums.CrossingOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.MedianLineAgreementsForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.AddBlockCrossingForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.MedianLineAgreementView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.MedianLineCrossingFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.MedianLineCrossingUrlFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.service.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings/median-line")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.DECOMMISSIONING
})
public class MedianLineCrossingController {

  private final PadMedianLineAgreementService padMedianLineAgreementService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final MedianLineCrossingFileService medianLineCrossingFileService;
  private final CrossingAgreementsTaskListService crossingAgreementsTaskListService;
  private final PadFileService padFileService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public MedianLineCrossingController(
      PadMedianLineAgreementService padMedianLineAgreementService,
      ApplicationBreadcrumbService applicationBreadcrumbService,
      MedianLineCrossingFileService medianLineCrossingFileService,
      CrossingAgreementsTaskListService crossingAgreementsTaskListService,
      PadFileService padFileService,
      ControllerHelperService controllerHelperService) {
    this.padMedianLineAgreementService = padMedianLineAgreementService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.medianLineCrossingFileService = medianLineCrossingFileService;
    this.crossingAgreementsTaskListService = crossingAgreementsTaskListService;
    this.padFileService = padFileService;
    this.controllerHelperService = controllerHelperService;
  }

  private ModelAndView getFormModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/medianLine")
        .addObject("errorList", List.of())
        .addObject("crossingOptions", MedianLineStatus.stream()
            .sorted(Comparator.comparing(MedianLineStatus::getDisplayOrder))
            .collect(StreamUtils.toLinkedHashMap(Enum::name, MedianLineStatus::getDisplayText)));
    applicationBreadcrumbService.fromCrossingSection(detail, modelAndView, CrossingAgreementTask.MEDIAN_LINE,
        "Median line");
    return modelAndView;
  }

  private ModelAndView getOverviewModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/overview")
        .addObject("overview", CrossingOverview.MEDIAN_LINE_CROSSING)
        .addObject("medianLineUrlFactory", new MedianLineCrossingUrlFactory(detail))
        .addObject("medianLineFiles",
            padFileService.getUploadedFileViews(detail, ApplicationFilePurpose.MEDIAN_LINE_CROSSING,
                ApplicationFileLinkStatus.FULL))
        .addObject("backUrl", ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null,
                null)));
    applicationBreadcrumbService.fromCrossings(detail.getPwaApplication(), modelAndView, "Median line crossing");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderMedianLineOverview(@PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               @PathVariable("applicationId") Integer applicationId,
                                               @ModelAttribute("form") MedianLineAgreementsForm form,
                                               PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    var entity = padMedianLineAgreementService.getMedianLineAgreement(detail);
    padMedianLineAgreementService.mapEntityToForm(entity, form);
    var modelAndView = getOverviewModelAndView(detail);
    if (entity.getAgreementStatus() != null) {
      modelAndView.addObject("medianLineAgreementView", new MedianLineAgreementView(
          entity.getAgreementStatus(),
          entity.getNegotiatorName(),
          entity.getNegotiatorEmail()
      ));
    }
    return modelAndView;
  }

  @PostMapping
  public ModelAndView postOverview(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AddBlockCrossingForm form,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    if (!padMedianLineAgreementService.isComplete(detail)) {
      var agreement = padMedianLineAgreementService.getMedianLineAgreement(detail);
      if (agreement.getAgreementStatus() == null) {
        return getOverviewModelAndView(detail)
            .addObject("errorMessage", "You must select the status of the agreement");
      } else {
        return getOverviewModelAndView(detail)
            .addObject("errorMessage", "At least one document is required");
      }
    }
    return ReverseRouter.redirect(on(CrossingAgreementsController.class)
        .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null,
            null));
  }


  @GetMapping("/edit")
  public ModelAndView renderMedianLineForm(@PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           @PathVariable("applicationId") Integer applicationId,
                                           @ModelAttribute("form") MedianLineAgreementsForm form,
                                           PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    var entity = padMedianLineAgreementService.getMedianLineAgreement(detail);
    padMedianLineAgreementService.mapEntityToForm(entity, form);
    return getFormModelAndView(detail);
  }

  @PostMapping("/edit")
  public ModelAndView postEditMedianLine(@PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         @PathVariable("applicationId") Integer applicationId,
                                         @Valid @ModelAttribute("form") MedianLineAgreementsForm form,
                                         BindingResult bindingResult,
                                         PwaApplicationContext applicationContext,
                                         ValidationType validationType) {
    bindingResult = padMedianLineAgreementService.validate(
        form,
        bindingResult,
        validationType,
        applicationContext.getApplicationDetail()
    );
    return postValidateSaveAndRedirect(applicationContext, form, bindingResult);
  }

  private ModelAndView postValidateSaveAndRedirect(PwaApplicationContext applicationContext,
                                                   MedianLineAgreementsForm form, BindingResult bindingResult) {
    // TODO: PWA-393 Add file uploads
    var detail = applicationContext.getApplicationDetail();
    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getFormModelAndView(detail), () -> {
      var entity = padMedianLineAgreementService.getMedianLineAgreement(detail);
      padMedianLineAgreementService.saveEntityUsingForm(entity, form);
      return crossingAgreementsTaskListService.getOverviewRedirect(detail, CrossingAgreementTask.MEDIAN_LINE);
    });
  }

}
