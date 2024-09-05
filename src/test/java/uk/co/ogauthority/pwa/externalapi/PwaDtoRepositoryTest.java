package uk.co.ogauthority.pwa.externalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;

@DataJpaTest
@RunWith(SpringRunner.class)
@ActiveProfiles("integration-test")
@DirtiesContext
@Transactional
public class PwaDtoRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;
  private MasterPwaDetail pwaDetail;
  private MasterPwaDetail secondPwaDetail;
  private MasterPwaDetail thirdPwaDetail;

  @Autowired
  PwaDtoRepository pwaDtoRepository;

  @Before
  public void setUp() {

    var pwa = new MasterPwa();
    var secondPwa = new MasterPwa();
    var thirdPwa = new MasterPwa();
    entityManager.persist(pwa);
    entityManager.persist(secondPwa);
    entityManager.persist(thirdPwa);

    pwaDetail = new MasterPwaDetail(pwa, MasterPwaDetailStatus.APPLICATION, "1/W/97", Instant.now(), null);
    secondPwaDetail = new MasterPwaDetail(secondPwa, MasterPwaDetailStatus.CONSENTED, "11/V/97", Instant.now(), null);
    thirdPwaDetail = new MasterPwaDetail(thirdPwa, null, "11/V/97", Instant.now(), null);
    entityManager.persist(pwaDetail);
    entityManager.persist(secondPwaDetail);
    entityManager.persist(thirdPwaDetail);
  }

  @Test
  public void searchPwas_searchByPwaId() {
    var searchedIds = List.of(pwaDetail.getMasterPwaId());
    var resultingPwaDtos = pwaDtoRepository.searchPwas(searchedIds, null, null, PageRequest.of(0, 10));

    assertThat(resultingPwaDtos)
        .extracting(PwaDto::getId)
        .containsExactly(pwaDetail.getMasterPwaId());
  }

  @Test
  public void searchPwas_searchByPwaReference_caseSensitive() {
    var pwaReference = "W";
    var resultingPwaDtos = pwaDtoRepository.searchPwas(null, pwaReference, null, PageRequest.of(0, 10));

    assertThat(resultingPwaDtos)
        .extracting(PwaDto::getReference)
        .containsExactly(pwaDetail.getReference());

    pwaReference = "w";
    resultingPwaDtos = pwaDtoRepository.searchPwas(null, pwaReference, null, PageRequest.of(0, 10));

    assertThat(resultingPwaDtos)
        .extracting(PwaDto::getReference)
        .containsExactly(pwaDetail.getReference());
  }

  @Test
  public void searchPwas_searchByStatus() {
    var resultingPwaDtos = pwaDtoRepository.searchPwas(null, null, MasterPwaDetailStatus.CONSENTED, PageRequest.of(0, 10));

    assertThat(resultingPwaDtos)
        .extracting(PwaDto::getId)
        .containsExactly(secondPwaDetail.getMasterPwaId());
  }

  @Test
  public void searchPwas_whenAllNull_assertAllPwasReturned() {
    var resultingPwaDtos = pwaDtoRepository.searchPwas(null, null, null, PageRequest.of(0, 10));

    assertThat(resultingPwaDtos)
        .extracting(PwaDto::getId)
        .containsExactly(pwaDetail.getMasterPwaId(), secondPwaDetail.getMasterPwaId(), thirdPwaDetail.getMasterPwaId());
  }
}