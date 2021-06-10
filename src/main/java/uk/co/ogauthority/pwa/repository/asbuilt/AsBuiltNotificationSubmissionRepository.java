package uk.co.ogauthority.pwa.repository.asbuilt;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;

@Repository
public interface AsBuiltNotificationSubmissionRepository extends CrudRepository<AsBuiltNotificationSubmission, Integer> {
}