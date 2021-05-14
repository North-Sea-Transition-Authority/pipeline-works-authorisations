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
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupRepository;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltInteractorServiceTest {

  private static final String AS_BUILT_REFERENCE = "AS/BUILT/REFERENCE";
  private static final LocalDate DEADLINE_DATE = LocalDate.of(2021, 1, 1);

  @Mock
  private AsBuiltNotificationGroupRepository asBuiltNotificationGroupRepository;

  @Mock
  private AsBuiltGroupStatusService asBuiltGroupStatusService;

  @Mock
  private AsBuiltGroupDeadlineService asBuiltGroupDeadlineService;

  @Mock
  private AsBuiltPipelineNotificationService asBuiltPipelineNotificationService;

  @Captor
  private ArgumentCaptor<AsBuiltNotificationGroup> notificationGroupArgumentCaptor;

  private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private AsBuiltInteractorService asBuiltInteractorService;

  private PwaConsent pwaConsent;

  private Person person;

  private Object[] getAllMockedServices(){
    return new Object[] {
        asBuiltNotificationGroupRepository,
        asBuiltGroupStatusService,
        asBuiltGroupDeadlineService,
        asBuiltPipelineNotificationService
    };
  }

  @Before
  public void setup() {

    asBuiltInteractorService = new AsBuiltInteractorService(
        asBuiltNotificationGroupRepository,
        asBuiltGroupStatusService,
        asBuiltGroupDeadlineService,
        asBuiltPipelineNotificationService,
        clock
    );

    person= PersonTestUtil.createDefaultPerson();
    pwaConsent = PwaConsentTestUtil.createInitial(null);

    when(asBuiltNotificationGroupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

  }


  @Test
  public void createAsBuiltNotification_serviceInteractions() {

    var pipelineSpecs = List.of(
        new AsBuiltPipelineNotificationSpec(new PipelineDetailId(1), PipelineChangeCategory.NEW_PIPELINE)
    );

    asBuiltInteractorService.createAsBuiltNotification(pwaConsent, AS_BUILT_REFERENCE, DEADLINE_DATE, person, pipelineSpecs);

    InOrder inOrder = Mockito.inOrder(getAllMockedServices());


    inOrder.verify(asBuiltNotificationGroupRepository).save(notificationGroupArgumentCaptor.capture());
    var newGroup = notificationGroupArgumentCaptor.getValue();

    inOrder.verify(asBuiltGroupStatusService).setNewTipStatus(newGroup, AsBuiltNotificationGroupStatus.NOT_STARTED, person);
    inOrder.verify(asBuiltGroupDeadlineService).setNewDeadline(newGroup, DEADLINE_DATE, person);
    inOrder.verify(asBuiltPipelineNotificationService).addPipelineDetailsToAsBuiltNotificationGroup(newGroup, pipelineSpecs);

    inOrder.verifyNoMoreInteractions();

  }

  @Test
  public void createAsBuiltNotification_asBuiltGroupValuesSetAsExpected() {

    var pipelineSpecs = List.of(
        new AsBuiltPipelineNotificationSpec(new PipelineDetailId(1), PipelineChangeCategory.NEW_PIPELINE)
    );

    asBuiltInteractorService.createAsBuiltNotification(pwaConsent, AS_BUILT_REFERENCE, DEADLINE_DATE, person, pipelineSpecs);


    verify(asBuiltNotificationGroupRepository).save(notificationGroupArgumentCaptor.capture());
    var newGroup = notificationGroupArgumentCaptor.getValue();

    assertThat(notificationGroupArgumentCaptor.getValue()).satisfies(asBuiltNotificationGroup -> {
      assertThat(asBuiltNotificationGroup.getCreatedTimestamp()).isEqualTo(clock.instant());
      assertThat(asBuiltNotificationGroup.getPwaConsent()).isEqualTo(pwaConsent);
      assertThat(asBuiltNotificationGroup.getReference()).isEqualTo(AS_BUILT_REFERENCE);
    });

  }
}