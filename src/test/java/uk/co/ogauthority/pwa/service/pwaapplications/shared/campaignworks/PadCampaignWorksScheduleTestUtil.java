package uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks;

import java.time.LocalDate;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorkSchedule;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorkSchedule_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorksPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorksPipeline_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadCampaignWorksScheduleTestUtil {

  //no instantiation
  private PadCampaignWorksScheduleTestUtil() {
  }


  public static PadCampaignWorkSchedule createPadCampaignWorkSchedule(PwaApplicationDetail pwaApplicationDetail){
    var sched = new PadCampaignWorkSchedule();
    sched.setPwaApplicationDetail(pwaApplicationDetail);
    sched.setWorkToDate(LocalDate.MAX);
    sched.setWorkFromDate(LocalDate.MIN);

    ObjectTestUtils.assertAllFieldsNotNull(sched, PadCampaignWorkSchedule.class, Set.of(PadCampaignWorkSchedule_.ID));
    return sched;
  }

  public static PadCampaignWorksPipeline createPadCampaignWorksPipeline(PadCampaignWorkSchedule padCampaignWorkSchedule,
                                                                        PadPipeline padPipeline){
    var sched = new PadCampaignWorksPipeline(padCampaignWorkSchedule, padPipeline);
    ObjectTestUtils.assertAllFieldsNotNull(sched, PadCampaignWorksPipeline.class, Set.of(PadCampaignWorksPipeline_.ID));
    return sched;
  }
}
