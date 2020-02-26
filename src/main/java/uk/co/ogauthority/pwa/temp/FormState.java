package uk.co.ogauthority.pwa.temp;

import java.io.Serializable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.FastTrackForm;
import uk.co.ogauthority.pwa.temp.model.form.LocationForm;
import uk.co.ogauthority.pwa.temp.model.form.ProjectInformationForm;

@Component
@Scope("session")
public class FormState implements Serializable {

  private AdministrativeDetailsForm administrativeDetailsForm;
  private ProjectInformationForm projectInformationForm;
  private LocationForm locationForm;
  private FastTrackForm fastTrackForm;

  public FormState() {
    administrativeDetailsForm = new AdministrativeDetailsForm();
    projectInformationForm = new ProjectInformationForm();
    locationForm = new LocationForm();
    fastTrackForm = new FastTrackForm();
  }

  public void save(AdministrativeDetailsForm administrativeDetailsForm) {
    this.administrativeDetailsForm = administrativeDetailsForm;
  }

  public void save(ProjectInformationForm projectInformationForm) {
    this.projectInformationForm = projectInformationForm;
  }

  public void save(LocationForm locationForm) {
    this.locationForm = locationForm;
  }

  public void save(FastTrackForm fastTrackForm) {
    this.fastTrackForm = fastTrackForm;
  }

  public void apply(AdministrativeDetailsForm form) {
    form.setProjectDescription(this.administrativeDetailsForm.getProjectDescription());
    form.setProjectDiagram(this.administrativeDetailsForm.getProjectDiagram());
    form.setAgreesToFdp(this.administrativeDetailsForm.getAgreesToFdp());
    form.setLocationFromShore(this.administrativeDetailsForm.getLocationFromShore());
    form.setWithinSafetyZone(this.administrativeDetailsForm.getWithinSafetyZone());
    form.setStructureNameIfYes(this.administrativeDetailsForm.getStructureNameIfYes());
    form.setStructureNameIfPartially(this.administrativeDetailsForm.getStructureNameIfPartially());
    form.setMethodOfTransportation(this.administrativeDetailsForm.getMethodOfTransportation());
    form.setLandfallDetails(this.administrativeDetailsForm.getLandfallDetails());
    form.setAcceptFundsLiability(this.administrativeDetailsForm.getAcceptFundsLiability());
    form.setAcceptOpolLiability(this.administrativeDetailsForm.getAcceptOpolLiability());
    form.setWhollyOffshore(this.administrativeDetailsForm.getWhollyOffshore());
  }

  public void apply(ProjectInformationForm form) {
    form.setWorkStartDay(this.projectInformationForm.getWorkStartDay());
    form.setWorkStartMonth(this.projectInformationForm.getWorkStartMonth());
    form.setWorkStartYear(this.projectInformationForm.getWorkStartYear());
    form.setEarliestCompletionDay(this.projectInformationForm.getEarliestCompletionDay());
    form.setEarliestCompletionMonth(this.projectInformationForm.getEarliestCompletionMonth());
    form.setEarliestCompletionYear(this.projectInformationForm.getEarliestCompletionYear());
    form.setLatestCompletionDay(this.projectInformationForm.getLatestCompletionDay());
    form.setField(this.projectInformationForm.getField());
    form.setLatestCompletionMonth(this.projectInformationForm.getLatestCompletionMonth());
    form.setLatestCompletionYear(this.projectInformationForm.getLatestCompletionYear());
    form.setDescription(this.projectInformationForm.getDescription());
  }

  public void apply(LocationForm form) {
    form.setMedianLineSelection(this.locationForm.getMedianLineSelection());
    form.setMedianLineAgreementOngoing(this.locationForm.getMedianLineAgreementOngoing());
    form.setNegotiatorNameOngoing(this.locationForm.getNegotiatorNameOngoing());
    form.setNegotiatorEmailOngoing(this.locationForm.getNegotiatorEmailOngoing());
    form.setMedianLineAgreementComplete(this.locationForm.getMedianLineAgreementComplete());
    form.setNegotiatorNameComplete(this.locationForm.getNegotiatorNameComplete());
    form.setNegotiatorEmailComplete(this.locationForm.getNegotiatorEmailComplete());
    form.setLikelySignificantImpact(this.locationForm.getLikelySignificantImpact());
    form.setEmtSubmitByDay(this.locationForm.getEmtSubmitByDay());
    form.setEmtSubmitByMonth(this.locationForm.getEmtSubmitByMonth());
    form.setEmtSubmitByYear(this.locationForm.getEmtSubmitByYear());
    form.setEmtStatement(this.locationForm.getEmtStatement());
    form.setAcceptEolRegulations(this.locationForm.getAcceptEolRegulations());
    form.setAcceptEolRemoval(this.locationForm.getAcceptEolRemoval());
    form.setAcceptRemovalProposal(this.locationForm.getAcceptRemovalProposal());
    form.setDecommissioningPlans(this.locationForm.getDecommissioningPlans());
  }

  public void apply(FastTrackForm form) {
    form.setJustification(this.fastTrackForm.getJustification());
  }

}
