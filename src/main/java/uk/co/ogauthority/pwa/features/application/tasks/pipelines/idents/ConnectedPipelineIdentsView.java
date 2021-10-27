package uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents;

import java.util.Collections;
import java.util.List;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.IdentView;

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
