package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.ApplicationFeeReportTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationPaymentSummariserTest {

  private static final String HEADLINE_FEE_DESC = "FEE_HEAD";
  private static final String FEE_ITEM_DESC = "FEE_ITEM";
  private static final int FEE_AMOUNT = 100;
  private static final String FEE_AMOUNT_FORMATTED = "1.00";

  private ApplicationPaymentSummariser applicationPaymentSummariser;

  private PwaApplication pwaApplication;

  @Before
  public void setUp() throws Exception {
    pwaApplication = new PwaApplication();
    applicationPaymentSummariser = new ApplicationPaymentSummariser();
  }

  @Test
  public void summarise_feeReport_mappedAsExpected() {

    var feeReport = ApplicationFeeReportTestUtil.createReport(
        pwaApplication,
        FEE_AMOUNT,
        HEADLINE_FEE_DESC,
        List.of(ApplicationFeeReportTestUtil.createApplicationFeeItem(FEE_ITEM_DESC, FEE_AMOUNT))
    );

    var result = applicationPaymentSummariser.summarise(feeReport);

    assertThat(result.getHeadlineSummary()).isEqualTo(HEADLINE_FEE_DESC);
    assertThat(result.getFormattedAmount()).isEqualTo(FEE_AMOUNT_FORMATTED);
    assertThat(result.getDisplayableFeeItemList()).hasOnlyOneElementSatisfying(displayableFeeItem -> {
      assertThat(displayableFeeItem.getDescription()).isEqualTo(FEE_ITEM_DESC);
      assertThat(displayableFeeItem.getFormattedAmount()).isEqualTo(FEE_AMOUNT_FORMATTED);
    });


  }
}