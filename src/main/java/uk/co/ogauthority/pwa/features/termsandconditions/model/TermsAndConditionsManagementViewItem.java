package uk.co.ogauthority.pwa.features.termsandconditions.model;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;

public class TermsAndConditionsManagementViewItem {

  private final String pwaReference;
  private final String viewPwaUrl;
  private final int variationTerm;
  private final String huooTerms;
  private final int depconParagraph;
  private final int depconSchedule;

  public TermsAndConditionsManagementViewItem(PwaTermsAndConditions pwaTermsAndConditions, MasterPwaService masterPwaService) {
    var masterPwaDetail = masterPwaService.getCurrentDetailOrThrow(pwaTermsAndConditions.getMasterPwa());

    this.pwaReference = masterPwaDetail.getReference();
    this.viewPwaUrl = ReverseRouter.route(on(PwaViewController.class)
        .renderViewPwa(masterPwaDetail.getMasterPwaId(), PwaViewTab.PIPELINES, null, null, null));
    this.variationTerm = pwaTermsAndConditions.getVariationTerm();
    this.huooTerms =  pwaTermsAndConditions.getHuooTerms();
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