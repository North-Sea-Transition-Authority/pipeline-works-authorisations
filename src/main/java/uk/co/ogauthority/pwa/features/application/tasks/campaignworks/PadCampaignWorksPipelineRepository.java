package uk.co.ogauthority.pwa.features.application.tasks.campaignworks;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface PadCampaignWorksPipelineRepository extends CrudRepository<PadCampaignWorksPipeline, Integer> {

  List<PadCampaignWorksPipeline> findAllByPadCampaignWorkSchedule_pwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail);

  List<PadCampaignWorksPipeline> findAllByPadCampaignWorkSchedule(PadCampaignWorkSchedule padCampaignWorkSchedule);

  List<PadCampaignWorksPipeline> findAllByPadPipeline(PadPipeline pipeline);

}