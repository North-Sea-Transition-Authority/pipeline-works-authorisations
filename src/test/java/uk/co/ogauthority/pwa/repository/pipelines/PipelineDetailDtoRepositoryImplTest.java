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
  private PipelineDetail pipelineDetail1, pipelineDetail2;
  private PwaConsent pwaConsent1, pwaConsent2;
  private static final Set<PipelineStatus> ON_SEABED_STATUSES = PipelineStatus.getStatusesWithState(PhysicalPipelineState.ON_SEABED);

  @Before
  public void setUp() {

    masterPwa = new MasterPwa();
    masterPwa.setId(1);

    var pipeline = new Pipeline();
    pipeline.setId(1);
    pipeline.setMasterPwa(masterPwa);

    // first consent, setting pipeline to IN_SERVICE
    pwaConsent1 = new PwaConsent();
    pwaConsent1.setId(1);
    pwaConsent1.setConsentInstant(baseTime.minus(5, ChronoUnit.MINUTES));
    pwaConsent1.setMasterPwa(masterPwa);

    // second consent, setting pipeline to RETURNED_TO_SHORE
    pwaConsent2 = new PwaConsent();
    pwaConsent2.setId(2);
    pwaConsent2.setConsentInstant(baseTime);
    pwaConsent2.setMasterPwa(masterPwa);

    var consentList = List.of(pwaConsent1, pwaConsent2);

    // detail linked to first consent
    pipelineDetail1 = new PipelineDetail();
    pipelineDetail1.setPipeline(pipeline);
    pipelineDetail1.setPwaConsent(pwaConsent1);
    pipelineDetail1.setStartTimestamp(pwaConsent1.getConsentInstant());
    pipelineDetail1.setPipelineStatus(PipelineStatus.IN_SERVICE);
    pipelineDetail1.setEndTimestamp(pwaConsent2.getConsentInstant());

    // detail linked to second consent
    pipelineDetail2 = new PipelineDetail();
    pipelineDetail2.setPipeline(pipeline);
    pipelineDetail2.setPwaConsent(pwaConsent2);
    pipelineDetail2.setStartTimestamp(pwaConsent2.getConsentInstant());
    pipelineDetail2.setPipelineStatus(PipelineStatus.RETURNED_TO_SHORE);
    pipelineDetail2.setTipFlag(true);

    var detailList = List.of(pipelineDetail1, pipelineDetail2);

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
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, ON_SEABED_STATUSES, pwaConsent2.getConsentInstant());

    // pipeline is returned to shore at consent2 time, therefore not on seabed and not found
    assertThat(details).isEmpty();

  }

  @Test
  public void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_onSeabed_atFirstConsentTime_pipelineReturned() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, ON_SEABED_STATUSES, pwaConsent1.getConsentInstant());

    // at time of first consent, pipeline was in service therefore on seabed, returned
    assertThat(details).hasSize(1);
    assertThat(details.get(0).getPipelineStatus()).isEqualTo(PipelineStatus.IN_SERVICE);

  }

  @Test
  public void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_allStatuses_atCurrentTime_pipelineReturned() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, PipelineStatus.currentStatusSet(), Instant.now());

    // pipeline is returned to shore at current time, no status filter applied
    assertThat(details).hasSize(1);
    assertThat(details.get(0).getPipelineStatus()).isEqualTo(PipelineStatus.RETURNED_TO_SHORE);

  }

  @Test
  public void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_allStatuses_atSecondConsentTime_pipelineReturned() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, PipelineStatus.currentStatusSet(), pwaConsent2.getConsentInstant());

    // pipeline is returned to shore at consent2 time, no status filter applied
    assertThat(details).hasSize(1);
    assertThat(details.get(0).getPipelineStatus()).isEqualTo(PipelineStatus.RETURNED_TO_SHORE);

  }

  @Test
  public void getAllPipelineOverviewsForMasterPwaAndStatusAtInstant_allStatuses_atFirstConsentTime_pipelineReturned() {

    var details = pipelineDetailDtoRepositoryImpl
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, PipelineStatus.currentStatusSet(), pwaConsent1.getConsentInstant());

    // pipeline is in service at consent1 time, no status filter applied
    assertThat(details).hasSize(1);
    assertThat(details.get(0).getPipelineStatus()).isEqualTo(PipelineStatus.IN_SERVICE);

  }

}
