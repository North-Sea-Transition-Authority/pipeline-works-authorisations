package uk.co.ogauthority.pwa.temp.components;

import java.io.Serializable;
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.temp.model.view.TechnicalDrawingView;

@Component
@Scope("session")
public class TechnicalDrawingsGodObject implements Serializable {

  private List<TechnicalDrawingView> technicalDrawingViews;

  public TechnicalDrawingsGodObject() {
    technicalDrawingViews = List.of();
  }

  public List<TechnicalDrawingView> getTechnicalDrawingViews() {
    return technicalDrawingViews;
  }

  public void setTechnicalDrawingViews(List<TechnicalDrawingView> technicalDrawingMap) {
    this.technicalDrawingViews = technicalDrawingMap;
  }
}
