-- do a merge insert so we dont break envs where this data has been patched manually.
MERGE INTO ${datasource.user}.template_text tt
USING (
  SELECT 'INITIAL_CONSENT_EMAIL_COVER_LETTER' text_type, 'Initial PWA consent email cover letter dummy text' text FROM dual
  UNION ALL
  SELECT 'VARIATION_CONSENT_EMAIL_COVER_LETTER' text_type, 'Variation consent email cover letter dummy text' text FROM dual
) merge_data
ON (tt.text_type = merge_data.text_type)
WHEN NOT MATCHED THEN
  INSERT(text_type, text)
  VALUES (merge_data.text_type, merge_data.text);

DECLARE
  l_do_doc_creation VARCHAR2(100);

  l_dt_id NUMBER;
  l_sched_2_section_id NUMBER;
  l_terms_conditions_section_id NUMBER;
  l_new_sc_id NUMBER;
  l_parent_sc_id NUMBER;

BEGIN

  SELECT CASE WHEN COUNT(*) = 0 THEN 'true' ELSE 'false' END
  INTO l_do_doc_creation
  FROM ${datasource.user}.document_templates dt;

  IF(l_do_doc_creation != 'true') THEN
    -- end early to avoid bricking peoples dev environments if doc template data already exists.
    RETURN;
  END IF;

  INSERT INTO ${datasource.user}.document_templates dt (mnem) VALUES ('PWA_CONSENT_DOCUMENT')
  RETURNING id INTO l_dt_id;

  INSERT INTO ${datasource.user}.dt_sections (dt_id, name, status, start_timestamp) VALUES (l_dt_id, 'SCHEDULE_2 ', 'ACTIVE', systimestamp)
  RETURNING id INTO l_sched_2_section_id;

  INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_sched_2_section_id)
  RETURNING id INTO l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
    id
    , sc_id
    , version_no
    , tip_flag
    , name
    , text
    , parent_sc_id
    , level_order
    , status
    , created_timestamp
    , created_by_person_id
  ) VALUES (
  ${datasource.user}.DT_SCV_ID_SEQ.nextval
  , l_new_sc_id
  , 1
  , 1
  , 'Line pipe'
  , 'The overall length of the pipeline, or parts thereof, shall be as described in column 6 of Table ''A'' and the external and internal diameters of the pipeline, or parts thereof, shall be described in columns 7 and 8 of that Table.  If applicable, the wall thickness of the pipeline shall be as described in column 9 of Table ''A'' and the pipeline insulation shall be as described in column 10 of Table ''A''.'
  , null -- parent sc id
  , 1
  , 'ACTIVE'
  , systimestamp
  , 1
  );

  INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_sched_2_section_id)
  RETURNING id INTO l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
     id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) VALUES (
  ${datasource.user}.dt_scv_id_seq.NEXTVAL
  , l_new_sc_id
  , 1
  , 1
  , 'Installation'
  , 'The pipeline shall be so installed that it will not unduly impede or prevent the laying of further pipelines or cables.'
  , null -- parent sc id
  , 2
  , 'ACTIVE'
  , SYSTIMESTAMP
  , 1
  );

  INSERT INTO ${datasource.user}.DT_SECTION_CLAUSES (id, s_id) VALUES (${datasource.user}.dt_section_clauses_id_seq.nextval, l_sched_2_section_id)
  RETURNING id INTO l_new_sc_id;

  l_parent_sc_id := l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
     id
   , sc_id
   , version_no
   , tip_flag
   , name
   , text
   , parent_sc_id
   , level_order
   , status
   , created_timestamp
   , created_by_person_id
  ) VALUES (
  ${datasource.user}.dt_scv_id_seq.nextval
  , l_new_sc_id
  , 1
  , 1
  , 'Trenching'
  , 'Trenching'
  , null -- parent sc id
  , 3
  , 'ACTIVE'
  , systimestamp
  , 1
  );

  INSERT INTO ${datasource.user}.DT_SECTION_CLAUSES (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_sched_2_section_id)
  RETURNING id INTO l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
   id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) VALUES (
  ${datasource.user}.dt_scv_id_seq.NEXTVAL
  , l_new_sc_id
  , 1
  , 1
  , 'Lowering pipeline'
  , 'As soon as practicable the pipeline (excluding risers and associated apparatus) shall be lowered into the subsoil of the seabed by trenching so that wherever practicable the uppermost surface of the pipeline is below the undisturbed level of the surrounding seabed.'
  , l_parent_sc_id
  , 1
  , 'ACTIVE'
  , systimestamp
  , 1
  );

  INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) VALUES (${datasource.user}.dt_section_clauses_id_seq.nextval, l_sched_2_section_id)
  RETURNING id INTO l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
   id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) VALUES (
  ${datasource.user}.dt_scv_id_seq.NEXTVAL
  , l_new_sc_id
  , 1
  , 1
  , 'Interference'
  , 'If at any subsequent time, any part of the pipeline, (excluding the risers and associated apparatus) remaining above the level of the seabed causes actual interference with fishing or with other activities or there is evidence that such interference is likely to occur, the Oil and Gas Authority may require that part of the pipeline should be lowered below the level of the surrounding seabed by trenching.  Any parts of the said pipeline not supported by the seabed shall be provided with suitable support if at any time the Oil and Gas Authority (having regards to the matters aforesaid) requires that support should be provided.'
  , l_parent_sc_id
  , 2
  , 'ACTIVE'
  , SYSTIMESTAMP
  , 1
  );

  INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) VALUES (${datasource.user}.dt_section_clauses_id_seq.NEXTVAL, l_sched_2_section_id)
  RETURNING id INTO l_new_sc_id;

  l_parent_sc_id := l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
    id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) VALUES (
  ${datasource.user}.dt_scv_id_seq.NEXTVAL
  , l_new_sc_id
  , 1
  , 1
  , 'OGA Information'
  , 'The notifications, information and documents referred to in term 11 of this authorisation are as follows:'
  , null -- parent sc id
  , 4
  , 'ACTIVE'
  , SYSTIMESTAMP
  , 1
  );

  INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_sched_2_section_id)
  RETURNING id INTO l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
   id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) values (
   ${datasource.user}.dt_scv_id_seq.NEXTVAL
  , l_new_sc_id
  , 1
  , 1
  , 'During Construction/Installation'
  , 'Notification of the commencement of the execution of works for the construction/installation of the pipeline, details of any significant incident or other factors which could affect the work schedule and notification of the termination of such works.'
  , l_parent_sc_id
  , 1
  , 'ACTIVE'
  , systimestamp
  , 1
  );

  INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) VALUES (${datasource.user}.dt_section_clauses_id_seq.nextval, l_sched_2_section_id)
  RETURNING id INTO l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
   id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) VALUES (
    ${datasource.user}.dt_scv_id_seq.NEXTVAL
   , l_new_sc_id
   , 1
   , 1
   , 'On Completion of Construction/Installation'
   , 'On Completion of Construction/Installation'
   , l_parent_sc_id
   , 2
   , 'ACTIVE'
   , SYSTIMESTAMP
   , 1
   );

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
    id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) VALUES (
   ${datasource.user}.dt_scv_id_seq.NEXTVAL
  , l_new_sc_id
  , 1
  , 1
  , 'Written Notification'
  , 'Written notification of whether the pipelines have been constructed and installed in accordance with the requirements of term 7 of this authorisation.'
  , l_parent_sc_id
  , 1
  , 'ACTIVE'
  , SYSTIMESTAMP
  , 1
  );

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
   id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) VALUES (
  ${datasource.user}.dt_scv_id_seq.NEXTVAL
  , l_new_sc_id
  , 1
  , 1
  , 'Copies of drawings'
  , 'Copies of drawings to a suitable scale indicating the position of the whole of the pipeline as constructed and installed and including details of subsea riser connections and subsea junctions (where applicable).'
  , l_parent_sc_id
  , 2
  , 'ACTIVE'
  , systimestamp
  , 1
  );

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
     id
   , sc_id
   , version_no
   , tip_flag
   , name
   , text
   , parent_sc_id
   , level_order
   , status
   , created_timestamp
   , created_by_person_id
  ) VALUES (
   ${datasource.user}.dt_scv_id_seq.NEXTVAL
  , l_new_sc_id
  , 1
  , 1
  , 'Co-ordinates'
  , 'Co-ordinates of the pipeline as laid (these should include the co-ordinates of any point at which the direction of the pipelines changes or where there are significant features).'
  , l_parent_sc_id
  , 3
  , 'ACTIVE'
  , SYSTIMESTAMP
  , 1
  );


  INSERT INTO ${datasource.user}.dt_sections (dt_id, name, status, start_timestamp) VALUES (l_dt_id, 'INITIAL_TERMS_AND_CONDITIONS', 'ACTIVE', systimestamp)
  RETURNING id INTO l_terms_conditions_section_id;

  l_new_sc_id := NULL;
  l_parent_sc_id := NULL;

  INSERT INTO ${datasource.user}.DT_SECTION_CLAUSES (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.NEXTVAL, l_terms_conditions_section_id)
  RETURNING id INTO l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
     id
   , sc_id
   , version_no
   , tip_flag
   , name
   , text
   , parent_sc_id
   , level_order
   , status
   , created_timestamp
   , created_by_person_id
  ) VALUES (
   ${datasource.user}.dt_scv_id_seq.NEXTVAL
  , l_new_sc_id
  , 1
  , 1
  , 'On date'
  , 'This Authorisation shall come into force on the date hereof.'
  , null -- parent sc id
  , 1
  , 'ACTIVE'
  , SYSTIMESTAMP
  , 1
  );

  INSERT INTO ${datasource.user}.DT_SECTION_CLAUSES (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_terms_conditions_section_id)
  RETURNING id INTO l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
     id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) VALUES (
   ${datasource.user}.dt_scv_id_seq.NEXTVAL
  , l_new_sc_id
  , 1
  , 1
  , 'Section 18'
  , 'Subject to the provisions of section 18 of the Act, this Authorisation shall be of unlimited duration.'
  , null -- parent sc id
  , 2
  , 'ACTIVE'
  , SYSTIMESTAMP
  , 1
  );

  INSERT INTO ${datasource.user}.DT_SECTION_CLAUSES (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.NEXTVAL, l_terms_conditions_section_id)
  RETURNING id INTO l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
   id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) values (
  ${datasource.user}.dt_scv_id_seq.NEXTVAL
  , l_new_sc_id
  , 1
  , 1
  , 'Not assignable'
  , 'This Authorisation is issued to the holder and is not assignable without the prior written consent of the Oil and Gas Authority.'
  , null -- parent sc id
  , 3
  , 'ACTIVE'
  , SYSTIMESTAMP
  , 1
  );

  INSERT INTO ${datasource.user}.DT_SECTION_CLAUSES (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_terms_conditions_section_id)
  RETURNING id INTO l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
  id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) VALUES (
   ${datasource.user}.dt_scv_id_seq.NEXTVAL
  , l_new_sc_id
  , 1
  , 1
  , 'Not assignable'
  , 'Save with the prior written consent of the Oil and Gas Authority, which consent may be subject to conditions, the pipeline shall be used only for the purpose specified in column 12 of Table ''A'' and the conveyance of substances and equipment for the purpose of testing, inspecting and maintaining the pipeline.'
  , null -- parent sc id
  , 4
  , 'ACTIVE'
  , systimestamp
  , 1
  );

  INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_terms_conditions_section_id)
  RETURNING id INTO l_new_sc_id;

  INSERT INTO ${datasource.user}.DT_SECTION_CLAUSE_VERSIONS (
    id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) VALUES (
    ${datasource.user}.DT_SCV_ID_SEQ.nextval
   , l_new_sc_id
   , 1
   , 1
   , 'Persons'
   , 'The holder may engage any persons or kinds of persons to execute the works for the construction of the pipeline provided that the holder shall notify the Oil and Gas Authority of the name and address of the principal contractor for the time being engaged in the execution of such works.'
   , null -- parent sc id
   , 5
   , 'ACTIVE'
   , systimestamp
   , 1
   );

  INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_terms_conditions_section_id)
  RETURNING id INTO l_new_sc_id;

  l_parent_sc_id := l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
  id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) VALUES (
    ${datasource.user}.dt_scv_id_seq.NEXTVAL
   , l_new_sc_id
   , 1
   , 1
   , 'Blank top-level'
   , ''
   , null -- parent sc id
   , 6
   , 'ACTIVE'
   , SYSTIMESTAMP
   , 1
   );

  INSERT INTO ${datasource.user}.DT_SECTION_CLAUSES (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_terms_conditions_section_id)
  RETURNING id INTO l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
    id
    , sc_id
    , version_no
    , tip_flag
    , name
    , text
    , parent_sc_id
    , level_order
    , status
    , created_timestamp
    , created_by_person_id
  ) values (
    ${datasource.user}.DT_SCV_ID_SEQ.nextval
   , l_new_sc_id
   , 1
   , 1
   , 'Part II'
   , 'The Oil and Gas Authority hereby consents to the use of the pipeline by those bodies specified in Part II of Schedule l hereto.'
   , l_parent_sc_id -- parent sc id
   , 1
   , 'ACTIVE'
   , SYSTIMESTAMP
   , 1
   );

  INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) VALUES (${datasource.user}.dt_section_clauses_id_seq.nextval, l_terms_conditions_section_id)
  RETURNING id INTO l_new_sc_id;

  INSERT INTO ${datasource.user}.dt_section_clause_versions (
  id
  , sc_id
  , version_no
  , tip_flag
  , name
  , text
  , parent_sc_id
  , level_order
  , status
  , created_timestamp
  , created_by_person_id
  ) values (
   ${datasource.user}.dt_scv_id_seq.NEXTVAL
  , l_new_sc_id
  , 1
  , 1
  , '17F'
  , 'Except as otherwise expressly provided by this Authorisation, and without prejudice to section 17F of the Act, no other body may use the pipeline or any part thereof, except with the prior written consent of the Oil  and  Gas  Authority  and  in  accordance  with  any  conditions  which may be attached to that consent.'
  , l_parent_sc_id -- parent sc id
  , 2
  , 'ACTIVE'
  , SYSTIMESTAMP
  , 1
  );

  DECLARE

    l_new_depcon_intro_sc_id NUMBER;
    l_depcon_intro_section_id NUMBER;

  BEGIN

    INSERT INTO ${datasource.user}.dt_sections (dt_id, name, status, start_timestamp) VALUES (l_dt_id, 'DEPCON_INTRO', 'ACTIVE', SYSTIMESTAMP)
    RETURNING id INTO l_depcon_intro_section_id;

    INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) VALUES (${datasource.user}.dt_section_clauses_id_seq.nextval, l_depcon_intro_section_id)
    RETURNING id INTO l_new_depcon_intro_sc_id;

    INSERT INTO ${datasource.user}.dt_section_clause_versions (
     id
    , sc_id
    , version_no
    , tip_flag
    , name
    , text
    , parent_sc_id
    , level_order
    , status
    , created_timestamp
    , created_by_person_id
    ) values (
    ${datasource.user}.dt_scv_id_seq.NEXTVAL
    , l_new_depcon_intro_sc_id
    , 1
    , 1
    , 'Opening paragraph'
    , 'The Oil and Gas Authority hereby consents, pursuant to paragraph X of Schedule X to the Works Authorisation dated XX XXX XXXX (reference number XX/W/XX), and any subsequent variations, to the deposit on the seabed of materials at the location, of the type and of the quantity as described in the deposit consent table hereto, subject to the following conditions: (i)If at any time the Oil and Gas Authority is of the opinion that any of those materials is causing or likely to cause an obstruction or hazard to other seabed users, the Oil and Gas Authority may, by notice, require the removal of those materials, and the materials shall be removed as soon as reasonably practicable thereafter. (ii)The Oil and Gas Authority may, by notice, require all or any of the said materials to be removed in the event of the holder ceasing to use the pipeline, or as soon is as practicable after the pipeline is deemed to be decommissioned.'
    , null -- parent sc id
    , 1
    , 'ACTIVE'
    , systimestamp
    , 1
    );

  END;

  DECLARE

    l_new_initial_intro_sc_id NUMBER;
    l_initial_intro_section_id NUMBER;

  BEGIN

    INSERT INTO ${datasource.user}.dt_sections (dt_id, name, status, start_timestamp) VALUES (l_dt_id, 'INITIAL_INTRO', 'ACTIVE', SYSTIMESTAMP)
    RETURNING id INTO l_initial_intro_section_id;

    INSERT INTO ${datasource.user}.DT_SECTION_CLAUSES (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_initial_intro_section_id)
    RETURNING id INTO l_new_initial_intro_sc_id;

    INSERT INTO ${datasource.user}.DT_SECTION_CLAUSE_VERSIONS (
     id
   , sc_id
   , version_no
   , tip_flag
   , name
   , text
   , parent_sc_id
   , level_order
   , status
   , created_timestamp
   , created_by_person_id
    ) values (
    ${datasource.user}.DT_SCV_ID_SEQ.nextval
   , l_new_initial_intro_sc_id
   , 1
   , 1
   , 'Opening paragraph'
   , 'The Oil and Gas Authority (hereinafter referred to as "the Oil and Gas Authority"), in exercise of the powers conferred by sections 14 and 15 of the Petroleum Act 1998 (hereinafter referred to as "the Act") and all other powers enabling the Oil and Gas Authority, hereby authorises the body specified in Part 1 of Schedule 1 hereto (hereinafter referred to as "the holder") to execute works in, under or over controlled waters for the construction of, and to use, a submarine pipeline, being the pipe or system of pipes described in Table ''A'' hereto together with valves, controls, pipe work and other apparatus or works which are in accordance with section 26 of the Act, associated with the pipeline (hereinafter together referred to as "the pipeline") and extending between the points described in columns 3 and 4 of the said Table ''A'' and along the line of route specified in that table and delineated on the admiralty chart numbered [insert drawing number] annexed hereto and thereon indicated by a continuous red line, or within 100 metres on either side of that line, being the limits within which lateral deviation from that line is permissible, subject to and on the following terms:'
   , null -- parent sc id
   , 1
   , 'ACTIVE'
   , SYSTIMESTAMP
   , 1
   );

  END;


  DECLARE
    l_variation_intro_new_sc_id NUMBER;
    l_variation_intro_section_id NUMBER;

  BEGIN
    INSERT INTO ${datasource.user}.dt_sections (dt_id, name, status, start_timestamp) VALUES (l_dt_id, 'VARIATION_INTRO', 'ACTIVE', systimestamp)
    RETURNING id INTO l_variation_intro_section_id;

    INSERT INTO ${datasource.user}.DT_SECTION_CLAUSES (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_variation_intro_section_id)
    RETURNING id INTO l_variation_intro_new_sc_id;

    INSERT INTO ${datasource.user}.DT_SECTION_CLAUSE_VERSIONS (
    id
    , sc_id
    , version_no
    , tip_flag
    , name
    , text
    , parent_sc_id
    , level_order
    , status
    , created_timestamp
    , created_by_person_id
    ) VALUES (
      ${datasource.user}.DT_SCV_ID_SEQ.nextval
     , l_variation_intro_new_sc_id
     , 1
     , 1
     , 'Opening paragraph'
     , 'The Oil and Gas Authority, pursuant to term 7 of the Works Authorisation dated [Insert Date] with reference: XX/W/XX, and any subsequent variations, hereby consents to the [installation / modification / removal/ any other text appropriate] of pipeline[s] [INSERT PL DETAILS] so as to conform to the [revised and new] Table ''A''[s] and drawing number[s] [Insert Drawing Number and Rev].'
     , null -- parent sc id
     , 1
     , 'ACTIVE'
     , systimestamp
     , 1
     );

  END;

  DECLARE

    l_huoo_initial_intro_sc_id NUMBER;
    l_huoo_intro_section_id NUMBER;

  BEGIN

    INSERT INTO ${datasource.user}.dt_sections (dt_id, name, status, start_timestamp) VALUES (l_dt_id, 'HUOO_INTRO', 'ACTIVE', SYSTIMESTAMP)
    RETURNING id INTO l_huoo_intro_section_id;

    INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id) values (${datasource.user}.dt_section_clauses_id_seq.nextval, l_huoo_intro_section_id)
    RETURNING id INTO l_huoo_initial_intro_sc_id;

    INSERT INTO ${datasource.user}.dt_section_clause_versions (
     id
    , sc_id
    , version_no
    , tip_flag
    , name
    , text
    , parent_sc_id
    , level_order
    , status
    , created_timestamp
    , created_by_person_id
    ) VALUES (
   ${datasource.user}.dt_scv_id_seq.NEXTVAL
    , l_huoo_initial_intro_sc_id
    , 1
    , 1
    , 'Opening paragraph'
    , 'The Oil and Gas Authority (hereinafter referred to as "the Oil and Gas Authority"), in exercise of the powers conferred by sections 14 and 15 of the Petroleum Act 1998 (hereinafter referred to as "the Act") and all other powers enabling the Oil and Gas Authority, hereby authorises the body specified in Part 1 of Schedule 1 hereto (hereinafter referred to as "the holder") to execute works in, under or over controlled waters for the construction of, and to use, a submarine pipeline, being the pipe or system of pipes described in Table ''A'' hereto together with valves, controls, pipe work and other apparatus or works which are in accordance with section 26 of the Act, associated with the pipeline (hereinafter together referred to as "the pipeline") and extending between the points described in columns 3 and 4 of the said Table ''A'' and along the line of route specified in that table and delineated on the admiralty chart numbered [insert drawing number] annexed hereto and thereon indicated by a continuous red line, or within 100 metres on either side of that line, being the limits within which lateral deviation from that line is permissible, subject to and on the following terms:'
    , null -- parent sc id
    , 1
    , 'ACTIVE'
    , SYSTIMESTAMP
    , 1
    );

  END;

  COMMIT;
END;

DECLARE
  l_mm_fields_count NUMBER;
BEGIN

  SELECT COUNT(*)
  INTO l_mm_fields_count
  FROM ${datasource.user}.mail_merge_fields;

  IF(l_mm_fields_count != 0) THEN
    -- END early to avoid duplicating mail merge fields inserted manually into dev schemas
    RETURN;
  END IF;

  INSERT INTO ${datasource.user}.mail_merge_fields (MNEM, TYPE)
  VALUES ('PROPOSED_START_OF_WORKS_DATE', 'AUTOMATIC' );

  INSERT INTO ${datasource.user}.mail_merge_fields (MNEM, TYPE)
  VALUES ('PROJECT_NAME', 'AUTOMATIC' );

  COMMIT;

END;










