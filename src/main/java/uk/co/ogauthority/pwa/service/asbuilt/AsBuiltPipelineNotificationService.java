package uk.co.ogauthority.pwa.service.asbuilt;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupPipelineRepository;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

/**
 * Perform database interactions for specific pipeline as--built notifications.
 */
@Component
public class AsBuiltPipelineNotificationService {

  private final AsBuiltNotificationGroupPipelineRepository asBuiltNotificationGroupPipelineRepository;
  private final PipelineDetailService pipelineDetailService;

  @Autowired
  AsBuiltPipelineNotificationService(
      AsBuiltNotificationGroupPipelineRepository asBuiltNotificationGroupPipelineRepository,
      PipelineDetailService pipelineDetailService) {
    this.asBuiltNotificationGroupPipelineRepository = asBuiltNotificationGroupPipelineRepository;
    this.pipelineDetailService = pipelineDetailService;
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

  public AsBuiltNotificationGroupPipeline getAsBuiltNotificationGroupPipeline(Integer asBuiltNotificationGroupId,
                                                                              PipelineDetailId pipelineDetailId) {
    return asBuiltNotificationGroupPipelineRepository.findByAsBuiltNotificationGroup_IdAndPipelineDetailId(asBuiltNotificationGroupId,
        pipelineDetailId);
  }

  public List<PipelineDetail> getPipelineDetailsForAsBuiltNotificationGroup(Integer asBuiltNotificationGroupId) {
    var asBuiltGroupPipelines = asBuiltNotificationGroupPipelineRepository
        .findAllByAsBuiltNotificationGroup_Id(asBuiltNotificationGroupId);

    return asBuiltGroupPipelines.stream()
        .map(ngGroupPipeline -> pipelineDetailService.getByPipelineDetailId(ngGroupPipeline.getPipelineDetailId().asInt()))
        .collect(Collectors.toList());
  }

  public PipelineDetail getPipelineDetail(Integer pipelineDetailId) {
    return pipelineDetailService.getByPipelineDetailId(pipelineDetailId);
  }

}
