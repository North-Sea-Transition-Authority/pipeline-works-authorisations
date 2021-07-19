package uk.co.ogauthority.pwa.service.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupRepository;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltNotificationGroupServiceTest {

  private AsBuiltNotificationGroupService asBuiltNotificationGroupService;

  @Mock
  private AsBuiltNotificationGroupRepository asBuiltNotificationGroupRepository;

  private static final Integer AS_BUILT_NOTIFICATION_GROUP_ID = 10;

  private final AsBuiltNotificationGroup asBuiltNotificationGroup = AsBuiltNotificationGroupTestUtil
      .createGroupWithConsent_withApplication_fromNgId(AS_BUILT_NOTIFICATION_GROUP_ID);

  @Before
  public void setup() {
    asBuiltNotificationGroupService = new AsBuiltNotificationGroupService(asBuiltNotificationGroupRepository);
  }

  @Test
  public void getAsBuiltNotificationGroup() {
    when(asBuiltNotificationGroupRepository.findById(AS_BUILT_NOTIFICATION_GROUP_ID))
        .thenReturn(Optional.of(asBuiltNotificationGroup));

    assertThat(asBuiltNotificationGroupService.getAsBuiltNotificationGroup(AS_BUILT_NOTIFICATION_GROUP_ID))
        .isEqualTo(Optional.of(asBuiltNotificationGroup));

    verify(asBuiltNotificationGroupRepository).findById(AS_BUILT_NOTIFICATION_GROUP_ID);

  }

  @Test
  public void getAsBuiltNotificationGroupPerConsent() {
    when(asBuiltNotificationGroupRepository.findByPwaConsent(asBuiltNotificationGroup.getPwaConsent()))
        .thenReturn(Optional.of(asBuiltNotificationGroup));

    assertThat(asBuiltNotificationGroupService.getAsBuiltNotificationGroupPerConsent(asBuiltNotificationGroup.getPwaConsent()))
        .isEqualTo(Optional.of(asBuiltNotificationGroup));

    verify(asBuiltNotificationGroupRepository).findByPwaConsent(asBuiltNotificationGroup.getPwaConsent());
  }

  @Test
  public void getMasterPwaForAsBuiltNotificationGroup() {
    when(asBuiltNotificationGroupRepository.findById(AS_BUILT_NOTIFICATION_GROUP_ID))
        .thenReturn(Optional.of(asBuiltNotificationGroup));

    assertThat(asBuiltNotificationGroupService.getMasterPwaForAsBuiltNotificationGroup(AS_BUILT_NOTIFICATION_GROUP_ID).getId())
        .isEqualTo(asBuiltNotificationGroup.getMasterPwaIdFromGroupConsent());

    verify(asBuiltNotificationGroupRepository).findById(AS_BUILT_NOTIFICATION_GROUP_ID);
  }

}
