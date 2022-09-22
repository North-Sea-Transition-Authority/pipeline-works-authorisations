package uk.co.ogauthority.pwa.repository.pipelines;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
// IJ seems to give spurious warnings when running with embedded H2
public class PipelineDetailDtoRepositoryImplTest {

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
  private PipelineDetail inServicePipeDetail, returnedToShorePipeDetail;
  private PwaConsent firstConsentInService, secondConsentReturnedToShore;
  private static final Set<PipelineStatus> ON_SEABED_STATUSES = PipelineStatus.getStatusesWithState(PhysicalPipelineState.ON_SEABED);

  /**
   * This method sets  up a PWA with two consents for a single pipeline, the first consent brings the pipeline into service, the second returns it to shore.
   * This data is then saved to the in-memory db for repo query testing.
   */
  @Before
  public void setUp() {

    masterPwa = new MasterPwa();
    masterPwa.setId(1);

    var pipeline = new Pipeline();
    pipeline.setId(1);
    pipeline.setMasterPwa(masterPwa);

    // first consent, setting pipeline to IN_SERVICE
    firstConsentInService = new PwaConsent();
    firstConsentInService.setId(1);
    firstConsentInService.setConsentInstant(baseTime.minus(5, ChronoUnit.MINUTES));
    firstConsentInService.setMasterPwa(masterPwa);

    // second consent, setting pipeline to RETURNED_TO_SHORE
    secondConsentReturnedToShore = new PwaConsent();
    secondConsentReturnedToShore.setId(2);
    secondConsentReturnedToShore.setConsentInstant(baseTime);
    secondConsentReturnedToShore.setMasterPwa(masterPwa);

    var consentList = List.of(firstConsentInService, secondConsentReturnedToShore);

    // detail linked to first consent
    inServicePipeDetail = new PipelineDetail();
    inServicePipeDetail.setPipeline(pipeline);
    inServicePipeDetail.setPwaConsent(firstConsentInService);
    inServicePipeDetail.setStartTimestamp(firstConsentInService.getConsentInstant());
    inServicePipeDetail.setPipelineStatus(PipelineStatus.IN_SERVICE);
    inServicePipeDetail.setEndTimestamp(secondConsentReturnedToShore.getConsentInstant());

    // detail linked to second consent
    returnedToShorePipeDetail = new PipelineDetail();
    returnedToShorePipeDetail.setPipeline(pipeline);
    returnedToShorePipeDetail.setPwaConsent(secondConsentReturnedToShore);
    returnedToShorePipeDetail.setStartTimestamp(secondConsentReturnedToShore.getConsentInstant());
    returnedToShorePipeDetail.setPipelineStatus(PipelineStatus.RETURNED_TO_SHORE);
    returnedToShorePipeDetail.setTipFlag(true);

    var detailList = List.of(inServicePipeDetail, returnedToShorePipeDetail);

    // store entities if not stored before
    if(masterPwaRepository.findById(1).isEmpty()) {

      masterPwaRepository.save(masterPwa);
      pipelineRepository.save(pipeline);
      pwaConsentRepository.saveAll(consentList);
      pipelineDetailRepository.saveAll(detailList);

    }

  }

  @Test
  public void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_onSeabed_atCurrentTime_pipelineNotFound() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, ON_SEABED_STATUSES, Instant.now());

    // pipeline is returned to shore at current time, therefore not on seabed and not found
    assertThat(details).isEmpty();

  }

  @Test
  public void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_onSeabed_atSecondConsentTime_pipelineNotFound() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, ON_SEABED_STATUSES, secondConsentReturnedToShore.getConsentInstant());

    // pipeline is returned to shore at consent2 time, therefore not on seabed and not found
    assertThat(details).isEmpty();

  }

  @Test
  public void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_onSeabed_atFirstConsentTime_pipelineReturned() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, ON_SEABED_STATUSES, firstConsentInService.getConsentInstant());

    // at time of first consent, pipeline was in service therefore on seabed, returned
    assertThat(details).hasSize(1);
    assertThat(details.get(0))
        .extracting(PipelineOverview::getPipelineStatus)
        .isEqualTo(PipelineStatus.IN_SERVICE);

  }

  @Test
  public void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_allStatuses_atCurrentTime_pipelineReturned() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, PipelineStatus.currentStatusSet(), Instant.now());

    // pipeline is returned to shore at current time, no status filter applied
    assertThat(details).hasSize(1);
    assertThat(details.get(0))
        .extracting(PipelineOverview::getPipelineStatus)
        .isEqualTo(PipelineStatus.RETURNED_TO_SHORE);

  }

  @Test
  public void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_allStatuses_atSecondConsentTime_pipelineReturned() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, PipelineStatus.currentStatusSet(), secondConsentReturnedToShore.getConsentInstant());

    // pipeline is returned to shore at consent2 time, no status filter applied
    assertThat(details).hasSize(1);
    assertThat(details.get(0))
        .extracting(PipelineOverview::getPipelineStatus)
        .isEqualTo(PipelineStatus.RETURNED_TO_SHORE);

  }

  @Test
  public void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_allStatuses_atFirstConsentTime_pipelineReturned() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, PipelineStatus.currentStatusSet(), firstConsentInService.getConsentInstant());

    // pipeline is in service at consent1 time, no status filter applied
    assertThat(details).hasSize(1);
    assertThat(details.get(0))
        .extracting(PipelineOverview::getPipelineStatus)
        .isEqualTo(PipelineStatus.IN_SERVICE);

  }

}
