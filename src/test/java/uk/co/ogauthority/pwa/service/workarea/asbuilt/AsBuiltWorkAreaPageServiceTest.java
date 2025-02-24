package uk.co.ogauthority.pwa.service.workarea.asbuilt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationWorkareaView;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationDtoRepository;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;

@ExtendWith(MockitoExtension.class)
class AsBuiltWorkAreaPageServiceTest {

  private static final int REQUESTED_PAGE = 0;

  private AsBuiltWorkAreaPageService asBuiltWorkAreaPageService;

  @Mock
  private AsBuiltNotificationDtoRepository asBuiltNotificationDtoRepository;

  private final AuthenticatedUserAccount user = new AuthenticatedUserAccount(
      new WebUserAccount(10),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA));


  @BeforeEach
  void setup() {
    asBuiltWorkAreaPageService = new AsBuiltWorkAreaPageService(asBuiltNotificationDtoRepository);
  }

  private Pageable getDefaultWorkAreaViewPageable(int requestedPage) {
    return PageRequest.of(requestedPage, WorkAreaService.PAGE_SIZE, AsBuiltWorkAreaSort.DEADLINE_DATE_ASC.getSort());
  }

  @Test
  void getPageView_asBuiltNotifications() {
    setupFakeAsBuiltNotificationResultPage(List.of(), REQUESTED_PAGE);

    asBuiltWorkAreaPageService.getAsBuiltNotificationsPageView(
        user, REQUESTED_PAGE);

    verify(asBuiltNotificationDtoRepository).findAllAsBuiltNotificationsForUser(eq(user), eq(getDefaultWorkAreaViewPageable(REQUESTED_PAGE)));

  }

  private void setupFakeAsBuiltNotificationResultPage(List<AsBuiltNotificationWorkareaView> results, int page) {
    var fakePage = new PageImpl<>(
        results,
        getDefaultWorkAreaViewPageable(page),
        results.size());
    when(asBuiltNotificationDtoRepository.findAllAsBuiltNotificationsForUser(any(), any()))
        .thenReturn(fakePage);

  }

}