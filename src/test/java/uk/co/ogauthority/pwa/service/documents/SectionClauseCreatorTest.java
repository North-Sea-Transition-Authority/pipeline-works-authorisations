package uk.co.ogauthority.pwa.service.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.documents.templates.TemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;
import uk.co.ogauthority.pwa.testutils.DocumentDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

@ExtendWith(MockitoExtension.class)
class SectionClauseCreatorTest {

  @Mock
  private Clock clock;

  private SectionClauseCreator sectionClauseCreator;

  @BeforeEach
  void setUp() {

    var ins = Instant.now();

    when(clock.instant()).thenReturn(ins);

    sectionClauseCreator = new SectionClauseCreator(clock);

  }

  @Test
  void createInstanceClauseVersionFromTemplate() {

    var templateClauseVersion = DocumentDtoTestUtils.createTemplateClauseVersion(2, clock);
    var dtoVersion = TemplateSectionClauseVersionDto.from(templateClauseVersion);

    var newVersion = sectionClauseCreator.createInstanceClauseVersionFromTemplate(dtoVersion, new Person(1, null, null, null, null));

    assertThat(newVersion.getName()).isEqualTo(dtoVersion.getName());
    assertThat(newVersion.getText()).isEqualTo(dtoVersion.getText());
    assertThat(newVersion.getLevelOrder()).isEqualTo(dtoVersion.getLevelOrder());
    assertThat(newVersion.getStatus()).isEqualTo(dtoVersion.getStatus());
    assertThat(newVersion.getTipFlag()).isEqualTo(dtoVersion.getTipFlag());
    assertThat(newVersion.getVersionNo()).isEqualTo(dtoVersion.getVersionNo());

    assertThat(newVersion.getCreatedTimestamp()).isEqualTo(dtoVersion.getCreatedTimestamp());
    assertThat(newVersion.getCreatedByPersonId()).isEqualTo(dtoVersion.getCreatedByPersonId());

    ObjectTestUtils.assertAllExpectedFieldsHaveValue(newVersion,
        List.of("id", "documentInstanceSectionClause", "parentDocumentInstanceSectionClause", "endedTimestamp", "endedByPersonId"));

  }

  @Test
  void setCommonData_allData() {

    var emptyInstanceClause = new DocumentInstanceSectionClauseVersion();

    sectionClauseCreator.setCommonData(
        emptyInstanceClause,
        "name",
        "text",
        2,
        SectionClauseVersionStatus.ACTIVE,
        5,
        new Person(1, null, null, null, null)
    );

    assertThat(emptyInstanceClause.getName()).isEqualTo("name");
    assertThat(emptyInstanceClause.getText()).isEqualTo("text");
    assertThat(emptyInstanceClause.getLevelOrder()).isEqualTo(2);
    assertThat(emptyInstanceClause.getStatus()).isEqualTo(SectionClauseVersionStatus.ACTIVE);
    assertThat(emptyInstanceClause.getCreatedByPersonId()).isEqualTo(new PersonId(1));
    assertThat(emptyInstanceClause.getVersionNo()).isEqualTo(5);

    ObjectTestUtils.assertAllExpectedFieldsHaveValue(emptyInstanceClause,
        List.of("id", "documentInstanceSectionClause", "parentDocumentInstanceSectionClause", "endedTimestamp", "endedByPersonId"));

  }

}
