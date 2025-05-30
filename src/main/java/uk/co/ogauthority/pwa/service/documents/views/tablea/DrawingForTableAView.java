package uk.co.ogauthority.pwa.service.documents.views.tablea;

import java.util.List;
import java.util.Objects;

public class DrawingForTableAView {

  private final List<TableAView> tableAViews;
  private final String projectName;
  private final String drawingFileId;
  private final String drawingReference;
  private final String imageSource;

  public DrawingForTableAView(List<TableAView> tableAViews, String projectName, String drawingFileId,
                              String drawingReference, String imageSource) {
    this.tableAViews = tableAViews;
    this.projectName = projectName;
    this.drawingFileId = drawingFileId;
    this.drawingReference = drawingReference;
    this.imageSource = imageSource;
  }


  public List<TableAView> getTableAViews() {
    return tableAViews;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getDrawingFileId() {
    return drawingFileId;
  }

  public String getDrawingReference() {
    return drawingReference;
  }

  public String getImageSource() {
    return imageSource;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DrawingForTableAView that = (DrawingForTableAView) o;
    return Objects.equals(tableAViews, that.tableAViews)
        && Objects.equals(projectName, that.projectName)
        && Objects.equals(drawingFileId, that.drawingFileId)
        && Objects.equals(drawingReference, that.drawingReference)
        && Objects.equals(imageSource, that.imageSource);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tableAViews, projectName, drawingFileId, drawingReference, imageSource);
  }
}



