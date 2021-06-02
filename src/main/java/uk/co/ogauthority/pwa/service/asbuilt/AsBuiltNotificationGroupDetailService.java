package uk.co.ogauthority.pwa.service.asbuilt;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupDetailRepository;

@Service
public class AsBuiltNotificationGroupDetailService {

  private final AsBuiltNotificationGroupDetailRepository asBuiltNotificationGroupDetailRepository;

  @Autowired
  public AsBuiltNotificationGroupDetailService(
      AsBuiltNotificationGroupDetailRepository asBuiltNotificationGroupDetailRepository) {
    this.asBuiltNotificationGroupDetailRepository = asBuiltNotificationGroupDetailRepository;
  }

  public Optional<AsBuiltNotificationGroupDetail> getAsBuiltNotificationGroupDetail(AsBuiltNotificationGroup asBuiltNotificationGroup) {
    return asBuiltNotificationGroupDetailRepository.findByAsBuiltNotificationGroupAndEndedTimestampIsNull(asBuiltNotificationGroup);
  }
}
