package uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Set;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.converters.ConsulteeGroupMemberRoleConverter;

@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED) // dont want to audit via envers the related ConsulteeGroup
@AuditTable("consultee_grp_team_members_aud") // oracle 30 char limit requires slight change to audit table name
@Table(name = "consultee_group_team_members")
public class ConsulteeGroupTeamMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "cg_id")
  private ConsulteeGroup consulteeGroup;

  @OneToOne
  @JoinColumn(name = "person_id")
  private Person person;

  @Convert(converter = ConsulteeGroupMemberRoleConverter.class)
  @Column(name = "csv_role_list")
  private Set<ConsulteeGroupMemberRole> roles;

  public ConsulteeGroupTeamMember() {
  }

  public ConsulteeGroupTeamMember(ConsulteeGroup consulteeGroup,
                                  Person person,
                                  Set<ConsulteeGroupMemberRole> roles) {
    this.consulteeGroup = consulteeGroup;
    this.person = person;
    this.roles = roles;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public ConsulteeGroup getConsulteeGroup() {
    return consulteeGroup;
  }

  public void setConsulteeGroup(
      ConsulteeGroup consulteeGroup) {
    this.consulteeGroup = consulteeGroup;
  }

  public Person getPerson() {
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }

  public Set<ConsulteeGroupMemberRole> getRoles() {
    return roles;
  }

  public void setRoles(
      Set<ConsulteeGroupMemberRole> roles) {
    this.roles = roles;
  }
}
