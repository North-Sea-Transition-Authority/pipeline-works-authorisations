package uk.co.ogauthority.pwa.features.application.tasks.huoo;

import java.util.List;

/**
 * View object used to support the Huoo Summary task screen.
 */
public final class PadHuooSummaryView {

  private final List<HuooOrganisationUnitRoleView> huooOrganisationUnitRoleViews;

  private final List<HuooTreatyAgreementView> treatyAgreementViews;

  private final boolean canShowHolderGuidance;

  public PadHuooSummaryView(
      List<HuooOrganisationUnitRoleView> huooOrganisationUnitRoleViews,
      List<HuooTreatyAgreementView> treatyAgreementViews, boolean canShowHolderGuidance) {
    this.huooOrganisationUnitRoleViews = huooOrganisationUnitRoleViews;
    this.treatyAgreementViews = treatyAgreementViews;
    this.canShowHolderGuidance = canShowHolderGuidance;
  }

  public List<HuooOrganisationUnitRoleView> getHuooOrganisationUnitRoleViews() {
    return huooOrganisationUnitRoleViews;
  }

  public List<HuooTreatyAgreementView> getTreatyAgreementViews() {
    return treatyAgreementViews;
  }

  public boolean canShowHolderGuidance() {
    return canShowHolderGuidance;
  }
}
