package uk.co.ogauthority.pwa.model.entity.asbuilt;

import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;

public final class AsBuiltNotificationGroupTestUtil {

  private AsBuiltNotificationGroupTestUtil(){
    throw new UnsupportedOperationException("not util for you!");
  }

  public static AsBuiltNotificationGroup createDefaultGroupWithConsent(){

    var consent = PwaConsentTestUtil.createInitial(null);
    return new AsBuiltNotificationGroup(consent, "DEFAULT_AS_BUILT_REFERENCE", Instant.now());
  }

}