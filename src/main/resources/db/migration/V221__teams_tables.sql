CREATE TABLE ${datasource.user}.teams (
  id VARCHAR2(4000) PRIMARY KEY,
  type VARCHAR2(500) NOT NULL,
  name VARCHAR2(4000) NOT NULL,
  scope_type VARCHAR2(500),
  scope_id VARCHAR2(500)
);

-- Scoped teams:
-- The CASE returns 'scope_type' only when scope_type is NOT NULL.
-- Only rows with a non-null scope_type are indexed, enforcing uniqueness on (type, scope_type, scope_id) for scoped teams.
CREATE UNIQUE INDEX ${datasource.user}.teams_scoped_unique ON ${datasource.user}.teams (
  type,
  CASE WHEN scope_type IS NOT NULL THEN scope_type ELSE NULL END,
  scope_id
);

-- Static teams:
-- The CASE returns 'type' only when scope_type IS NULL.
-- Only rows with a null scope_type are indexed, enforcing uniqueness on type for static teams.
CREATE UNIQUE INDEX ${datasource.user}.teams_static_unique ON ${datasource.user}.teams (
  CASE WHEN scope_type IS NULL THEN type ELSE NULL END
);

CREATE TABLE ${datasource.user}.teams_aud (
  rev INTEGER NOT NULL,
  revtype INTEGER,
  id VARCHAR2(4000),
  type VARCHAR2(500),
  name VARCHAR2(4000),
  scope_type VARCHAR2(500),
  scope_id VARCHAR2(500),
  CONSTRAINT teams_aud_pk PRIMARY KEY (rev, id),
  CONSTRAINT teams_aud_rev_fk FOREIGN KEY (rev) REFERENCES ${datasource.user}.audit_revisions (rev)
);

CREATE INDEX ${datasource.user}.teams_aud_rev_idx ON ${datasource.user}.teams_aud (rev);

CREATE TABLE ${datasource.user}.team_roles (
  id VARCHAR2(4000) PRIMARY KEY,
  team_id VARCHAR2(4000) NOT NULL REFERENCES ${datasource.user}.teams(id),
  role VARCHAR2(4000) NOT NULL,
  wua_id INTEGER NOT NULL
);

CREATE INDEX ${datasource.user}.team_roles_team_id_idx ON ${datasource.user}.team_roles(team_id);

CREATE TABLE ${datasource.user}.team_roles_aud (
  rev INTEGER NOT NULL,
  revtype INTEGER,
  id VARCHAR2(4000),
  team_id VARCHAR2(4000),
  role VARCHAR2(4000),
  wua_id INTEGER,
  CONSTRAINT team_roles_aud_pk PRIMARY KEY (rev, id),
  CONSTRAINT team_roles_aud_rev_fk FOREIGN KEY (rev) REFERENCES ${datasource.user}.audit_revisions (rev)
);

CREATE INDEX ${datasource.user}.team_roles_aud_rev_idx ON ${datasource.user}.team_roles_aud (rev);
