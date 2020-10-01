ALTER TABLE ${datasource.user}.pwa_consent_organisation_roles ADD (
  CONSTRAINT pcor_huoo_type_ck CHECK( type IN ('PORTAL_ORG', 'TREATY_AGREEMENT'))
);

-- additional huoo type for applications that is not allowed in the consented model
ALTER TABLE ${datasource.user}.pad_organisation_roles ADD (
  CONSTRAINT por_huoo_type_ck CHECK( type IN ('PORTAL_ORG', 'TREATY_AGREEMENT', 'UNASSIGNED_PIPELINE_SPLIT'))
);


ALTER TABLE ${datasource.user}.pad_organisation_roles DROP CONSTRAINT PAD_ORGANISATION_ROLES_CHECK;

ALTER TABLE ${datasource.user}.pad_organisation_roles ADD (
  CONSTRAINT pad_organisation_roles_check CHECK (
      (ou_id IS NULL AND agreement IS NOT NULL)
      OR (ou_id IS NOT NULL AND agreement IS NULL)
      OR (ou_id IS NULL AND agreement IS NOT NULL)
      OR (ou_id IS NULL AND agreement IS NULL AND type = 'UNASSIGNED_PIPELINE_SPLIT')
    )
  );
