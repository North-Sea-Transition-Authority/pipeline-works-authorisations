package uk.co.ogauthority.pwa.service.documents.generation;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.DepositDrawingsService;

@RunWith(MockitoJUnitRunner.class)
public class DepositDrawingsGeneratorServiceTest {

  @Mock
  private DepositDrawingsService depositDrawingsService;

  @Mock
  private ConsentDocumentImageService consentDocumentImageService;

  private DepositDrawingsGeneratorService depositDrawingsGeneratorService;

  private PadDepositDrawing drawing1;
  private PadDepositDrawing drawing2;

  private PwaApplicationDetail detail = new PwaApplicationDetail();

  @Before
  public void setUp() throws Exception {

    depositDrawingsGeneratorService = new DepositDrawingsGeneratorService(depositDrawingsService, consentDocumentImageService);

    drawing1 = new PadDepositDrawing();
    drawing1.setReference("drawing1Ref");
    drawing1.setFile(new PadFile(detail, "id1", ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, ApplicationFileLinkStatus.FULL));

    drawing2 = new PadDepositDrawing();
    drawing2.setReference("drawing2Ref");
    drawing2.setFile(new PadFile(detail, "id2", ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, ApplicationFileLinkStatus.FULL));

    when(depositDrawingsService.getAllDepositDrawingsForDetail(any())).thenReturn(List.of(drawing1, drawing2));

    when(consentDocumentImageService.convertFilesToImageSourceMap(Set.of("id1", "id2")))
        .thenReturn(Map.of(
            "id1", "file1Uri",
            "id2", "file2Uri"));

  }

  @Test
  public void getDocumentSectionData() {

    var docSectionData = depositDrawingsGeneratorService.getDocumentSectionData(detail);

    verify(consentDocumentImageService, times(1)).convertFilesToImageSourceMap(Set.of("id1", "id2"));

    assertThat(docSectionData.getTemplatePath()).isEqualTo("documents/consents/sections/depositDrawings.ftl");
    assertThat(docSectionData.getTemplateModel()).containsOnly(
        entry("sectionName", DocumentSection.DEPOSIT_DRAWINGS.getDisplayName()),
        entry("drawingRefToFileIdMap", Map.of("drawing1Ref", "id1", "drawing2Ref", "id2")),
        entry("fileIdToImgSourceMap", Map.of("id1", "file1Uri", "id2", "file2Uri"))
    );

  }
}