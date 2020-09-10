package uk.co.ogauthority.pwa.integration.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.files.PadFile_;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation_;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDetailVersioningService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.ProjectInformationTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
// IJ seems to give spurious warnings when running with embedded H2
public class PwaApplicationDetailVersioningServiceIntegrationTest {

  private final static int PERSON_ID = 1;
  private final static int WUA_ID = 2;


  @Autowired
  private EntityManager entityManager;

  @Autowired
  private PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService;

  private MasterPwa masterPwa;
  private PwaApplication pwaApplication;
  private PwaApplicationVersionContainer firstVersionApplicationContainer;

  private Person person = new Person(PERSON_ID, "forename", "surname", "email", "telephone");
  private WebUserAccount webUserAccount = new WebUserAccount(WUA_ID, person);

  public void setup() {

    var firstVersionPwaDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL
    );

    pwaApplication = firstVersionPwaDetail.getPwaApplication();
    masterPwa = pwaApplication.getMasterPwa();
    masterPwa.setId(null);
    entityManager.persist(masterPwa);
    pwaApplication.setId(null);
    entityManager.persist(pwaApplication);
    firstVersionPwaDetail.setId(null);
    entityManager.persist(firstVersionPwaDetail);
    firstVersionApplicationContainer = createAndPersistDefaultApplicationDetail(firstVersionPwaDetail);

  }

  // use this to dummy up and persist all possible form entities
  private PwaApplicationVersionContainer createAndPersistDefaultApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {

    createProjInfoData(pwaApplicationDetail);

    return getApplicationDetailContainer(pwaApplicationDetail);
  }

  private void createAndPersistPadFileWithRandomFileId(PwaApplicationDetail pwaApplicationDetail,
                                                       ApplicationFilePurpose applicationFilePurpose){
    byte[] array = new byte[7]; // length is bounded by 7
    new Random().nextBytes(array);
    String generalPurposeRandomString = new String(array, Charset.forName("UTF-8"));
    var uploadedFile = new UploadedFile(
        generalPurposeRandomString,
        generalPurposeRandomString,
        generalPurposeRandomString,
        0L,
        Instant.now(),
        FileUploadStatus.CURRENT);

    entityManager.persist(uploadedFile);

    var padFile = new PadFile(
        pwaApplicationDetail,
        generalPurposeRandomString,
        applicationFilePurpose,
        ApplicationFileLinkStatus.FULL);
    padFile.setDescription(generalPurposeRandomString);
    entityManager.persist(padFile);
  }

  private void createProjInfoData(PwaApplicationDetail pwaApplicationDetail) {
    var projectInfo = ProjectInformationTestUtils.buildEntity(LocalDate.now());
    projectInfo.setPwaApplicationDetail(pwaApplicationDetail);
    entityManager.persist(projectInfo);
    createAndPersistPadFileWithRandomFileId(pwaApplicationDetail, ApplicationFilePurpose.PROJECT_INFORMATION);
  }

  private PadProjectInformation getProjInfo(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    CriteriaQuery<PadProjectInformation> criteriaQuery = cb.createQuery(PadProjectInformation.class);
    Root<PadProjectInformation> projInfo = criteriaQuery.from(PadProjectInformation.class);
    return entityManager.createQuery(
        criteriaQuery
            .where(cb.equal(projInfo.get(PadProjectInformation_.pwaApplicationDetail), pwaApplicationDetail))
    ).getSingleResult();
  }

  private List<PadFile> getAllAppDetailPadFiles(PwaApplicationDetail pwaApplicationDetail){
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadFile> criteriaQuery = cb.createQuery(PadFile.class);
    Root<PadFile> padFile = criteriaQuery.from(PadFile.class);
    return entityManager.createQuery(
        criteriaQuery
            .where(cb.equal(padFile.get(PadFile_.pwaApplicationDetail), pwaApplicationDetail))
    ).getResultList();

  }

  private PwaApplicationVersionContainer getApplicationDetailContainer(PwaApplicationDetail pwaApplicationDetail){

    var container = new PwaApplicationVersionContainer(pwaApplicationDetail);
    container.setPadProjectInformation(getProjInfo(pwaApplicationDetail));
    container.setPadFiles(getAllAppDetailPadFiles(pwaApplicationDetail));
    return container;

  }

  @Transactional
  @Test
  public void createNewApplicationVersion_allFormEntitiesGetCopiedExactly() {
    setup();

   var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

   var newVersionContainer = getApplicationDetailContainer(newVersionDetail);

   var commonIgnoredComparisonFields = new String[] {"pwaApplicationDetail" , "id"};

   assertThat(
       EqualsBuilder.reflectionEquals(
       firstVersionApplicationContainer.getPadProjectInformation(),
       newVersionContainer.getPadProjectInformation(),
       commonIgnoredComparisonFields
       )).isTrue();

    assertThat(
        EqualsBuilder.reflectionEquals(
            firstVersionApplicationContainer.getPadFiles(),
            newVersionContainer.getPadFiles(),
            commonIgnoredComparisonFields
        )).isTrue();
  }
}
