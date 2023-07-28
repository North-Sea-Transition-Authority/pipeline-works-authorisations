package uk.co.ogauthority.pwa.features.termsandconditions.model;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;

public class TermsAndConditionsManagementViewItem {

  private final String pwaReference;
  private final String viewPwaUrl;
  private final int variationTerm;
  private final String huooTerms;
  private final int depconParagraph;
  private final int depconSchedule;

  public TermsAndConditionsManagementViewItem(PwaTermsAndConditions pwaTermsAndConditions, String pwaReference) {
    this.pwaReference = pwaReference;
    this.viewPwaUrl = ReverseRouter.route(on(PwaViewController.class) // TODO: PWA2020-80: change route to edit page
        .renderViewPwa(pwaTermsAndConditions.getMasterPwa().getId(), PwaViewTab.PIPELINES, null, null, null));
    this.variationTerm = pwaTermsAndConditions.getVariationTerm();
    this.huooTerms =  formatHuooTerms(pwaTermsAndConditions);
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

  private String formatHuooTerms(PwaTermsAndConditions terms) {
    Integer[] huooTerms = {terms.getHuooTermOne(), terms.getHuooTermTwo(), terms.getHuooTermThree()};
    Arrays.sort(huooTerms);
    return String.format("%s, %s & %s", huooTerms[0], huooTerms[1], huooTerms[2]);
  }
}