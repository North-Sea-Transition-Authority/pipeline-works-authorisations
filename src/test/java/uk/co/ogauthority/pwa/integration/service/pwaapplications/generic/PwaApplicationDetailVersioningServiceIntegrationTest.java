package uk.co.ogauthority.pwa.integration.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.files.PadFile_;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole_;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileTestContainer;
import uk.co.ogauthority.pwa.service.fileupload.PadFileTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDetailVersioningService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.ProjectInformationTestUtils;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingTestUtil;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;
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

  private final static int OU_ID_1 = 10;
  private final static int OU_ID_2 = 20;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService;

  private MasterPwa masterPwa;
  private PwaApplication pwaApplication;
  private PwaApplicationVersionContainer firstVersionApplicationContainer;

  private Person person = new Person(PERSON_ID, "forename", "surname", "email", "telephone");
  private WebUserAccount webUserAccount = new WebUserAccount(WUA_ID, person);

  private PortalOrganisationUnit portalOrganisationUnit1;
  private PortalOrganisationUnit portalOrganisationUnit2;

  public void setup() throws IllegalAccessException {

    var firstVersionPwaDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL
    );

    portalOrganisationUnit1 = new PortalOrganisationUnit(OU_ID_1, "Org 1 name");
    portalOrganisationUnit2 = new PortalOrganisationUnit(OU_ID_2, "Org 2 name");
    entityManager.persist(portalOrganisationUnit1);
    entityManager.persist(portalOrganisationUnit2);

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


  private SimplePadPipelineContainer createAndPersistPipeline(
      PwaApplicationDetail pwaApplicationDetail) throws IllegalAccessException {

    var identData = PadPipelineTestUtil.createPadPipeline(pwaApplicationDetail, PipelineType.PRODUCTION_FLOWLINE);
    var ident = identData.getPadPipelineIdent();
    var padPipeline = ident.getPadPipeline();
    var pipeline = padPipeline.getPipeline();
    entityManager.persist(pipeline);
    entityManager.persist(padPipeline);
    entityManager.persist(ident);
    entityManager.persist(identData);
    return new SimplePadPipelineContainer(identData);

  }

  // use this to dummy up and persist all possible form entities
  private PwaApplicationVersionContainer createAndPersistDefaultApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) throws IllegalAccessException {

    createProjInfoData(pwaApplicationDetail);
    var simplePadPipelineContainer = createAndPersistPipeline(pwaApplicationDetail);
    createPadTechnicalDrawingAndLink(pwaApplicationDetail, simplePadPipelineContainer.getPadPipeline());

    createHuooData(pwaApplicationDetail, simplePadPipelineContainer.getPadPipeline().getPipeline());

    return getApplicationDetailContainer(pwaApplicationDetail);
  }

  private PadFileTestContainer createAndPersistPadFileWithRandomFileId(PwaApplicationDetail pwaApplicationDetail,
                                                                       ApplicationFilePurpose applicationFilePurpose) {
    var padFileTestContainer = PadFileTestUtil.createPadFileWithRandomFileIdAndData(
        pwaApplicationDetail,
        applicationFilePurpose);
    entityManager.persist(padFileTestContainer.getUploadedFile());
    entityManager.persist(padFileTestContainer.getPadFile());
    return padFileTestContainer;
  }

  private void createPadTechnicalDrawingAndLink(PwaApplicationDetail pwaApplicationDetail, PadPipeline padPipeline) {
    var tdFileContainer = createAndPersistPadFileWithRandomFileId(pwaApplicationDetail,
        ApplicationFilePurpose.PIPELINE_DRAWINGS);
    var td = PadTechnicalDrawingTestUtil.createPadTechnicalDrawing(pwaApplicationDetail, tdFileContainer.getPadFile());
    var link = PadTechnicalDrawingTestUtil.createPadTechnicalDrawingLink(td, padPipeline);
    entityManager.persist(td);
    entityManager.persist(link);

  }

  private void createProjInfoData(PwaApplicationDetail pwaApplicationDetail) {
    var projectInfo = ProjectInformationTestUtils.buildEntity(LocalDate.now());
    projectInfo.setPwaApplicationDetail(pwaApplicationDetail);
    entityManager.persist(projectInfo);
    createAndPersistPadFileWithRandomFileId(pwaApplicationDetail, ApplicationFilePurpose.PROJECT_INFORMATION);
  }


  private void createHuooData(PwaApplicationDetail pwaApplicationDetail, Pipeline pipeline) {

    var holder = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, portalOrganisationUnit1);
    holder.setPwaApplicationDetail(pwaApplicationDetail);
    var user = PadOrganisationRoleTestUtil.createTreatyRole(HuooRole.USER, TreatyAgreement.NORWAY);
    user.setPwaApplicationDetail(pwaApplicationDetail);
    var operator = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, portalOrganisationUnit1);
    operator.setPwaApplicationDetail(pwaApplicationDetail);
    var owner = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, portalOrganisationUnit2);
    owner.setPwaApplicationDetail(pwaApplicationDetail);

    entityManager.persist(holder);
    entityManager.persist(user);
    entityManager.persist(operator);
    entityManager.persist(owner);

    var holderLink = new PadPipelineOrganisationRoleLink(holder, pipeline);
    var userLink = new PadPipelineOrganisationRoleLink(user, pipeline);
    var operatorLink = new PadPipelineOrganisationRoleLink(operator, pipeline);
    var ownerLink = new PadPipelineOrganisationRoleLink(owner, pipeline);

    entityManager.persist(holderLink);
    entityManager.persist(userLink);
    entityManager.persist(operatorLink);
    entityManager.persist(ownerLink);

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

  private List<PadFile> getAllAppDetailPadFiles(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadFile> criteriaQuery = cb.createQuery(PadFile.class);
    Root<PadFile> padFile = criteriaQuery.from(PadFile.class);
    return entityManager.createQuery(
        criteriaQuery
            .where(cb.equal(padFile.get(PadFile_.pwaApplicationDetail), pwaApplicationDetail))
    ).getResultList();

  }

  private SimplePadPipelineContainer getPadPipeline(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadPipelineIdentData> criteriaQuery = cb.createQuery(PadPipelineIdentData.class);
    Root<PadPipelineIdentData> pipelineIdentDataRoot = criteriaQuery.from(PadPipelineIdentData.class);
    Join<PadPipelineIdentData, PadPipelineIdent> identJoin = pipelineIdentDataRoot.join(
        PadPipelineIdentData_.padPipelineIdent);
    Join<PadPipelineIdent, PadPipeline> padPipelineJoin = identJoin.join(PadPipelineIdent_.padPipeline);
    var result = entityManager.createQuery(
        criteriaQuery
            .where(cb.equal(padPipelineJoin.get(PadPipeline_.pwaApplicationDetail), pwaApplicationDetail))
    ).getSingleResult();

    return new SimplePadPipelineContainer(result);

  }

  private PadTechnicalDrawingLink getPadTechnicalDrawingLink(PwaApplicationDetail pwaApplicationDetail) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadTechnicalDrawingLink> criteriaQuery = cb.createQuery(PadTechnicalDrawingLink.class);
    Root<PadTechnicalDrawingLink> padTechnicalDrawingLinkRoot = criteriaQuery.from(PadTechnicalDrawingLink.class);
    Join<PadTechnicalDrawingLink, PadTechnicalDrawing> technicalDrawingJoin = padTechnicalDrawingLinkRoot
        .join(PadTechnicalDrawingLink_.technicalDrawing);

    var result = entityManager.createQuery(
        criteriaQuery
            .where(cb.equal(technicalDrawingJoin.get(PadTechnicalDrawing_.pwaApplicationDetail), pwaApplicationDetail))
    ).getSingleResult();

    return result;

  }

  private List<PadPipelineOrganisationRoleLink> getPadPipelineLinks(PwaApplicationDetail pwaApplicationDetail){
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PadPipelineOrganisationRoleLink> criteriaQuery = cb.createQuery(PadPipelineOrganisationRoleLink.class);
    Root<PadPipelineOrganisationRoleLink> padPipelineOrganisationRoleLinkRoot = criteriaQuery.from(PadPipelineOrganisationRoleLink.class);
    Join<PadPipelineOrganisationRoleLink, PadOrganisationRole> organisationRoleJoin = padPipelineOrganisationRoleLinkRoot
        .join(PadPipelineOrganisationRoleLink_.padOrgRole);

    var result = entityManager.createQuery(
        criteriaQuery
            .where(cb.equal(organisationRoleJoin.get(PadOrganisationRole_.pwaApplicationDetail), pwaApplicationDetail))
    ).getResultList();

    return result;

  }

  private PwaApplicationVersionContainer getApplicationDetailContainer(PwaApplicationDetail pwaApplicationDetail) {

    var container = new PwaApplicationVersionContainer(pwaApplicationDetail);
    container.setPadProjectInformation(getProjInfo(pwaApplicationDetail));
    container.setPadFiles(getAllAppDetailPadFiles(pwaApplicationDetail));
    container.setSimplePadPipelineContainer(getPadPipeline(pwaApplicationDetail));
    container.setPadTechnicalDrawingLink(getPadTechnicalDrawingLink(pwaApplicationDetail));
    container.setPadTechnicalDrawing(getPadTechnicalDrawingLink(pwaApplicationDetail).getTechnicalDrawing());
    container.setHuooRoles(getPadPipelineLinks(pwaApplicationDetail));
    return container;

  }

  @Transactional
  @Test
  public void createNewApplicationVersion_projectInformationMappedAsExpected() throws IllegalAccessException {
    setup();

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = getApplicationDetailContainer(newVersionDetail);

    var commonIgnoredComparisonFields = new String[]{"pwaApplicationDetail", "id"};

    assertThat(
        EqualsBuilder.reflectionEquals(
            firstVersionApplicationContainer.getPadProjectInformation(),
            newVersionContainer.getPadProjectInformation(),
            commonIgnoredComparisonFields
        )).isTrue();

    assertPadFileDetailsMatch(
        firstVersionApplicationContainer.getPadFile(ApplicationFilePurpose.PROJECT_INFORMATION),
        newVersionContainer.getPadFile(ApplicationFilePurpose.PROJECT_INFORMATION)
    );
  }

  private void assertPadFileDetailsMatch(PadFile lhs, PadFile rhs) {
    assertThat(lhs)
        .extracting(
            PadFile_.DESCRIPTION,
            PadFile_.FILE_ID,
            PadFile_.FILE_LINK_STATUS,
            PadFile_.PURPOSE)
        .containsExactly(
            rhs.getDescription(),
            rhs.getFileId(),
            rhs.getFileLinkStatus(),
            rhs.getPurpose());

  }

  @Transactional
  @Test
  public void createNewApplicationVersion_allPadFilesMappedAsExpected() throws IllegalAccessException {
    setup();

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = getApplicationDetailContainer(newVersionDetail);
    // test each PadFile linked to first version matches that linked to new version
    Arrays.stream(ApplicationFilePurpose.values())
        .forEach(applicationFilePurpose -> {
          if (firstVersionApplicationContainer.getPadFile(applicationFilePurpose) != null) {
            var v1PadFile = firstVersionApplicationContainer.getPadFile(applicationFilePurpose);
            var v2PadFile = newVersionContainer.getPadFile(applicationFilePurpose);
            assertPadFileDetailsMatch(v1PadFile, v2PadFile);
          }
        });
  }


  @Transactional
  @Test
  public void createNewApplicationVersion_allPipelineDataMappedAsExpected() throws IllegalAccessException {
    setup();

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = getApplicationDetailContainer(newVersionDetail);

    assertThat(EqualsBuilder.reflectionEquals(
        firstVersionApplicationContainer.getSimplePadPipelineContainer().getPadPipeline(),
        newVersionContainer.getSimplePadPipelineContainer().getPadPipeline(),
        PadPipeline_.ID, PadPipeline_.PWA_APPLICATION_DETAIL
    )).isTrue();
    assertThat(EqualsBuilder.reflectionEquals(
        firstVersionApplicationContainer.getSimplePadPipelineContainer().getPadPipelineIdent(),
        newVersionContainer.getSimplePadPipelineContainer().getPadPipelineIdent(),
        PadPipelineIdent_.ID, PadPipelineIdent_.PAD_PIPELINE
    )).isTrue();
    assertThat(EqualsBuilder.reflectionEquals(
        firstVersionApplicationContainer.getSimplePadPipelineContainer().getPadPipelineIdentData(),
        newVersionContainer.getSimplePadPipelineContainer().getPadPipelineIdentData(),
        PadPipelineIdentData_.ID, PadPipelineIdentData_.PAD_PIPELINE_IDENT
    )).isTrue();

    assertThat(
        EqualsBuilder.reflectionEquals(
            firstVersionApplicationContainer.getPadTechnicalDrawing(),
            newVersionContainer.getPadTechnicalDrawing(),
            //ignore
            PadTechnicalDrawing_.PWA_APPLICATION_DETAIL,
            PadTechnicalDrawing_.ID,
            PadTechnicalDrawing_.FILE
        )
    ).isTrue();

    assertPadFileDetailsMatch(
        firstVersionApplicationContainer.getPadFile(ApplicationFilePurpose.PIPELINE_DRAWINGS),
        newVersionContainer.getPadFile(ApplicationFilePurpose.PIPELINE_DRAWINGS)
    );

    assertThat(firstVersionApplicationContainer.getPadTechnicalDrawingLink().getPipeline().getPipelineId())
        .isEqualTo(newVersionContainer.getPadTechnicalDrawingLink().getPipeline().getPipelineId());
  }

  @Transactional
  @Test
  public void createNewApplicationVersion_huooRoleLinksMappedAsExpected() throws IllegalAccessException {
    setup();

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = getApplicationDetailContainer(newVersionDetail);

    var padOrgRoleIgnoreFields = Set.of(PadOrganisationRole_.ID, PadOrganisationRole_.PWA_APPLICATION_DETAIL);
    var padPipelineOrgRoleIgnoreFields = Set.of(PadPipelineOrganisationRoleLink_.ID, PadPipelineOrganisationRoleLink_.PAD_ORG_ROLE);

    // HOLDER
    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getHuooRole(HuooRole.HOLDER).getLeft(),
        newVersionContainer.getHuooRole(HuooRole.HOLDER).getLeft(),
        padOrgRoleIgnoreFields
    );

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getHuooRole(HuooRole.HOLDER).getRight(),
        newVersionContainer.getHuooRole(HuooRole.HOLDER).getRight(),
        padPipelineOrgRoleIgnoreFields
    );

    // USER
    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getHuooRole(HuooRole.USER).getLeft(),
        newVersionContainer.getHuooRole(HuooRole.USER).getLeft(),
        padOrgRoleIgnoreFields
    );

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getHuooRole(HuooRole.USER).getRight(),
        newVersionContainer.getHuooRole(HuooRole.USER).getRight(),
        padPipelineOrgRoleIgnoreFields
    );

    // OPERATOR
    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getHuooRole(HuooRole.OPERATOR).getLeft(),
        newVersionContainer.getHuooRole(HuooRole.OPERATOR).getLeft(),
        padOrgRoleIgnoreFields
    );

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getHuooRole(HuooRole.OPERATOR).getRight(),
        newVersionContainer.getHuooRole(HuooRole.OPERATOR).getRight(),
        padPipelineOrgRoleIgnoreFields
    );

    // OWNER
    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getHuooRole(HuooRole.OWNER).getLeft(),
        newVersionContainer.getHuooRole(HuooRole.OWNER).getLeft(),
        padOrgRoleIgnoreFields
    );

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getHuooRole(HuooRole.OWNER).getRight(),
        newVersionContainer.getHuooRole(HuooRole.OWNER).getRight(),
        padPipelineOrgRoleIgnoreFields
    );
  }
}
