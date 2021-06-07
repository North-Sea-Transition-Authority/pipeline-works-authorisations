package uk.co.ogauthority.pwa.model.entity.masterpwas;

import java.time.Instant;

public class MasterPwaTestUtil {

  private MasterPwaTestUtil() {
    throw new UnsupportedOperationException("No util for you!");
  }

  public static MasterPwa create() {
    var masterPwa = new MasterPwa();
    masterPwa.setCreatedTimestamp(Instant.now());
    return masterPwa;
  }

  public static MasterPwa create(Integer id) {
    var masterPwa = new MasterPwa();
    masterPwa.setId(id);
    masterPwa.setCreatedTimestamp(Instant.now());
    return masterPwa;
  }

}