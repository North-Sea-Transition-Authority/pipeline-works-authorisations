package uk.co.ogauthority.pwa.service.asbuilt.view;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupDetailService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltPipelineNotificationService;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltViewerServiceTest {

  private AsBuiltViewerService asBuiltViewerService;

  @Mock
  private AsBuiltNotificationViewService asBuiltNotificationViewService;

  @Mock
  private AsBuiltNotificationSummaryService asBuiltNotificationSummaryService;

  @Mock
  private AsBuiltNotificationGroupService asBuiltNotificationGroupService;

  @Mock
  private AsBuiltNotificationGroupDetailService asBuiltNotificationGroupDetailService;

  @Mock
  private AsBuiltPipelineNotificationService asBuiltPipelineNotificationService;

  private static final int NOTIFICATION_GROUP_ID = 1;
  private final Person person = PersonTestUtil.createDefaultPerson();
  private final PwaConsent pwaConsent = PwaConsentTestUtil.createPwaConsent(40, "CONSENT_REF", Instant.now());
  private final AsBuiltNotificationGroup asBuiltNotificationGroup = new AsBuiltNotificationGroup(pwaConsent, "APP_REF", Instant.now());
  private final AsBuiltNotificationGroupDetail
      asBuiltNotificationGroupDetail = new AsBuiltNotificationGroupDetail(asBuiltNotificationGroup, LocalDate
      .now(), person.getId(), Instant.now());

  @Before
  public void setup() {
    asBuiltViewerService = new AsBuiltViewerService(asBuiltNotificationViewService, asBuiltNotificationSummaryService,
        asBuiltNotificationGroupService, asBuiltNotificationGroupDetailService, asBuiltPipelineNotificationService);

    asBuiltNotificationGroup.setId(NOTIFICATION_GROUP_ID);
    when(asBuiltNotificationGroupService.getAsBuiltNotificationGroup(asBuiltNotificationGroup.getId()))
        .thenReturn(Optional.of(asBuiltNotificationGroup));
    when(asBuiltNotificationGroupDetailService.getAsBuiltNotificationGroupDetail(asBuiltNotificationGroup))
        .thenReturn(Optional.of(asBuiltNotificationGroupDetail));
    when(asBuiltPipelineNotificationService.getPipelineDetailsForAsBuiltNotificationGroup(asBuiltNotificationGroup.getId()))
        .thenReturn(List.of());
  }

  @Test
  public void getAsBuiltNotificationGroupSummaryView_serviceCalledSuccessfully() {
    asBuiltViewerService.getAsBuiltNotificationGroupSummaryView(asBuiltNotificationGroup.getId());
    verify(asBuiltNotificationSummaryService).getAsBuiltNotificationGroupSummaryView(asBuiltNotificationGroupDetail);
  }

  @Test
  public void getAsBuiltPipelineNotificationSubmissionViews_serviceCalledSuccessfully() {
    asBuiltViewerService.getAsBuiltPipelineNotificationSubmissionViews(asBuiltNotificationGroup.getId());
    verify(asBuiltNotificationViewService).getAsBuiltPipelineNotificationSubmissionViews(asBuiltNotificationGroup.getId(), List.of());
  }

}
