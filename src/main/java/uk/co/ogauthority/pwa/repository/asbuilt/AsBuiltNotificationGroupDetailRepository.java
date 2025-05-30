package uk.co.ogauthority.pwa.repository.asbuilt;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;

public interface AsBuiltNotificationGroupDetailRepository extends CrudRepository<AsBuiltNotificationGroupDetail, Integer> {

  Optional<AsBuiltNotificationGroupDetail> findByAsBuiltNotificationGroupAndEndedTimestampIsNull(
      AsBuiltNotificationGroup asBuiltNotificationGroup);

  List<AsBuiltNotificationGroupDetail> findAllByAsBuiltNotificationGroupInAndDeadlineDate(List<AsBuiltNotificationGroup>
                                                                                              asBuiltNotificationGroups,
                                                                                          LocalDate deadlineDate);

}