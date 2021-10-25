package uk.co.ogauthority.pwa.features.application.tasklist.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTaskGroupTest {

  @Test
  public void applicationTaskGroup_allApplicationTasksAssignedAGroup(){
    var allTasks = ApplicationTaskGroup.asList().stream()
        .flatMap(applicationTaskGroup -> applicationTaskGroup.getApplicationTaskSet().stream())
        .collect(Collectors.toSet());

    assertThat(allTasks).isEqualTo(ApplicationTask.stream().collect(Collectors.toSet()));

  }


}