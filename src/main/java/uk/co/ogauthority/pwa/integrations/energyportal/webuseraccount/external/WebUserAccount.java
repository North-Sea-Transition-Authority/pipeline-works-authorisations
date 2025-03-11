package uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

/**
 * A portal WebUserAccount from securemgr.
 * A WebUserAccount is linked to a Person. A Person may have many associated WebUserAccounts.
 */
@Entity
@Immutable
@Table(name = "user_accounts")
public class WebUserAccount implements Serializable {

  private static final long serialVersionUID = 1;

  @Id
  protected int wuaId;

  protected String title;
  protected String forename;
  protected String surname;
  protected String emailAddress;
  protected String loginId;

  @Enumerated(EnumType.STRING)
  protected WebUserAccountStatus accountStatus;

  @ManyToOne
  @JoinColumn(name = "person_id", referencedColumnName = "id", updatable = false, insertable = false)
  protected Person person;

  public WebUserAccount() {

  }

  @VisibleForTesting
  public WebUserAccount(int wuaId) {
    this.wuaId = wuaId;
  }

  @VisibleForTesting
  public WebUserAccount(int wuaId, Person person) {
    this.wuaId = wuaId;
    this.person = person;
  }

  // TODO: Remove in PWARE-63
  @VisibleForTesting
  WebUserAccount(int wuaId,
                 String emailAddress,
                 String loginId,
                 WebUserAccountStatus accountStatus,
                 Person person) {
    this.wuaId = wuaId;
    this.emailAddress = emailAddress;
    this.loginId = loginId;
    this.accountStatus = accountStatus;
    this.person = person;
  }

  public void setWuaId(int wuaId) {
    this.wuaId = wuaId;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setForename(String forename) {
    this.forename = forename;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public void setLoginId(String loginId) {
    this.loginId = loginId;
  }

  public void setAccountStatus(WebUserAccountStatus accountStatus) {
    this.accountStatus = accountStatus;
  }

  public void setPerson(Person person) {
    this.person = person;
  }

  public int getWuaId() {
    return wuaId;
  }

  public String getTitle() {
    return title;
  }

  public String getForename() {
    return forename;
  }

  public String getSurname() {
    return surname;
  }

  public String getFullName() {
    return String.format("%s %s", forename, surname);
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public String getLoginId() {
    return loginId;
  }

  public WebUserAccountStatus getAccountStatus() {
    return accountStatus;
  }

  public Person getLinkedPerson() {
    return person;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WebUserAccount that = (WebUserAccount) o;
    return wuaId == that.wuaId && Objects.equals(title, that.title) && Objects.equals(forename,
        that.forename) && Objects.equals(surname, that.surname) && Objects.equals(emailAddress,
        that.emailAddress) && Objects.equals(loginId,
        that.loginId) && accountStatus == that.accountStatus && Objects.equals(person, that.person);
  }

  @Override
  public int hashCode() {
    return Objects.hash(wuaId, title, forename, surname, emailAddress, loginId, accountStatus, person);
  }

}
