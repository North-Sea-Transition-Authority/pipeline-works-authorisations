-- envers table has new columns [rev, revtype] and foreign keys. but is identical to source except constraints are removed.
CREATE TABLE ${datasource.user}.consultee_grp_team_members_aud (
  rev INTEGER NOT NULL
, revtype INTEGER
, id NUMBER NOT NULL
, cg_id NUMBER
, person_id NUMBER
, csv_role_list VARCHAR2(4000)
, PRIMARY KEY (rev, id)
, FOREIGN KEY (rev) REFERENCES ${datasource.user}.audit_revisions(rev)
) TABLESPACE tbsdata;

CREATE TABLE ${datasource.user}.pwa_application_contacts_aud (
  rev INTEGER NOT NULL
, revtype INTEGER
, id NUMBER NOT NULL
, pwa_application_id NUMBER
, person_id NUMBER
, csv_role_list VARCHAR2(4000)
, PRIMARY KEY (rev, id)
, FOREIGN KEY (rev) REFERENCES ${datasource.user}.audit_revisions(rev)

) TABLESPACE tbsdata;