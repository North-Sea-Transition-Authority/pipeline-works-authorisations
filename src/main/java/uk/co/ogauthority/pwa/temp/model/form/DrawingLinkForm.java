package uk.co.ogauthority.pwa.temp.model.form;

import java.util.List;
import uk.co.ogauthority.pwa.temp.model.view.PipelineView;

public class DrawingLinkForm {

  private List<PipelineView> pipelineViews;

  public List<PipelineView> getPipelineViews() {
    return pipelineViews;
  }

  public void setPipelineViews(
      List<PipelineView> pipelineViews) {
    this.pipelineViews = pipelineViews;
  }
}
