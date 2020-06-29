package uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.appprocessing.consultations.consultees.ConsulteeGroupTeamManagementController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class ConsulteeGroupTeamView {

  private final Integer consulteeGroupId;
  private final String name;
  private final String manageUrl;

  public ConsulteeGroupTeamView(Integer consulteeGroupId, String name) {
    this.consulteeGroupId = consulteeGroupId;
    this.name = name;
    this.manageUrl = ReverseRouter
        .route(on(ConsulteeGroupTeamManagementController.class).renderTeamMembers(consulteeGroupId, null));
  }

  public Integer getConsulteeGroupId() {
    return consulteeGroupId;
  }

  public String getName() {
    return name;
  }

  public String getManageUrl() {
    return manageUrl;
  }
}
