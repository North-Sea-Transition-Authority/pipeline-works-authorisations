package uk.co.ogauthority.pwa.repository.pwaapplications.shared.campaignworks;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorksPipeline;

public interface PadCampaignWorksPipelineRepository extends CrudRepository<PadCampaignWorksPipeline, Integer> {

  List<PadCampaignWorksPipeline> findAllByPadCampaignWorkSchedule_pwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}