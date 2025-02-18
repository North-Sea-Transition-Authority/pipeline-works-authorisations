package uk.co.ogauthority.pwa.externalapi;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;

@DataJpaTest
@RunWith(SpringRunner.class)
@ActiveProfiles("integration-test")
@DirtiesContext
@Transactional
class PwaConsentDtoRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  private MasterPwaDetail firstPwaDetail;
  private MasterPwaDetail secondPwaDetail;

  private PwaConsent firstPwaConsent;
  private PwaConsent secondPwaConsent;

  @Autowired
  private PwaConsentDtoRepository pwaConsentDtoRepository;

  @BeforeEach
  void setUp() {
    var currentDate = Instant.now();

    var firstPwa = new MasterPwa();
    entityManager.persist(firstPwa);
    firstPwaDetail = new MasterPwaDetail(firstPwa, null, "7/W/98",
        currentDate.minus(50, ChronoUnit.DAYS), PwaResourceType.PETROLEUM);
    entityManager.persist(firstPwaDetail);

    var secondPwa = new MasterPwa();
    entityManager.persist(secondPwa);
    secondPwaDetail = new MasterPwaDetail(secondPwa, null, "10/W/12",
        currentDate.minus(30, ChronoUnit.DAYS), PwaResourceType.PETROLEUM);
    secondPwaDetail.setEndInstant(Instant.now());
    entityManager.persist(secondPwaDetail);

    firstPwaConsent = new PwaConsent();
    firstPwaConsent.setMasterPwa(firstPwa);
    firstPwaConsent.setCreatedInstant(currentDate.minus(50, ChronoUnit.DAYS));
    firstPwaConsent.setConsentType(PwaConsentType.INITIAL_PWA);
    firstPwaConsent.setConsentInstant(currentDate.minus(45, ChronoUnit.DAYS));
    entityManager.persist(firstPwaConsent);

    secondPwaConsent = new PwaConsent();
    secondPwaConsent.setMasterPwa(firstPwa);
    secondPwaConsent.setCreatedInstant(currentDate.minus(40, ChronoUnit.DAYS));
    secondPwaConsent.setConsentType(PwaConsentType.VARIATION);
    secondPwaConsent.setConsentInstant(currentDate.minus(30, ChronoUnit.DAYS));
    entityManager.persist(secondPwaConsent);

    PwaConsent thirdPwaConsent = new PwaConsent();
    thirdPwaConsent.setMasterPwa(secondPwa);
    thirdPwaConsent.setCreatedInstant(currentDate.minus(30, ChronoUnit.DAYS));
    thirdPwaConsent.setConsentType(PwaConsentType.INITIAL_PWA);
    entityManager.persist(thirdPwaConsent);
  }

  @Test
  void searchPwaConsents_searchByPwaIds() {
    var searchedIds = List.of(firstPwaDetail.getMasterPwaId(), secondPwaDetail.getMasterPwaId());
    var resultingConsentDtos = pwaConsentDtoRepository.searchPwaConsents(searchedIds);

    assertThat(resultingConsentDtos)
        .extracting(PwaConsentDto::getId)
        .containsOnly(firstPwaConsent.getId(), secondPwaConsent.getId());
  }

  @Test
  void searchPwaConsents_whenNoPwaIds_thenReturnEmptyList() {
    var resultingConsentDtos = pwaConsentDtoRepository.searchPwaConsents(Collections.emptyList());
    assertThat(resultingConsentDtos).isEmpty();
  }
}
