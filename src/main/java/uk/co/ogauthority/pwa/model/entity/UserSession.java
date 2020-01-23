package uk.co.ogauthority.pwa.model.entity;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.Table;

@Entity
@Table(name = "user_sessions")
@NamedEntityGraph(name = UserSession.USER_ACCOUNT_ENTITY_GRAPH, attributeNodes = @NamedAttributeNode("authenticatedUserAccount"))
public class UserSession {

  public static final String USER_ACCOUNT_ENTITY_GRAPH = "UserSession.authenticatedUserAccount";

  @Id
  private String id;

  // Don't load AuthenticatedUserAccount unless requested (via the named entity graph)
  // loading their privs is expensive and not needed for simple session validation.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private AuthenticatedUserAccount authenticatedUserAccount;

  private Instant loginTimestamp;

  private Instant lastAccessTimestamp;

  private Instant logoutTimestamp;

  public UserSession() {
  }

  public UserSession(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public AuthenticatedUserAccount getAuthenticatedUserAccount() {
    return authenticatedUserAccount;
  }

  public void setAuthenticatedUserAccount(AuthenticatedUserAccount authenticatedUserAccount) {
    this.authenticatedUserAccount = authenticatedUserAccount;
  }


  public Instant getLoginTimestamp() {
    return loginTimestamp;
  }

  public void setLoginTimestamp(Instant loginTimestamp) {
    this.loginTimestamp = loginTimestamp;
  }

  public Instant getLastAccessTimestamp() {
    return lastAccessTimestamp;
  }

  public void setLastAccessTimestamp(Instant lastAccessTimestamp) {
    this.lastAccessTimestamp = lastAccessTimestamp;
  }

  public Instant getLogoutTimestamp() {
    return logoutTimestamp;
  }

  public void setLogoutTimestamp(Instant logoutTimestamp) {
    this.logoutTimestamp = logoutTimestamp;
  }
}

