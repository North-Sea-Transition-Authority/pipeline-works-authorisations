package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.CrossingAgreementsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.MedianLineAgreementsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadMedianLineAgreementService;
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
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public MedianLineCrossingController(
      PadMedianLineAgreementService padMedianLineAgreementService,
      PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.padMedianLineAgreementService = padMedianLineAgreementService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  private ModelAndView getMedianLineModelAndView() {
    return new ModelAndView("pwaApplication/shared/crossings/medianLine")
        .addObject("crossingOptions", MedianLineStatus.stream()
            .sorted(Comparator.comparing(MedianLineStatus::getDisplayOrder))
            .collect(StreamUtils.toLinkedHashMap(Enum::name, MedianLineStatus::getDisplayText)));
  }

  @GetMapping
  public ModelAndView renderMedianLineForm(@PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           @ModelAttribute("form") MedianLineAgreementsForm form,
                                           PwaApplicationContext applicationContext,
                                           AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    var entity = padMedianLineAgreementService.getMedianLineAgreementForDraft(detail);
    padMedianLineAgreementService.mapEntityToForm(entity, form);
    return getMedianLineModelAndView();
  }

  @PostMapping(params = "Save and complete later")
  public ModelAndView postContinueMedianLine(@PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             @ModelAttribute("form") MedianLineAgreementsForm form,
                                             PwaApplicationContext applicationContext,
                                             AuthenticatedUserAccount user) {
    return pwaApplicationRedirectService.getTaskListRedirect(
        applicationContext.getApplicationDetail().getPwaApplication()
    );
  }

  @PostMapping(params = "Complete")
  public ModelAndView postCompleteMedianLine(@PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             @ModelAttribute("form") MedianLineAgreementsForm form,
                                             PwaApplicationContext applicationContext,
                                             AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    return ReverseRouter.redirect(on(CrossingAgreementsController.class)
        .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), null, null));
  }

}
