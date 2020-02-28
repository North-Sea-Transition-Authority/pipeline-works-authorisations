package uk.co.ogauthority.pwa.controller.pwaapplications.shared;


import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PickPwaForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.masterpwa.MasterPwaAuthorisationService;
import uk.co.ogauthority.pwa.service.masterpwa.MasterPwaDto;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationTypePathUrl}")
public class PickExistingPwaController {

  private static final Set<PwaApplicationType> VALID_START_APPLICATION_TYPES = EnumSet.of(
      PwaApplicationType.CAT_1_VARIATION
  );

  private final PwaApplicationService pwaApplicationService;
  private final MasterPwaAuthorisationService masterPwaAuthorisationService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public PickExistingPwaController(
      PwaApplicationService pwaApplicationService,
      MasterPwaAuthorisationService masterPwaAuthorisationService,
      PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.pwaApplicationService = pwaApplicationService;
    this.masterPwaAuthorisationService = masterPwaAuthorisationService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }


  @GetMapping("/pick-pwa-for-application")
  public ModelAndView renderPickPwaToStartApplication(@PathVariable("applicationTypePathUrl")
                                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                      @ModelAttribute("form") PickPwaForm form,
                                                      AuthenticatedUserAccount user) {
    checkApplicationTypeValid(pwaApplicationType);
    return getPickPwaModelAndView(user);
  }

  private ModelAndView getPickPwaModelAndView(AuthenticatedUserAccount user) {

    Map<String, String> selectablePwaMap = masterPwaAuthorisationService.getMasterPwaDtosWhereUserIsAuthorised(user)
        .stream()
        .sorted(Comparator.comparing(MasterPwaDto::getReference))
        .collect(StreamUtils.toLinkedHashMap(pwa -> String.valueOf(pwa.getMasterPwaId()), MasterPwaDto::getReference));

    return new ModelAndView("pwaApplication/shared/pickPwaForApplication")
        .addObject("selectablePwaMap", selectablePwaMap)
        .addObject("workareaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea()))
        .addObject("errorList", List.of());
  }

  @PostMapping("/pick-pwa-for-application")
  public ModelAndView pickPwaAndStartApplication(@PathVariable("applicationTypePathUrl")
                                                 @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                 @ModelAttribute("form") @Valid PickPwaForm form,
                                                 BindingResult bindingResult,
                                                 AuthenticatedUserAccount user) {
    checkApplicationTypeValid(pwaApplicationType);
    return ControllerUtils.validateAndRedirect(bindingResult, getPickPwaModelAndView(user), () -> {

      var masterPwa = masterPwaAuthorisationService.getMasterPwaIfAuthorised(form.getMasterPwaId(), user);
      var newApplication = pwaApplicationService.createVariationPwaApplication(user, masterPwa, pwaApplicationType);
      return pwaApplicationRedirectService.getTaskListRedirect(newApplication, user);
    });
  }

  private void checkApplicationTypeValid(PwaApplicationType pwaApplicationType) {
    if (!VALID_START_APPLICATION_TYPES.contains(pwaApplicationType)) {
      throw new AccessDeniedException("Unsupported type for pick pwa: " + pwaApplicationType);
    }
  }


}
