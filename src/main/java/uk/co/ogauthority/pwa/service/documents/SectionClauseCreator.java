package uk.co.ogauthority.pwa.service.documents;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.documents.templates.TemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;

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

    setCommonData(
        newVersion,
        clauseVersion.getName(),
        clauseVersion.getText(),
        clauseVersion.getLevelOrder(),
        clauseVersion.getStatus(),
        1,
        creatingPerson);

    return newVersion;

  }

  public void setCommonData(SectionClauseVersion version,
                            String name,
                            String text,
                            Integer levelOrder,
                            SectionClauseVersionStatus status,
                            Integer versionNo,
                            Person creatingPerson) {
    version.setName(name);
    version.setText(text);
    version.setLevelOrder(levelOrder);
    version.setStatus(status);
    version.setTipFlag(true);
    version.setVersionNo(versionNo);
    version.setCreatedTimestamp(clock.instant());
    version.setCreatedByPersonId(creatingPerson.getId());
  }

}
