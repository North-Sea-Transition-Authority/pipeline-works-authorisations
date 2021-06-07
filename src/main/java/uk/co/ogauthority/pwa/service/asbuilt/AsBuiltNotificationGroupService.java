package uk.co.ogauthority.pwa.service.asbuilt;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupRepository;


@Service
public class AsBuiltNotificationGroupService {

  private final AsBuiltNotificationGroupRepository asBuiltNotificationGroupRepository;

  @Autowired
  public AsBuiltNotificationGroupService(AsBuiltNotificationGroupRepository asBuiltNotificationGroupRepository) {
    this.asBuiltNotificationGroupRepository = asBuiltNotificationGroupRepository;
  }

  public Optional<AsBuiltNotificationGroup> getAsBuiltNotificationGroup(Integer ngId) {
    return asBuiltNotificationGroupRepository.findById(ngId);
  }

  MasterPwa getMasterPwaForAsBuiltNotificationGroup(Integer ngId) {
    var asBuiltNotificationGroup = getAsBuiltNotificationGroup(ngId).orElseThrow(
        () -> new IllegalStateException(String.format("Could not find as-built notification group with id %s", ngId)));
    var consent = asBuiltNotificationGroup.getPwaConsent();
    return consent.getMasterPwa();
  }

}
