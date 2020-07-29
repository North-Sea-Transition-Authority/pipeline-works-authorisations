package uk.co.ogauthority.pwa.service.diff;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class DiffTestWithUnsupportedListField {

  List<Instant> instantList;

  public DiffTestWithUnsupportedListField() {
    this.instantList = Arrays.asList(Instant.now());
  }

  public List<Instant> getInstantList() {
    return instantList;
  }

  public void setInstantList(List<Instant> instantList) {
    this.instantList = instantList;
  }
}
