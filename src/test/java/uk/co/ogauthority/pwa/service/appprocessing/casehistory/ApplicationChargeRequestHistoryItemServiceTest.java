package uk.co.ogauthority.pwa.service.appprocessing.casehistory;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReportTestUtil;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationChargeRequestHistoryItemServiceTest {

  private final int PENNIES = 100;
  private final String  SUMMARY = "SUMMARY";

  @Mock
  private ApplicationChargeRequestService applicationChargeRequestService;

  @Mock
  private ApplicationPaymentSummariser applicationPaymentSummariser;

  @Mock
  private PersonService personService;


  private PwaApplication pwaApplication;
  private Person fakePerson;

  private ApplicationChargeRequestHistoryItemService caseHistoryService;


  @Before
  public void setUp() throws Exception {
    caseHistoryService = new ApplicationChargeRequestHistoryItemService(
        applicationChargeRequestService,
        applicationPaymentSummariser,
        personService
    );

    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getPwaApplication();

    fakePerson = PersonTestUtil.createDefaultPerson();
    when(personService.getPersonById(any())).thenReturn(fakePerson);
  }

  @Test
  public void getCaseHistoryItemViews_whenRequestOpen() {
    var applicationChargeRequestReport = ApplicationChargeRequestReportTestUtil.createOpenReport(
        PENNIES,
        SUMMARY,
        List.of()

    );
    when(applicationChargeRequestService.getAllApplicationChargeRequestReportsForApplication(any()))
        .thenReturn(List.of(applicationChargeRequestReport));

    var caseHistoryItems = caseHistoryService.getCaseHistoryItemViews(pwaApplication);

  }
}