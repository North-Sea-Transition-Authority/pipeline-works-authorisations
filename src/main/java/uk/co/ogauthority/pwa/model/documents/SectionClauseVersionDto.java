package uk.co.ogauthority.pwa.model.documents;

import java.time.Instant;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;

public interface SectionClauseVersionDto {

  Integer getVersionId();

  String getSectionName();

  Integer getClauseId();

  Integer getVersionNo();

  Boolean getTipFlag();

  String getName();

  String getText();

  Integer getParentClauseId();

  Integer getLevelNumber();

  Integer getLevelOrder();

  SectionClauseVersionStatus getStatus();

  Instant getCreatedTimestamp();

  PersonId getCreatedByPersonId();

  Instant getEndedTimestamp();

  PersonId getEndedByPersonId();

}
