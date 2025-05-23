package uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist;

import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingLinkService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentDataService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.CopiedEntityIdTuple;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;

@Service
public class PadPipelineDataCopierService {

  private final PadPipelineIdentService padPipelineIdentService;

  private final PadPipelineIdentDataService padPipelineIdentDataService;

  private final PadTechnicalDrawingService padTechnicalDrawingService;

  private final PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;

  private final PadFileService padFileService;

  private final EntityCopyingService entityCopyingService;

  private final EntityManager entityManager;

  private final PadFileManagementService padFileManagementService;

  public PadPipelineDataCopierService(
      PadPipelineIdentService padPipelineIdentService,
      PadPipelineIdentDataService padPipelineIdentDataService,

      // In an ideal world, this would not rely on PadPipeline service at all. Structural fix would be to only deal with
      // form mappings in controllers and only deal with entity objects business logic code.
      // Moving all mapping to controller is too large a refactor to do now.
      @Lazy PadTechnicalDrawingService padTechnicalDrawingService,

      // Also in an ideal world, this would not rely on PadPipeline service at all. Structural fix would be to only deal with
      // form mappings in controllers  and only deal with entity objects business logic code.
      // Removing the dependency from this service ends up with the link being in PadTechnicalDrawingService,
      // which does not resolve the circular dependency and no time for large refactor.
      @Lazy PadTechnicalDrawingLinkService padTechnicalDrawingLinkService,
      PadFileService padFileService, EntityCopyingService entityCopyingService,
      EntityManager entityManager,
      PadFileManagementService padFileManagementService
  ) {
    this.padPipelineIdentService = padPipelineIdentService;
    this.padPipelineIdentDataService = padPipelineIdentDataService;
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.padTechnicalDrawingLinkService = padTechnicalDrawingLinkService;
    this.padFileService = padFileService;
    this.entityCopyingService = entityCopyingService;
    this.entityManager = entityManager;
    this.padFileManagementService = padFileManagementService;
  }


  /**
   * Copy all form data directly linked to PadPipelines.
   */
  void copyAllPadPipelineData(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail,
                              Supplier<Collection<PadPipeline>> fromDetailPadPipelines) {

    Set<CopiedEntityIdTuple<Integer, PadPipeline>> copiedPadPipelineEntityIds = copyPipelineData(
        fromDetail,
        toDetail,
        fromDetailPadPipelines
    );

    copyPipelineDrawingData(fromDetail, toDetail, copiedPadPipelineEntityIds);
  }


  private Set<CopiedEntityIdTuple<Integer, PadPipeline>> copyPipelineData(PwaApplicationDetail fromDetail,
                                                                          PwaApplicationDetail toDetail,
                                                                          Supplier<Collection<PadPipeline>> fromDetailPadPipelines) {
    Set<CopiedEntityIdTuple<Integer, PadPipeline>> copiedPadPipelineEntityIds = entityCopyingService
        .duplicateEntitiesAndSetParent(
            fromDetailPadPipelines,
            toDetail,
            PadPipeline.class
        );

    Set<CopiedEntityIdTuple<Integer, PadPipelineIdent>> copiedPadPipelineIdentEntityIds = entityCopyingService
        .duplicateEntitiesAndSetParentFromCopiedEntities(
            () -> padPipelineIdentService.getAllByPwaApplicationDetail(fromDetail),
            copiedPadPipelineEntityIds,
            PadPipelineIdent.class
        );

    Set<CopiedEntityIdTuple<Integer, PadPipelineIdentData>> copiedPadPipelineIdentDataEntityIds = entityCopyingService
        .duplicateEntitiesAndSetParentFromCopiedEntities(
            () -> padPipelineIdentDataService.getAllPipelineIdentDataForPwaApplicationDetail(fromDetail),
            copiedPadPipelineIdentEntityIds,
            PadPipelineIdentData.class
        );

    return copiedPadPipelineEntityIds;
  }

