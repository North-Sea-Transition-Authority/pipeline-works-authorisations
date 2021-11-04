package uk.co.ogauthority.pwa.features.application.tasks.campaignworks;


import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface PadCampaignWorkScheduleRepository extends CrudRepository<PadCampaignWorkSchedule, Integer> {
  Optional<PadCampaignWorkSchedule> findByPwaApplicationDetailAndId(PwaApplicationDetail pwaApplicationDetail, int id);

  List<PadCampaignWorkSchedule> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);
}