package uk.co.ogauthority.pwa.service.appprocessing.tabs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.appprocessing.casehistory.CaseHistoryService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;

@RunWith(MockitoJUnitRunner.class)
public class CaseHistoryTabContentServiceTest {

  @Mock
  private CaseHistoryService caseHistoryService;

  private CaseHistoryTabContentService caseHistoryTabContentService;

  private WebUserAccount wua;
  private AuthenticatedUserAccount authenticatedUserAccount;

  @Before
  public void setUp() {

    caseHistoryTabContentService = new CaseHistoryTabContentService(caseHistoryService);

    wua = new WebUserAccount(1);
    authenticatedUserAccount = new AuthenticatedUserAccount(wua, EnumSet.allOf(PwaUserPrivilege.class));

  }

  @Test
  public void getTabContentModelMap_caseHistoryTab_populated() {

    var processingContext = createContextWithPermissions(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA);

    var caseHistoryList = List.of(
        new CaseHistoryItemView("Test header", Instant.now(), "Person label", new PersonId(1), Map.of())
    );
    when(caseHistoryService.getCaseHistory(processingContext.getPwaApplication())).thenReturn(caseHistoryList);

    var modelMap = caseHistoryTabContentService.getTabContent(processingContext, AppProcessingTab.CASE_HISTORY);

    verify(caseHistoryService, times(1)).getCaseHistory(processingContext.getPwaApplication());

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(tuple("caseHistoryItems", caseHistoryList));

  }

  @Test
  public void getTabContentModelMap_differentTab_empty() {

    var processingContext = createContextWithPermissions(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA);

    var modelMap = caseHistoryTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verifyNoMoreInteractions(caseHistoryService);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(tuple("caseHistoryItems", List.of()));

  }

  private PwaAppProcessingContext createContextWithPermissions(PwaAppProcessingPermission... permissions) {
    return new PwaAppProcessingContext(
        new PwaApplicationDetail(),
        wua,
        Set.of(permissions),
        null
    );
  }

}
