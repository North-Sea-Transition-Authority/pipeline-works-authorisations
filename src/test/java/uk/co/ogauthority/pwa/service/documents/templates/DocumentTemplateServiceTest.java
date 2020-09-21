package uk.co.ogauthority.pwa.service.documents.templates;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.exception.documents.DocumentTemplateException;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplate;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.enums.documents.DocumentTemplateSectionStatus;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionClauseVersionRepository;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionRepository;
import uk.co.ogauthority.pwa.service.documents.DocumentDtoFactory;
import uk.co.ogauthority.pwa.testutils.DocumentDtoTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DocumentTemplateServiceTest {

  @Mock
  private DocumentTemplateSectionRepository templateSectionRepository;

  @Mock
  private DocumentTemplateSectionClauseVersionRepository templateSectionClauseVersionRepository;

  @Mock
  private DocumentDtoFactory documentDtoFactory;

  @Mock
  private Clock clock;

  private DocumentTemplateService documentTemplateService;

  @Before
  public void setUp() {

    var inst = Instant.now();
    when(clock.instant()).thenReturn(inst);

    documentTemplateService = new DocumentTemplateService(templateSectionRepository, templateSectionClauseVersionRepository, documentDtoFactory);

  }

  @Test
  public void populateDocumentDtoFromTemplateMnem() {

    var template = new DocumentTemplate();

    Map<DocumentTemplateSection, List<DocumentTemplateSectionClauseVersion>> sectionToClauseListMap = DocumentDtoTestUtils.createArgMap(template, clock);

    var sections = new ArrayList<>(sectionToClauseListMap.keySet());

    when(templateSectionRepository.getAllByDocumentTemplate_MnemAndStatusIs(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, DocumentTemplateSectionStatus.ACTIVE))
        .thenReturn(sections);

    var clauseVersions = sectionToClauseListMap.values().stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    when(templateSectionClauseVersionRepository.getAllByDocumentTemplateSectionClause_DocumentTemplateSectionInAndTipFlagIsTrue(sections))
        .thenReturn(clauseVersions);

    documentTemplateService.populateDocumentDtoFromTemplateMnem(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

    verify(documentDtoFactory, times(1)).create(eq(sectionToClauseListMap));

  }

  @Test(expected = DocumentTemplateException.class)
  public void populateDocumentDtoFromTemplateMnem_noSections() {

    when(templateSectionRepository.getAllByDocumentTemplate_MnemAndStatusIs(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, DocumentTemplateSectionStatus.ACTIVE))
        .thenReturn(List.of());

    documentTemplateService.populateDocumentDtoFromTemplateMnem(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

  }

}
