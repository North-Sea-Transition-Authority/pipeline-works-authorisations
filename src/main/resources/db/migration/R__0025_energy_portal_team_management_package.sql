GRANT SELECT ON decmgr.resources TO ${datasource.user};
GRANT SELECT ON decmgr.resource_people TO ${datasource.user};
GRANT EXECUTE ON decmgr.contact TO ${datasource.user};

CREATE OR REPLACE PACKAGE ${datasource.user}.TEAM_MANAGEMENT AS
  /*
  * Adds a user to a team into a list of roles. This is a wrapper of the
  * decmgr.add_members_to_roles procedure whereby the list of roles is passed
  * as a comma separated value string.
  *
  * @param p_res_id, the res_id of the team to add the new user to
  * @param p_role_names, the csv string containing the roles names to add the
    user into
  * @param p_person_id, the id of person to be added in the new roles
  * @param p_requesting_wua_id, the wua_id of the person to be added in the new roles
  */
  PROCEDURE update_user_roles(
    p_res_id            decmgr.resources.id%type
  , p_role_names_csv    VARCHAR2
  , p_person_id         decmgr.resource_people.id%type
  , p_requesting_wua_id INTEGER
  );

  /*
  * Removes user from all roles in team identified by the resource id. This is
  * a wrapper of the decmgr.remove_team_member procedure.
  *
  * @param p_res_id, the res_id of the team to remove the person from
  * @param p_person_id, the id of person to be added in the new roles
  * @param p_requesting_wua_id, the wua_id of the person to be removed
  */
  PROCEDURE remove_user_from_team (
    p_res_id            NUMBER
  , p_person_id         decmgr.resource_people.id%TYPE
  , p_requesting_wua_id NUMBER
  );

END TEAM_MANAGEMENT;
/

CREATE OR REPLACE PACKAGE BODY ${datasource.user}.TEAM_MANAGEMENT AS

  PROCEDURE update_user_roles(
      p_res_id            decmgr.resources.id%type
    , p_role_names_csv    VARCHAR2
    , p_person_id         decmgr.resource_people.id%type
    , p_requesting_wua_id INTEGER
  ) IS

    l_role_name_list  bpmmgr.varchar2_list_type;
    l_person_id_list  bpmmgr.number_list_type;

  BEGIN

    l_role_name_list := envmgr.st.split(p_role_names_csv);

    decmgr.contact.add_members_to_roles(
        p_res_id            => p_res_id
      , p_role_name_list    => l_role_name_list
      , p_person_id_list    => bpmmgr.number_list_type(p_person_id)
      , p_requesting_wua_id => p_requesting_wua_id
    );

  END update_user_roles;


  PROCEDURE remove_user_from_team (
      p_res_id            NUMBER
    , p_person_id         decmgr.resource_people.id%TYPE
    , p_requesting_wua_id NUMBER
  ) IS

  BEGIN

    decmgr.contact.remove_team_member_no_checks(
        p_res_id            => p_res_id
      , p_person_id         => p_person_id
      , p_requesting_wua_id => p_requesting_wua_id
    );

  END remove_user_from_team;

END TEAM_MANAGEMENT;
