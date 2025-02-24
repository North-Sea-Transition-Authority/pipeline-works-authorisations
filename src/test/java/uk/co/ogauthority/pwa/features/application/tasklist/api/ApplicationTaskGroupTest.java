package uk.co.ogauthority.pwa.features.application.tasklist.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;

@ExtendWith(MockitoExtension.class)
class ApplicationTaskGroupTest {

  @Test
  void applicationTaskGroup_petorleum_allApplicationTasksAssignedAGroup(){
    var allTasks = ApplicationTaskGroup.asList().stream()
        .flatMap(applicationTaskGroup -> applicationTaskGroup.getApplicationTaskSet(PwaResourceType.PETROLEUM).stream())
        .collect(Collectors.toSet());

    assertThat(allTasks).isEqualTo(EnumSet.complementOf(EnumSet.of(ApplicationTask.CARBON_STORAGE_INFORMATION)));
  }

  @Test
  void applicationTaskGroup_ccus_allApplicationTasksAssignedAGroup(){
    var allTasks = ApplicationTaskGroup.asList().stream()
        .flatMap(applicationTaskGroup -> applicationTaskGroup.getApplicationTaskSet(PwaResourceType.CCUS).stream())
        .collect(Collectors.toSet());

    assertThat(allTasks).isEqualTo(EnumSet.complementOf(EnumSet.of(ApplicationTask.FIELD_INFORMATION)));
  }
}
