package uk.co.ogauthority.pwa.model.auditrevisions;


import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;

public class AuditRevisionUtil {

  private static final ThreadLocal<AuthenticatedUserAccount> fallbackAuditUser = new ThreadLocal<>();

  private AuditRevisionUtil() {
  }

  public static AuthenticatedUserAccount getFallbackAuditUser() {
    return fallbackAuditUser.get();
  }

  public static void withFallbackAuditUser(AuthenticatedUserAccount user, Runnable runnable) {
    fallbackAuditUser.set(user);
    try {
      runnable.run();
    } finally {
      fallbackAuditUser.remove();
    }
  }
}