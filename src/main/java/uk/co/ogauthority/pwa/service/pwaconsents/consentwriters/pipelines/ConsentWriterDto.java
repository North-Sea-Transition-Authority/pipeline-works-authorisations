package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;

/**
 * Storage object used to keep track of consent writer changes between writers.
 */
public class ConsentWriterDto {

  private List<PwaConsentOrganisationRole> activeConsentRoles;
  private List<PwaConsentOrganisationRole> consentRolesAdded;
  private List<PwaConsentOrganisationRole> consentRolesEnded;

  private Map<Pipeline, PipelineDetail> pipelineToNewDetailMap;

  public ConsentWriterDto() {
    activeConsentRoles = new ArrayList<>();
    consentRolesAdded = new ArrayList<>();
    consentRolesEnded = new ArrayList<>();
    pipelineToNewDetailMap = new HashMap<>();
  }

  public List<PwaConsentOrganisationRole> getActiveConsentRoles() {
    return activeConsentRoles;
  }

  public void setActiveConsentRoles(List<PwaConsentOrganisationRole> activeConsentRoles) {
    this.activeConsentRoles = activeConsentRoles;
  }

  public List<PwaConsentOrganisationRole> getConsentRolesAdded() {
    return consentRolesAdded;
  }

  public void setConsentRolesAdded(List<PwaConsentOrganisationRole> consentRolesAdded) {
    this.consentRolesAdded = consentRolesAdded;
  }

  public List<PwaConsentOrganisationRole> getConsentRolesEnded() {
    return consentRolesEnded;
  }

  public void setConsentRolesEnded(List<PwaConsentOrganisationRole> consentRolesEnded) {
    this.consentRolesEnded = consentRolesEnded;
  }

  public Map<Pipeline, PipelineDetail> getPipelineToNewDetailMap() {
    return pipelineToNewDetailMap;
  }

  public void setPipelineToNewDetailMap(Map<Pipeline, PipelineDetail> pipelineToNewDetailMap) {
    this.pipelineToNewDetailMap = pipelineToNewDetailMap;
  }

  public void storePipeline(Pipeline pipeline, PipelineDetail newDetail) {
    pipelineToNewDetailMap.put(pipeline, newDetail);
  }

}