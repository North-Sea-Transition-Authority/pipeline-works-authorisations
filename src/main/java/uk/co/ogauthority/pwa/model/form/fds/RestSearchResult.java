package uk.co.ogauthority.pwa.model.form.fds;

import java.util.List;

public class RestSearchResult {
  List<RestSearchItem> results;

  public RestSearchResult(List<RestSearchItem> results) {
    this.results = results;
  }

  public List<RestSearchItem> getResults() {
    return results;
  }
}
