package uk.co.ogauthority.pwa.temp.model.form;

import uk.co.ogauthority.pwa.temp.model.admindetails.WithinSafetyZone;

public class AdministrativeDetailsForm {

  private String projectDescription;
  private Object projectDiagram;
  private Boolean agreesToFdp;
  private String locationFromShore;
  private WithinSafetyZone withinSafetyZone;
  private String structureNameIfYes;
  private String structureNameIfPartially;
  private String methodOfTransportation;
  private String landfallDetails;
  private Boolean acceptFundsLiability;
  private Boolean acceptOpolLiability;
  private Boolean whollyOffshore;

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

  public WithinSafetyZone getWithinSafetyZone() {
    return withinSafetyZone;
  }

  public void setWithinSafetyZone(WithinSafetyZone withinSafetyZone) {
    this.withinSafetyZone = withinSafetyZone;
  }

  public String getStructureNameIfYes() {
    return structureNameIfYes;
  }

  public void setStructureNameIfYes(String structureNameIfYes) {
    this.structureNameIfYes = structureNameIfYes;
  }

  public String getStructureNameIfPartially() {
    return structureNameIfPartially;
  }

  public void setStructureNameIfPartially(String structureNameIfPartially) {
    this.structureNameIfPartially = structureNameIfPartially;
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

  public Boolean getWhollyOffshore() {
    return whollyOffshore;
  }

  public void setWhollyOffshore(Boolean whollyOffshore) {
    this.whollyOffshore = whollyOffshore;
  }
}
