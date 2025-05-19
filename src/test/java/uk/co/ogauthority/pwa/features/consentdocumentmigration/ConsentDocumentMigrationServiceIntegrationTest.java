package uk.co.ogauthority.pwa.features.consentdocumentmigration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.fivium.fileuploadlibrary.s3.S3Exception;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.filemanagement.s3.PwaS3FileService;
import uk.co.ogauthority.pwa.features.filemanagement.s3.S3File;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@ActiveProfiles(profiles = {"devtools", "integration-test"})
@ExtendWith(MockitoExtension.class)
class ConsentDocumentMigrationServiceIntegrationTest {

  @Autowired
  private ConsentDocumentMigrationService consentDocumentMigrationService;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private DocumentMigrationRecordRepository documentMigrationRecordRepository;

  @MockBean
  private PwaS3FileService pwaS3FileService;

  @MockBean
  private FileService fileService;

  private List<S3File> s3Files;

  void setUp() throws S3Exception, FileNotFoundException {
    var firstVersionPwaDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL
    );

    var pwaApplication = firstVersionPwaDetail.getPwaApplication();
    var masterPwa = pwaApplication.getMasterPwa();
    masterPwa.setId(null);
    entityManager.persist(masterPwa);
    pwaApplication.setId(null);
    entityManager.persist(pwaApplication);
    firstVersionPwaDetail.setId(null);
    entityManager.persist(firstVersionPwaDetail);

    var masterPwaDetail = new MasterPwaDetail();
    masterPwaDetail.setId(null);
    masterPwaDetail.setMasterPwa(masterPwa);
    masterPwaDetail.setReference("1/W/25");
    masterPwaDetail.setMasterPwaDetailStatus(MasterPwaDetailStatus.CONSENTED);
    entityManager.persist(masterPwaDetail);

    var pwaConsent = PwaConsentTestUtil.createInitial(masterPwa);
    pwaConsent.setReference("1/W/25");
    entityManager.persist(pwaConsent);

    s3Files = List.of(
        new S3File("Howard (1-W-25) PWA Consent Document (1-W-25).pdf", 1L),
        new S3File("Howard (1-W-25) Variation Consent Document (1-V-25).pdf", 1L),
        new S3File("Howard (1-W-25) HUOO Variation Consent Document (2-V-25).pdf", 1L),
        new S3File("Howard (1-W-25) Decommissioning Consent Document (3-V-25).pdf", 1L),
        new S3File("Howard (1-W-25) Deposit Consent Document (1-D-25).pdf", 1L)
    );

    when(pwaS3FileService.listS3Files("pwa-migration")).thenReturn(s3Files);

    var fileInputStream = new FileInputStream(ResourceUtils.getFile("classpath:legacy-consent-migration-test.csv"));

