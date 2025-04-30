package uk.co.ogauthority.pwa.model.auditrevisions;

import org.hibernate.envers.RevisionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.ogauthority.pwa.util.SecurityUtils;

public class AuditRevisionListener implements RevisionListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuditRevisionListener.class);

  @Override
  public void newRevision(Object revisionEntity) {
    if (!(revisionEntity instanceof AuditRevision auditRevision)) {
      LOGGER.error("Object passed to AuditRevisionListener.newRevision which is not an instance of AuditRevision");
      return;
    }

    var possibleUser = SecurityUtils.getAuthenticatedUserFromSecurityContext();

    if (possibleUser.isEmpty()) {
      LOGGER.warn("No principal available for audit revision {}", auditRevision.getId());
      return;
    }

    auditRevision.setPersonId(possibleUser.get().getLinkedPerson().getId().asInt());
  }
}
