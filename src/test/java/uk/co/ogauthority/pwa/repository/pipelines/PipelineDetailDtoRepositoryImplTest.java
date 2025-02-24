package uk.co.ogauthority.pwa.repository.pipelines;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PhysicalPipelineState;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaRepository;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentRepository;

// IJ seems to give spurious warnings when running with embedded H2
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
class PipelineDetailDtoRepositoryImplTest {

  @Autowired
  private PipelineDetailDtoRepositoryImpl pipelineDetailDtoRepositoryImpl;

  @Autowired
  private PipelineDetailRepository pipelineDetailRepository;

  @Autowired
  private PipelineRepository pipelineRepository;

  @Autowired
  private MasterPwaRepository masterPwaRepository;

  @Autowired
  private PwaConsentRepository pwaConsentRepository;

  private final Instant baseTime = Instant.now().minus(1, ChronoUnit.MINUTES);

  private MasterPwa masterPwa;
  private PwaConsent firstConsentInService, secondConsentReturnedToShore;
  private static final Set<PipelineStatus> ON_SEABED_STATUSES = PipelineStatus.getStatusesWithState(PhysicalPipelineState.ON_SEABED);

  /**
   * This method sets  up a PWA with two consents for a single pipeline, the first consent brings the pipeline into service, the second returns it to shore.
   * This data is then saved to the in-memory db for repo query testing.
   */
  @BeforeEach
  void setUp() {

    // Create or retrieve the MasterPwa entity
    masterPwa = getOrCreateMasterPwa(1);

    // Create or retrieve the Pipeline entity
    var pipeline = getOrCreatePipeline(1, masterPwa);

    // Create or retrieve consents
    firstConsentInService = getOrCreatePwaConsent(1, baseTime.minus(5, ChronoUnit.MINUTES), masterPwa);
    secondConsentReturnedToShore = getOrCreatePwaConsent(2, baseTime, masterPwa);

    // Prepare PipelineDetail entities
    var inServicePipeDetail = getOrCreatePipelineDetailByPipelineAndConsent(
        pipeline,
        firstConsentInService,
        firstConsentInService.getConsentInstant(),
        PipelineStatus.IN_SERVICE,
        secondConsentReturnedToShore.getConsentInstant().minusSeconds(1),
        false
    );

    var returnedToShorePipeDetail = getOrCreatePipelineDetailByPipelineAndConsent(
        pipeline,
        secondConsentReturnedToShore,
        secondConsentReturnedToShore.getConsentInstant(),
        PipelineStatus.RETURNED_TO_SHORE,
        null, // No end timestamp here
        true
    );
  }

  private MasterPwa getOrCreateMasterPwa(int id) {
    return masterPwaRepository.findById(id)
        .orElseGet(() -> {
          var masterPwa = new MasterPwa();
          return masterPwaRepository.save(masterPwa);
        });
  }

  private Pipeline getOrCreatePipeline(int id, MasterPwa masterPwa) {
    return pipelineRepository.findById(id)
        .orElseGet(() -> {
          var pipeline = new Pipeline();
          pipeline.setMasterPwa(masterPwa);
          return pipelineRepository.save(pipeline);
        });
  }

  private PwaConsent getOrCreatePwaConsent(int id, Instant consentInstant, MasterPwa masterPwa) {
    return pwaConsentRepository.findById(id)
        .orElseGet(() -> {
          var pwaConsent = new PwaConsent();
          pwaConsent.setConsentInstant(consentInstant);
          pwaConsent.setMasterPwa(masterPwa);
          return pwaConsentRepository.save(pwaConsent);
        });
  }

  private PipelineDetail getOrCreatePipelineDetailByPipelineAndConsent(
      Pipeline pipeline,
      PwaConsent pwaConsent,
      Instant startTimestamp,
      PipelineStatus status,
      Instant endTimestamp,
      boolean tipFlag
  ) {
    return pipelineDetailRepository.findAllByPipeline_Id(pipeline.getId())
        .stream()
        .filter(pipelineDetail -> pipelineDetail.getPwaConsent().getId() == pwaConsent.getId())
        .findFirst()
        .orElseGet(() -> {
          var pipelineDetail = new PipelineDetail();
          pipelineDetail.setPipeline(pipeline);
          pipelineDetail.setPwaConsent(pwaConsent);
          pipelineDetail.setStartTimestamp(startTimestamp);
          pipelineDetail.setPipelineStatus(status);
          pipelineDetail.setEndTimestamp(endTimestamp);
          pipelineDetail.setTipFlag(tipFlag);
          return pipelineDetailRepository.save(pipelineDetail);
        });
  }

  @Test
  void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_onSeabed_atCurrentTime_pipelineNotFound() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, ON_SEABED_STATUSES, Instant.now());

    // pipeline is returned to shore at current time, therefore not on seabed and not found
    assertThat(details).isEmpty();

  }

  @Test
  void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_onSeabed_atSecondConsentTime_pipelineNotFound() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, ON_SEABED_STATUSES, secondConsentReturnedToShore.getConsentInstant());

    // pipeline is returned to shore at consent2 time, therefore not on seabed and not found
    assertThat(details).isEmpty();

  }

  @Test
  void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_onSeabed_atFirstConsentTime_pipelineReturned() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, ON_SEABED_STATUSES, firstConsentInService.getConsentInstant());

    // at time of first consent, pipeline was in service therefore on seabed, returned
    assertThat(details).hasSize(1);
    assertThat(details.get(0))
        .extracting(PipelineOverview::getPipelineStatus)
        .isEqualTo(PipelineStatus.IN_SERVICE);

  }

  @Test
  void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_allStatuses_atCurrentTime_pipelineReturned() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, PipelineStatus.currentStatusSet(), Instant.now());

    // pipeline is returned to shore at current time, no status filter applied
    assertThat(details).hasSize(1);
    assertThat(details.get(0))
        .extracting(PipelineOverview::getPipelineStatus)
        .isEqualTo(PipelineStatus.RETURNED_TO_SHORE);

  }

  @Test
  void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_allStatuses_atSecondConsentTime_pipelineReturned() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, PipelineStatus.currentStatusSet(), secondConsentReturnedToShore.getConsentInstant());

    // pipeline is returned to shore at consent2 time, no status filter applied
    assertThat(details).hasSize(1);
    assertThat(details.get(0))
        .extracting(PipelineOverview::getPipelineStatus)
        .isEqualTo(PipelineStatus.RETURNED_TO_SHORE);

  }

  @Test
  void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_allStatuses_atFirstConsentTime_pipelineReturned() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, PipelineStatus.currentStatusSet(), firstConsentInService.getConsentInstant());

    // pipeline is in service at consent1 time, no status filter applied
    assertThat(details).hasSize(1);
    assertThat(details.get(0))
        .extracting(PipelineOverview::getPipelineStatus)
        .isEqualTo(PipelineStatus.IN_SERVICE);

  }

}
