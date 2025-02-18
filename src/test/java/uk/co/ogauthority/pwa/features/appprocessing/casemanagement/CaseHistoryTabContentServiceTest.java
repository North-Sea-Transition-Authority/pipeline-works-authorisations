package uk.co.ogauthority.pwa.features.appprocessing.casemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.appprocessing.casehistory.CaseHistoryService;

@ExtendWith(MockitoExtension.class)
class CaseHistoryTabContentServiceTest {

  @Mock
  private CaseHistoryService caseHistoryService;

  private CaseHistoryTabContentService caseHistoryTabContentService;

  private WebUserAccount wua;

  @BeforeEach
  void setUp() {

    caseHistoryTabContentService = new CaseHistoryTabContentService(caseHistoryService);

    wua = new WebUserAccount(1);

  }

  @Test
  void getTabContentModelMap_caseHistoryTab_populated() {

    var processingContext = createContextWithPermissions(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA);

    var caseHistoryList = List.of(
        new CaseHistoryItemView.Builder("Test header", Instant.now(), new PersonId(1)).build()
    );
    when(caseHistoryService.getCaseHistory(processingContext.getPwaApplication())).thenReturn(caseHistoryList);

    var modelMap = caseHistoryTabContentService.getTabContent(processingContext, AppProcessingTab.CASE_HISTORY);

    verify(caseHistoryService, times(1)).getCaseHistory(processingContext.getPwaApplication());

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(tuple("caseHistoryItems", caseHistoryList));

  }

  @Test
  void getTabContentModelMap_differentTab_empty() {

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
        null,
        null,
        Set.of());
  }

}
