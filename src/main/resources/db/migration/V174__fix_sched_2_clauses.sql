DECLARE

    l_sched2_s_id NUMBER;
    l_new_sc_id NUMBER;
    l_new_parent_sc_id NUMBER;

BEGIN

    SELECT s.id
    INTO l_sched2_s_id
    FROM ${datasource.user}.dt_sections s
    WHERE s.name = 'SCHEDULE_2';

    SELECT s.sc_id
    INTO l_new_parent_sc_id
    FROM ${datasource.user}.dt_section_clause_versions s
    WHERE s.name = 'On Completion of Construction/Installation'
    AND s.tip_flag = 1
    AND s.version_no = 1;

    INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_sched2_s_id)
    RETURNING id INTO l_new_sc_id;

    UPDATE ${datasource.user}.dt_section_clause_versions
    SET
        sc_id = l_new_sc_id
      , parent_sc_id = l_new_parent_sc_id
    WHERE name = 'Written Notification'
    AND tip_flag = 1
    AND version_no = 1;

    INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_sched2_s_id)
    RETURNING id INTO l_new_sc_id;

    UPDATE ${datasource.user}.dt_section_clause_versions
    SET
        sc_id = l_new_sc_id
      , parent_sc_id = l_new_parent_sc_id
    WHERE name = 'Copies of drawings'
    AND tip_flag = 1
    AND version_no = 1;

    INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_sched2_s_id)
    RETURNING id INTO l_new_sc_id;

    UPDATE ${datasource.user}.dt_section_clause_versions
    SET
        sc_id = l_new_sc_id
      , parent_sc_id = l_new_parent_sc_id
    WHERE name = 'Co-ordinates'
    AND tip_flag = 1
    AND version_no = 1;

EXCEPTION
    -- if there is no data or too many rows then the clause must have been modified/deleted already
    WHEN NO_DATA_FOUND THEN NULL;
    WHEN TOO_MANY_ROWS THEN NULL;
END;