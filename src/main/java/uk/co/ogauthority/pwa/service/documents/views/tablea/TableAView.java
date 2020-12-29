package uk.co.ogauthority.pwa.service.documents.views.tablea;


import java.util.List;
import java.util.Objects;

public class TableAView {

  private final String pipelineName;
  private final TableARowView headerRow;
  private final List<TableARowView> identRows;
  private final Integer totalRows;


  public TableAView(String pipelineName, TableARowView headerRow,
                    List<TableARowView> identRows) {
    this.pipelineName = pipelineName;
    this.headerRow = headerRow;
    this.identRows = identRows;
    this.totalRows = identRows.size() + 1;
  }

  public String getPipelineName() {
    return pipelineName;
  }

  public TableARowView getHeaderRow() {
    return headerRow;
  }

  public List<TableARowView> getIdentRows() {
    return identRows;
  }

  public Integer getTotalRows() {
    return totalRows;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TableAView that = (TableAView) o;
    return Objects.equals(pipelineName, that.pipelineName)
        && Objects.equals(headerRow, that.headerRow)
        && Objects.equals(identRows, that.identRows)
        && Objects.equals(totalRows, that.totalRows);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineName, headerRow, identRows, totalRows);
  }
}