    when(pwaS3FileService.downloadFile("pwa-migration", "legacy-consent-migration-test.csv")).thenReturn(fileInputStream);
  }

  @Test
  @Transactional
  void verify() throws S3Exception, IOException{
    setUp();

    consentDocumentMigrationService.verify();

    assertThat(documentMigrationRecordRepository.findAll())
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
        .isEqualTo(createDocumentMigrationRecordsPreMigration());
  }

  @Test
  @Transactional
  void migrate() throws S3Exception, IOException {
    setUp();

    when(pwaS3FileService.downloadFile(eq("pwa-migration"), anyString())).thenReturn(new ByteArrayInputStream(new byte[1]));

    var fileUploadResponse = mock(FileUploadResponse.class);

    when(fileService.upload(any())).thenReturn(fileUploadResponse);
    when(fileUploadResponse.getError()).thenReturn(null);
    when(fileUploadResponse.getFileId()).thenReturn(UUID.randomUUID());

    documentMigrationRecordRepository.saveAll(createDocumentMigrationRecordsPreMigration());

    consentDocumentMigrationService.migrate();

    assertThat(documentMigrationRecordRepository.findAll())
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
        .isEqualTo(createDocumentMigrationRecordsPostMigration());
  }

  private List<DocumentMigrationRecord> createDocumentMigrationRecordsPreMigration() {
    return createDocumentMigrationRecords(false);
  }

  private List<DocumentMigrationRecord> createDocumentMigrationRecordsPostMigration() {
    return createDocumentMigrationRecords(true);
  }

  private List<DocumentMigrationRecord> createDocumentMigrationRecords(boolean hasBeenMigrated) {
    var initialRecord1 = new DocumentMigrationRecord();
    initialRecord1.setFilename("Howard (1-W-25) PWA Consent Document (1-W-25).pdf");
    initialRecord1.setPwaReference("1/W/25");
    initialRecord1.setFieldName("Howard");
    initialRecord1.setConsentDoc("1/W/25");
    initialRecord1.setConsentDate("2025-05-08");
    initialRecord1.setConsentType("PWA");
    initialRecord1.setIncorrectPwaReference("");
    initialRecord1.setAction("Upload");
    initialRecord1.setFileLocated(true);
    initialRecord1.setDestinationRecordExists(true);
    initialRecord1.setMigrationSuccessful(hasBeenMigrated);

    var variationRecord = new DocumentMigrationRecord();
    variationRecord.setFilename("Howard (1-W-25) Variation Consent Document (1-V-25).pdf");
    variationRecord.setPwaReference("1/W/25");
    variationRecord.setFieldName("Howard");
    variationRecord.setConsentDoc("1/V/25");
    variationRecord.setConsentDate("2025-05-09");
    variationRecord.setConsentType("Variation");
    variationRecord.setIncorrectPwaReference("");
    variationRecord.setAction("Record");
    variationRecord.setFileLocated(true);
    variationRecord.setDestinationRecordExists(hasBeenMigrated);
    variationRecord.setMigrationSuccessful(hasBeenMigrated);

    var huooVariationRecord = new DocumentMigrationRecord();
    huooVariationRecord.setFilename("Howard (1-W-25) HUOO Variation Consent Document (2-V-25).pdf");
    huooVariationRecord.setPwaReference("1/W/25");
    huooVariationRecord.setFieldName("Howard");
    huooVariationRecord.setConsentDoc("2/V/25");
    huooVariationRecord.setConsentDate("2025-05-10");
    huooVariationRecord.setConsentType("HUOO Variation");
    huooVariationRecord.setIncorrectPwaReference("");
    huooVariationRecord.setAction("Record");
    huooVariationRecord.setFileLocated(true);
    huooVariationRecord.setDestinationRecordExists(hasBeenMigrated);
    huooVariationRecord.setMigrationSuccessful(hasBeenMigrated);

    var depositRecord = new DocumentMigrationRecord();
    depositRecord.setFilename("Howard (1-W-25) Deposit Consent Document (1-D-25).pdf");
    depositRecord.setPwaReference("1/W/25");
    depositRecord.setFieldName("Howard");
    depositRecord.setConsentDoc("1/D/25");
    depositRecord.setConsentDate("2025-05-11");
    depositRecord.setConsentType("Deposit");
    depositRecord.setIncorrectPwaReference("");
    depositRecord.setAction("Record");
    depositRecord.setFileLocated(true);
    depositRecord.setDestinationRecordExists(hasBeenMigrated);
    depositRecord.setMigrationSuccessful(hasBeenMigrated);

    var decommissioningRecord = new DocumentMigrationRecord();
    decommissioningRecord.setFilename("Howard (1-W-25) Decommissioning Consent Document (3-V-25).pdf");
    decommissioningRecord.setPwaReference("1/W/25");
    decommissioningRecord.setFieldName("Howard");
    decommissioningRecord.setConsentDoc("3/V/25");
    decommissioningRecord.setConsentDate("2025-05-12");
    decommissioningRecord.setConsentType("Decommissioning");
    decommissioningRecord.setIncorrectPwaReference("");
    decommissioningRecord.setAction("Record");
    decommissioningRecord.setFileLocated(true);
    decommissioningRecord.setDestinationRecordExists(hasBeenMigrated);
    decommissioningRecord.setMigrationSuccessful(hasBeenMigrated);

    var initialRecord2 = new DocumentMigrationRecord();
    initialRecord2.setFilename(null);
    initialRecord2.setPwaReference("2/W/25");
    initialRecord2.setFieldName("Jesse");
    initialRecord2.setConsentDoc("2/W/25");
    initialRecord2.setConsentDate("2025-05-09");
    initialRecord2.setConsentType("PWA");
    initialRecord2.setIncorrectPwaReference("");
    initialRecord2.setAction("Upload");
    initialRecord2.setFileLocated(false);
    initialRecord2.setDestinationRecordExists(false);
    initialRecord2.setMigrationSuccessful(false);

    return List.of(
        initialRecord1,
        variationRecord,
        huooVariationRecord,
        depositRecord,
        decommissioningRecord,
        initialRecord2
    );
  }

}