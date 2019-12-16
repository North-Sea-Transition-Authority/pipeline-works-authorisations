CREATE OR REPLACE FUNCTION ${datasource.user}.user_system_privileges(user_id VARCHAR2) RETURN VARCHAR2 IS

  l_sys_privs_list bpmmgr.uref_priv_list_type;
  l_all_privs bpmmgr.varchar2_list_type;
  l_result VARCHAR2(32767);

BEGIN

  l_sys_privs_list := bpmmgr.security.getSystemPrivs(
    p_grantee_uref => user_id || 'WUA'
  , p_priv_list_type => '*'
  );

  l_all_privs := bpmmgr.varchar2_list_type();

  FOR l_sys_privs_index IN 1..l_sys_privs_list.COUNT
  LOOP
    l_all_privs := l_all_privs MULTISET UNION DISTINCT st.split(upper(l_sys_privs_list(l_sys_privs_index).priv_list));
  END LOOP;

  SELECT LISTAGG(priv, ',') WITHIN GROUP (ORDER BY 1)
  INTO l_result
  FROM (SELECT DISTINCT t.column_value priv FROM TABLE(l_all_privs) t);

  RETURN l_result;
END;
/