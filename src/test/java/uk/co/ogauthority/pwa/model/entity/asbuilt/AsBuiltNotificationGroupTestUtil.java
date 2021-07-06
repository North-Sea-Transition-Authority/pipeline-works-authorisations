package uk.co.ogauthority.pwa.model.entity.asbuilt;

import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;

public final class AsBuiltNotificationGroupTestUtil {

  private AsBuiltNotificationGroupTestUtil(){
    throw new UnsupportedOperationException("not util for you!");
  }

  public static AsBuiltNotificationGroup createDefaultGroupWithConsent(){
    var masterPwa = MasterPwaTestUtil.create(10);
    var consent = PwaConsentTestUtil.createInitial(masterPwa);
    return new AsBuiltNotificationGroup(consent, "DEFAULT_AS_BUILT_REFERENCE", Instant.now());
  }

  public static AsBuiltNotificationGroup createGroupWithConsent_withNgId(Integer ngId){
    var masterPwa = new MasterPwa();
    masterPwa.setId(1);
    var consent = PwaConsentTestUtil.createInitial(masterPwa);
    var group = new AsBuiltNotificationGroup(consent, "DEFAULT_AS_BUILT_REFERENCE", Instant.now());
    group.setId(ngId);
    return group;
  }

}