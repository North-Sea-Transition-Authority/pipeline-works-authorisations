package uk.co.ogauthority.pwa.service.asbuilt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupDetailRepository;

@ExtendWith(MockitoExtension.class)
class AsBuiltNotificationGroupDetailServiceTest {

  private AsBuiltNotificationGroupDetailService asBuiltNotificationGroupDetailService;

  @Mock
  private AsBuiltNotificationGroupDetailRepository asBuiltNotificationGroupDetailRepository;

  private final PwaConsent pwaConsent = PwaConsentTestUtil.createPwaConsent(50, "CONSENT_REF", Instant.now());
  private final AsBuiltNotificationGroup asBuiltNotificationGroup = new AsBuiltNotificationGroup(pwaConsent, "APP_REF", Instant.now());


  @BeforeEach
  void setup() {
    asBuiltNotificationGroupDetailService = new AsBuiltNotificationGroupDetailService(asBuiltNotificationGroupDetailRepository);
    when(asBuiltNotificationGroupDetailRepository.findByAsBuiltNotificationGroupAndEndedTimestampIsNull(any(
        AsBuiltNotificationGroup.class))).thenReturn(
        Optional.of(new AsBuiltNotificationGroupDetail(asBuiltNotificationGroup, LocalDate.now(), mock(
            PersonId.class), mock(Instant.class))));
  }

  @Test
  void getAsBuiltNotificationGroupDetail_repositoryCalled() {
    asBuiltNotificationGroupDetailService.getAsBuiltNotificationGroupDetail(asBuiltNotificationGroup);
    verify(asBuiltNotificationGroupDetailRepository).findByAsBuiltNotificationGroupAndEndedTimestampIsNull(asBuiltNotificationGroup);
  }
}
