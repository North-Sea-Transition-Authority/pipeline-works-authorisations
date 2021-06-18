package uk.co.ogauthority.pwa.model.docgen;

/**
 * JSON payload representing the state of a docgen run.
 */
public class DocgenRunStatusResult {

  private final long docgenRunId;
  private final DocgenRunStatus status;
  private final String onCompleteUrl;

  public DocgenRunStatusResult(DocgenRun docgenRun, String onCompleteUrl) {
    this.docgenRunId = docgenRun.getId();
    this.status = docgenRun.getStatus();
    this.onCompleteUrl = onCompleteUrl;
  }

  public long getDocgenRunId() {
    return docgenRunId;
  }

  public DocgenRunStatus getStatus() {
    return status;
  }

  public String getOnCompleteUrl() {
    return onCompleteUrl;
  }

}