  private void copyPipelineDrawingData(PwaApplicationDetail fromDetail,
                                       PwaApplicationDetail toDetail,
                                       Set<CopiedEntityIdTuple<Integer, PadPipeline>> copiedPadPipelineEntityIds) {

    //1. Copy the root pad file links and create lookup of old id to new PadFile
    Map<Integer, PadFile> originalPadFileIdToDuplicateEntityReference = entityCopyingService
        .createMapOfOriginalIdToNewEntityReference(
            padFileService.copyPadFilesToPwaApplicationDetail(
                fromDetail,
                toDetail,
                ApplicationDetailFilePurpose.PIPELINE_DRAWINGS,
                ApplicationFileLinkStatus.FULL
            )
        );

    // 2. Create lookup of old PadPipeline Id to new PadPipeline
    Map<Integer, PadPipeline> originalPadPipelineIdToDuplicateEntityReference = entityCopyingService
        .createMapOfOriginalIdToNewEntityReference(copiedPadPipelineEntityIds);

    // 3. duplicate PadTechicalDrawing link for detail
    Set<CopiedEntityIdTuple<Integer, PadTechnicalDrawing>> copiedPadTechnicalDrawingEntityIds = entityCopyingService
        .duplicateEntitiesAndSetParent(
            () -> padTechnicalDrawingService.getDrawings(fromDetail),
            toDetail,
            PadTechnicalDrawing.class
        );

    // 4. copy uploaded files to new application detail
    padFileManagementService.copyUploadedFiles(fromDetail, toDetail, FileDocumentType.PIPELINE_DRAWINGS);

    var fromFiles = padFileManagementService.getUploadedFiles(fromDetail, FileDocumentType.PIPELINE_DRAWINGS).stream()
        .collect(Collectors.toMap(UploadedFile::getName, Function.identity()));

    var toFiles = padFileManagementService.getUploadedFiles(toDetail, FileDocumentType.PIPELINE_DRAWINGS).stream()
        .collect(Collectors.toMap(UploadedFile::getName, Function.identity()));

    var originalToCopiedFileIDs = fromFiles.entrySet().stream()
        .collect(Collectors.toMap(e -> e.getValue().getId(), e -> toFiles.get(e.getKey()).getId()));

    // 5. manually set the drawing PadFile links as they still point to previous detail's files.
    var duplicatedTechnicalDrawings = padTechnicalDrawingService.getDrawings(toDetail);

    duplicatedTechnicalDrawings.forEach(padTechnicalDrawing -> {
          // point the new pad file to the new file id
          var fileWithOldId = originalPadFileIdToDuplicateEntityReference.get(padTechnicalDrawing.getFile().getId());
          fileWithOldId.setFileId(originalToCopiedFileIDs.get(fileWithOldId.getFileId()));

          entityManager.persist(fileWithOldId);

          padTechnicalDrawing.setFile(
              fileWithOldId
          );
          entityManager.persist(padTechnicalDrawing);
        }
    );

    // 6. Duplicate the PadTechnicalDrawingLinks and reparent to the new technical drawings
    Set<CopiedEntityIdTuple<Integer, PadTechnicalDrawingLink>> copiedPadTechnicalDrawingLinkEntityIds =
        entityCopyingService.duplicateEntitiesAndSetParentFromCopiedEntities(
            () -> padTechnicalDrawingLinkService.getLinksFromAppDetail(fromDetail),
            copiedPadTechnicalDrawingEntityIds,
            PadTechnicalDrawingLink.class
        );

    // 7. Manually re-point the links to the duplicated PadPipeline as this is still looking at the previous detail's padPipeline.
    var duplicatedTechnicalDrawingLinks = padTechnicalDrawingLinkService.getLinksFromAppDetail(toDetail);
    duplicatedTechnicalDrawingLinks.forEach(padTechnicalDrawingLink -> {
      padTechnicalDrawingLink.setPipeline(
          originalPadPipelineIdToDuplicateEntityReference.get(padTechnicalDrawingLink.getPipeline().getId())
      );
      entityManager.persist(padTechnicalDrawingLink);
    });

  }

}
