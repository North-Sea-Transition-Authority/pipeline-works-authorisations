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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.DepositDrawingsService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadDepositDrawing;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@RunWith(MockitoJUnitRunner.class)
public class DepositDrawingsGeneratorServiceTest {

  @Mock
  private DepositDrawingsService depositDrawingsService;

  @Mock
  private PermanentDepositService permanentDepositService;

  @Mock
  private ConsentDocumentImageService consentDocumentImageService;

  @InjectMocks
  private DepositDrawingsGeneratorService depositDrawingsGeneratorService;

  private PadDepositDrawing drawing1;
  private PadDepositDrawing drawing2;

  private PwaApplicationDetail detail = new PwaApplicationDetail();

  @Before
  public void setUp() throws Exception {

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
    when(permanentDepositService.permanentDepositsAreToBeMadeOnApp(detail)).thenReturn(true);

    var docSectionData = depositDrawingsGeneratorService.getDocumentSectionData(detail, null, DocGenType.PREVIEW);

    verify(consentDocumentImageService, times(1)).convertFilesToImageSourceMap(Set.of("id1", "id2"));

    assertThat(docSectionData.getTemplatePath()).isEqualTo("documents/consents/sections/depositDrawings.ftl");
    assertThat(docSectionData.getTemplateModel()).containsOnly(
        entry("sectionName", DocumentSection.DEPOSIT_DRAWINGS.getDisplayName()),
        entry("drawingRefToFileIdMap", Map.of("drawing1Ref", "id1", "drawing2Ref", "id2")),
        entry("fileIdToImgSourceMap", Map.of("id1", "file1Uri", "id2", "file2Uri"))
    );

  }

  @Test
  public void getDocumentSectionData_noDrawings() {
    when(permanentDepositService.permanentDepositsAreToBeMadeOnApp(detail)).thenReturn(true);
    when(depositDrawingsService.getAllDepositDrawingsForDetail(detail)).thenReturn(List.of());

    var docSectionData = depositDrawingsGeneratorService.getDocumentSectionData(detail, null, DocGenType.PREVIEW);

    assertThat(docSectionData).isNull();

  }

  @Test
  public void getDocumentSectionData_notIncludingPermanentDeposits() {
    when(permanentDepositService.permanentDepositsAreToBeMadeOnApp(detail)).thenReturn(false);
    var docSectionData = depositDrawingsGeneratorService.getDocumentSectionData(detail, null, DocGenType.PREVIEW);
    assertThat(docSectionData).isNull();

  }

}