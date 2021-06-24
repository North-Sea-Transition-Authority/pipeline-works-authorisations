package uk.co.ogauthority.pwa.service.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;
import uk.co.ogauthority.pwa.model.form.asbuilt.AsBuiltNotificationSubmissionForm;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupRepository;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltInteractorServiceTest {

  private static final String AS_BUILT_REFERENCE = "AS/BUILT/REFERENCE";
  private static final LocalDate DEADLINE_DATE = LocalDate.of(2021, 1, 1);

  @Mock
  private AsBuiltNotificationGroupRepository asBuiltNotificationGroupRepository;

  @Mock
  private AsBuiltNotificationGroupStatusService asBuiltNotificationGroupStatusService;

  @Mock
  private AsBuiltGroupDeadlineService asBuiltGroupDeadlineService;

  @Mock
  private AsBuiltPipelineNotificationService asBuiltPipelineNotificationService;

  @Mock
  private AsBuiltNotificationSubmissionService asBuiltNotificationSubmissionService;

  @Captor
  private ArgumentCaptor<AsBuiltNotificationGroup> notificationGroupArgumentCaptor;

  private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private AsBuiltInteractorService asBuiltInteractorService;

  private PwaConsent pwaConsent;

  private final AuthenticatedUserAccount user = AuthenticatedUserAccountTestUtil.createAllPrivUserAccount(100);

  private Object[] getAllMockedServices(){
    return new Object[] {
        asBuiltNotificationGroupRepository,
        asBuiltNotificationGroupStatusService,
        asBuiltGroupDeadlineService,
        asBuiltPipelineNotificationService
    };
  }

  @Before
  public void setup() {

    asBuiltInteractorService = new AsBuiltInteractorService(
        asBuiltNotificationGroupRepository,
        asBuiltNotificationGroupStatusService,
        asBuiltGroupDeadlineService,
        asBuiltPipelineNotificationService,
        asBuiltNotificationSubmissionService, clock
    );

    pwaConsent = PwaConsentTestUtil.createInitial(null);

    when(asBuiltNotificationGroupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

  }


  @Test
  public void createAsBuiltNotification_serviceInteractions() {

    var pipelineSpecs = List.of(
        new AsBuiltPipelineNotificationSpec(new PipelineDetailId(1), PipelineChangeCategory.NEW_PIPELINE)
    );

    asBuiltInteractorService.createAsBuiltNotification(pwaConsent, AS_BUILT_REFERENCE, DEADLINE_DATE, user.getLinkedPerson(), pipelineSpecs);

    InOrder inOrder = Mockito.inOrder(getAllMockedServices());


    inOrder.verify(asBuiltNotificationGroupRepository).save(notificationGroupArgumentCaptor.capture());
    var newGroup = notificationGroupArgumentCaptor.getValue();

    inOrder.verify(asBuiltNotificationGroupStatusService).setInitialGroupStatus(newGroup, user.getLinkedPerson());
    inOrder.verify(asBuiltGroupDeadlineService).setNewDeadline(newGroup, DEADLINE_DATE, user.getLinkedPerson());
    inOrder.verify(asBuiltPipelineNotificationService).addPipelineDetailsToAsBuiltNotificationGroup(newGroup, pipelineSpecs);

    inOrder.verifyNoMoreInteractions();

  }

  @Test
  public void createAsBuiltNotification_asBuiltGroupValuesSetAsExpected() {

    var pipelineSpecs = List.of(
        new AsBuiltPipelineNotificationSpec(new PipelineDetailId(1), PipelineChangeCategory.NEW_PIPELINE)
    );

    asBuiltInteractorService.createAsBuiltNotification(pwaConsent, AS_BUILT_REFERENCE, DEADLINE_DATE, user.getLinkedPerson(), pipelineSpecs);


    verify(asBuiltNotificationGroupRepository).save(notificationGroupArgumentCaptor.capture());
    var newGroup = notificationGroupArgumentCaptor.getValue();

    assertThat(notificationGroupArgumentCaptor.getValue()).satisfies(asBuiltNotificationGroup -> {
      assertThat(asBuiltNotificationGroup.getCreatedTimestamp()).isEqualTo(clock.instant());
      assertThat(asBuiltNotificationGroup.getPwaConsent()).isEqualTo(pwaConsent);
      assertThat(asBuiltNotificationGroup.getReference()).isEqualTo(AS_BUILT_REFERENCE);
    });

  }

  @Test
  public void submitAsBuiltNotification_callsSubmissionService() {
    var abngPipeline = new AsBuiltNotificationGroupPipeline();
    var form = new AsBuiltNotificationSubmissionForm();
    asBuiltInteractorService.submitAsBuiltNotification(abngPipeline, form, user);
    verify(asBuiltNotificationSubmissionService).submitAsBuiltNotification(abngPipeline, form, user);
  }

}