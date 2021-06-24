package uk.co.ogauthority.pwa.service.workarea;



import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;

public final class WorkAreaContextTestUtil {

  private WorkAreaContextTestUtil() {
    throw new UnsupportedOperationException("not util for you!");
  }

  public static WorkAreaContext createPwaManagerContext(AuthenticatedUserAccount user) {
    return new WorkAreaContext(
        user,
        EnumSet.of(WorkAreaUserType.PWA_MANAGER),
        List.of(WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, WorkAreaTab.REGULATOR_WAITING_ON_OTHERS)
    );

  }

  public static WorkAreaContext createContextWithAllTabs(AuthenticatedUserAccount user) {
    return new WorkAreaContext(
        user,
        EnumSet.of(WorkAreaUserType.PWA_MANAGER),
       WorkAreaTab.stream().collect(Collectors.toList())
    );

  }

  public static WorkAreaContext createContextWithZeroUserTabs(AuthenticatedUserAccount user) {
    return new WorkAreaContext(
        user,
        EnumSet.noneOf(WorkAreaUserType.class),
        List.of()
    );

  }
}


