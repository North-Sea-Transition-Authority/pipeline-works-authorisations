package uk.co.ogauthority.pwa.controller.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.controller.OrganisationGroupRestController;
import uk.co.ogauthority.pwa.model.enums.teams.ManageTeamType;
import uk.co.ogauthority.pwa.model.form.teammanagement.AddOrganisationTeamForm;
import uk.co.ogauthority.pwa.model.teams.PwaTeamType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;
import uk.co.ogauthority.pwa.service.teams.TeamCreationService;

@Controller
@RequestMapping("/create-organisation-team")
public class CreateTeamsController {

  private final ControllerHelperService controllerHelperService;
  private final TeamCreationService teamCreationService;

  @Autowired
  public CreateTeamsController(ControllerHelperService controllerHelperService,
                               TeamCreationService teamCreationService) {
    this.controllerHelperService = controllerHelperService;
    this.teamCreationService = teamCreationService;
  }



  @GetMapping
  public ModelAndView getNewOrganisationTeam(AuthenticatedUserAccount currentUser) {
    return getNewOrganisationTeamModelAndView(new AddOrganisationTeamForm());
  }

  @PostMapping
  public ModelAndView createNewOrganisationTeam(@Valid @ModelAttribute("form") AddOrganisationTeamForm form,
                                                BindingResult bindingResult,
                                                ValidationType validationType,
                                                AuthenticatedUserAccount currentUser) {
    bindingResult = teamCreationService.validate(form, bindingResult, validationType);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getNewOrganisationTeamModelAndView(form),
        () -> {
          var resourceId = teamCreationService.getOrCreateOrganisationGroupTeam(form, currentUser);
          return ReverseRouter.redirect(on(PortalTeamManagementController.class).renderTeamMembers(resourceId, null));
        }
    );
  }

  private ModelAndView getNewOrganisationTeamModelAndView(AddOrganisationTeamForm form) {
    return new ModelAndView("teamManagementOld/addNewOrganisationTeam")
        .addObject("form", form)
        .addObject("teamTypeDisplayName", PwaTeamType.ORGANISATION.name().toLowerCase())
        .addObject("organisationRestUrl",
            SearchSelectorService.route(on(OrganisationGroupRestController.class).searchOrganisations(null))
        )
        .addObject("linkSecondaryActionUrl", ManageTeamType.ORGANISATION_TEAMS.getLinkUrl());
  }

}
