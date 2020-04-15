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
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.MedianLineAgreementsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings/median-line")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION
})
public class MedianLineCrossingController {

  private final PadMedianLineAgreementService padMedianLineAgreementService;

  private final ApplicationBreadcrumbService applicationBreadcrumbService;

  @Autowired
  public MedianLineCrossingController(
      PadMedianLineAgreementService padMedianLineAgreementService,
      ApplicationBreadcrumbService applicationBreadcrumbService) {
    this.padMedianLineAgreementService = padMedianLineAgreementService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
  }

  private ModelAndView getMedianLineModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/medianLine")
        .addObject("errorList", List.of())
        .addObject("crossingOptions", MedianLineStatus.stream()
            .sorted(Comparator.comparing(MedianLineStatus::getDisplayOrder))
            .collect(StreamUtils.toLinkedHashMap(Enum::name, MedianLineStatus::getDisplayText)));
    applicationBreadcrumbService.fromCrossings(detail.getPwaApplication(), modelAndView, "Median line");
    return modelAndView;
  }

  @GetMapping
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView renderMedianLineForm(@PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           @PathVariable("applicationId") Integer applicationId,
                                           @ModelAttribute("form") MedianLineAgreementsForm form,
                                           PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    var entity = padMedianLineAgreementService.getMedianLineAgreement(detail);
    padMedianLineAgreementService.mapEntityToForm(entity, form);
    return getMedianLineModelAndView(detail);
  }

  @PostMapping(params = "Save and complete later")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView postContinueMedianLine(@PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             @PathVariable("applicationId") Integer applicationId,
                                             @Valid @ModelAttribute("form") MedianLineAgreementsForm form,
                                             BindingResult bindingResult,
                                             PwaApplicationContext applicationContext) {
    padMedianLineAgreementService.validate(
        form,
        bindingResult,
        ValidationType.PARTIAL,
        applicationContext.getApplicationDetail()
    );

    return postValidateSaveAndRedirect(applicationContext, form, bindingResult);
  }

  @PostMapping(params = "Complete")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView postCompleteMedianLine(@PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             @PathVariable("applicationId") Integer applicationId,
                                             @Valid @ModelAttribute("form") MedianLineAgreementsForm form,
                                             BindingResult bindingResult,
                                             PwaApplicationContext applicationContext) {
    padMedianLineAgreementService.validate(
        form,
        bindingResult,
        ValidationType.FULL,
        applicationContext.getApplicationDetail()
    );
    return postValidateSaveAndRedirect(applicationContext, form, bindingResult);
  }

  private ModelAndView postValidateSaveAndRedirect(PwaApplicationContext applicationContext,
                                                   MedianLineAgreementsForm form, BindingResult bindingResult) {
    // TODO: PWA-393 Add file uploads
    var detail = applicationContext.getApplicationDetail();
    return ControllerUtils.checkErrorsAndRedirect(bindingResult, getMedianLineModelAndView(detail), () -> {
      var entity = padMedianLineAgreementService.getMedianLineAgreement(detail);
      padMedianLineAgreementService.saveEntityUsingForm(entity, form);
      return ReverseRouter.redirect(on(CrossingAgreementsController.class)
          .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), null, null));
    });
  }

}
