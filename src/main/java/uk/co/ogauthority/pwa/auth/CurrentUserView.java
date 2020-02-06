package uk.co.ogauthority.pwa.auth;

public class CurrentUserView {

  private final boolean isAuthenticated;
  private final String fullName;

  public static CurrentUserView authenticated(AuthenticatedUserAccount authenticatedUserAccount) {
    return new CurrentUserView(true, authenticatedUserAccount.getFullName());
  }

  public static CurrentUserView unauthenticated() {
    return new CurrentUserView(false, null);
  }

  private CurrentUserView(boolean isAuthenticated, String fullName) {
    this.isAuthenticated = isAuthenticated;
    this.fullName = fullName;
  }

  public boolean isAuthenticated() {
    return isAuthenticated;
  }

  public String getFullName() {
    return fullName;
  }

}
