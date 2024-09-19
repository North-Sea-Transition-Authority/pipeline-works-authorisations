package uk.co.ogauthority.pwa.features.termsandconditions.model;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.features.termsandconditions.controller.TermsAndConditionsFormController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class TermsAndConditionsManagementViewItem {

  private final String pwaReference;
  private final String viewPwaUrl;
  private final int variationTerm;
  private final String huooTerms;
  private final int depconParagraph;
  private final int depconSchedule;

  public TermsAndConditionsManagementViewItem(PwaTermsAndConditions pwaTermsAndConditions, String pwaReference) {
    this.pwaReference = pwaReference;
    this.viewPwaUrl = ReverseRouter.route(on(TermsAndConditionsFormController.class)
        .renderEditTermsAndConditionsForm(null, pwaTermsAndConditions.getMasterPwa().getId(), null));
    this.variationTerm = pwaTermsAndConditions.getVariationTerm();
    this.huooTerms = pwaTermsAndConditions.getHuooString();
    this.depconParagraph = pwaTermsAndConditions.getDepconParagraph();
    this.depconSchedule = pwaTermsAndConditions.getDepconSchedule();
  }

  public String getPwaReference() {
    return pwaReference;
  }

  public String getViewPwaUrl() {
    return viewPwaUrl;
  }

  public int getVariationTerm() {
    return variationTerm;
  }

  public String getHuooTerms() {
    return huooTerms;
  }

  public int getDepconParagraph() {
    return depconParagraph;
  }

  public int getDepconSchedule() {
    return depconSchedule;
  }
}