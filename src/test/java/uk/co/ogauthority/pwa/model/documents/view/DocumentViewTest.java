package uk.co.ogauthority.pwa.model.documents.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.documents.SectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.service.documents.DocumentViewService;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.SectionClauseVersionDtoTestUtils;

@ExtendWith(MockitoExtension.class)
class DocumentViewTest {

  @Mock
  private Clock clock;

  private final DocumentViewService documentViewService = new DocumentViewService();

  @BeforeEach
  void setUp() {
    var instant = Instant.now();
    when(clock.instant()).thenReturn(instant);
  }

  @Test
  void getSectionClauseView() {

    var person = PersonTestUtil.createDefaultPerson();
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var docSpec = DocumentSpec.getSpecForApplication(detail.getPwaApplicationType(), detail.getResourceType());
    var clauseDtos = SectionClauseVersionDtoTestUtils
        .getTemplateSectionClauseVersionDtoList(1, docSpec, clock, person, 2, 3, 3)
        .stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    var docSource = new TemplateDocumentSource(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT);

    var docView = documentViewService.createDocumentView(PwaDocumentType.TEMPLATE, docSource, clauseDtos);

    var expectedDto = clauseDtos.get(0);

    var sectionClauseVersionView = docView.getSectionClauseView(expectedDto.getClauseId());

    assertThat(sectionClauseVersionView.getName()).isEqualTo(expectedDto.getName());
    assertThat(sectionClauseVersionView.getText()).isEqualTo(expectedDto.getText());
    assertThat(sectionClauseVersionView.getChildClauses().get(0).getName()).isEqualTo(clauseDtos.get(1).getName());
    assertThat(sectionClauseVersionView.getChildClauses().get(0).getText()).isEqualTo(clauseDtos.get(1).getText());
    assertThat(sectionClauseVersionView.getChildClauses().get(0).getChildClauses().get(0).getName()).isEqualTo(clauseDtos.get(2).getName());
    assertThat(sectionClauseVersionView.getChildClauses().get(0).getChildClauses().get(0).getText()).isEqualTo(clauseDtos.get(2).getText());

  }

}
