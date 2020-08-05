package uk.co.ogauthority.pwa.repository.pwaapplications.shared.campaignworks;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorkSchedule;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorksPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

public interface PadCampaignWorksPipelineRepository extends CrudRepository<PadCampaignWorksPipeline, Integer> {

  List<PadCampaignWorksPipeline> findAllByPadCampaignWorkSchedule_pwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail);

  List<PadCampaignWorksPipeline> findAllByPadCampaignWorkSchedule(PadCampaignWorkSchedule padCampaignWorkSchedule);

  List<PadCampaignWorksPipeline> findAllByPadPipeline(PadPipeline pipeline);

}