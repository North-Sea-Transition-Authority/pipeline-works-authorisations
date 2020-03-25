package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDate;
import java.time.ZoneId;
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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.FastTrackForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadProjectInformationService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.FastTrackValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/fast-track")
public class FastTrackController {

  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PadFastTrackService padFastTrackService;
  private final PadProjectInformationService padProjectInformationService;
  private final FastTrackValidator validator;

  @Autowired
  public FastTrackController(
      PwaApplicationRedirectService pwaApplicationRedirectService,
      PwaApplicationDetailService pwaApplicationDetailService,
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadFastTrackService padFastTrackService,
      PadProjectInformationService padProjectInformationService,
      FastTrackValidator validator) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padFastTrackService = padFastTrackService;
    this.padProjectInformationService = padProjectInformationService;
    this.validator = validator;
  }

  @SuppressWarnings("checkstyle:CommentsIndentation")
  private ModelAndView getFastTrackModelAndView(PwaApplicationDetail detail) {
    var startDate = LocalDate.now();
    var projectInformation = padProjectInformationService.getPadProjectInformationData(detail);
    var modelAndView = new ModelAndView("pwaApplication/shared/fastTrack");
    if (projectInformation.getProposedStartTimestamp() != null) {
      startDate = LocalDate.ofInstant(projectInformation.getProposedStartTimestamp(), ZoneId.systemDefault());
      modelAndView.addObject("startDate", DateUtils.formatDate(startDate));
      modelAndView.addObject("modifyStartDateUrl",
          ReverseRouter.route(on(ProjectInformationController.class)
              .renderProjectInformation(detail.getApplicationType(), detail.getPwaApplication().getId(), null, null)));
    }
    applicationBreadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView, "Fast-track");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderFastTrack(@PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("applicationId") Integer applicationId,
                                      @ModelAttribute("form") FastTrackForm form,
                                      AuthenticatedUserAccount user) {
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {
      var entity = padFastTrackService.getFastTrackForDraft(detail);
      padFastTrackService.mapEntityToForm(entity, form);
      return getFastTrackModelAndView(detail);
    });
  }

  @PostMapping(params = "Complete")
  public ModelAndView postCompleteFastTrack(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            @Valid @ModelAttribute("form") FastTrackForm form,
                                            BindingResult bindingResult,
                                            AuthenticatedUserAccount user) {
    validator.validate(form, bindingResult);
    return postValidateSaveAndRedirect(applicationId, pwaApplicationType, form, bindingResult, user);
  }

  @PostMapping(params = "Save and complete later")
  public ModelAndView postContinueFastTrack(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            @Valid @ModelAttribute("form") FastTrackForm form,
                                            BindingResult bindingResult,
                                            AuthenticatedUserAccount user) {
    return postValidateSaveAndRedirect(applicationId, pwaApplicationType, form, bindingResult, user);
  }

  private ModelAndView postValidateSaveAndRedirect(Integer applicationId, PwaApplicationType pwaApplicationType,
                                                   FastTrackForm form,
                                                   BindingResult bindingResult,
                                                   AuthenticatedUserAccount user) {
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {
      return ControllerUtils.validateAndRedirect(bindingResult, getFastTrackModelAndView(detail), () -> {
        var entity = padFastTrackService.getFastTrackForDraft(detail);
        padFastTrackService.saveEntityUsingForm(entity, form);
        return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
      });
    });
  }

}
