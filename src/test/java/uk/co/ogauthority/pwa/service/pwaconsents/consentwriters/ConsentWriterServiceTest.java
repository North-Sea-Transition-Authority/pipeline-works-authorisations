package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.PipelineHuooWriter;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.PipelineWriter;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
public class ConsentWriterServiceTest {

  @MockBean
  private AreaWriter areaWriter;

  @MockBean
  private HuooWriter huooWriter;

  @MockBean
  private InitialPwaMasterDetailWriter initialPwaMasterDetailWriter;

  @MockBean
  private PipelineWriter pipelineWriter;

  @MockBean
  private PipelineHuooWriter pipelineHuooWriter;

  @MockBean
  private TaskListService taskListService;

  @MockBean
  private HolderChangeEmailService holderChangeEmailService;

  @Autowired
  private ConsentWriterService consentWriterService;

  private List<ApplicationTask> applicationTasks;

  private PwaApplicationDetail detail;
  private PwaConsent consent;

  private ConsentWriterDto consentWriterDto;

  private void configureDefaultMockWriterBehaviour(ConsentWriter consentWriterMock){
    when(consentWriterMock.writerIsApplicable(any(), any())).thenCallRealMethod();
    when(consentWriterMock.getExecutionOrder()).thenCallRealMethod();
    when(consentWriterMock.write(any(), any(), any())).thenReturn(consentWriterDto);
  }

  @Before
  public void setUp() throws Exception {

    consentWriterDto = new ConsentWriterDto();
    consentWriterDto.setConsentRolesAdded(List.of(new PwaConsentOrganisationRole()));
    consentWriterDto.setConsentRolesEnded(List.of(new PwaConsentOrganisationRole()));

    configureDefaultMockWriterBehaviour(areaWriter);
    configureDefaultMockWriterBehaviour(huooWriter);
    configureDefaultMockWriterBehaviour(initialPwaMasterDetailWriter);
    configureDefaultMockWriterBehaviour(pipelineWriter);
    configureDefaultMockWriterBehaviour(pipelineHuooWriter);

    applicationTasks = ApplicationTask.stream().collect(Collectors.toList());
    when(taskListService.getShownApplicationTasksForDetail(any()))
        .thenReturn(applicationTasks);

    detail = new PwaApplicationDetail();
    consent = new PwaConsent();
    consent.setConsentType(PwaConsentType.INITIAL_PWA);

  }

  @Test
  public void updateConsentedData_allTasks_initial_inOrder_noHolderChangeEmail() {

    consentWriterService.updateConsentedData(detail, consent);

    var inOrder = Mockito.inOrder(areaWriter, huooWriter, initialPwaMasterDetailWriter, pipelineWriter, pipelineHuooWriter);

    inOrder.verify(initialPwaMasterDetailWriter, times(1)).write(eq(detail), eq(consent), any());
    inOrder.verify(areaWriter, times(1)).write(eq(detail), eq(consent), any());
    inOrder.verify(huooWriter, times(1)).write(eq(detail), eq(consent), any());
    inOrder.verify(pipelineWriter, times(1)).write(eq(detail), eq(consent), any());

    verifyNoInteractions(holderChangeEmailService);

  }

  @Test
  public void updateConsentedData_allTasks_variation_inOrder_holderChangeEmail() {

    consent.setConsentType(PwaConsentType.VARIATION);

    consentWriterService.updateConsentedData(detail, consent);

    var inOrder = Mockito.inOrder(areaWriter, huooWriter, initialPwaMasterDetailWriter, pipelineWriter, pipelineHuooWriter);

    inOrder.verify(areaWriter, times(1)).write(eq(detail), eq(consent), any());
    inOrder.verify(huooWriter, times(1)).write(eq(detail), eq(consent), any());
    inOrder.verify(pipelineWriter, times(1)).write(eq(detail), eq(consent), any());

    verify(holderChangeEmailService, times(1)).sendHolderChangeEmail(
        detail.getPwaApplication(),
        consentWriterDto.getConsentRolesEnded(),
        consentWriterDto.getConsentRolesAdded());

  }

  @Test
  public void updateConsentedData_notInitialConsent_noInitialConsentWrite() {
    consent.setConsentType(PwaConsentType.VARIATION);
    consentWriterService.updateConsentedData(detail, consent);

    verify(initialPwaMasterDetailWriter, times(0)).write(any(), any(), any());

  }

  @Test
  public void updateConsentedData_initialConsent_consentWrite() {

    consentWriterService.updateConsentedData(detail, consent);

    verify(initialPwaMasterDetailWriter, times(1)).write(eq(detail), eq(consent), any());

  }

  @Test
  public void updateConsentedData_noFieldTask_areaWriteForCarbonStorage() {

    applicationTasks.remove(ApplicationTask.FIELD_INFORMATION);

    consentWriterService.updateConsentedData(detail, consent);

    verify(areaWriter).write(detail, consent, consentWriterDto);

  }

  @Test
  public void updateConsentedData_noFieldTask_areaWriteForField() {

    applicationTasks.remove(ApplicationTask.CARBON_STORAGE_INFORMATION);

    consentWriterService.updateConsentedData(detail, consent);

    verify(areaWriter).write(detail, consent, consentWriterDto);

  }

  @Test
  public void updateConsentedData_noAreaTask_noWrite() {

    applicationTasks.remove(ApplicationTask.CARBON_STORAGE_INFORMATION);
    applicationTasks.remove(ApplicationTask.FIELD_INFORMATION);

    consentWriterService.updateConsentedData(detail, consent);

    verify(areaWriter, never()).write(detail, consent, consentWriterDto);

  }

  @Test
  public void updateConsentedData_noHuooTask_noHuooWrite() {

    applicationTasks.remove(ApplicationTask.HUOO);

    consentWriterService.updateConsentedData(detail, consent);

    verify(huooWriter, times(0)).write(detail, consent, consentWriterDto);

  }

  @Test
  public void updateConsentedData_noPipelinesTask_noPipelineWrite() {

    applicationTasks.remove(ApplicationTask.PIPELINES);

    consentWriterService.updateConsentedData(detail, consent);

    verify(pipelineWriter, times(0)).write(detail, consent, consentWriterDto);

  }

  @Test
  public void updateConsentedData_noPipelineHuooTask_noPipelineHuooWrite() {

    applicationTasks.remove(ApplicationTask.PIPELINES_HUOO);

    consentWriterService.updateConsentedData(detail, consent);

    verify(pipelineHuooWriter, times(0)).write(detail, consent, consentWriterDto);

  }

}
