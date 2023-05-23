package uk.co.ogauthority.pwa.integration.model.entity.masterpwas;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldId;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailField;
import uk.co.ogauthority.pwa.model.enums.PwaResourceType;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailFieldRepository;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
// IJ seems to give spurious warnings when running with embedded H2
public class MasterPwaDetailFieldIntegrationtest {

  private MasterPwa masterPwa;
  private MasterPwaDetail masterPwaDetail;

  @Autowired
  private MasterPwaDetailFieldRepository masterPwaDetailFieldRepository;

  @Autowired
  private EntityManager entityManager;

  public void setup() {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL
    );

    var pwaApplication = pwaApplicationDetail.getPwaApplication();

    masterPwa = pwaApplication.getMasterPwa();
    masterPwa.setId(null);
    entityManager.persist(masterPwa);

    masterPwaDetail = new MasterPwaDetail(masterPwa, MasterPwaDetailStatus.CONSENTED, "ref", Instant.now(), PwaResourceType.PETROLEUM);
    entityManager.persist(masterPwaDetail);

  }

  @Transactional
  @Test
  public void testMappingAndRepositoryQuery_whenManualField(){
    setup();

    var manualField = new MasterPwaDetailField();
    manualField.setManualFieldName("test");
    manualField.setMasterPwaDetail(masterPwaDetail);
    entityManager.persist(manualField);

    var fields = masterPwaDetailFieldRepository.findByMasterPwaDetail(masterPwaDetail);

    assertThat(fields).contains(manualField);
    assertThat(fields.get(0).getDevukFieldId()).isNull();

  }

  @Transactional
  @Test
  public void testMappingAndRepositoryQuery_whenDevukField(){
    setup();

    var manualField = new MasterPwaDetailField();
    manualField.setDevukFieldId(new DevukFieldId(1));
    manualField.setMasterPwaDetail(masterPwaDetail);
    entityManager.persist(manualField);

    var fields = masterPwaDetailFieldRepository.findByMasterPwaDetail(masterPwaDetail);

    assertThat(fields).contains(manualField);
    assertThat(fields.get(0).getDevukFieldId().asInt()).isEqualTo(1);

  }

}
