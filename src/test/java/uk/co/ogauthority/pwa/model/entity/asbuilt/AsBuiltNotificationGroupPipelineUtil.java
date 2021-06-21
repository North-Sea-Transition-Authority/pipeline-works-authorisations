package uk.co.ogauthority.pwa.model.entity.asbuilt;

import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;

public class AsBuiltNotificationGroupPipelineUtil {

  public static AsBuiltNotificationGroupPipeline createDefaultAsBuiltNotificationGroupPipeline(PipelineDetailId pipelineDetailId) {
    var asBuiltGroup = AsBuiltNotificationGroupTestUtil.createDefaultGroupWithConsent();
    return new AsBuiltNotificationGroupPipeline(asBuiltGroup, pipelineDetailId, PipelineChangeCategory.NEW_PIPELINE);
  }

  public static AsBuiltNotificationGroupPipeline createAsBuiltNotificationGroupPipeline(AsBuiltNotificationGroup asBuiltNotificationGroup,
                                                                                        PipelineDetailId pipelineDetailId,
                                                                                        PipelineChangeCategory pipelineChangeCategory) {
    return new AsBuiltNotificationGroupPipeline(asBuiltNotificationGroup, pipelineDetailId, pipelineChangeCategory);
  }

}
