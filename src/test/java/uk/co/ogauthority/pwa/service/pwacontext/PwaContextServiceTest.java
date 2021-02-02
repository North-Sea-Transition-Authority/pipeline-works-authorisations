package uk.co.ogauthority.pwa.service.pwacontext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaManagementService;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchService;

@RunWith(MockitoJUnitRunner.class)
public class PwaContextServiceTest {

  @Mock
  private PwaPermissionService pwaPermissionService;

  @Mock
  private MasterPwaManagementService masterPwaManagementService;

  @Mock
  private ConsentSearchService consentSearchService;

  private PwaContextService contextService;

  private MasterPwa masterPwa;
  private AuthenticatedUserAccount user;
  private ConsentSearchResultView consentSearchResultView;

  @Before
  public void setUp() {

    masterPwa = new MasterPwa();
    masterPwa.setId(1);

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());

    var consentSearchItem = new ConsentSearchItem();
    consentSearchItem.setFirstConsentTimestamp(Instant.now());
    consentSearchItem.setLatestConsentTimestamp(Instant.now());
    consentSearchResultView = ConsentSearchResultView.fromSearchItem(consentSearchItem);

    contextService = new PwaContextService(pwaPermissionService, masterPwaManagementService, consentSearchService);

    when(masterPwaManagementService.getMasterPwaById(masterPwa.getId())).thenReturn(masterPwa);
    when(consentSearchService.getConsentSearchResultView(masterPwa.getId())).thenReturn(consentSearchResultView);
  }



  @Test(expected = AccessDeniedException.class)
  public void validateAndCreate_userHasNoPermissions() {
    when(pwaPermissionService.getPwaPermissions(masterPwa, user)).thenReturn(Set.of());
    var contextParams = new PwaContextParams(1, user);
    contextService.validateAndCreate(contextParams);
  }

  @Test
  public void validateAndCreate_permissionRequired_userHasCorrectPermissions() {
    var validPermissions = Set.of(PwaPermission.VIEW_PWA);
    when(pwaPermissionService.getPwaPermissions(masterPwa, user)).thenReturn(validPermissions);

    var contextParams = new PwaContextParams(1, user).requiredPwaPermissions(Set.of(PwaPermission.VIEW_PWA));
    var context = contextService.validateAndCreate(contextParams);
    assertThat(context.getUser()).isEqualTo(user);
    assertThat(context.getPwaPermissions()).isEqualTo(validPermissions);
    assertThat(context.getConsentSearchResultView()).isEqualTo(consentSearchResultView);
  }


}
