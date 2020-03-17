package uk.co.ogauthority.pwa.controller.pwaapplications.initial.fields;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.InitialTaskListController;
import uk.co.ogauthority.pwa.model.entity.fields.DevukField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.ApplicationHolderOrganisation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.fields.PwaFieldForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.fields.DevukFieldService;
import uk.co.ogauthority.pwa.service.fields.PwaApplicationFieldService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.ApplicationHolderService;
import uk.co.ogauthority.pwa.service.pwaapplications.initial.PwaFieldFormValidator;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@RequestMapping("/pwa-application/initial/{applicationId}/fields")
public class InitialFieldsController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final DevukFieldService devukFieldService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ApplicationHolderService applicationHolderService;
  private final PwaApplicationFieldService pwaApplicationFieldService;
  private final PwaFieldFormValidator pwaFieldFormValidator;

  @Autowired
  public InitialFieldsController(ApplicationBreadcrumbService breadcrumbService,
                                 DevukFieldService devukFieldService,
                                 PwaApplicationDetailService pwaApplicationDetailService,
                                 ApplicationHolderService applicationHolderService,
                                 PwaApplicationFieldService pwaApplicationFieldService,
                                 PwaFieldFormValidator pwaFieldFormValidator) {
    this.breadcrumbService = breadcrumbService;
    this.devukFieldService = devukFieldService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.applicationHolderService = applicationHolderService;
    this.pwaApplicationFieldService = pwaApplicationFieldService;
    this.pwaFieldFormValidator = pwaFieldFormValidator;
  }

  private ModelAndView getFieldsModelAndView(PwaApplicationDetail pwaApplicationDetail, PwaFieldForm form,
                                             AuthenticatedUserAccount user) {
    var modelAndView = new ModelAndView("pwaApplication/initial/fieldInformation")
        .addObject("backUrl",
            ReverseRouter.route(on(InitialTaskListController.class)
                .viewTaskList(pwaApplicationDetail.getMasterPwaApplicationId(), null)));

    var holders = applicationHolderService.getHoldersFromApplicationDetail(pwaApplicationDetail);

    modelAndView.addObject("fields",
        pwaApplicationFieldService.getActiveFieldsForApplicationDetail(pwaApplicationDetail));
    modelAndView.addObject("fieldMap", getDevukFieldMap(holders));

    breadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView, "Field information");

    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderFields(@PathVariable("applicationId") Integer applicationId,
                                   @ModelAttribute("form") PwaFieldForm form, AuthenticatedUserAccount user) {
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {

      var modelAndView = getFieldsModelAndView(detail, form, user);

      var fields = pwaApplicationFieldService.getActiveFieldsForApplicationDetail(detail);
      form.setLinkedToField(detail.getLinkedToField());
      if (fields.size() == 1) {
        if (fields.get(0).isLinkedToDevuk()) {
          form.setFieldId(fields.get(0).getDevukField().getFieldId());
        }
      }

      return modelAndView;
    });
  }

  @PostMapping
  public ModelAndView postFields(@PathVariable("applicationId") Integer applicationId, AuthenticatedUserAccount user,
                                 @Valid @ModelAttribute("form") PwaFieldForm form, BindingResult bindingResult) {

    pwaFieldFormValidator.validate(form, bindingResult);
    var isLinkedtoField = form.getLinkedToField();
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail ->
        ControllerUtils.validateAndRedirect(bindingResult, getFieldsModelAndView(detail, form, user), () -> {
          var fieldList = new ArrayList<DevukField>();
          if (isLinkedtoField) {
            fieldList.add(devukFieldService.findById(form.getFieldId()));
          }
          pwaApplicationFieldService.setFields(detail, fieldList);
          return ReverseRouter.redirect(on(InitialTaskListController.class).viewTaskList(applicationId, null));
        })
    );
  }

  private Map<String, String> getDevukFieldMap(List<ApplicationHolderOrganisation> holders) {
    return holders.stream()
        .flatMap(
            org -> devukFieldService.getByOrganisationUnitWithStatusCodes(org.getOrganisationUnit(),
                List.of(500, 600, 700))
                .stream())
        .sorted(Comparator.comparing(DevukField::getFieldName))
        .collect(
            StreamUtils.toLinkedHashMap(devukField -> devukField.getFieldId().toString(), DevukField::getFieldName));
  }

}
