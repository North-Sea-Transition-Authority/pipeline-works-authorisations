package uk.co.ogauthority.pwa.service.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.documents.TemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.testutils.DocumentDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class SectionClauseCreatorTest {

  @Mock
  private Clock clock;

  private SectionClauseCreator sectionClauseCreator;

  @Before
  public void setUp() {

    var ins = Instant.now();

    when(clock.instant()).thenReturn(ins);

    sectionClauseCreator = new SectionClauseCreator(clock);

  }

  @Test
  public void createInstanceClauseVersionFromTemplate() {

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

}
