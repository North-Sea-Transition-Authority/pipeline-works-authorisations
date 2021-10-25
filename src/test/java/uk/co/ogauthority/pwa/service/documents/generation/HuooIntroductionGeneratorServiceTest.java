package uk.co.ogauthority.pwa.service.documents.generation;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.model.documents.instances.DocumentInstanceSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.documents.view.SectionClauseVersionView;
import uk.co.ogauthority.pwa.model.documents.view.SectionView;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadHuooRoleMetadataProvider;
import uk.co.ogauthority.pwa.testutils.DocumentDtoTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class HuooIntroductionGeneratorServiceTest {

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private MailMergeService mailMergeService;

  @Mock
  private PadHuooRoleMetadataProvider padHuooRoleMetadataProvider;

  private HuooIntroductionGeneratorService huooIntroductionGeneratorService;

  private DocumentInstance documentInstance = new DocumentInstance();

  private PwaApplicationDetail detail = new PwaApplicationDetail();

  private DocumentView docView;

  @Before
  public void setUp() throws Exception {

    huooIntroductionGeneratorService = new HuooIntroductionGeneratorService(
        documentInstanceService,
        mailMergeService,
        padHuooRoleMetadataProvider);

    DocumentInstanceSectionClauseVersionDto dto1 = DocumentDtoTestUtils
        .getDocumentInstanceSectionClauseVersionDto(DocumentSection.HUOO_INTRO.name(), "intro", 1, 1);
    DocumentInstanceSectionClauseVersionDto dto2 = DocumentDtoTestUtils
        .getDocumentInstanceSectionClauseVersionDto(DocumentSection.HUOO_INTRO.name(), "not intro", 1, 2);

    var sectionView = new SectionView();
    var clauseList = new ArrayList<SectionClauseVersionView>();
    clauseList.add(SectionClauseVersionView.from(dto1));
    clauseList.add(SectionClauseVersionView.from(dto2));
    sectionView.setClauses(clauseList);

    docView = new DocumentView(PwaDocumentType.INSTANCE, detail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);
    docView.setSections(List.of(sectionView));

    when(documentInstanceService.getDocumentView(documentInstance, DocumentSection.HUOO_INTRO))
        .thenReturn(docView);

    when(padHuooRoleMetadataProvider.getRoleCountMap(detail)).thenReturn(Map.of(
        HuooRole.HOLDER, 1,
        HuooRole.USER, 2,
        HuooRole.OPERATOR, 1,
        HuooRole.OWNER, 1
    ));

  }

  @Test
  public void getDocumentSectionData_dataPresent() {

    var docSectionData = huooIntroductionGeneratorService.getDocumentSectionData(detail, documentInstance, DocGenType.PREVIEW);

    verify(mailMergeService, times(1)).mailMerge(docView, DocGenType.PREVIEW);

    // remove first clause from docview as it is used in intro paragraph
    docView.getSections().get(0).getClauses().remove(0);

    var roleNameTextMap = Map.of(
        "HOLDER", "HOLDER",
        "USER", "USERS",
        "OPERATOR", "OPERATOR",
        "OWNER", "OWNER"
    );

    assertThat(docSectionData.getTemplatePath()).isEqualTo("documents/consents/sections/huooIntro.ftl");
    assertThat(docSectionData.getTemplateModel()).containsOnly(
        entry("docView", docView),
        entry("introParagraph", "intro"),
        entry("sectionType", DocumentSection.HUOO_INTRO),
        entry("orgRoleNameToTextMap", roleNameTextMap)
    );

  }

}