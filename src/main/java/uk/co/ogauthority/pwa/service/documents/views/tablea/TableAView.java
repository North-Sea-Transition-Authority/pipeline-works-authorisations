package uk.co.ogauthority.pwa.service.documents.views.tablea;


import java.util.List;
import java.util.Objects;

public class TableAView {

  private final String pipelineName;
  private final TableARowView headerRow;
  private final List<TableARowView> identRows;
  private final Integer totalRows;
  private final String footnote;


  public TableAView(String pipelineName, TableARowView headerRow,
                    List<TableARowView> identRows, String footnote) {
    this.pipelineName = pipelineName;
    this.headerRow = headerRow;
    this.identRows = identRows;
    this.totalRows = identRows.size() + 1;
    this.footnote = footnote;
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

  public String getFootnote() {
    return footnote;
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
        && Objects.equals(totalRows, that.totalRows)
        && Objects.equals(footnote, that.footnote);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineName, headerRow, identRows, totalRows, footnote);
  }
}
