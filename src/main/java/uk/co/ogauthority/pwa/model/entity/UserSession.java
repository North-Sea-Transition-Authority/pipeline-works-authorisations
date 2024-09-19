package uk.co.ogauthority.pwa.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.Optional;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;

@Entity
@Table(name = "user_sessions")
public class UserSession {

  @Id
  private String id;
  private int wuaId;
  private Instant loginTimestamp;
  private Instant lastAccessTimestamp;
  private Instant logoutTimestamp;
  private Integer proxyUserWuaId;

  @Transient
  // Fetch manually when needed
  private AuthenticatedUserAccount authenticatedUserAccount;

  public UserSession() { }

  public UserSession(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public int getWuaId() {
    return wuaId;
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

  public Optional<Integer> getProxyUserWuaId() {
    return Optional.ofNullable(proxyUserWuaId);
  }

  public AuthenticatedUserAccount getAuthenticatedUserAccount() {
    return authenticatedUserAccount;
  }

  public void setAuthenticatedUserAccount(AuthenticatedUserAccount authenticatedUserAccount) {
    this.authenticatedUserAccount = authenticatedUserAccount;
  }
}

