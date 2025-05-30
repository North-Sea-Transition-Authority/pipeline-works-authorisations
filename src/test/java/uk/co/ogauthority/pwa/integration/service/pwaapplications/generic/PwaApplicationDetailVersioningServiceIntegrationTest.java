package uk.co.ogauthority.pwa.integration.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.files.PadFile_;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTaskService;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.PadCampaignWorkSchedule_;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.PadCampaignWorksScheduleTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossingTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossing_;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.PadCrossedBlockOwner_;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.PadCrossedBlockTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.PadCrossedBlock_;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.PadMedianLineAgreement_;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossingOwner_;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossingTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossing_;
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.PadDesignOpConditionsTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.PadDesignOpConditions_;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.PadEnvironmentalDecommissioningTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.PadEnvironmentalDecommissioning_;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.PadFastTrackTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.PadFastTrack_;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadFieldTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadLinkedArea_;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.PadFluidCompositionInfoTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.PadFluidCompositionInfo_;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.Chemical;
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PadPipelineTechInfoTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PadPipelineTechInfo_;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRole_;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadFacilityTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadFacility_;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadLocationDetailTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadLocationDetails_;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOptionTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOption_;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.OtherPipelineProperty;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PadPipelineOtherPropertiesTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PadPipelineOtherProperties_;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PropertyPhase;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadDepositDrawing_;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadPermanentDepositTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadPermanentDeposit_;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartDocumentForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawing_;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelineOrganisationRoleLink_;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline_;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData_;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent_;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PermanentDepositMade;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationTestUtils;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.integration.PwaApplicationIntegrationTestHelper;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.external.DevukFacility;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukField;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.LicenceStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsLicence;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApplicationApproval;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.fileupload.PadFileManagementTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDetailVersioningService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementTestUtil;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
@Disabled //TODO: PWARE-71 fix tests failing on drone and re-enable
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
// IJ seems to give spurious warnings when running with embedded H2
public class PwaApplicationDetailVersioningServiceIntegrationTest {

  private final static int PERSON_ID = 1;
  private final static int WUA_ID = 2;

  private final static int OU_ID_1 = 10;
  private final static int OU_ID_2 = 20;

  private final static int FIELD_ID = 100;
  private final static int FACILITY_ID = 111;
  private final static int PEARS_LICENCE_ID = 122;

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

  private DevukFacility devukFacility;

  private PearsLicence pearsLicence;

  private PwaApplicationIntegrationTestHelper testHelper;
  @Autowired
  private PadFileManagementService padFileManagementService;
  @Qualifier("fileService")
  @Autowired
  private FileService fileService;

  public void setup(PwaApplicationType pwaApplicationType, boolean isFastTrack) throws IllegalAccessException {
    testHelper = new PwaApplicationIntegrationTestHelper(entityManager);

    var firstVersionPwaDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        pwaApplicationType
    );

    devukField = new DevukField(FIELD_ID, "some field", 500);
    entityManager.persist(devukField);

    devukFacility = new DevukFacility(FACILITY_ID, "some facility");
    entityManager.persist(devukFacility);

    pearsLicence = new PearsLicence(
        PEARS_LICENCE_ID,
        "licence type",
        10,
        "licence name",
        LicenceStatus.EXTANT);

    entityManager.persist(pearsLicence);

    portalOrganisationUnit1 = PortalOrganisationTestUtils.generateOrganisationUnit(OU_ID_1, "Org 1 name");
    portalOrganisationUnit2 = PortalOrganisationTestUtils.generateOrganisationUnit(OU_ID_2, "Org 2 name");
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

