package uk.co.ogauthority.pwa.service.workarea;



import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class WorkAreaContextTest {

  @Test
  public void containsAppEventSubscriberType_whenDoesContainType() {

    var context = new WorkAreaContext(null, EnumSet.of(ApplicationEventSubscriberType.PWA_MANAGER), List.of());
    assertThat(context.containsAppEventSubscriberType(ApplicationEventSubscriberType.PWA_MANAGER)).isTrue();
  }

  @Test
  public void containsAppEventSubscriberType_whenDoesNotContainType() {

    var context = new WorkAreaContext(null, EnumSet.of(ApplicationEventSubscriberType.PWA_MANAGER), List.of());
    assertThat(context.containsAppEventSubscriberType(ApplicationEventSubscriberType.CASE_OFFICER)).isFalse();
  }

  @Test
  public void getSortedUserTabs_tabsInSameOrderAsConstructed() {

    var context = new WorkAreaContext(null, Set.of(), List.of(WorkAreaTab.OPEN_CONSULTATIONS, WorkAreaTab.AS_BUILT_NOTIFICATIONS));
    assertThat(context.getSortedUserTabs()).containsExactly(WorkAreaTab.OPEN_CONSULTATIONS, WorkAreaTab.AS_BUILT_NOTIFICATIONS);
  }

  @Test
  public void getDefaultTab_whenTabsExist_getsFirstTabFromConstructorList() {

    var context = new WorkAreaContext(null, Set.of(), List.of(WorkAreaTab.OPEN_CONSULTATIONS, WorkAreaTab.AS_BUILT_NOTIFICATIONS));
    assertThat(context.getDefaultTab()).contains(WorkAreaTab.OPEN_CONSULTATIONS);
  }

  @Test
  public void getDefaultTab_whenNoTabsExist() {

    var context = new WorkAreaContext(null, Set.of(), List.of());
    assertThat(context.getDefaultTab()).isEmpty();
  }
}