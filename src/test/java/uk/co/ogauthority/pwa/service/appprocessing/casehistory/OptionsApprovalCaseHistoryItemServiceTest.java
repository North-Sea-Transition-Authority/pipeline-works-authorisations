package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApplicationApproval;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;

@RunWith(MockitoJUnitRunner.class)
public class OptionsApprovalCaseHistoryItemServiceTest {

  @Mock
  ApproveOptionsService approveOptionsService;

  OptionsApprovalCaseHistoryItemService historyItemService;

  @Before
  public void setup() {
    historyItemService = new OptionsApprovalCaseHistoryItemService(approveOptionsService);
  }

  @Test
  public void requestHistory_NoApproval() {
    when(approveOptionsService.getOptionsApproval(getPwaApplication())).thenReturn(Optional.empty());
    assertThat(historyItemService.getCaseHistoryItemViews(getPwaApplication())).isEqualTo(Collections.emptyList());
  }

  @Test
  public void requestHistory_SingleApproval() {
    when(approveOptionsService.getOptionsApproval(getPwaApplication())).thenReturn(Optional.of(getApproval()));
    var caseHistoryItems = historyItemService.getCaseHistoryItemViews(getPwaApplication());

    assertThat(caseHistoryItems.size()).isEqualTo(1);

    var applicationHistoryItem = caseHistoryItems.get(0);
    assertThat(applicationHistoryItem).isInstanceOf(CaseHistoryItemView.class);
    assertThat(applicationHistoryItem.getHeaderText()).isEqualTo("Application options approved");
    assertThat(applicationHistoryItem.getPersonId().asInt()).isEqualTo(1000);

  }

  private PwaApplication getPwaApplication() {
    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1000);

    return pwaApplication;
  }

  private OptionsApplicationApproval getApproval() {
    var applicationApproval = new OptionsApplicationApproval();
    applicationApproval.setCreatedByPersonId(new PersonId(1000));
    applicationApproval.setCreatedTimestamp(Instant.now());
    applicationApproval.setPwaApplication(getPwaApplication());

    return applicationApproval;
  }
}