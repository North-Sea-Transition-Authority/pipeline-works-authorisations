package uk.co.ogauthority.pwa.controller.pwaapplications.initial;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
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
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.initial.PadEnvironmentalDecommissioning;
import uk.co.ogauthority.pwa.model.form.pwaapplications.initial.EnvDecomForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.ApplicationHolderService;
import uk.co.ogauthority.pwa.service.pwaapplications.initial.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.service.pwaapplications.initial.validators.PadEnvDecomValidator;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@RequestMapping("/pwa-application/initial/{applicationId}/env-decom")
public class InitialEnvironmentalDecomController {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ApplicationHolderService applicationHolderService;
  private final PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;
  private final PadEnvDecomValidator validator;

  @Autowired
  public InitialEnvironmentalDecomController(
      PwaApplicationDetailService pwaApplicationDetailService,
      ApplicationHolderService applicationHolderService,
      PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService,
      PadEnvDecomValidator validator) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.applicationHolderService = applicationHolderService;
    this.padEnvironmentalDecommissioningService = padEnvironmentalDecommissioningService;
    this.validator = validator;
  }

  private ModelAndView getAdminDetailsModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var holders = applicationHolderService.getHoldersFromApplicationDetail(pwaApplicationDetail)
        .stream()
        .map(holderOrg -> holderOrg.getOrganisationUnit().getName())
        .collect(Collectors.toList());
    var holderNames = StringUtils.join(holders, ", ");
    var modelAndView = new ModelAndView("pwaApplication/initial/environmentalAndDecommissioning")
        .addObject("holderCompanyNames", holderNames)
        .addObject("hseSafetyZones", Arrays.stream(HseSafetyZone.values())
            .sorted(Comparator.comparing(HseSafetyZone::getDisplayOrder))
            .collect(StreamUtils.toLinkedHashMap(Enum::name, HseSafetyZone::getDisplayText)));
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderAdminDetails(@PathVariable("applicationId") Integer applicationId,
                                         @ModelAttribute("form") EnvDecomForm form,
                                         AuthenticatedUserAccount user) {
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {
      var adminDetail = padEnvironmentalDecommissioningService.getEnvDecomData(detail);
      var modelAndView = getAdminDetailsModelAndView(detail);
      mapDataToForm(form, adminDetail);
      return modelAndView;
    });
  }

  @PostMapping(params = "Complete")
  public ModelAndView postCompleteAdminDetails(@PathVariable("applicationId") Integer applicationId,
                                               @Valid @ModelAttribute("form") EnvDecomForm form,
                                               BindingResult bindingResult,
                                               AuthenticatedUserAccount user) {
    validator.validate(form, bindingResult);
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail ->
        ControllerUtils.validateAndRedirect(bindingResult, getAdminDetailsModelAndView(detail), () -> {
          var adminDetail = padEnvironmentalDecommissioningService.getEnvDecomData(detail);
          saveForm(form, adminDetail);
          return ReverseRouter.redirect(on(InitialTaskListController.class).viewTaskList(applicationId));
        }));
  }

  @PostMapping(params = "Save and complete later")
  public ModelAndView postContinueAdminDetails(@PathVariable("applicationId") Integer applicationId,
                                               @ModelAttribute("form") EnvDecomForm form,
                                               BindingResult bindingResult,
                                               AuthenticatedUserAccount user) {
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail ->
        ControllerUtils.validateAndRedirect(bindingResult, getAdminDetailsModelAndView(detail), () -> {
          var adminDetail = padEnvironmentalDecommissioningService.getEnvDecomData(detail);
          saveForm(form, adminDetail);
          return ReverseRouter.redirect(on(InitialTaskListController.class).viewTaskList(applicationId));
        })
    );
  }

  private void mapDataToForm(EnvDecomForm form, PadEnvironmentalDecommissioning padEnvironmentalDecommissioning) {
    form.setAcceptsEolRegulations(padEnvironmentalDecommissioning.getAcceptsEolRegulations());
    form.setAcceptsEolRemoval(padEnvironmentalDecommissioning.getAcceptsEolRemoval());
    form.setAcceptsRemovalProposal(padEnvironmentalDecommissioning.getAcceptsRemovalProposal());
    form.setAcceptsOpolLiability(padEnvironmentalDecommissioning.getAcceptsOpolLiability());
    form.setDecommissioningPlans(padEnvironmentalDecommissioning.getDecommissioningPlans());
    form.setDischargeFundsAvailable(padEnvironmentalDecommissioning.getDischargeFundsAvailable());
    form.setEmtHasOutstandingPermits(padEnvironmentalDecommissioning.getEmtHasOutstandingPermits());
    form.setEmtHasSubmittedPermits(padEnvironmentalDecommissioning.getEmtHasSubmittedPermits());
    form.setPermitsSubmitted(padEnvironmentalDecommissioning.getPermitsSubmitted());
    form.setPermitsPendingSubmission(padEnvironmentalDecommissioning.getPermitsPendingSubmission());
    form.setTransboundaryEffect(padEnvironmentalDecommissioning.getTransboundaryEffect());
    if (padEnvironmentalDecommissioning.getEmtSubmissionTimestamp() != null) {
      var localDate = LocalDate.ofInstant(padEnvironmentalDecommissioning.getEmtSubmissionTimestamp(), ZoneId.systemDefault());
      form.setEmtSubmissionDay(localDate.getDayOfMonth());
      form.setEmtSubmissionMonth(localDate.getMonthValue());
      form.setEmtSubmissionYear(localDate.getYear());
    }
  }

  private void saveForm(EnvDecomForm form, PadEnvironmentalDecommissioning padEnvironmentalDecommissioning) {
    padEnvironmentalDecommissioning.setAcceptsEolRegulations(form.getAcceptsEolRegulations());
    padEnvironmentalDecommissioning.setAcceptsEolRemoval(form.getAcceptsEolRemoval());
    padEnvironmentalDecommissioning.setAcceptsRemovalProposal(form.getAcceptsRemovalProposal());
    padEnvironmentalDecommissioning.setAcceptsOpolLiability(form.getAcceptsOpolLiability());
    padEnvironmentalDecommissioning.setDecommissioningPlans(form.getDecommissioningPlans());
    padEnvironmentalDecommissioning.setDischargeFundsAvailable(form.getDischargeFundsAvailable());
    padEnvironmentalDecommissioning.setEmtHasOutstandingPermits(form.getEmtHasOutstandingPermits());
    padEnvironmentalDecommissioning.setEmtHasSubmittedPermits(form.getEmtHasSubmittedPermits());
    padEnvironmentalDecommissioning.setPermitsSubmitted(form.getPermitsSubmitted());
    padEnvironmentalDecommissioning.setPermitsPendingSubmission(form.getPermitsPendingSubmission());
    padEnvironmentalDecommissioning.setEmtSubmissionTimestamp(null);
    try {
      var localDate = LocalDate.of(
          form.getEmtSubmissionYear(),
          form.getEmtSubmissionMonth(),
          form.getEmtSubmissionDay()
      );
      var instant = Instant.ofEpochSecond(localDate.toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC));
      padEnvironmentalDecommissioning.setEmtSubmissionTimestamp(instant);
    } catch (Exception e) {
      padEnvironmentalDecommissioning.setEmtSubmissionTimestamp(null);
    }
    padEnvironmentalDecommissioning.setTransboundaryEffect(form.getTransboundaryEffect());
    padEnvironmentalDecommissioningService.save(padEnvironmentalDecommissioning);
  }

}
