package uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingLinkService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentDataService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.CopiedEntityIdTuple;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PadPipelineDataCopierServiceTest {

  @Mock
  private PadPipelineIdentService padPipelineIdentService;

  @Mock
  private PadPipelineIdentDataService padPipelineIdentDataService;

  @Mock
  private PadTechnicalDrawingService padTechnicalDrawingService;

  @Mock
  private PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;

  @Mock
  private PadFileService padFileService;

  @Mock
  private EntityCopyingService entityCopyingService;

  @Mock
  private EntityManager entityManager;

  @Mock
  private PadFileManagementService padFileManagementService;

  private PadPipelineDataCopierService padPipelineDataCopierService;
  private PwaApplicationDetail fromDetail;

  @BeforeEach
  void setUp() {
    padPipelineDataCopierService = new PadPipelineDataCopierService(
        padPipelineIdentService,
        padPipelineIdentDataService,
        padTechnicalDrawingService,
        padTechnicalDrawingLinkService,
        padFileService,
        entityCopyingService,
        entityManager,
        padFileManagementService
    );

    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    fromDetail = new PwaApplicationDetail(pwaApplication, null, null, null);
  }

  @Test
  void copyAllPadPipelineData_alreadyCopied_isNoOp() {
    var toDetail = new PwaApplicationDetail(new PwaApplication(null, PwaApplicationType.INITIAL, null), null, null, null);

    when(padTechnicalDrawingService.getDrawings(toDetail)).thenReturn(List.of(new PadTechnicalDrawing()));

    padPipelineDataCopierService.copyAllPadPipelineData(fromDetail, toDetail, List::of);

    verify(padTechnicalDrawingService, times(1)).getDrawings(toDetail);
    verifyNoMoreInteractions(padTechnicalDrawingService);
    verifyNoInteractions(
        entityCopyingService,
        padPipelineIdentService,
        padPipelineIdentDataService,
        padTechnicalDrawingLinkService,
        padFileService,
        padFileManagementService,
        entityManager
    );
  }

  @Test
  void copyAllPadPipelineData_copiesDrawingsAndRepointsFileLinkAtCopiedFile() {
    var toDetail = new PwaApplicationDetail(new PwaApplication(null, PwaApplicationType.INITIAL, null), null, null, null);
    toDetail.setId(2);

    var originalFileId = UUID.randomUUID();

    // the pad_files bridge row is reflection-copied verbatim by EntityCopyingService, so it initially
    // carries the OLD fileId until copyPipelineDrawingData re-points it at the newly copied file.
    var copiedPadFile = new PadFile();
    copiedPadFile.setId(200);
    copiedPadFile.setFileId(originalFileId);

    var copiedDrawing = new PadTechnicalDrawing();
    copiedDrawing.setId(20);
    copiedDrawing.setPwaApplicationDetail(toDetail);
    // reflection-copied verbatim: still references the original PadFile (id 100) until repointed below.
    var originalPadFileReference = new PadFile();
    originalPadFileReference.setId(100);
    copiedDrawing.setFile(originalPadFileReference);

    @SuppressWarnings("unchecked")
    var padFileCopyTuple = (CopiedEntityIdTuple<Integer, PadFile>) mock(CopiedEntityIdTuple.class);

    when(entityCopyingService.duplicateEntitiesAndSetParent(any(), eq(toDetail), eq(PadPipeline.class)))
        .thenReturn(Set.of());
    when(entityCopyingService.duplicateEntitiesAndSetParentFromCopiedEntities(any(), eq(Set.of()), any()))
        .thenReturn(Set.of());
    when(entityCopyingService.duplicateEntitiesAndSetParent(any(), eq(toDetail), eq(PadTechnicalDrawing.class)))
        .thenReturn(Set.of());
    when(entityCopyingService.createMapOfOriginalIdToNewEntityReference(eq(Set.of())))
        .thenReturn(Map.of());
    when(entityCopyingService.createMapOfOriginalIdToNewEntityReference(eq(Set.of(padFileCopyTuple))))
        .thenReturn(Map.of(100, copiedPadFile));

    // not yet copied, so the guard lets the method proceed; second call (post file-relinking) returns the copy.
    when(padTechnicalDrawingService.getDrawings(toDetail)).thenReturn(List.of(), List.of(copiedDrawing));
    when(padTechnicalDrawingLinkService.getLinksFromAppDetail(toDetail)).thenReturn(List.of());

    when(padFileService.copyPadFilesToPwaApplicationDetail(
        fromDetail, toDetail, ApplicationDetailFilePurpose.PIPELINE_DRAWINGS, ApplicationFileLinkStatus.FULL))
        .thenReturn(Set.of(padFileCopyTuple));

    var copiedFileId = UUID.randomUUID();
    when(padFileManagementService.copyUploadedFiles(fromDetail, toDetail, FileDocumentType.PIPELINE_DRAWINGS))
        .thenReturn(Map.of(originalFileId, copiedFileId));

    padPipelineDataCopierService.copyAllPadPipelineData(fromDetail, toDetail, List::of);

    // the copied drawing must end up pointing at the copied PadFile bridge row...
    assertThat(copiedDrawing.getFile()).isEqualTo(copiedPadFile);
    // ...whose fileId has been re-pointed at the newly copied file, not the original one it was cloned with.
    assertThat(copiedPadFile.getFileId()).isEqualTo(copiedFileId);

    verify(entityManager, times(1)).persist(copiedPadFile);
    verify(entityManager, times(1)).persist(copiedDrawing);
  }
}
