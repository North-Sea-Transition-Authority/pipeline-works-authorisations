package uk.co.ogauthority.pwa.service.documents;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.documents.templates.TemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;

@Service
public class SectionClauseCreator {

  private final Clock clock;

  @Autowired
  public SectionClauseCreator(@Qualifier("utcClock") Clock clock) {
    this.clock = clock;
  }

  public DocumentInstanceSectionClauseVersion createInstanceClauseVersionFromTemplate(TemplateSectionClauseVersionDto clauseVersion,
                                                                                      Person creatingPerson) {

    var newVersion = new DocumentInstanceSectionClauseVersion();

    newVersion.setName(clauseVersion.getName());
    newVersion.setText(clauseVersion.getText());
    newVersion.setLevelOrder(clauseVersion.getLevelOrder());
    newVersion.setStatus(clauseVersion.getStatus());
    newVersion.setTipFlag(true);
    newVersion.setVersionNo(1);

    newVersion.setCreatedTimestamp(clock.instant());
    newVersion.setCreatedByPersonId(creatingPerson.getId());

    return newVersion;

  }
}
