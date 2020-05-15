package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.Collections;
import java.util.List;

/**
 * This stores a list of idents related to a continuous portion of a pipeline without gaps.
 * The endIdent is used on the pipeline overview to provide the final node heading of each view.
 */
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
