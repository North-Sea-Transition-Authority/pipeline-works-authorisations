package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
public class ConsentWriterServiceTest {

  @MockBean
  private FieldWriter fieldWriter;

  @MockBean
  private TaskListService taskListService;

  @Autowired
  private ConsentWriterService consentWriterService;

  private List<ApplicationTask> applicationTasks;

  @Before
  public void setUp() throws Exception {

    when(fieldWriter.getTaskDependentOn()).thenCallRealMethod();
    when(fieldWriter.getExecutionOrder()).thenCallRealMethod();

    applicationTasks = ApplicationTask.stream().collect(Collectors.toList());
    when(taskListService.getShownApplicationTasksForDetail(any()))
        .thenReturn(applicationTasks);

  }

  @Test
  public void updateConsentedData_allTasks_inOrder() {

    var detail = new PwaApplicationDetail();
    var consent = new PwaConsent();

    consentWriterService.updateConsentedData(detail, consent);

    var inOrder = Mockito.inOrder(fieldWriter);

    inOrder.verify(fieldWriter, times(1)).write(detail, consent);

  }

  @Test
  public void updateConsentedData_noFieldTask_noFieldWrite() {

    applicationTasks.remove(ApplicationTask.FIELD_INFORMATION);

    var detail = new PwaApplicationDetail();
    var consent = new PwaConsent();

    consentWriterService.updateConsentedData(detail, consent);

    verify(fieldWriter, times(0)).write(detail, consent);

  }

}