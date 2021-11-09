package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltInteractorService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltPipelineNotificationSpec;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConsentAsBuiltWriterServiceTest {

  private static final PipelineId PIPELINE_ID_1 = new PipelineId(1);
  private static final PipelineId PIPELINE_ID_2 = new PipelineId(2);

  // consent am and pm must be for the same day.
  private static final Instant CONSENT_INSTANT_AM = LocalDateTime.of(2021, 1, 1, 1, 0)
      .toInstant(ZoneOffset.UTC);
  private static final Instant CONSENT_INSTANT_PM = LocalDateTime.of(2021, 1, 1, 23, 59)
      .toInstant(ZoneOffset.UTC);

  private static final Instant PROJECT_COMPLETION_DATE_INSTANT = LocalDateTime.of(2021, 2, 1, 11, 0)
      .toInstant(ZoneOffset.UTC);

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private AsBuiltInteractorService asBuiltInteractorService;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private UserAccountService userAccountService;

  @Captor
  private ArgumentCaptor<List<AsBuiltPipelineNotificationSpec>> asBuiltPipelineNotificationListCaptor;


  private ConsentAsBuiltWriterService asBuiltWriterService;

  private PwaConsent pwaConsent;

  private PwaApplicationDetail pwaApplicationDetail;

  private ConsentWriterDto consentWriterDto;

  private LocalDate deadlineDate;

  private WebUserAccount systemWua;
  private Person systemPerson;

  private PipelineDetail pipeline1Detail, pipeline2Detail;

  @Before
  public void setup() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    pwaApplicationDetail.getPwaApplication().setAppReference("APP/REFERENCE");

    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getPwaApplication())).thenReturn(
        pwaApplicationDetail);

    pwaConsent = PwaConsentTestUtil.createPwaConsent(1, "CONSENT/REFERENCE", CONSENT_INSTANT_AM);
    pwaConsent.setSourcePwaApplication(pwaApplicationDetail.getPwaApplication());

    consentWriterDto = new ConsentWriterDto();

    systemPerson = PersonTestUtil.createDefaultPerson();
    systemWua = new WebUserAccount(1, systemPerson);
    when(userAccountService.getSystemWebUserAccount()).thenReturn(systemWua);

    when(padProjectInformationService.getLatestProjectCompletionDate(pwaApplicationDetail))
        .thenReturn(Optional.of(PROJECT_COMPLETION_DATE_INSTANT));

    pipeline1Detail = PipelineDetailTestUtil.createPipelineDetail(
        1, PIPELINE_ID_1, Instant.now(), pwaConsent);
    pipeline2Detail = PipelineDetailTestUtil.createPipelineDetail(
        2, PIPELINE_ID_2, Instant.now(), pwaConsent);

    consentWriterDto.setPipelineToNewDetailMap(
        Map.of(
            pipeline1Detail.getPipeline(), pipeline1Detail,
            pipeline2Detail.getPipeline(), pipeline2Detail
        )
    );

    asBuiltWriterService = new ConsentAsBuiltWriterService(
        pipelineDetailService,
        asBuiltInteractorService,
        padProjectInformationService,
        pwaApplicationDetailService,
        userAccountService
    );
  }

  @Test
  public void write_consentWriterHasMappedPipelineDetails_pipelinesInService_multipleDetailsExist() {

    when(pipelineDetailService.countPipelineDetailsPerPipeline(
        Set.of(pipeline1Detail.getPipeline(), pipeline2Detail.getPipeline())))
        .thenReturn(Map.of(
            PIPELINE_ID_1, 2L,
            PIPELINE_ID_2, 4L
        ));

    consentWriterDto = asBuiltWriterService.write(pwaApplicationDetail, pwaConsent, consentWriterDto);

    verify(asBuiltInteractorService).createAsBuiltNotification(
        eq(pwaConsent),
        eq(pwaApplicationDetail.getPwaApplicationRef()),
        any(), // test deadline date separately
        eq(systemPerson),
        asBuiltPipelineNotificationListCaptor.capture()
    );
    verifyNoMoreInteractions(asBuiltInteractorService);

    assertThat(asBuiltPipelineNotificationListCaptor.getValue()).hasSize(2)
        .allSatisfy(asBuiltPipelineNotificationSpec ->
            assertThat(asBuiltPipelineNotificationSpec.getPipelineChangeCategory()).isEqualTo(
                PipelineChangeCategory.CONSENT_UPDATE))
        .anySatisfy(asBuiltPipelineNotificationSpec ->
            assertThat(asBuiltPipelineNotificationSpec.getPipelineDetailId()).isEqualTo(
                pipeline1Detail.getPipelineDetailId()))
        .anySatisfy(asBuiltPipelineNotificationSpec ->
            assertThat(asBuiltPipelineNotificationSpec.getPipelineDetailId()).isEqualTo(
                pipeline2Detail.getPipelineDetailId()));

  }

  @Test
  public void write_consentWriterHasMappedPipelineDetails_pipelinesInService_singleDetailExists() {

    when(pipelineDetailService.countPipelineDetailsPerPipeline(
        Set.of(pipeline1Detail.getPipeline(), pipeline2Detail.getPipeline())))
        .thenReturn(Map.of(
            PIPELINE_ID_1, 1L,
            PIPELINE_ID_2, 1L
        ));

    consentWriterDto = asBuiltWriterService.write(pwaApplicationDetail, pwaConsent, consentWriterDto);

    verify(asBuiltInteractorService).createAsBuiltNotification(
        eq(pwaConsent),
        eq(pwaApplicationDetail.getPwaApplicationRef()),
        any(), // test deadline date separately
        eq(systemPerson),
        asBuiltPipelineNotificationListCaptor.capture()
    );
    verifyNoMoreInteractions(asBuiltInteractorService);

    assertThat(asBuiltPipelineNotificationListCaptor.getValue()).hasSize(2)
        .allSatisfy(asBuiltPipelineNotificationSpec ->
            assertThat(asBuiltPipelineNotificationSpec.getPipelineChangeCategory()).isEqualTo(
                PipelineChangeCategory.NEW_PIPELINE))
        .anySatisfy(asBuiltPipelineNotificationSpec ->
            assertThat(asBuiltPipelineNotificationSpec.getPipelineDetailId()).isEqualTo(
                pipeline1Detail.getPipelineDetailId()))
        .anySatisfy(asBuiltPipelineNotificationSpec ->
            assertThat(asBuiltPipelineNotificationSpec.getPipelineDetailId()).isEqualTo(
                pipeline2Detail.getPipelineDetailId()));

  }

  @Test
  public void write_consentWriterHasMappedPipelineDetails_pipelinesNeverLaid() {

    pipeline1Detail.setPipelineStatus(PipelineStatus.NEVER_LAID);
    pipeline2Detail.setPipelineStatus(PipelineStatus.NEVER_LAID);

    when(pipelineDetailService.countPipelineDetailsPerPipeline(
        Set.of(pipeline1Detail.getPipeline(), pipeline2Detail.getPipeline())))
        .thenReturn(Map.of(
            PIPELINE_ID_1, 2L,
            PIPELINE_ID_2, 2L
        ));

    consentWriterDto = asBuiltWriterService.write(pwaApplicationDetail, pwaConsent, consentWriterDto);

    verifyNoInteractions(asBuiltInteractorService);

  }

  @Test
  public void write_consentWriterHasMappedPipelineDetails_pipelinesTransferred() {

    pipeline1Detail.setPipelineStatus(PipelineStatus.TRANSFERRED);
    pipeline2Detail.setPipelineStatus(PipelineStatus.TRANSFERRED);

    when(pipelineDetailService.countPipelineDetailsPerPipeline(
        Set.of(pipeline1Detail.getPipeline(), pipeline2Detail.getPipeline())))
        .thenReturn(Map.of(
            PIPELINE_ID_1, 2L,
            PIPELINE_ID_2, 2L
        ));

    consentWriterDto = asBuiltWriterService.write(pwaApplicationDetail, pwaConsent, consentWriterDto);

    verifyNoInteractions(asBuiltInteractorService);

  }

  @Test
  public void write_consentWriterHasMappedPipelineDetails_pipelinesOutOfUseOrReturnedToShow_singleDetailExists() {

    pipeline1Detail.setPipelineStatus(PipelineStatus.OUT_OF_USE_ON_SEABED);
    pipeline2Detail.setPipelineStatus(PipelineStatus.RETURNED_TO_SHORE);

    when(pipelineDetailService.countPipelineDetailsPerPipeline(
        Set.of(pipeline1Detail.getPipeline(), pipeline2Detail.getPipeline())))
        .thenReturn(Map.of(
            // can leave pipeline details counts as 1 as not important with statuses under test
            PIPELINE_ID_1, 1L,
            PIPELINE_ID_2, 1L
        ));

    consentWriterDto = asBuiltWriterService.write(pwaApplicationDetail, pwaConsent, consentWriterDto);

    verify(asBuiltInteractorService).createAsBuiltNotification(
        eq(pwaConsent),
        eq(pwaApplicationDetail.getPwaApplicationRef()),
        any(), // test deadline date separately
        eq(systemPerson),
        asBuiltPipelineNotificationListCaptor.capture()
    );
    verifyNoMoreInteractions(asBuiltInteractorService);

    assertThat(asBuiltPipelineNotificationListCaptor.getValue()).hasSize(2)
        .anySatisfy(asBuiltPipelineNotificationSpec -> {
          assertThat(asBuiltPipelineNotificationSpec.getPipelineDetailId()).isEqualTo(
              pipeline1Detail.getPipelineDetailId());
          assertThat(asBuiltPipelineNotificationSpec.getPipelineChangeCategory()).isEqualTo(
              PipelineChangeCategory.OUT_OF_USE);
        })
        .anySatisfy(asBuiltPipelineNotificationSpec -> {
          assertThat(asBuiltPipelineNotificationSpec.getPipelineDetailId()).isEqualTo(
              pipeline2Detail.getPipelineDetailId());
          assertThat(asBuiltPipelineNotificationSpec.getPipelineChangeCategory()).isEqualTo(
              PipelineChangeCategory.OUT_OF_USE);
        });

  }


  // Want to confirm hour of day consented has no impact on deadline date for options consents.
  @Test
  public void write_optionsApplicationIsConsentSource_consentedAM() {
    when(pipelineDetailService.countPipelineDetailsPerPipeline(
        Set.of(pipeline1Detail.getPipeline(), pipeline2Detail.getPipeline())))
        .thenReturn(Map.of(
            PIPELINE_ID_1, 1L,
            PIPELINE_ID_2, 1L
        ));

    consentWriterDto = asBuiltWriterService.write(pwaApplicationDetail, pwaConsent, consentWriterDto);

    verify(asBuiltInteractorService).createAsBuiltNotification(
        eq(pwaConsent),
        eq(pwaApplicationDetail.getPwaApplicationRef()),
        eq(LocalDate.ofInstant(CONSENT_INSTANT_AM.plus(7, ChronoUnit.DAYS), ZoneId.systemDefault())),
        eq(systemPerson),
        any()
    );
    verifyNoMoreInteractions(asBuiltInteractorService);
  }

  // Want to confirm hour of day consented has no impact on deadline date for options consents.
  @Test
  public void write_optionsApplicationIsConsentSource_consentedPM() {
    pwaConsent.setConsentInstant(CONSENT_INSTANT_PM);

    when(pipelineDetailService.countPipelineDetailsPerPipeline(
        Set.of(pipeline1Detail.getPipeline(), pipeline2Detail.getPipeline())))
        .thenReturn(Map.of(
            PIPELINE_ID_1, 1L,
            PIPELINE_ID_2, 1L
        ));

    consentWriterDto = asBuiltWriterService.write(pwaApplicationDetail, pwaConsent, consentWriterDto);

    verify(asBuiltInteractorService).createAsBuiltNotification(
        eq(pwaConsent),
        eq(pwaApplicationDetail.getPwaApplicationRef()),
        eq(LocalDate.ofInstant(CONSENT_INSTANT_AM.plus(7, ChronoUnit.DAYS), ZoneId.systemDefault())),
        eq(systemPerson),
        any()
    );
    verifyNoMoreInteractions(asBuiltInteractorService);
  }

  @Test
  public void write_nonOptionsApplicationIsConsentSource() {
    when(pipelineDetailService.countPipelineDetailsPerPipeline(
        Set.of(pipeline1Detail.getPipeline(), pipeline2Detail.getPipeline())))
        .thenReturn(Map.of(
            PIPELINE_ID_1, 1L,
            PIPELINE_ID_2, 1L
        ));

    var nonOptionsAppTypes = EnumSet.allOf(PwaApplicationType.class);
    nonOptionsAppTypes.remove(PwaApplicationType.OPTIONS_VARIATION);

    for (var appType : nonOptionsAppTypes) {

      pwaApplicationDetail.getPwaApplication().setApplicationType(appType);

      consentWriterDto = asBuiltWriterService.write(pwaApplicationDetail, pwaConsent, consentWriterDto);

    }

    verify(asBuiltInteractorService, times( nonOptionsAppTypes.size())).createAsBuiltNotification(
        eq(pwaConsent),
        eq(pwaApplicationDetail.getPwaApplicationRef()),
        eq(LocalDate.ofInstant(PROJECT_COMPLETION_DATE_INSTANT.plus(7, ChronoUnit.DAYS), ZoneId.systemDefault())),
        eq(systemPerson),
        any()
    );

  }
}