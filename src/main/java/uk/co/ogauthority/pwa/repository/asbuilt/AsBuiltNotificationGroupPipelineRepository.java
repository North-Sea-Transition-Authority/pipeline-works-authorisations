package uk.co.ogauthority.pwa.repository.asbuilt;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;

public interface AsBuiltNotificationGroupPipelineRepository extends CrudRepository<AsBuiltNotificationGroupPipeline, Integer> {
  List<AsBuiltNotificationGroupPipeline> findAllByAsBuiltNotificationGroup_Id(Integer asBuiltNotificationGroupId);
}