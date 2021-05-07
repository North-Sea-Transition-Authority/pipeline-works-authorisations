package uk.co.ogauthority.pwa.service.asbuilt;

import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupPipelineRepository;

/**
 * Perform database interactions for specific pipeline as--built notifications.
 */
@Component
class AsBuiltPipelineNotificationService {

  private final AsBuiltNotificationGroupPipelineRepository asBuiltNotificationGroupPipelineRepository;

  @Autowired
  AsBuiltPipelineNotificationService(
      AsBuiltNotificationGroupPipelineRepository asBuiltNotificationGroupPipelineRepository) {
    this.asBuiltNotificationGroupPipelineRepository = asBuiltNotificationGroupPipelineRepository;
  }


  void addPipelineDetailsToAsBuiltNotificationGroup(AsBuiltNotificationGroup group,
                                                    Collection<AsBuiltPipelineNotificationSpec> asBuiltPipelineNotificationSpecs) {

    var newPipelineNotifications = asBuiltPipelineNotificationSpecs
        .stream()
        .map(pipelineNotificationSpec -> new AsBuiltNotificationGroupPipeline(
            group,
            pipelineNotificationSpec.getPipelineDetailId(),
            pipelineNotificationSpec.getPipelineChangeCategory()
        ))
        .collect(Collectors.toList());

    asBuiltNotificationGroupPipelineRepository.saveAll(newPipelineNotifications);


  }
}
