package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.Collections;
import java.util.List;

public class ConnectedPipelineIdentsView {

  private final List<IdentView> identViews;
  private final IdentView endIdent;


  public ConnectedPipelineIdentsView(List<IdentView> identViews) {
    this.identViews = Collections.unmodifiableList(identViews);
    this.endIdent = identViews.get(identViews.size() - 1);
  }

  public List<IdentView> getIdentViews() {
    return identViews;
  }

  public IdentView getEndIdent() {
    return endIdent;
  }
}
