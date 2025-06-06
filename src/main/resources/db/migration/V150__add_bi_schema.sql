CREATE USER ${datasource.bi-user} IDENTIFIED BY "${datasource.bi-user-password}"
DEFAULT TABLESPACE tbsdata
TEMPORARY TABLESPACE TEMP
PROFILE mgr_user
QUOTA UNLIMITED ON TBSBLOB
QUOTA UNLIMITED ON TBSCLOB
QUOTA UNLIMITED ON TBSDATA
QUOTA UNLIMITED ON TBSIDX;

GRANT CREATE SESSION TO ${datasource.bi-user};

GRANT SELECT ON ${datasource.user}.application_update_requests TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.app_files TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.assignments TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.assignment_audit_log TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.as_built_notification_groups TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.as_built_notif_grp_details TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.as_built_notif_grp_pipelines TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.as_built_notif_grp_status_hist TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.audit_revisions TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.case_notes TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.case_note_document_links TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.consultation_requests TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.consultation_responses TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.consultee_groups TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.consultee_group_details TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.consultee_group_team_members TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.di_sc_versions TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.di_section_clauses TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.document_instances TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.document_templates TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.dt_sections TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.dt_section_clauses TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.dt_section_clause_versions TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.fee_items TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.fee_periods TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.fee_period_details TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.fee_period_detail_fee_items TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.mail_merge_fields TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.migrated_pipeline_auths TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.migration_master_logs TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.migration_pipeline_logs TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.options_application_approvals TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.options_app_appr_deadline_hist TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_blocks TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_block_crossing_owners TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_cable_crossings TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_campaign_works_pipelines TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_campaign_work_schedule TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_confirmation_of_option TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_consent_reviews TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_deposit_drawings TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_deposit_drawing_links TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_deposit_pipelines TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_design_op_conditions TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_env_and_decom TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_fast_track_information TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_fields TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_files TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_fluid_composition_info TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_location_details TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_median_line_agreements TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_organisation_roles TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_permanent_deposits TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_pipelines TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_pipeline_crossings TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_pipeline_crossing_owners TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_pipeline_idents TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_pipeline_ident_data TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_pipeline_org_role_links TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_pipeline_other_properties TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_pipeline_tech_info TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_project_information TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_safety_zone_structures TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_technical_drawings TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pad_technical_drawing_links TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.parallel_consent_check_log TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pipelines TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pipeline_details TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pipeline_detail_idents TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pipeline_detail_ident_data TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pipeline_detail_migration_data TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pipeline_detail_migr_huoo_data TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pipeline_migration_config TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pipeline_org_role_links TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.public_notices TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.public_notice_dates TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.public_notice_documents TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.public_notice_document_links TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.public_notice_requests TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pwas TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pwa_applications TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pwa_application_contacts TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pwa_application_details TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pwa_app_charge_payment_attempt TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pwa_app_charge_requests TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pwa_app_charge_request_details TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pwa_app_charge_request_items TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pwa_consents TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pwa_consent_organisation_roles TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pwa_details TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pwa_detail_fields TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.pwa_payment_requests TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.template_text TO ${datasource.bi-user};
GRANT SELECT ON ${datasource.user}.uploaded_files TO ${datasource.bi-user};