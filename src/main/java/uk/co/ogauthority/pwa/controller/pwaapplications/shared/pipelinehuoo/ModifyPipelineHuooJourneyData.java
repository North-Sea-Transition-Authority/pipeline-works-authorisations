package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections4.SetUtils;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class ModifyPipelineHuooJourneyData implements Serializable {
  // Random but consistent uuid for class so de-serialisation of session objects wont crash apps after restarts.
  // If this file changes in a way that means any serialised object would be incompatible, this needs to change.
  // Examples of changes are renaming/adding/removing/changing type of member variables
  // Its likely there would need to be a patch that clears the session variable table after any such change
  // TODO PWA-633
  private static final long serialVersionUID = 1L;
  private Integer pwaApplicationDetailId;
  private HuooRole journeyRoleType;
  private Set<String> pickedPipelineIds = new HashSet<>();
  private Set<Integer> organisationUnitIds = new HashSet<>();
  private Set<TreatyAgreement> treatyAgreements = EnumSet.noneOf(TreatyAgreement.class);

  public ModifyPipelineHuooJourneyData() {
  }

  public HuooRole getJourneyRoleType() {
    return journeyRoleType;
  }

  public Set<String> getPickedPipelineIds() {
    return pickedPipelineIds;
  }

  public Set<Integer> getOrganisationUnitIds() {
    return organisationUnitIds;
  }

  public Integer getPwaApplicationDetailId() {
    return pwaApplicationDetailId;
  }

  public Set<TreatyAgreement> getTreatyAgreements() {
    return treatyAgreements;
  }

  public void reset() {
    this.journeyRoleType = null;
    this.pickedPipelineIds = new HashSet<>();
    this.organisationUnitIds = new HashSet<>();
    this.treatyAgreements = EnumSet.noneOf(TreatyAgreement.class);
  }

  /* On every update of form of journey, ensure that the journey data is going to make sense, else reset it.*/
  private void updateResetCheck(PwaApplicationDetail pwaApplicationDetail, HuooRole huooRole) {
    if (!huooRole.equals(this.getJourneyRoleType()) || !pwaApplicationDetail.getId().equals(this.pwaApplicationDetailId)) {
      this.reset();
      this.journeyRoleType = huooRole;
      this.pwaApplicationDetailId = pwaApplicationDetail.getId();
    }
  }

  public void updateJourneyPipelineData(PwaApplicationDetail pwaApplicationDetail,
                                        HuooRole huooRole,
                                        Set<String> pickedPipelineStrings) {
    updateResetCheck(pwaApplicationDetail, huooRole);
    this.pickedPipelineIds = SetUtils.emptyIfNull(pickedPipelineStrings);
  }

  public void updateJourneyOrganisationData(PwaApplicationDetail pwaApplicationDetail,
                                            HuooRole huooRole,
                                            Set<Integer> organisationUnitIds,
                                            Set<TreatyAgreement> treatyAgreements) {
    updateResetCheck(pwaApplicationDetail, huooRole);
    this.organisationUnitIds = SetUtils.emptyIfNull(organisationUnitIds);
    this.treatyAgreements = SetUtils.emptyIfNull(treatyAgreements);
  }

  public void updateFormWithPipelineJourneyData(PwaApplicationDetail pwaApplicationDetail,
                                                HuooRole huooRole,
                                                PickHuooPipelinesForm pickHuooPipelinesForm) {
    updateResetCheck(pwaApplicationDetail, huooRole);
    pickHuooPipelinesForm.setPickedPipelineStrings(this.getPickedPipelineIds());
  }

  public void updateFormWithOrganisationRoleJourneyData(PwaApplicationDetail pwaApplicationDetail,
                                                        HuooRole huooRole,
                                                        PickHuooPipelinesForm pickHuooPipelinesForm) {
    updateResetCheck(pwaApplicationDetail, huooRole);
    pickHuooPipelinesForm.setOrganisationUnitIds(this.organisationUnitIds);
    pickHuooPipelinesForm.setTreatyAgreements(this.treatyAgreements);
  }

}
