package uk.co.ogauthority.pwa.service.search.consents.testutil;

import java.time.Instant;
import java.util.Set;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaTestUtil;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;

public class PwaContextTestUtil {

  private static int PWA_ID1 = 1;
  private static String PWA_REF1 = "PWA Reference 1";

  private PwaContextTestUtil(){}



  private static ConsentSearchResultView createConsentSearchResultView() {
    return new ConsentSearchResultView(PWA_ID1, PWA_REF1, PwaResourceType.PETROLEUM, null, null,
        Instant.now(), null, Instant.now());
  }

  public static PwaContext createPwaContext(MasterPwa masterPwa, WebUserAccount user, Set<PwaPermission> pwaPermissions) {
    return new PwaContext(masterPwa, user, pwaPermissions, createConsentSearchResultView());
  }

  public static PwaContext createPwaContext() {
    var masterPwa = MasterPwaTestUtil.create();
    var user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), Set.of(
        PwaUserPrivilege.PWA_ACCESS));
    return createPwaContext(masterPwa, user, Set.of(PwaPermission.VIEW_PWA));
  }

}
