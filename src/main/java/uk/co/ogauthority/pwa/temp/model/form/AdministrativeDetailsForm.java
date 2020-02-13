package uk.co.ogauthority.pwa.temp.model.form;

public class AdministrativeDetailsForm {

  private String projectDescription;
  private Object projectDiagram;
  private Boolean agreesToFdp;
  private String locationFromShore;
  private Boolean withinSafetyZone;
  private String structureName;
  private String methodOfTransportation;
  private String landfallDetails;
  private Boolean acceptFundsLiability;
  private Boolean acceptOpolLiability;

  public String getProjectDescription() {
    return projectDescription;
  }

  public void setProjectDescription(String projectDescription) {
    this.projectDescription = projectDescription;
  }

  public Object getProjectDiagram() {
    return projectDiagram;
  }

  public void setProjectDiagram(Object projectDiagram) {
    this.projectDiagram = projectDiagram;
  }

  public Boolean getAgreesToFdp() {
    return agreesToFdp;
  }

  public void setAgreesToFdp(Boolean agreesToFdp) {
    this.agreesToFdp = agreesToFdp;
  }

  public String getLocationFromShore() {
    return locationFromShore;
  }

  public void setLocationFromShore(String locationFromShore) {
    this.locationFromShore = locationFromShore;
  }

  public Boolean getWithinSafetyZone() {
    return withinSafetyZone;
  }

  public void setWithinSafetyZone(Boolean withinSafetyZone) {
    this.withinSafetyZone = withinSafetyZone;
  }

  public String getStructureName() {
    return structureName;
  }

  public void setStructureName(String structureName) {
    this.structureName = structureName;
  }

  public String getMethodOfTransportation() {
    return methodOfTransportation;
  }

  public void setMethodOfTransportation(String methodOfTransportation) {
    this.methodOfTransportation = methodOfTransportation;
  }

  public String getLandfallDetails() {
    return landfallDetails;
  }

  public void setLandfallDetails(String landfallDetails) {
    this.landfallDetails = landfallDetails;
  }

  public Boolean getAcceptFundsLiability() {
    return acceptFundsLiability;
  }

  public void setAcceptFundsLiability(Boolean acceptFundsLiability) {
    this.acceptFundsLiability = acceptFundsLiability;
  }

  public Boolean getAcceptOpolLiability() {
    return acceptOpolLiability;
  }

  public void setAcceptOpolLiability(Boolean acceptOpolLiability) {
    this.acceptOpolLiability = acceptOpolLiability;
  }
}
