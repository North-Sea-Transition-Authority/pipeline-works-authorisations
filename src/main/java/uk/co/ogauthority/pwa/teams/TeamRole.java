package uk.co.ogauthority.pwa.teams;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Types;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "team_roles")
@Audited
public class TeamRole {

  @Id
  @UuidGenerator
  @JdbcTypeCode(Types.VARCHAR) // TODO teams - remove for PG
  private UUID id;

  @JoinColumn(name = "team_id")
  @ManyToOne
  private Team team;

  @Enumerated(EnumType.STRING)
  private Role role;

  private Long wuaId;

  public TeamRole() {
  }

  TeamRole(UUID id) {
    this.id = id;
  }

  public UUID getId() {
    return id;
  }

  public Team getTeam() {
    return team;
  }

  public void setTeam(Team team) {
    this.team = team;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public Long getWuaId() {
    return wuaId;
  }

  public void setWuaId(Long wuaId) {
    this.wuaId = wuaId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TeamRole teamRole = (TeamRole) o;
    return Objects.equals(id, teamRole.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
