package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class GroupedIdentView {

  private List<IdentView> identViews;
  private String maxLength;
  private IdentView endIdent;


  public GroupedIdentView(List<IdentView> identViews) {
    this.identViews = Collections.unmodifiableList(identViews);
    this.maxLength = identViews.stream()
        .map(IdentView::getLength)
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO)
        .toPlainString();
    this.endIdent = identViews.get(identViews.size() - 1);
  }

  public List<IdentView> getIdentViews() {
    return identViews;
  }

  public String getMaxLength() {
    return maxLength;
  }

  public IdentView getEndIdent() {
    return endIdent;
  }
}
