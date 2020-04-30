package uk.co.ogauthority.pwa.temp.model.view;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.io.Serializable;
import java.util.List;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.controller.TechnicalDrawingsTempController;

public class TechnicalDrawingView implements Serializable {

  private final String image;
  private final List<PipelineView> pipelineViewList;
  private final Integer drawingId;
  private final String name;

  public TechnicalDrawingView(Integer drawingId, String image,
                              List<PipelineView> pipelineViewList, String name) {
    this.drawingId = drawingId;
    this.pipelineViewList = pipelineViewList;
    this.image = image;
    this.name = name;
  }

  public String getImageUrl() {
    return image;
  }

  public List<PipelineView> getPipelineViewList() {
    return pipelineViewList;
  }

  public Integer getDrawingId() {
    return drawingId;
  }

  public String getEditRoute(Integer applicationId) {
    return ReverseRouter.route(on(TechnicalDrawingsTempController.class).viewDrawingEdit(applicationId, drawingId, null));
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TechnicalDrawingView) {
      var viewObject = (TechnicalDrawingView) obj;
      return drawingId.equals(viewObject.drawingId) && image.equals(viewObject.image);
    }
    return false;
  }
}
