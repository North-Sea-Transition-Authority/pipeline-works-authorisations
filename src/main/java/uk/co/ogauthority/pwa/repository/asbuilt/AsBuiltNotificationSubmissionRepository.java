package uk.co.ogauthority.pwa.repository.asbuilt;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;

@Repository
public interface AsBuiltNotificationSubmissionRepository extends CrudRepository<AsBuiltNotificationSubmission, Integer> {

  Optional<AsBuiltNotificationSubmission> findByAsBuiltNotificationGroupPipelineAndTipFlagIsTrue(
      AsBuiltNotificationGroupPipeline asBuiltNotificationGroupPipeline);

  List<AsBuiltNotificationSubmission> findAllByAsBuiltNotificationGroupPipelineIn(
      List<AsBuiltNotificationGroupPipeline> asBuiltNotificationGroupPipelines);

  List<AsBuiltNotificationSubmission> findAllByAsBuiltNotificationGroupPipelineInAndTipFlagIsTrue(
      List<AsBuiltNotificationGroupPipeline> asBuiltNotificationGroupPipelines);

}