package uk.co.ogauthority.pwa.repository.asbuilt;


import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatusHistory;

public interface AsBuiltNotificationGroupStatusHistoryRepository extends CrudRepository<AsBuiltNotificationGroupStatusHistory, Integer> {

  Optional<AsBuiltNotificationGroupStatusHistory> findByAsBuiltNotificationGroupAndEndedTimestampIsNull(
      AsBuiltNotificationGroup asBuiltNotificationGroup);

  Optional<AsBuiltNotificationGroupStatusHistory> findByAsBuiltNotificationGroupAndStatusAndEndedTimestampIsNull(
      AsBuiltNotificationGroup asBuiltNotificationGroup,
      AsBuiltNotificationGroupStatus asBuiltNotificationGroupStatus);

  List<AsBuiltNotificationGroupStatusHistory> findAllByEndedTimestampIsNullAndStatusIsNot(AsBuiltNotificationGroupStatus status);

}