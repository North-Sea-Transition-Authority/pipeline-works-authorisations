package uk.co.ogauthority.pwa.features.application.tasks.fasttrack.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDate;
import java.time.ZoneId;
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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.FastTrackForm;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.PadFastTrackService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.controller.ProjectInformationController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/fast-track")
public class FastTrackController {

  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PadFastTrackService padFastTrackService;
  private final PadProjectInformationService padProjectInformationService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public FastTrackController(
      PwaApplicationRedirectService pwaApplicationRedirectService,
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadFastTrackService padFastTrackService,
      PadProjectInformationService padProjectInformationService,
      ControllerHelperService controllerHelperService) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padFastTrackService = padFastTrackService;
    this.padProjectInformationService = padProjectInformationService;
    this.controllerHelperService = controllerHelperService;
  }

  private ModelAndView getFastTrackModelAndView(PwaApplicationDetail detail) {
    var projectInformation = padProjectInformationService.getPadProjectInformationData(detail);
    var modelAndView = new ModelAndView("pwaApplication/shared/fastTrack");
    if (projectInformation.getProposedStartTimestamp() != null) {
      var startDate = LocalDate.ofInstant(projectInformation.getProposedStartTimestamp(), ZoneId.systemDefault());
      modelAndView.addObject("startDate", DateUtils.formatDate(startDate));
      modelAndView.addObject("modifyStartDateUrl",
          ReverseRouter.route(on(ProjectInformationController.class)
              .renderProjectInformation(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)));
    }
    applicationBreadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView, "Fast-track");
    return modelAndView;
  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView renderFastTrack(@PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("applicationId") Integer applicationId,
                                      PwaApplicationContext applicationContext,
                                      @ModelAttribute("form") FastTrackForm form,
                                      AuthenticatedUserAccount user) {

    var detail = applicationContext.getApplicationDetail();

    assertFastTrackAllowed(detail);

    var entity = padFastTrackService.getFastTrackForDraft(detail);
    padFastTrackService.mapEntityToForm(entity, form);
    return getFastTrackModelAndView(detail);

  }

  @PostMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView postFastTrack(@PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    @PathVariable("applicationId") Integer applicationId,
                                    PwaApplicationContext applicationContext,
                                    @ModelAttribute("form") FastTrackForm form,
                                    BindingResult bindingResult,
                                    AuthenticatedUserAccount user,
                                    ValidationType validationType) {

    var detail = applicationContext.getApplicationDetail();

    assertFastTrackAllowed(detail);

    bindingResult = padFastTrackService.validate(form,
        bindingResult,
        validationType,
        applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getFastTrackModelAndView(detail), () -> {
      var entity = padFastTrackService.getFastTrackForDraft(detail);
      padFastTrackService.saveEntityUsingForm(entity, form);
      return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
    });

  }

  private void assertFastTrackAllowed(PwaApplicationDetail pwaApplicationDetail) {
    if (!padFastTrackService.isFastTrackRequired(pwaApplicationDetail)) {
      throw new AccessDeniedException(String.format("Application detail (%s) doesn't require fast-track",
          pwaApplicationDetail.getId()));
    }
  }

}
