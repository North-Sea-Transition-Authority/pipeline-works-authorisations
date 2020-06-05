package uk.co.ogauthority.pwa.repository.pwaapplications.shared.campaignworks;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorkSchedule;

public interface PadCampaignWorkScheduleRepository extends CrudRepository<PadCampaignWorkSchedule, Integer> {
  Optional<PadCampaignWorkSchedule> findByPwaApplicationDetailAndId(PwaApplicationDetail pwaApplicationDetail, int id);
}