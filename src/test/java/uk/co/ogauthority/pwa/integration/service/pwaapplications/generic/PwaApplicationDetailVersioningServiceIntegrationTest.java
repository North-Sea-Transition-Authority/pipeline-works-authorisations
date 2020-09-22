package uk.co.ogauthority.pwa.integration.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import javax.persistence.EntityManager;
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
import uk.co.ogauthority.pwa.integration.PwaApplicationIntegrationTestHelper;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField_;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.files.PadFile_;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadEnvironmentalDecommissioning_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorkSchedule_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDepositTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole_;
import uk.co.ogauthority.pwa.service.devuk.PadFieldTestUtil;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.fileupload.PadFileTestContainer;
import uk.co.ogauthority.pwa.service.fileupload.PadFileTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationTaskService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDetailVersioningService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.PadCampaignWorksScheduleTestUtil;
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

  private final static int FIELD_ID = 100;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService;

  @Autowired
  private ApplicationTaskService applicationTaskService;

  private MasterPwa masterPwa;
  private PwaApplication pwaApplication;
  private PwaApplicationVersionContainer firstVersionApplicationContainer;

  private Person person = new Person(PERSON_ID, "forename", "surname", "email", "telephone");
  private WebUserAccount webUserAccount = new WebUserAccount(WUA_ID, person);

  private PortalOrganisationUnit portalOrganisationUnit1;
  private PortalOrganisationUnit portalOrganisationUnit2;

  private DevukField devukField;

  private PwaApplicationIntegrationTestHelper testHelper;

  public void setup(PwaApplicationType pwaApplicationType) throws IllegalAccessException {

    testHelper = new PwaApplicationIntegrationTestHelper(entityManager);

    var firstVersionPwaDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        pwaApplicationType
    );

    devukField = new DevukField(FIELD_ID, "some field", 500);
    entityManager.persist(devukField);

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

    if (pwaApplicationDetail.getPwaApplicationType() == PwaApplicationType.OPTIONS_VARIATION) {
      createSupplementaryDocument(pwaApplicationDetail);
      createOptionsTemplateDocument(pwaApplicationDetail);
    }

    if (applicationTaskService.canShowTask(ApplicationTask.PIPELINES, pwaApplicationDetail)) {
      var simplePadPipelineContainer = createAndPersistPipeline(pwaApplicationDetail);
      createPadTechnicalDrawingAndLink(pwaApplicationDetail, simplePadPipelineContainer.getPadPipeline());
      createHuooData(pwaApplicationDetail, simplePadPipelineContainer.getPadPipeline().getPipeline());
      createAndPersistPermanentDepositData(pwaApplicationDetail, simplePadPipelineContainer);
      createCampaignWorksData(pwaApplicationDetail, simplePadPipelineContainer);
    }

    createProjInfoData(pwaApplicationDetail);
    createPadFieldLinks(pwaApplicationDetail);
    createPadEnvDecom(pwaApplicationDetail);
    createOtherPipelineDiagramLinks(pwaApplicationDetail);
    createPartnerLetterDocument(pwaApplicationDetail);

    return testHelper.getApplicationDetailContainer(pwaApplicationDetail);
  }

  private void createSupplementaryDocument(PwaApplicationDetail pwaApplicationDetail){
    createAndPersistPadFileWithRandomFileId(pwaApplicationDetail, ApplicationDetailFilePurpose.SUPPLEMENTARY_DOCUMENTS);
  }

  private void createOptionsTemplateDocument(PwaApplicationDetail pwaApplicationDetail){
    createAndPersistPadFileWithRandomFileId(pwaApplicationDetail, ApplicationDetailFilePurpose.OPTIONS_TEMPLATE);
  }

  private void createPadEnvDecom(PwaApplicationDetail pwaApplicationDetail){
    var entity = PadEnvironmentalDecommissioningTestUtil.createPadEnvironmentalDecommissioning(pwaApplicationDetail);
    entityManager.persist(entity);
  }

  private void createPadFieldLinks(PwaApplicationDetail pwaApplicationDetail){
    var pf1 = PadFieldTestUtil.createDevukPadField(pwaApplicationDetail, devukField);
    entityManager.persist(pf1);
    var pf2 = PadFieldTestUtil.createManualPadField(pwaApplicationDetail);
    entityManager.persist(pf2);
  }

  private void createAndPersistPermanentDepositData(PwaApplicationDetail pwaApplicationDetail,
                                                    SimplePadPipelineContainer simplePadPipelineContainer) {

    var permanentDeposit = PadPermanentDepositTestUtil.createPadDepositWithAllFieldsPopulated(pwaApplicationDetail);
    entityManager.persist(permanentDeposit);

    var ppdFileContainer = createAndPersistPadFileWithRandomFileId(pwaApplicationDetail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS);
    var depositDrawing = PadPermanentDepositTestUtil.createPadDepositDrawing(pwaApplicationDetail, ppdFileContainer.getPadFile());
    entityManager.persist(depositDrawing);

    var depositDrawingLink = PadPermanentDepositTestUtil.createPadDepositDrawingLink(permanentDeposit, depositDrawing);
    entityManager.persist(depositDrawingLink);

    var depositPipeline = PadPermanentDepositTestUtil.createDepositPipeline(permanentDeposit, simplePadPipelineContainer.getPadPipeline());
    entityManager.persist(depositPipeline);
  }

  private PadFileTestContainer createAndPersistPadFileWithRandomFileId(PwaApplicationDetail pwaApplicationDetail,
                                                                       ApplicationDetailFilePurpose applicationDetailFilePurpose) {
    var padFileTestContainer = PadFileTestUtil.createPadFileWithRandomFileIdAndData(
        pwaApplicationDetail,
        applicationDetailFilePurpose);
    entityManager.persist(padFileTestContainer.getUploadedFile());
    entityManager.persist(padFileTestContainer.getPadFile());
    return padFileTestContainer;
  }

  private void createPadTechnicalDrawingAndLink(PwaApplicationDetail pwaApplicationDetail, PadPipeline padPipeline) {
    var tdFileContainer = createAndPersistPadFileWithRandomFileId(pwaApplicationDetail,
        ApplicationDetailFilePurpose.PIPELINE_DRAWINGS);
    var td = PadTechnicalDrawingTestUtil.createPadTechnicalDrawing(pwaApplicationDetail, tdFileContainer.getPadFile());
    var link = PadTechnicalDrawingTestUtil.createPadTechnicalDrawingLink(td, padPipeline);
    entityManager.persist(td);
    entityManager.persist(link);

  }

  private void createProjInfoData(PwaApplicationDetail pwaApplicationDetail) {
    var projectInfo = ProjectInformationTestUtils.buildEntity(LocalDate.now());
    projectInfo.setPwaApplicationDetail(pwaApplicationDetail);
    entityManager.persist(projectInfo);
    createAndPersistPadFileWithRandomFileId(pwaApplicationDetail, ApplicationDetailFilePurpose.PROJECT_INFORMATION);
  }

  private void createCampaignWorksData(PwaApplicationDetail pwaApplicationDetail,
                                       SimplePadPipelineContainer simplePadPipelineContainer) {
    var schedule = PadCampaignWorksScheduleTestUtil.createPadCampaignWorkSchedule(pwaApplicationDetail);
    entityManager.persist(schedule);

    var schedulePipeline = PadCampaignWorksScheduleTestUtil.createPadCampaignWorksPipeline(
        schedule,
        simplePadPipelineContainer.getPadPipeline());
    entityManager.persist(schedulePipeline);

  }

  private void createOtherPipelineDiagramLinks(PwaApplicationDetail pwaApplicationDetail) {
    var umbilicalFileContainer = createAndPersistPadFileWithRandomFileId(
        pwaApplicationDetail, ApplicationDetailFilePurpose.ADMIRALTY_CHART);
    var admiraltyChartFileContainer = createAndPersistPadFileWithRandomFileId(
        pwaApplicationDetail, ApplicationDetailFilePurpose.UMBILICAL_CROSS_SECTION);
  }

  private void createPartnerLetterDocument(PwaApplicationDetail pwaApplicationDetail) {
    var copiedPartnerLetterEntityIds = createAndPersistPadFileWithRandomFileId(
        pwaApplicationDetail, ApplicationDetailFilePurpose.PARTNER_LETTERS);
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

  @Transactional
  @Test
  public void createNewApplicationVersion_projectInformationMappedAsExpected() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    var commonIgnoredComparisonFields = new String[]{"pwaApplicationDetail", "id"};

    assertThat(
        EqualsBuilder.reflectionEquals(
            firstVersionApplicationContainer.getPadProjectInformation(),
            newVersionContainer.getPadProjectInformation(),
            commonIgnoredComparisonFields
        )).isTrue();

    assertPadFileDetailsMatch(
        firstVersionApplicationContainer.getPadFile(ApplicationDetailFilePurpose.PROJECT_INFORMATION),
        newVersionContainer.getPadFile(ApplicationDetailFilePurpose.PROJECT_INFORMATION)
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
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);
    // test each PadFile linked to first version matches that linked to new version
    Arrays.stream(ApplicationDetailFilePurpose.values())
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
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

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
        firstVersionApplicationContainer.getPadFile(ApplicationDetailFilePurpose.PIPELINE_DRAWINGS),
        newVersionContainer.getPadFile(ApplicationDetailFilePurpose.PIPELINE_DRAWINGS)
    );

    assertThat(firstVersionApplicationContainer.getPadTechnicalDrawingLink().getPipeline().getPipelineId())
        .isEqualTo(newVersionContainer.getPadTechnicalDrawingLink().getPipeline().getPipelineId());
  }

  @Transactional
  @Test
  public void createNewApplicationVersion_huooRoleLinksMappedAsExpected() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    var padOrgRoleIgnoreFields = Set.of(PadOrganisationRole_.ID, PadOrganisationRole_.PWA_APPLICATION_DETAIL);
    var padPipelineOrgRoleIgnoreFields = Set.of(PadPipelineOrganisationRoleLink_.ID,
        PadPipelineOrganisationRoleLink_.PAD_ORG_ROLE);

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

  @Transactional
  @Test
  public void createNewApplicationVersion_permanentDepositCopiedAsExpected() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getPadDepositPipeline().getPadPermanentDeposit(),
        newVersionContainer.getPadDepositPipeline().getPadPermanentDeposit(),
        Set.of(PadPermanentDeposit_.ID, PadPermanentDeposit_.PWA_APPLICATION_DETAIL)
    );

    assertThat(firstVersionApplicationContainer.getPadDepositPipeline().getPadPipeline().getPipelineId())
        .isEqualTo(newVersionContainer.getPadDepositPipeline().getPadPipeline().getPipelineId());

    assertThat(newVersionContainer.getPadDepositPipeline().getPadPipeline())
        .isEqualTo(newVersionContainer.getSimplePadPipelineContainer().getPadPipeline());

    // make sure drawing link to perm deposits match
    assertThat(newVersionContainer.getPadDepositDrawingLink().getPadPermanentDeposit())
        .isEqualTo(newVersionContainer.getPadDepositPipeline().getPadPermanentDeposit());

    // make sure different padFile
    assertThat(firstVersionApplicationContainer.getPadDepositDrawingLink()
        .getPadDepositDrawing().getFile().getId())
        .isNotEqualTo(newVersionContainer.getPadDepositDrawingLink()
            .getPadDepositDrawing().getFile().getId());

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getPadDepositDrawingLink().getPadDepositDrawing(),
        newVersionContainer.getPadDepositDrawingLink().getPadDepositDrawing(),
        Set.of(PadDepositDrawing_.ID, PadDepositDrawing_.PWA_APPLICATION_DETAIL, PadDepositDrawing_.FILE)
    );

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getPadDepositDrawingLink().getPadPermanentDeposit(),
        newVersionContainer.getPadDepositDrawingLink().getPadPermanentDeposit(),
        Set.of(PadPermanentDeposit_.ID, PadPermanentDeposit_.PWA_APPLICATION_DETAIL)
    );

  }

  @Transactional
  @Test
  public void createNewApplicationVersion_campaignWorkScheduleCopiedAsExpected() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getPadCampaignWorksPipeline().getPadCampaignWorkSchedule(),
        newVersionContainer.getPadCampaignWorksPipeline().getPadCampaignWorkSchedule(),
        Set.of(PadCampaignWorkSchedule_.ID, PadCampaignWorkSchedule_.PWA_APPLICATION_DETAIL)
    );

    assertThat(firstVersionApplicationContainer.getPadCampaignWorksPipeline().getPadPipeline().getPipelineId())
        .isEqualTo(newVersionContainer.getPadCampaignWorksPipeline().getPadPipeline().getPipelineId());

    assertThat(newVersionContainer.getPadCampaignWorksPipeline().getPadPipeline())
        .isEqualTo(newVersionContainer.getPadCampaignWorksPipeline().getPadPipeline());

  }

  @Transactional
  @Test
  public void createNewApplicationVersion_padFieldsCopiedAsExpected() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    var v1ManualField = firstVersionApplicationContainer.getPadFields().stream()
        .filter(f -> f.getFieldName() != null)
        .findFirst().orElseThrow(() -> new RuntimeException("Expected to find manual field"));

    var v2ManualField = newVersionContainer.getPadFields().stream()
        .filter(f -> f.getFieldName() != null)
        .findFirst().orElseThrow(() -> new RuntimeException("Expected to find manual field"));

    var v1DevukField = firstVersionApplicationContainer.getPadFields().stream()
        .filter(f -> f.getDevukField() != null)
        .findFirst().orElseThrow(() -> new RuntimeException("Expected to find devuk field"));

    var v2DevukField = newVersionContainer.getPadFields().stream()
        .filter(f -> f.getDevukField() != null)
        .findFirst().orElseThrow(() -> new RuntimeException("Expected to find devuk field"));

    ObjectTestUtils.assertValuesEqual(v1ManualField, v2ManualField,
        Set.of(PadField_.ID, PadField_.PWA_APPLICATION_DETAIL));

    ObjectTestUtils.assertValuesEqual(v1DevukField, v2DevukField,
        Set.of(PadField_.ID, PadField_.PWA_APPLICATION_DETAIL));

  }

  @Transactional
  @Test
  public void createNewApplicationVersion_padEnvDecomCopiedAsExpected() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getPadEnvironmentalDecommissioning(),
        newVersionContainer.getPadEnvironmentalDecommissioning(),
        Set.of(PadEnvironmentalDecommissioning_.ID, PadEnvironmentalDecommissioning_.PWA_APPLICATION_DETAIL)
    );

  }

  @Transactional
  @Test
  public void createNewApplicationVersion_otherPipelineDiagrams() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    assertPadFileDetailsMatch(
        firstVersionApplicationContainer.getPadFile(ApplicationDetailFilePurpose.UMBILICAL_CROSS_SECTION),
        newVersionContainer.getPadFile(ApplicationDetailFilePurpose.UMBILICAL_CROSS_SECTION)
    );

    assertPadFileDetailsMatch(
        firstVersionApplicationContainer.getPadFile(ApplicationDetailFilePurpose.ADMIRALTY_CHART),
        newVersionContainer.getPadFile(ApplicationDetailFilePurpose.ADMIRALTY_CHART)
    );

  }

  @Transactional
  @Test
  public void createNewApplicationVersion_partnerLetters() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    assertPadFileDetailsMatch(
        firstVersionApplicationContainer.getPadFile(ApplicationDetailFilePurpose.PARTNER_LETTERS),
        newVersionContainer.getPadFile(ApplicationDetailFilePurpose.PARTNER_LETTERS)
    );

  }

  @Transactional
  @Test
  public void createNewApplicationVersion_optionVariationDocuments() throws IllegalAccessException {
    setup(PwaApplicationType.OPTIONS_VARIATION);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    assertPadFileDetailsMatch(
        firstVersionApplicationContainer.getPadFile(ApplicationDetailFilePurpose.OPTIONS_TEMPLATE),
        newVersionContainer.getPadFile(ApplicationDetailFilePurpose.OPTIONS_TEMPLATE)
    );

    assertPadFileDetailsMatch(
        firstVersionApplicationContainer.getPadFile(ApplicationDetailFilePurpose.SUPPLEMENTARY_DOCUMENTS),
        newVersionContainer.getPadFile(ApplicationDetailFilePurpose.SUPPLEMENTARY_DOCUMENTS)
    );

  }
}
