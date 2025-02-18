package uk.co.ogauthority.pwa.service.workarea;



import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WorkAreaContextTest {

  @Test
  void containsWorkAreaUserType_whenDoesContainType() {

    var context = new WorkAreaContext(null, EnumSet.of(WorkAreaUserType.PWA_MANAGER), List.of());
    assertThat(context.containsWorkAreaUserType(WorkAreaUserType.PWA_MANAGER)).isTrue();
  }

  @Test
  void containsWorkAreaUserType_whenDoesNotContainType() {

    var context = new WorkAreaContext(null, EnumSet.of(WorkAreaUserType.PWA_MANAGER), List.of());
    assertThat(context.containsWorkAreaUserType(WorkAreaUserType.CASE_OFFICER)).isFalse();
  }

  @Test
  void getSortedUserTabs_tabsInSameOrderAsConstructed() {

    var context = new WorkAreaContext(null, Set.of(), List.of(WorkAreaTab.OPEN_CONSULTATIONS, WorkAreaTab.AS_BUILT_NOTIFICATIONS));
    assertThat(context.getSortedUserTabs()).containsExactly(WorkAreaTab.OPEN_CONSULTATIONS, WorkAreaTab.AS_BUILT_NOTIFICATIONS);
  }

  @Test
  void getDefaultTab_whenTabsExist_getsFirstTabFromConstructorList() {

    var context = new WorkAreaContext(null, Set.of(), List.of(WorkAreaTab.OPEN_CONSULTATIONS, WorkAreaTab.AS_BUILT_NOTIFICATIONS));
    assertThat(context.getDefaultTab()).contains(WorkAreaTab.OPEN_CONSULTATIONS);
  }

  @Test
  void getDefaultTab_whenNoTabsExist() {

    var context = new WorkAreaContext(null, Set.of(), List.of());
    assertThat(context.getDefaultTab()).isEmpty();
  }
}