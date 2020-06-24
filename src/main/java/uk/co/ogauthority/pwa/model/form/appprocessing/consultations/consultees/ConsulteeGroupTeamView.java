package uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees;

public class ConsulteeGroupTeamView {

  private final Integer id;
  private final String name;
  private final String manageUrl;

  public ConsulteeGroupTeamView(Integer id, String name) {
    this.id = id;
    this.name = name;
    this.manageUrl = "add later";
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getManageUrl() {
    return manageUrl;
  }
}