    firstVersionApplicationContainer = createAndPersistDefaultApplicationDetail(firstVersionPwaDetail, isFastTrack);
  }

  public void setup(PwaApplicationType pwaApplicationType) throws IllegalAccessException {
    setup(pwaApplicationType, false);
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
      PwaApplicationDetail pwaApplicationDetail,
      boolean isFastTrack)
      throws IllegalAccessException {

    if (pwaApplicationDetail.getPwaApplicationType() == PwaApplicationType.OPTIONS_VARIATION) {
      createSupplementaryDocument(pwaApplicationDetail);
      createOptionsTemplateDocument(pwaApplicationDetail);
      createPadConfirmationOfOption(pwaApplicationDetail);
    }

    createPipelineData(pwaApplicationDetail);
    createProjInfoData(pwaApplicationDetail, isFastTrack);
    createPadFieldLinks(pwaApplicationDetail);
    createPadEnvDecom(pwaApplicationDetail);
    createPartnerLetterDocument(pwaApplicationDetail);
    createPadLocationDetailsData(pwaApplicationDetail);
    createAllCrossingData(pwaApplicationDetail);
    createGeneralTechDetailsData(pwaApplicationDetail);
    createFluidCompositionData(pwaApplicationDetail);
    createOtherPropertiesData(pwaApplicationDetail);
    createDesignOpeConditionsData(pwaApplicationDetail);
    createFastTrackData(pwaApplicationDetail);


    return testHelper.getApplicationDetailContainer(pwaApplicationDetail);
  }

  private void createPadConfirmationOfOption(PwaApplicationDetail pwaApplicationDetail){
    // need to create approval in order get this task to show
    var approval = new OptionsApplicationApproval();
    approval.setPwaApplication(pwaApplicationDetail.getPwaApplication());
    entityManager.persist(approval);

    var confirmation = PadConfirmationOfOptionTestUtil.createConfirmationOfOption(pwaApplicationDetail);
    entityManager.persist(confirmation);
  }

  private void createFastTrackData(PwaApplicationDetail pwaApplicationDetail){
    if (applicationTaskService.canShowTask(ApplicationTask.FAST_TRACK, pwaApplicationDetail)) {
      var fastTrack = PadFastTrackTestUtil.createPadFastTrack(pwaApplicationDetail);
      entityManager.persist(fastTrack);
    }
  }

  private void createDesignOpeConditionsData(PwaApplicationDetail pwaApplicationDetail) {
    if (applicationTaskService.canShowTask(ApplicationTask.DESIGN_OP_CONDITIONS, pwaApplicationDetail)) {
      var designOpConditions = PadDesignOpConditionsTestUtil.createPadDesignOpConditions(pwaApplicationDetail);
      entityManager.persist(designOpConditions);
    }

  }

  private void createOtherPropertiesData(PwaApplicationDetail pwaApplicationDetail) {
    if (applicationTaskService.canShowTask(ApplicationTask.PIPELINE_OTHER_PROPERTIES, pwaApplicationDetail)) {
      OtherPipelineProperty.asList().forEach(otherPipelineProperty -> {
        if (otherPipelineProperty.ordinal() % 3 == 0) {
          entityManager.persist(
              PadPipelineOtherPropertiesTestUtil.createNotAvailableProperty(pwaApplicationDetail, otherPipelineProperty)
          );
        } else {
          entityManager.persist(
              PadPipelineOtherPropertiesTestUtil.createAvailableProperty(pwaApplicationDetail, otherPipelineProperty)
          );
        }


      });
    }
  }

  private void createFluidCompositionData(PwaApplicationDetail pwaApplicationDetail) {
    // create fluids for all chemicals across the range of fluid amounts
    if (applicationTaskService.canShowTask(ApplicationTask.FLUID_COMPOSITION, pwaApplicationDetail)) {
      Chemical.getAll().forEach(chemical -> {
        if (chemical.ordinal() % 3 == 0) {
          entityManager.persist(PadFluidCompositionInfoTestUtil.createSignificantFluid(pwaApplicationDetail, chemical));
        } else if (chemical.ordinal() % 3 == 1) {
          entityManager.persist(PadFluidCompositionInfoTestUtil.createTraceFluid(pwaApplicationDetail, chemical));
        } else {
          entityManager.persist(PadFluidCompositionInfoTestUtil.createNotPresentFluid(pwaApplicationDetail, chemical));
        }
      });
    }
  }

  private void createAllCrossingData(PwaApplicationDetail pwaApplicationDetail) {
    if (applicationTaskService.canShowTask(ApplicationTask.CROSSING_AGREEMENTS, pwaApplicationDetail)) {
      pwaApplicationDetail.setCablesCrossed(true);
      pwaApplicationDetail.setMedianLineCrossed(true);
      pwaApplicationDetail.setPipelinesCrossed(true);
      entityManager.persist(pwaApplicationDetail);

      createPadCrossedBlockData(pwaApplicationDetail);
      createPadCableCrossingData(pwaApplicationDetail);
      createPadPipelineCrossingData(pwaApplicationDetail);
      createPadMedianLineData(pwaApplicationDetail);
    }
  }

  private void createPipelineData(PwaApplicationDetail pwaApplicationDetail) throws IllegalAccessException {
    if (applicationTaskService.canShowTask(ApplicationTask.PIPELINES, pwaApplicationDetail)) {
      var simplePadPipelineContainer = createAndPersistPipeline(pwaApplicationDetail);
      createPadTechnicalDrawingAndLink(pwaApplicationDetail, simplePadPipelineContainer.getPadPipeline());
      createHuooData(pwaApplicationDetail, simplePadPipelineContainer.getPadPipeline().getPipeline());
      createAndPersistPermanentDepositData(pwaApplicationDetail, simplePadPipelineContainer);
      createCampaignWorksData(pwaApplicationDetail, simplePadPipelineContainer);
      createOtherPipelineDiagramLinks(pwaApplicationDetail);
    }
  }

  private void createGeneralTechDetailsData(PwaApplicationDetail pwaApplicationDetail) {
    if (applicationTaskService.canShowTask(ApplicationTask.GENERAL_TECH_DETAILS, pwaApplicationDetail)) {
      var td = PadPipelineTechInfoTestUtil.createPadPipelineTechInfo(pwaApplicationDetail);
      entityManager.persist(td);
    }
  }

  private void createPadMedianLineData(PwaApplicationDetail pwaApplicationDetail) {
    var medianLineAgreement = PadMedianLineAgreementTestUtil.createPadMedianLineAgreement(pwaApplicationDetail);
    entityManager.persist(medianLineAgreement);

    createAndPersistUploadedFileWithRandomFileId(pwaApplicationDetail, FileDocumentType.MEDIAN_LINE_CROSSING);
  }

  private void createPadPipelineCrossingData(PwaApplicationDetail pwaApplicationDetail) {
    createAndPersistUploadedFileWithRandomFileId(pwaApplicationDetail, FileDocumentType.PIPELINE_CROSSINGS);

    var pipelineCrossing = PadPipelineCrossingTestUtil.createPadPipelineCrossing(pwaApplicationDetail);
    var portalOrgCrossingOwner = PadPipelineCrossingTestUtil.createPortalOrgPadPipelineCrossingOwner(
        pipelineCrossing,
        portalOrganisationUnit2);

    var manualOrgCrossingOwner = PadPipelineCrossingTestUtil.createManualOrgPadPipelineCrossingOwner(pipelineCrossing);

    entityManager.persist(pipelineCrossing);
    entityManager.persist(portalOrgCrossingOwner);
    entityManager.persist(manualOrgCrossingOwner);

  }

  private void createPadCableCrossingData(PwaApplicationDetail pwaApplicationDetail) {
    createAndPersistUploadedFileWithRandomFileId(pwaApplicationDetail, FileDocumentType.CABLE_CROSSINGS);
    var padCableCrossing = PadCableCrossingTestUtil.createPadCableCrossing(pwaApplicationDetail);
    entityManager.persist(padCableCrossing);

  }

  private void createPadCrossedBlockData(PwaApplicationDetail pwaApplicationDetail) {
    createAndPersistUploadedFileWithRandomFileId(pwaApplicationDetail, FileDocumentType.BLOCK_CROSSINGS);

    var crossedBlock1 = PadCrossedBlockTestUtil.createUnlicensedPadCrossedBlock(
        pwaApplicationDetail,
        CrossingOwner.PORTAL_ORGANISATION);
    var crossedBlock1Owner = PadCrossedBlockTestUtil.createPortalOrgPadCrossedBlockOwner(
        crossedBlock1,
        portalOrganisationUnit1);

    entityManager.persist(crossedBlock1);
    entityManager.persist(crossedBlock1Owner);

    var crossedBlock2 = PadCrossedBlockTestUtil.createLicensedPadCrossedBlock(
        pwaApplicationDetail,
        CrossingOwner.PORTAL_ORGANISATION,
        pearsLicence);
    var crossedBlock2Owner = PadCrossedBlockTestUtil.createManualPadCrossedBlockOwner(crossedBlock2);

    entityManager.persist(crossedBlock2);
    entityManager.persist(crossedBlock2Owner);
  }

  private void createPadLocationDetailsData(PwaApplicationDetail pwaApplicationDetail) {
    if (applicationTaskService.canShowTask(ApplicationTask.LOCATION_DETAILS, pwaApplicationDetail)) {
      createAndPersistUploadedFileWithRandomFileId(pwaApplicationDetail, FileDocumentType.LOCATION_DETAILS);
      var manualPadFacility = PadFacilityTestUtil.createManualFacility(pwaApplicationDetail);
      var devukPadFacility = PadFacilityTestUtil.createDevukLinkedFacility(pwaApplicationDetail, devukFacility);
      var padLocationDetails = PadLocationDetailTestUtil.createPadLocationDetails(pwaApplicationDetail);
      entityManager.persist(manualPadFacility);
      entityManager.persist(devukPadFacility);
      entityManager.persist(padLocationDetails);
    }
  }

  private void createSupplementaryDocument(PwaApplicationDetail pwaApplicationDetail) {
    createAndPersistUploadedFileWithRandomFileId(pwaApplicationDetail, FileDocumentType.SUPPLEMENTARY_DOCUMENTS);
  }

  private void createOptionsTemplateDocument(PwaApplicationDetail pwaApplicationDetail) {
    createAndPersistUploadedFileWithRandomFileId(pwaApplicationDetail, FileDocumentType.OPTIONS_TEMPLATE);
  }

  private void createPadEnvDecom(PwaApplicationDetail pwaApplicationDetail) {
    var entity = PadEnvironmentalDecommissioningTestUtil.createPadEnvironmentalDecommissioning(pwaApplicationDetail);
    entityManager.persist(entity);
  }

  private void createPadFieldLinks(PwaApplicationDetail pwaApplicationDetail) {
    var pf1 = PadFieldTestUtil.createDevukPadField(pwaApplicationDetail, devukField);
    entityManager.persist(pf1);
    var pf2 = PadFieldTestUtil.createManualPadField(pwaApplicationDetail);
    entityManager.persist(pf2);
  }

  private void createAndPersistPermanentDepositData(PwaApplicationDetail pwaApplicationDetail,
                                                    SimplePadPipelineContainer simplePadPipelineContainer) {

    var permanentDeposit = PadPermanentDepositTestUtil.createPadDepositWithAllFieldsPopulated(pwaApplicationDetail);
    entityManager.persist(permanentDeposit);

    var padFile = createAndPersistPadFileWithRandomFileId(
        pwaApplicationDetail,
        FileDocumentType.DEPOSIT_DRAWINGS,
        ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS
    );
    var depositDrawing = PadPermanentDepositTestUtil.createPadDepositDrawing(
        pwaApplicationDetail,
        padFile);
    entityManager.persist(depositDrawing);

    var depositDrawingLink = PadPermanentDepositTestUtil.createPadDepositDrawingLink(permanentDeposit, depositDrawing);
    entityManager.persist(depositDrawingLink);

    var depositPipeline = PadPermanentDepositTestUtil.createDepositPipeline(
        permanentDeposit,
        simplePadPipelineContainer.getPadPipeline().getPipeline());
    entityManager.persist(depositPipeline);
  }

  private UUID createAndPersistUploadedFileWithRandomFileId(PwaApplicationDetail pwaApplicationDetail,
                                                            FileDocumentType fileDocumentType) {
    var response = fileService.upload(builder -> builder
        .withFileSource(FileSource.fromMultipartFile(PadFileManagementTestUtil.createRandomMultipartFile()))
        .build());

    var fileForm = new UploadedFileForm();
    fileForm.setFileId(response.getFileId());
    fileForm.setFileName(response.getFileName());
    fileForm.setFileDescription("description");

    var form = new AdmiraltyChartDocumentForm();
    form.setUploadedFiles(List.of(fileForm));

    padFileManagementService.saveFiles(form, pwaApplicationDetail, fileDocumentType);
    return response.getFileId();
  }

  private PadFile createAndPersistPadFileWithRandomFileId(PwaApplicationDetail pwaApplicationDetail,
                                                                       FileDocumentType fileDocumentType,
                                                                       ApplicationDetailFilePurpose applicationDetailFilePurpose) {
    var fileId = createAndPersistUploadedFileWithRandomFileId(pwaApplicationDetail, fileDocumentType);

    var padFile = PadFileManagementTestUtil.createPadFileWithRandomFileIdAndData(
        pwaApplicationDetail,
        fileId,
        applicationDetailFilePurpose);
    entityManager.persist(padFile);
    return padFile;
  }

  private void createPadTechnicalDrawingAndLink(PwaApplicationDetail pwaApplicationDetail, PadPipeline padPipeline) {
    var tdFile = createAndPersistPadFileWithRandomFileId(
        pwaApplicationDetail,
        FileDocumentType.PIPELINE_DRAWINGS,
        ApplicationDetailFilePurpose.PIPELINE_DRAWINGS
    );

    var td = PadTechnicalDrawingTestUtil.createPadTechnicalDrawing(pwaApplicationDetail, tdFile);
    var link = PadTechnicalDrawingTestUtil.createPadTechnicalDrawingLink(td, padPipeline);
    entityManager.persist(td);
    entityManager.persist(link);
  }

  private void createProjInfoData(PwaApplicationDetail pwaApplicationDetail, boolean forceFastTrackStartDate) {

    var projectInfo = ProjectInformationTestUtils.buildEntity(
        forceFastTrackStartDate ? LocalDate.now() : LocalDate.now().plusMonths(12L)
    );
    projectInfo.setPwaApplicationDetail(pwaApplicationDetail);
    projectInfo.setPermanentDepositsMade(PermanentDepositMade.THIS_APP); // set to this app to justify having deposits
    entityManager.persist(projectInfo);

    createAndPersistUploadedFileWithRandomFileId(pwaApplicationDetail, FileDocumentType.PROJECT_INFORMATION);
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
    createAndPersistUploadedFileWithRandomFileId(pwaApplicationDetail, FileDocumentType.ADMIRALTY_CHART);
    createAndPersistUploadedFileWithRandomFileId(pwaApplicationDetail, FileDocumentType.UMBILICAL_CROSS_SECTION);
  }

  private void createPartnerLetterDocument(PwaApplicationDetail pwaApplicationDetail) {
    createAndPersistUploadedFileWithRandomFileId(pwaApplicationDetail, FileDocumentType.PARTNER_LETTERS);
  }


  private void createHuooData(PwaApplicationDetail pwaApplicationDetail, Pipeline pipeline) {

    var holder = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, portalOrganisationUnit1);
    holder.setPwaApplicationDetail(pwaApplicationDetail);
    var user = PadOrganisationRoleTestUtil.createTreatyRole(HuooRole.USER, TreatyAgreement.ANY_TREATY_COUNTRY);
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
  void createNewApplicationVersion_projectInformationMappedAsExpected() throws IllegalAccessException {
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

    assertUploadedFileDetailsMatch(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        newVersionDetail,
        FileDocumentType.PROJECT_INFORMATION
    );
  }

  private void assertUploadedFileDetailsMatch(
      PwaApplicationDetail pwaApplicationDetail1,
      PwaApplicationDetail pwaApplicationDetail2,
      FileDocumentType fileDocumentType
  ) {
    var file1 = padFileManagementService.getUploadedFiles(pwaApplicationDetail1, fileDocumentType).getFirst();
    var file2 = padFileManagementService.getUploadedFiles(pwaApplicationDetail2, fileDocumentType).getFirst();

    assertThat(file1)
        .extracting(
            UploadedFile::getName,
            UploadedFile::getDescription,
            UploadedFile::getDocumentType
        ).containsExactly(
            file2.getName(),
            file2.getDescription(),
            file2.getDocumentType()
        );
  }

  private void assertPadFileDetailsMatch(PadFile lhs, PadFile rhs) {
    assertThat(lhs)
        .extracting(
            PadFile_.DESCRIPTION,
            PadFile_.FILE_LINK_STATUS,
            PadFile_.PURPOSE)
        .containsExactly(
            rhs.getDescription(),
            rhs.getFileLinkStatus(),
            rhs.getPurpose());

    // Test that the copied pad file points to the file id of the copied file instead of the original.
    assertThat(lhs).extracting(PadFile_.FILE_ID).isNotEqualTo(rhs.getFileId());
  }

  @Transactional
  @Test
  void createNewApplicationVersion_allPadFilesMappedAsExpected() throws IllegalAccessException {
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
  void createNewApplicationVersion_allPipelineDataMappedAsExpected() throws IllegalAccessException {
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
  void createNewApplicationVersion_huooRoleLinksMappedAsExpected() throws IllegalAccessException {
    setup(PwaApplicationType.OPTIONS_VARIATION);

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
  void createNewApplicationVersion_permanentDepositCopiedAsExpected() throws IllegalAccessException {
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

    assertThat(firstVersionApplicationContainer.getPadDepositPipeline().getPipeline().getPipelineId())
        .isEqualTo(newVersionContainer.getPadDepositPipeline().getPipeline().getPipelineId());

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
  void createNewApplicationVersion_campaignWorkScheduleCopiedAsExpected() throws IllegalAccessException {
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
  void createNewApplicationVersion_padFieldsCopiedAsExpected() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    var v1ManualField = firstVersionApplicationContainer.getPadFields().stream()
        .filter(f -> f.getAreaName() != null)
        .findFirst().orElseThrow(() -> new RuntimeException("Expected to find manual field"));

    var v2ManualField = newVersionContainer.getPadFields().stream()
        .filter(f -> f.getAreaName() != null)
        .findFirst().orElseThrow(() -> new RuntimeException("Expected to find manual field"));

    var v1DevukField = firstVersionApplicationContainer.getPadFields().stream()
        .filter(f -> f.getDevukField() != null)
        .findFirst().orElseThrow(() -> new RuntimeException("Expected to find devuk field"));

    var v2DevukField = newVersionContainer.getPadFields().stream()
        .filter(f -> f.getDevukField() != null)
        .findFirst().orElseThrow(() -> new RuntimeException("Expected to find devuk field"));

    ObjectTestUtils.assertValuesEqual(v1ManualField, v2ManualField,
        Set.of(PadLinkedArea_.ID, PadLinkedArea_.PWA_APPLICATION_DETAIL));

    ObjectTestUtils.assertValuesEqual(v1DevukField, v2DevukField,
        Set.of(PadLinkedArea_.ID, PadLinkedArea_.PWA_APPLICATION_DETAIL));

  }

  @Transactional
  @Test
  void createNewApplicationVersion_padEnvDecomCopiedAsExpected() throws IllegalAccessException {
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
  void createNewApplicationVersion_otherPipelineDiagrams() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    assertUploadedFileDetailsMatch(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        newVersionDetail,
        FileDocumentType.UMBILICAL_CROSS_SECTION
    );

    assertUploadedFileDetailsMatch(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        newVersionDetail,
        FileDocumentType.ADMIRALTY_CHART
    );

  }

  @Transactional
  @Test
  void createNewApplicationVersion_partnerLetters() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    assertUploadedFileDetailsMatch(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        newVersionDetail,
        FileDocumentType.PARTNER_LETTERS
    );
  }

  @Transactional
  @Test
  void createNewApplicationVersion_optionVariationDocuments() throws IllegalAccessException {
    setup(PwaApplicationType.OPTIONS_VARIATION);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    assertUploadedFileDetailsMatch(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        newVersionDetail,
        FileDocumentType.OPTIONS_TEMPLATE
    );

    assertUploadedFileDetailsMatch(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        newVersionDetail,
        FileDocumentType.SUPPLEMENTARY_DOCUMENTS
    );
  }

  @Transactional
  @Test
  void createNewApplicationVersion_locationDetailsCopedAsExpected() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getPadLocationDetails(),
        newVersionContainer.getPadLocationDetails(),
        Set.of(PadLocationDetails_.ID, PadLocationDetails_.PWA_APPLICATION_DETAIL)
    );

    var manualFacilityV1 = firstVersionApplicationContainer.getPadFacilities()
        .stream()
        .filter(o -> o.getFacilityNameManualEntry() != null)
        .findFirst().orElse(null);
    var manualFacilityV2 = newVersionContainer.getPadFacilities()
        .stream()
        .filter(o -> o.getFacilityNameManualEntry() != null)
        .findFirst().orElse(null);

    var devukFacilityV1 = firstVersionApplicationContainer.getPadFacilities()
        .stream()
        .filter(o -> o.getFacility() != null)
        .findFirst().orElse(null);
    var devukFacilityV2 = newVersionContainer.getPadFacilities()
        .stream()
        .filter(o -> o.getFacility() != null)
        .findFirst().orElse(null);

    ObjectTestUtils.assertValuesEqual(
        manualFacilityV1,
        manualFacilityV2,
        Set.of(PadFacility_.ID, PadFacility_.PWA_APPLICATION_DETAIL)
    );

    ObjectTestUtils.assertValuesEqual(
        devukFacilityV1,
        devukFacilityV2,
        Set.of(PadFacility_.ID, PadFacility_.PWA_APPLICATION_DETAIL)
    );

    assertUploadedFileDetailsMatch(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        newVersionDetail,
        FileDocumentType.LOCATION_DETAILS
    );
  }

  @Transactional
  @Test
  void createNewApplicationVersion_locationDetails_licenceBlockCrossings() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    assertUploadedFileDetailsMatch(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        newVersionDetail,
        FileDocumentType.BLOCK_CROSSINGS
    );

    var unlicensedCrossedBlockOwnerV1 = firstVersionApplicationContainer.getPadCrossedBlockOwners()
        .stream()
        .filter(crossedBlockOwner -> crossedBlockOwner.getPadCrossedBlock().getLicence() == null)
        .findFirst().orElse(null);
    var unlicensedCrossedBlockOwnerV2 = newVersionContainer.getPadCrossedBlockOwners()
        .stream()
        .filter(crossedBlockOwner -> crossedBlockOwner.getPadCrossedBlock().getLicence() == null)
        .findFirst().orElse(null);

    var licensedCrossedBlockOwnerV1 = firstVersionApplicationContainer.getPadCrossedBlockOwners()
        .stream()
        .filter(crossedBlockOwner -> crossedBlockOwner.getPadCrossedBlock().getLicence() != null)
        .findFirst().orElse(null);
    var licensedCrossedBlockOwnerV2 = newVersionContainer.getPadCrossedBlockOwners()
        .stream()
        .filter(crossedBlockOwner -> crossedBlockOwner.getPadCrossedBlock().getLicence() != null)
        .findFirst().orElse(null);

    // test unlicensed crossed block
    ObjectTestUtils.assertValuesEqual(
        unlicensedCrossedBlockOwnerV1,
        unlicensedCrossedBlockOwnerV2,
        Set.of(PadCrossedBlockOwner_.ID, PadCrossedBlockOwner_.PAD_CROSSED_BLOCK));

    ObjectTestUtils.assertValuesEqual(
        unlicensedCrossedBlockOwnerV1.getPadCrossedBlock(),
        unlicensedCrossedBlockOwnerV2.getPadCrossedBlock(),
        Set.of(PadCrossedBlock_.ID, PadCrossedBlock_.PWA_APPLICATION_DETAIL));

    // test licensed crossed block
    ObjectTestUtils.assertValuesEqual(
        licensedCrossedBlockOwnerV1,
        licensedCrossedBlockOwnerV2,
        Set.of(PadCrossedBlockOwner_.ID, PadCrossedBlockOwner_.PAD_CROSSED_BLOCK));

    ObjectTestUtils.assertValuesEqual(
        licensedCrossedBlockOwnerV1.getPadCrossedBlock(),
        licensedCrossedBlockOwnerV2.getPadCrossedBlock(),
        Set.of(PadCrossedBlock_.ID, PadCrossedBlock_.PWA_APPLICATION_DETAIL));
  }

  @Transactional
  @Test
  void createNewApplicationVersion_locationDetails_cableCrossings() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    assertUploadedFileDetailsMatch(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        newVersionDetail,
        FileDocumentType.CABLE_CROSSINGS
    );

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getPadCableCrossing(),
        newVersionContainer.getPadCableCrossing(),
        Set.of(PadCableCrossing_.ID, PadCableCrossing_.PWA_APPLICATION_DETAIL));
  }

  @Transactional
  @Test
  void createNewApplicationVersion_locationDetails_pipelineCrossings() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    assertUploadedFileDetailsMatch(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        newVersionDetail,
        FileDocumentType.PIPELINE_CROSSINGS
    );

    var portalOrgOwnerV1 = firstVersionApplicationContainer.getPadPipelineCrossingOwners()
        .stream()
        .filter(pipelineCrossingOwner -> pipelineCrossingOwner.getOrganisationUnit() != null)
        .findFirst().orElse(null);
    var portalOrgOwnerV2 = newVersionContainer.getPadPipelineCrossingOwners()
        .stream()
        .filter(pipelineCrossingOwner -> pipelineCrossingOwner.getOrganisationUnit() != null)
        .findFirst().orElse(null);

    var manualOwnerV1 = firstVersionApplicationContainer.getPadPipelineCrossingOwners()
        .stream()
        .filter(pipelineCrossingOwner -> pipelineCrossingOwner.getManualOrganisationEntry() != null)
        .findFirst().orElse(null);
    var manualOwnerV2 = newVersionContainer.getPadPipelineCrossingOwners()
        .stream()
        .filter(pipelineCrossingOwner -> pipelineCrossingOwner.getManualOrganisationEntry() != null)
        .findFirst().orElse(null);

    // test portal org owner
    ObjectTestUtils.assertValuesEqual(
        portalOrgOwnerV1,
        portalOrgOwnerV2,
        Set.of(PadPipelineCrossingOwner_.ID, PadPipelineCrossingOwner_.PAD_PIPELINE_CROSSING));

    ObjectTestUtils.assertValuesEqual(
        portalOrgOwnerV1.getPadPipelineCrossing(),
        portalOrgOwnerV2.getPadPipelineCrossing(),
        Set.of(PadPipelineCrossing_.ID, PadPipelineCrossing_.PWA_APPLICATION_DETAIL));

    // test manual org owner
    ObjectTestUtils.assertValuesEqual(
        manualOwnerV1,
        manualOwnerV2,
        Set.of(PadPipelineCrossingOwner_.ID, PadPipelineCrossingOwner_.PAD_PIPELINE_CROSSING));

    ObjectTestUtils.assertValuesEqual(
        manualOwnerV1.getPadPipelineCrossing(),
        manualOwnerV2.getPadPipelineCrossing(),
        Set.of(PadPipelineCrossing_.ID, PadPipelineCrossing_.PWA_APPLICATION_DETAIL));
  }

  @Transactional
  @Test
  void createNewApplicationVersion_locationDetails_medianLineCrossing() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    assertUploadedFileDetailsMatch(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        newVersionDetail,
        FileDocumentType.MEDIAN_LINE_CROSSING
    );

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getPadMedianLineAgreement(),
        newVersionContainer.getPadMedianLineAgreement(),
        Set.of(PadMedianLineAgreement_.ID, PadMedianLineAgreement_.PWA_APPLICATION_DETAIL));
  }

  @Transactional
  @Test
  void createNewApplicationVersion_techDetails_generalTechDetails() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getPadPipelineTechInfo(),
        newVersionContainer.getPadPipelineTechInfo(),
        Set.of(PadPipelineTechInfo_.ID, PadPipelineTechInfo_.PWA_APPLICATION_DETAIL));
  }

  @Transactional
  @Test
  void createNewApplicationVersion_techDetails_fluidComposition() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    Chemical.getAll().forEach(chemical ->
        ObjectTestUtils.assertValuesEqual(
            firstVersionApplicationContainer.getPadFluidCompositionForChemical(chemical),
            newVersionContainer.getPadFluidCompositionForChemical(chemical),
            Set.of(PadFluidCompositionInfo_.ID, PadPipelineTechInfo_.PWA_APPLICATION_DETAIL))
    );
  }

  @Transactional
  @Test
  void createNewApplicationVersion_techDetails_otherProperties() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    firstVersionApplicationContainer.getPwaApplicationDetail().setOtherPhaseDescription("Other Phase Description");
    firstVersionApplicationContainer.getPwaApplicationDetail().setPipelinePhaseProperties(
        Set.of(PropertyPhase.OIL, PropertyPhase.GAS)
    );

    entityManager.persist(firstVersionApplicationContainer.getPwaApplicationDetail());

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    assertThat(firstVersionApplicationContainer.getPwaApplicationDetail().getOtherPhaseDescription())
        .isEqualTo(newVersionContainer.getPwaApplicationDetail().getOtherPhaseDescription());

    assertThat(firstVersionApplicationContainer.getPwaApplicationDetail().getPipelinePhaseProperties())
        .isEqualTo(newVersionContainer.getPwaApplicationDetail().getPipelinePhaseProperties());

    OtherPipelineProperty.asList().forEach(otherPipelineProperty ->
        ObjectTestUtils.assertValuesEqual(
            firstVersionApplicationContainer.getPadPipelineOtherProperty(otherPipelineProperty),
            newVersionContainer.getPadPipelineOtherProperty(otherPipelineProperty),
            Set.of(PadPipelineOtherProperties_.ID, PadPipelineOtherProperties_.PWA_APPLICATION_DETAIL))
    );
  }

  @Transactional
  @Test
  void createNewApplicationVersion_techDetails_designOpConditions() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL);

    entityManager.persist(firstVersionApplicationContainer.getPwaApplicationDetail());

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getPadDesignOpConditions(),
        newVersionContainer.getPadDesignOpConditions(),
        Set.of(PadDesignOpConditions_.ID, PadDesignOpConditions_.PWA_APPLICATION_DETAIL)
    );

  }

  @Transactional
  @Test
  void createNewApplicationVersion_fastTrack() throws IllegalAccessException {
    setup(PwaApplicationType.INITIAL, true);

    entityManager.persist(firstVersionApplicationContainer.getPwaApplicationDetail());

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getPadFastTrack(),
        newVersionContainer.getPadFastTrack(),
        Set.of(PadFastTrack_.ID, PadFastTrack_.PWA_APPLICATION_DETAIL)
    );

  }

  @Transactional
  @Test
  void createNewApplicationVersion_padConfirmationOfOption() throws IllegalAccessException {
    setup(PwaApplicationType.OPTIONS_VARIATION, true);

    entityManager.persist(firstVersionApplicationContainer.getPwaApplicationDetail());

    var newVersionDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        firstVersionApplicationContainer.getPwaApplicationDetail(),
        webUserAccount
    );

    var newVersionContainer = testHelper.getApplicationDetailContainer(newVersionDetail);

    ObjectTestUtils.assertValuesEqual(
        firstVersionApplicationContainer.getPadConfirmationOfOption(),
        newVersionContainer.getPadConfirmationOfOption(),
        Set.of(PadConfirmationOfOption_.ID, PadConfirmationOfOption_.PWA_APPLICATION_DETAIL)
    );

  }
}
