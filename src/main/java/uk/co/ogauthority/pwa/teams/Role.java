package uk.co.ogauthority.pwa.teams;

public enum Role {
  // Global
  TEAM_ADMINISTRATOR("Team administrator", "Add, remove and update members of this team"),

  // Regulator
  ORGANISATION_MANAGER("Organisation team manager", "Manage organisation access to PWAs"),
  CONSULTEE_GROUP_MANAGER("Consultee group manager", "Manage consultee group access to PWAs"),
  PWA_MANAGER("PWA manager", "Accept applications and allocate case officers"),
  CASE_OFFICER("Case officer", "Process applications and run consultations"),
  CONSENT_VIEWER("PWA consent viewer", "Search for and view consented PWA data"),
  AS_BUILT_NOTIFICATION_ADMIN("As-built notification admin", "Manage as-built notifications"),
  TEMPLATE_CLAUSE_MANAGER("Template clause manager", "Manage document template clauses"),

  // Organisation
  APPLICATION_CREATOR("Application creator", "Create PWA and associated applications"),
  APPLICATION_SUBMITTER("Application submitter", "Submit applications to the NSTA"),
  FINANCE_ADMIN("Finance administrator", "Pay for any submitted PWA application"),
  AS_BUILT_NOTIFICATION_SUBMITTER("As-built notification submitter", "Submit as-built notifications to the NSTA"),

  // Consults
  RECIPIENT("Consultation recipient", "Receives PWA consultation requests from the NSTA"),
  RESPONDER("Consultation responder", "Responds to the NSTA on PWA consultations"),
  ;


  private final String name;

  private final String description;

  Role(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }
}
