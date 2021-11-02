package uk.co.ogauthority.pwa.model.auditrevisions;

import org.hibernate.envers.RevisionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;

public class AuditRevisionListener implements RevisionListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuditRevisionListener.class);

  @Override
  public void newRevision(Object revisionEntity) {
    if (revisionEntity instanceof AuditRevision) {
      AuditRevision auditRevision = (AuditRevision) revisionEntity;
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

      if (principal instanceof WebUserAccount) {
        var wua = (WebUserAccount) principal;
        auditRevision.setPersonId(wua.getLinkedPerson().getId().asInt());
      } else {
        LOGGER.debug("Principal when auditing change is not a web user account");
      }
    } else {
      LOGGER.error("Object passed to AuditRevisionListener.newRevision which is not an instance of AuditRevision");
    }
  }
}
