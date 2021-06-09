package uk.co.ogauthority.pwa.model.entity.masterpwas.contacts;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.converters.PwaContactRoleConverter;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED) // dont want to add audit table associated with parent entities
@Table(name = "pwa_application_contacts")
public class PwaContact {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "pwa_application_id", referencedColumnName = "id")
  private PwaApplication pwaApplication;

  @OneToOne
  @JoinColumn(name = "person_id")
  private Person person;

  @Convert(converter = PwaContactRoleConverter.class)
  @Column(name = "csv_role_list")
  private Set<PwaContactRole> roles;

  public PwaContact() {
  }

  public PwaContact(PwaApplication pwaApplication, Person person, Set<PwaContactRole> roles) {
    this.pwaApplication = pwaApplication;
    this.person = person;
    this.roles = roles;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }

  public void setPwaApplication(PwaApplication pwaApplication) {
    this.pwaApplication = pwaApplication;
  }

  public Person getPerson() {
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }

  public Set<PwaContactRole> getRoles() {
    return roles;
  }

  public void setRoles(Set<PwaContactRole> roles) {
    this.roles = roles;
  }
}
