package uk.co.ogauthority.pwa.controller.pwaapplications.initial.fields;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.fields.PwaFieldForm;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.devuk.DevukFieldService;
import uk.co.ogauthority.pwa.service.devuk.PadFieldService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/fields")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class PadPwaFieldsController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final DevukFieldService devukFieldService;
  private final PadFieldService padFieldService;
  private final ControllerHelperService controllerHelperService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public PadPwaFieldsController(ApplicationBreadcrumbService breadcrumbService,
                                DevukFieldService devukFieldService,
                                PadFieldService padFieldService,
                                ControllerHelperService controllerHelperService,
                                PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.breadcrumbService = breadcrumbService;
    this.devukFieldService = devukFieldService;
    this.padFieldService = padFieldService;
    this.controllerHelperService = controllerHelperService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  private ModelAndView getFieldsModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/fieldInformation/fieldInformation")
        .addObject("backUrl",
            pwaApplicationRedirectService.getTaskListRoute(pwaApplicationDetail.getPwaApplication()));

    modelAndView.addObject("fields", padFieldService.getActiveFieldsForApplicationDetail(pwaApplicationDetail));
    modelAndView.addObject("fieldMap", getDevukFieldMap());
    modelAndView.addObject("errorList", List.of());

    breadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView, "Field information");

    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderFields(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") PwaFieldForm form,
      AuthenticatedUserAccount user,
      PwaApplicationContext applicationContext
  ) {

    var modelAndView = getFieldsModelAndView(applicationContext.getApplicationDetail());
    padFieldService.mapEntityToForm(applicationContext.getApplicationDetail(), form);
    return modelAndView;

  }

  @PostMapping
  public ModelAndView postFields(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      AuthenticatedUserAccount user,
      @ModelAttribute("form") PwaFieldForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext,
      ValidationType validationType) {

    bindingResult = padFieldService.validate(form, bindingResult, validationType, applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getFieldsModelAndView(applicationContext.getApplicationDetail()), () -> {

          padFieldService.updateFieldInformation(applicationContext.getApplicationDetail(), form);

          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());

        });

  }

  private Map<String, String> getDevukFieldMap() {
    return devukFieldService.getByStatusCodes(List.of(500, 600, 700))
            .stream()
            .sorted(Comparator.comparing(DevukField::getFieldName))
            .collect(
                    StreamUtils.toLinkedHashMap(devukField -> devukField.getFieldId().toString(), DevukField::getFieldName));
  }

}
