package uk.co.ogauthority.pwa.integrations.energyportal.people.external;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import org.hibernate.annotations.Immutable;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;


/**
 * A resource Person, from decmgr.resource_people.
 */
@Entity
@Immutable
@Table(name = "people")
public class Person implements Serializable, EmailRecipient {

  private static final long serialVersionUID = 1;

  @Id
  private Integer id;

  private String forename;
  private String surname;
  private String emailAddress;
  private String telephoneNo;

  public Person() {}

  @VisibleForTesting
  public Person(Integer id, String forename, String surname, String emailAddress, String telephoneNo) {
    this.id = id;
    this.forename = forename;
    this.surname = surname;
    this.telephoneNo = telephoneNo;
    this.emailAddress = emailAddress;
  }

  public void setId(Integer id) {
    this.id = id;
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

  public void setTelephoneNo(String telephoneNo) {
    this.telephoneNo = telephoneNo;
  }

  public PersonId getId() {
    return new PersonId(id);
  }

  public String getForename() {
    return forename;
  }

  public String getSurname() {
    return surname;
  }

  public String getFullName() {
    return forename + " " + surname;
  }

  @Override
  public String getEmailAddress() {
    return emailAddress;
  }

  public String getTelephoneNo() {
    return telephoneNo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Person person = (Person) o;
    return id.equals(person.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
