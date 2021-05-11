package uk.co.ogauthority.pwa.service.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupPipelineRepository;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltPipelineNotificationServiceTest {

  @Mock
  private AsBuiltNotificationGroupPipelineRepository asBuiltNotificationGroupPipelineRepository;

  @Captor
  private ArgumentCaptor<List<AsBuiltNotificationGroupPipeline>> notificationGroupPipelineListCaptor;

  private AsBuiltPipelineNotificationService asBuiltPipelineNotificationService;

  private AsBuiltNotificationGroup asBuiltNotificationGroup;

  @Before
  public void setup() {

    asBuiltNotificationGroup = AsBuiltNotificationGroupTestUtil.createDefaultGroupWithConsent();
    asBuiltPipelineNotificationService = new AsBuiltPipelineNotificationService(
        asBuiltNotificationGroupPipelineRepository);

  }

  @Test
  public void addPipelineDetailsToAsBuiltNotificationGroup_noPipelineNotificationsSpecs() {
    asBuiltPipelineNotificationService.addPipelineDetailsToAsBuiltNotificationGroup(
        asBuiltNotificationGroup,
        List.of()
    );

    verify(asBuiltNotificationGroupPipelineRepository).saveAll(notificationGroupPipelineListCaptor.capture());
    assertThat(notificationGroupPipelineListCaptor.getValue()).isEmpty();

  }

  @Test
  public void addPipelineDetailsToAsBuiltNotificationGroup_pipelineNotificationsSpecsMappedAsExpected() {

    var pipelineSpec1 = new AsBuiltPipelineNotificationSpec(new PipelineDetailId(1),
        PipelineChangeCategory.NEW_PIPELINE);
    var pipelineSpec2 = new AsBuiltPipelineNotificationSpec(new PipelineDetailId(2),
        PipelineChangeCategory.CONSENT_UPDATE);
    asBuiltPipelineNotificationService.addPipelineDetailsToAsBuiltNotificationGroup(
        asBuiltNotificationGroup,
        List.of(pipelineSpec1, pipelineSpec2)
    );

    verify(asBuiltNotificationGroupPipelineRepository).saveAll(notificationGroupPipelineListCaptor.capture());
    assertThat(notificationGroupPipelineListCaptor.getValue()).hasSize(2)
        .allSatisfy(asBuiltNotificationGroupPipeline ->
            assertThat(asBuiltNotificationGroupPipeline.getAsBuiltNotificationGroup()).isEqualTo(asBuiltNotificationGroup))
        .anySatisfy(asBuiltNotificationGroupPipeline -> {
          assertThat(asBuiltNotificationGroupPipeline.getPipelineDetailId()).isEqualTo(pipelineSpec1.getPipelineDetailId());
          assertThat(asBuiltNotificationGroupPipeline.getPipelineChangeCategory()).isEqualTo(pipelineSpec1.getPipelineChangeCategory());
        })
        .anySatisfy(asBuiltNotificationGroupPipeline -> {
          assertThat(asBuiltNotificationGroupPipeline.getPipelineDetailId()).isEqualTo(pipelineSpec2.getPipelineDetailId());
          assertThat(asBuiltNotificationGroupPipeline.getPipelineChangeCategory()).isEqualTo(pipelineSpec2.getPipelineChangeCategory());
        });

  }
}