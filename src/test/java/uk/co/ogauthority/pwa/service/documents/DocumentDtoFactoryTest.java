package uk.co.ogauthority.pwa.service.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.documents.SectionDto;
import uk.co.ogauthority.pwa.model.documents.templates.TemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplate;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.testutils.DocumentDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DocumentDtoFactoryTest {

  @Mock
  private Clock clock;

  private DocumentDtoFactory factory;

  @Before
  public void setUp() {

    var ins = Instant.now();
    when(clock.instant()).thenReturn(ins);

    factory = new DocumentDtoFactory();

  }

  @Test
  public void create_whenDocumentHasMultipleSections_andMultipleClausesPerSection() {

    var template = new DocumentTemplate();
    template.setId(1);
    template.setMnem(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

    var sectionToClauseListMap = DocumentDtoTestUtils.createArgMap(template, clock);

    var docDto = factory.create(sectionToClauseListMap);

    assertThat(docDto.getDocumentTemplate()).isEqualTo(template);

    for (SectionDto sectionDto: docDto.getSections()) {

      // verify that template section with name exists, retrieve it
      var templateSection = getSectionFromMapByName(sectionToClauseListMap, sectionDto.getName());
      assertThat(templateSection).isNotNull();

      // for each dto clause, check that there is a matching template clause
      for (TemplateSectionClauseVersionDto clauseVersionDto: sectionDto.getClauses()) {

        var sectionTemplateClauses = sectionToClauseListMap.get(templateSection);

        var templateClauseVersion = sectionTemplateClauses.stream()
            .filter(v -> v.getId().equals(clauseVersionDto.getId()))
            .findFirst()
            .orElseThrow();

        // check that the dto fields and template clause fields match
        assertThat(clauseVersionDto.getId()).isEqualTo(templateClauseVersion.getId());
        assertThat(clauseVersionDto.getName()).isEqualTo(templateClauseVersion.getName());
        assertThat(clauseVersionDto.getText()).isEqualTo(templateClauseVersion.getText());
        assertThat(clauseVersionDto.getTipFlag()).isEqualTo(templateClauseVersion.getTipFlag());
        assertThat(clauseVersionDto.getStatus()).isEqualTo(clauseVersionDto.getStatus());
        assertThat(clauseVersionDto.getVersionNo()).isEqualTo(templateClauseVersion.getVersionNo());
        assertThat(clauseVersionDto.getLevelOrder()).isEqualTo(templateClauseVersion.getLevelOrder());
        assertThat(clauseVersionDto.getCreatedByPersonId()).isEqualTo(templateClauseVersion.getCreatedByPersonId());
        assertThat(clauseVersionDto.getCreatedTimestamp()).isEqualTo(templateClauseVersion.getCreatedTimestamp());

        assertThat(clauseVersionDto.getTemplateClauseRecord()).isEqualTo(templateClauseVersion.getClause());
        assertThat(clauseVersionDto.getParentTemplateClause()).isEqualTo(templateClauseVersion.getParentClause().orElse(null));

        // check no empty fields on our dto object that we aren't expecting
        ObjectTestUtils.assertAllExpectedFieldsHaveValue(clauseVersionDto, List.of("endedTimestamp", "endedByPersonId"));

      }

    }

  }

  private DocumentTemplateSection getSectionFromMapByName(Map<DocumentTemplateSection, List<DocumentTemplateSectionClauseVersion>> map,
                                                          String name) {

    return map.keySet().stream()
        .filter(section -> section.getName().equals(name))
        .findFirst()
        .orElseThrow();

  }


}
