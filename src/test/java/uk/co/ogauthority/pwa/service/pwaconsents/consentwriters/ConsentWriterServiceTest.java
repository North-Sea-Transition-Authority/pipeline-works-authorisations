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
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
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
  private HuooWriter huooWriter;

  @MockBean
  private InitialPwaMasterDetailWriter initialPwaMasterDetailWriter;

  @MockBean
  private TaskListService taskListService;

  @Autowired
  private ConsentWriterService consentWriterService;

  private List<ApplicationTask> applicationTasks;

  private  PwaApplicationDetail detail;
  private PwaConsent consent;

  private void configureDefaultMockWriterBehaviour(ConsentWriter consentWriterMock){
    when(consentWriterMock.writerIsApplicable(any(), any())).thenCallRealMethod();
    when(consentWriterMock.getExecutionOrder()).thenCallRealMethod();
  }

  @Before
  public void setUp() throws Exception {

    configureDefaultMockWriterBehaviour(fieldWriter);
    configureDefaultMockWriterBehaviour(huooWriter);
    configureDefaultMockWriterBehaviour(initialPwaMasterDetailWriter);

    applicationTasks = ApplicationTask.stream().collect(Collectors.toList());
    when(taskListService.getShownApplicationTasksForDetail(any()))
        .thenReturn(applicationTasks);

    detail = new PwaApplicationDetail();
    consent = new PwaConsent();
    consent.setConsentType(PwaConsentType.INITIAL_PWA);

  }

  @Test
  public void updateConsentedData_allTasks_inOrder() {

    consentWriterService.updateConsentedData(detail, consent);

    var inOrder = Mockito.inOrder(fieldWriter, huooWriter, initialPwaMasterDetailWriter);

    inOrder.verify(initialPwaMasterDetailWriter, times(1)).write(detail, consent);
    inOrder.verify(fieldWriter, times(1)).write(detail, consent);
    inOrder.verify(huooWriter, times(1)).write(detail, consent);

  }

  @Test
  public void updateConsentedData_notInitialConsent_noInitialConsentWrite() {
    consent.setConsentType(PwaConsentType.VARIATION);
    consentWriterService.updateConsentedData(detail, consent);

    verify(initialPwaMasterDetailWriter, times(0)).write(any(), any());

  }

  @Test
  public void updateConsentedData_initialConsent_consentWrite() {

    consentWriterService.updateConsentedData(detail, consent);

    verify(initialPwaMasterDetailWriter, times(1)).write(detail, consent);

  }

  @Test
  public void updateConsentedData_noFieldTask_noFieldWrite() {

    applicationTasks.remove(ApplicationTask.FIELD_INFORMATION);

    consentWriterService.updateConsentedData(detail, consent);

    verify(fieldWriter, times(0)).write(detail, consent);

  }

  @Test
  public void updateConsentedData_noHuooTask_noHuooWrite() {

    applicationTasks.remove(ApplicationTask.HUOO);

    consentWriterService.updateConsentedData(detail, consent);

    verify(huooWriter, times(0)).write(detail, consent);

  }

}