package uk.co.ogauthority.pwa.temp.model.view.pipelinetechnical;

import java.io.Serializable;

public class PipelineDesignCodeView implements Serializable {

  private String title;
  private String description;

  public PipelineDesignCodeView(String title, String description) {
    this.title = title;
    this.description = description;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}