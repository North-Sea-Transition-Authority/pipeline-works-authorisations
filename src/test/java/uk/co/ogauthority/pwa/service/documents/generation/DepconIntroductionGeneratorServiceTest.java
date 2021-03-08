package uk.co.ogauthority.pwa.service.documents.generation;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.documents.instances.DocumentInstanceSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.documents.view.SectionClauseVersionView;
import uk.co.ogauthority.pwa.model.documents.view.SectionView;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.testutils.DocumentDtoTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DepconIntroductionGeneratorServiceTest {

  @Mock
  private DocumentInstanceService documentInstanceService;

  private DepconIntroductionGeneratorService depconIntroductionGeneratorService;

  private DocumentInstance documentInstance;

  private PwaApplicationDetail detail = new PwaApplicationDetail();

  private DocumentView docView;

  @Before
  public void setUp() throws Exception {

    depconIntroductionGeneratorService = new DepconIntroductionGeneratorService(documentInstanceService);

    DocumentInstanceSectionClauseVersionDto dto1 = DocumentDtoTestUtils
        .getDocumentInstanceSectionClauseVersionDto(DocumentSection.DEPCON_INTRO.name(), "intro", 1, 1);
    DocumentInstanceSectionClauseVersionDto dto2 = DocumentDtoTestUtils
        .getDocumentInstanceSectionClauseVersionDto(DocumentSection.DEPCON_INTRO.name(), "not intro", 1, 2);

    var sectionView = new SectionView();
    var clauseList = new ArrayList<SectionClauseVersionView>();
    clauseList.add(SectionClauseVersionView.from(dto1));
    clauseList.add(SectionClauseVersionView.from(dto2));
    sectionView.setClauses(clauseList);

    docView = new DocumentView(PwaDocumentType.INSTANCE, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);
    docView.setSections(List.of(sectionView));

    when(documentInstanceService.getDocumentView(documentInstance, DocumentSection.DEPCON_INTRO))
        .thenReturn(docView);

  }

  @Test
  public void getDocumentSectionData_dataPresent() {

    var docSectionData = depconIntroductionGeneratorService.getDocumentSectionData(detail, null);

    // remove first clause from docview as it is used in intro paragraph
    docView.getSections().get(0).getClauses().remove(0);

    assertThat(docSectionData.getTemplatePath()).isEqualTo("documents/consents/sections/depconIntro.ftl");
    assertThat(docSectionData.getTemplateModel()).containsOnly(
        entry("docView", docView),
        entry("introParagraph", "intro"),
        entry("sectionType", DocumentSection.DEPCON_INTRO)
    );

  }

}