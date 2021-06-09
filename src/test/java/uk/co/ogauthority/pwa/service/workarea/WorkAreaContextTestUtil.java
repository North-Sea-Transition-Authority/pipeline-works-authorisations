package uk.co.ogauthority.pwa.service.workarea;



import java.util.EnumSet;
import java.util.List;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;

public final class WorkAreaContextTestUtil {

  private WorkAreaContextTestUtil() {
    throw new UnsupportedOperationException("not util for you!");
  }

  public static WorkAreaContext createPwaManagerContext(AuthenticatedUserAccount user) {
    return new WorkAreaContext(
        user,
        EnumSet.of(ApplicationEventSubscriberType.PWA_MANAGER),
        List.of(WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, WorkAreaTab.REGULATOR_WAITING_ON_OTHERS)
    );

  }

  public static WorkAreaContext createContextWithZeroUserTabs(AuthenticatedUserAccount user) {
    return new WorkAreaContext(
        user,
        EnumSet.noneOf(ApplicationEventSubscriberType.class),
        List.of()
    );

  }
}


