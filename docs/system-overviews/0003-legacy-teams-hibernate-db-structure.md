# System Overview - Legacy Teams Database Structure

## Introduction
This document provides a detailed overview of the hibernate database structure used in the legacy teams system and their underlying views.

## Entity Details

### `Person`
- **View**: `people`
```sql
SELECT
  xrph.rp_id id
, xrph.forename
, xrph.surname
, xrph.portal_email_address email_address
, xrph.telephone_hash_code telephone_no
FROM decmgr.xview_resource_people_history xrph
WHERE xrph.status_control = 'C'
```
- **Attributes**:
    - `id`: Integer (Primary Key)
    - `forename`: String
    - `surname`: String
    - `emailAddress`: String
    - `telephoneNo`: String
- **Relationships**:
    - One-to-Many with `WebUserAccount`

### `WebUserAccount`
- **View**: `user_accounts`
```sql
SELECT
  wua.id wua_id
, wua.title
, wua.forename
, wua.surname
, wua.primary_email_address email_address
, wua.resource_person_id person_id
, wua.login_id login_id
, wua.account_status
FROM securemgr.web_user_accounts wua;
```
- **Attributes**:
    - `wuaId`: Integer (Primary Key)
    - `title`: String
    - `forename`: String
    - `surname`: String
    - `emailAddress`: String
    - `loginId`: String
    - `accountStatus`: Enum (`WebUserAccountStatus`)
    - `person`: Integer
- **Relationships**:
    - Many-to-One with `Person`

### `PortalTeam`
- **View**: `portal_resources`
```sql
SELECT xr.res_id
     , xr.res_type
     , xr.res_name
     , xr.description
FROM decmgr.xview_resources xr;
```
- **Attributes**:
    - `resId`: Integer (Primary Key)
    - `name`: String
    - `description`: String
    - `portalTeamType`: PortalTeamType
- **Relationships**:
    - Many-to-One with `PortalTeamType`
- **Example Data**:

| RES_ID | RES_TYPE              | RES_NAME              | DESCRIPTION                             |
|:-------|:----------------------|:----------------------|:----------------------------------------|
| 39048  | PWA_ORGANISATION_TEAM | PWA Organisation Team | PWA Organisation Team - DOM OIL CORP    |
| 36385  | PWA_ORGANISATION_TEAM | PWA Organisation Team | PWA Organisation Team - DASHWORTH GROUP |
| 35281  | PWA_REGULATOR_TEAM    | PWA OGA Team          | PWA OGA Team                            |
| 38955  | PWA_USERS             | PWA Users             | PWA Users                               |

### `PortalTeamMember`
- **View**: `portal_res_members_current`
```sql
SELECT DISTINCT rmc.res_id
              , rmc.person_id
FROM decmgr.resource_members_current rmc
WHERE rmc.wua_id IS NOT NULL;
```
- **Attributes**:
    - `portalTeamMemberId`: Composite Key (`PortalTeamMemberId` - PersonId, ResId)
    - `person`: Integer
    - `portalTeam`: PortalTeam
- **Relationships**:
    - Many-to-One with `PortalTeam`
- **Example Data**:

| RES_ID | PERSON_ID |
|:-------|:----------|
| 6100   | 38043     |
| 9500   | 58233     |
| 20000  | 51039     |

### `PortalTeamUsage`
- **View**: `portal_resource_usages_current`
```sql
SELECT ruc.res_id
     , ruc.uref
     , ruc.purpose
FROM decmgr.resource_usages_current ruc;
```
- **Attributes**:
    - `portalTeamUsageId`: Composite Key (`PortalTeamUsageId` - ResId, Uref, Purpose)
    - `portalTeam`: PortalTeam
    - `purpose`: Enum (`PortalTeamUsagePurpose`, only has `PRIMARY_DATA`)
    - `uref`: String
- **Relationships**:
    - Many-to-One with `PortalTeam`
- **Example Data**:

| RES_ID | UREF            | PURPOSE      |
|:-------|:----------------|:-------------|
| 36385  | 6543++REGORGGRP | PRIMARY_DATA |


### `PortalTeamTypeRole`
- **View**: `portal_resource_type_roles`
```sql
SELECT xrtr.res_type
, xrtr.role_name
, xrtr.role_title
, xrtr.role_description
, xrtr.min_mems
, xrtr.max_mems
, xrtr.display_seq
FROM decmgr.xview_resource_type_roles xrtr;
```
- **Attributes**:
    - `portalTeamTypeRoleId`: Composite Key (`PortalTeamTypeRoleId` - ResType, RoleName)
    - `portalTeamType`: PortalTeamType
    - `name`: String
    - `title`: String
    - `description`: String
    - `minMems`: Integer
    - `maxMems`: Integer
    - `displaySeq`: Integer
- **Relationships**:
    - Many-to-One with `PortalTeamType`
- **Example Data**:

| RES_TYPE              | ROLE_NAME                | ROLE_TITLE                          | ROLE_DESCRIPTION                                                                                       | MIN_MEMS | MAX_MEMS | DISPLAY_SEQ |
|:----------------------|:-------------------------|:------------------------------------|:-------------------------------------------------------------------------------------------------------|:---------|:---------|:------------|
| PWA_ORGANISATION_TEAM | RESOURCE_COORDINATOR     | Team Administrator                  | Set up user access \(Team Administrator\)                                                              | 1        | 999      | 10          |
| PWA_ORGANISATION_TEAM | APPLICATION_CREATE       | Application Creator                 | Can create new PWA applications \(Application Creator\)                                                | 0        | 999      | 20          |
| PWA_ORGANISATION_TEAM | APPLICATION_SUBMITTER    | Application Submitter               | Can submit PWA applications \(Application Submitter\)                                                  | 0        | 999      | 30          |
| PWA_ORGANISATION_TEAM | FINANCE_ADMIN            | Finance Administrator               | Can pay for all submitted PWA applications \(Finance administrator\)                                   | 0        | 999      | 40          |
| PWA_ORGANISATION_TEAM | AS_BUILT_NOTIF_SUBMITTER | As-built Notification Submitter     | Can view and submit all as-built notifications for issued consents \(As-built Notification Submitter\) | 0        | 999      | 50          |
| PWA_REGULATOR_TEAM    | ORGANISATION_MANAGER     | Organisation Team Manager           | Manage organisation access to PWAs \(Organisation Team Manager\)                                       | 0        | 999      | 50          |
| PWA_REGULATOR_TEAM    | RESOURCE_COORDINATOR     | Team Administrator                  | Manage access to the team \(Team Administrator\)                                                       | 1        | 999      | 10          |
| PWA_REGULATOR_TEAM    | PWA_MANAGER              | PWA Manager                         | Accept applications and allocate case officers \(PWA Manager\)                                         | 0        | 999      | 100         |
| PWA_REGULATOR_TEAM    | CASE_OFFICER             | Case Officer                        | Process applications and run consultations \(Case Officer\)                                            | 0        | 999      | 150         |
| PWA_REGULATOR_TEAM    | PWA_CONSENT_VIEWER       | PWA Consent Viewer                  | Search for and view consented PWA data \(PWA Consent Viewer\)                                          | 0        | 999      | 160         |
| PWA_REGULATOR_TEAM    | AS_BUILT_NOTIF_ADMIN     | As-built Notification Administrator | View and submit outstanding as-built notifications \(As-built Notification Administrator\)             | 0        | 999      | 180         |
| PWA_REGULATOR_TEAM    | TEMPLATE_CLAUSE_MANAGER  | Template Clause Manager             | View and edit document template clauses \(Template Clause Manager\)                                    | 0        | 999      | 190         |
| PWA_USERS             | PWA_ACCESS               | Access PWA                          | Users in this role may access PWA without being in holder or NSTA teams                                | 0        | 1000     | 11          |


### `PortalTeamMemberRole`
- **View**: `portal_res_memb_current_roles`
```sql
SELECT DISTINCT rmc.person_id
, rmc.res_id
, rmc.res_type
, rmc.role_name
FROM decmgr.resource_members_current rmc
WHERE rmc.wua_id IS NOT NULL;
```
- **Attributes**:
    - `portalTeamMemberRoleId`: Composite Key (`PortalTeamMemberRoleId` - PersonId, ResId, ResType, RoleName)
    - `portalTeamMember`: PortalTeamMember
    - `portalTeamTypeRole`: PortalTeamTypeRole
- **Relationships**:
    - Many-to-One with `PortalTeamMember`
    - Many-to-One with `PortalTeamTypeRole`
- **Example Data**:

| PERSON_ID | RES_ID | RES_TYPE              | ROLE_NAME            |
|:----------|:-------|:----------------------|:---------------------|
| 53507     | 35111  | PWA_ORGANISATION_TEAM | RESOURCE_COORDINATOR |
| 53507     | 35279  | PWA_ORGANISATION_TEAM | FINANCE_ADMIN        |
| 53507     | 35279  | PWA_ORGANISATION_TEAM | APPLICATION_CREATE   |
| 53507     | 35281  | PWA_REGULATOR_TEAM    | CASE_OFFICER         |
| 53507     | 35281  | PWA_REGULATOR_TEAM    | PWA_CONSENT_VIEWER   |
| 53507     | 35281  | PWA_REGULATOR_TEAM    | ORGANISATION_MANAGER |
| 53507     | 38955  | PWA_USERS             | PWA_ACCESS           |

### `PortalTeamType`
- **View**: `portal_resource_types`
```sql
SELECT xrt.res_type
, xrt.res_type_title
, xrt.res_type_description
, xrt.scoped_within
FROM decmgr.xview_resource_types xrt;
```
- **Attributes**:
    - `type`: String
    - `title`: String
    - `description`: String
    - `portalTeamScopeType`: Enum (`PortalTeamScopeType`)
- **Example Data**:

| RES_TYPE              | RES_TYPE_TITLE         | RES_TYPE_DESCRIPTION                              | SCOPED_WITHIN |
|:----------------------|:-----------------------|:--------------------------------------------------|:--------------|
| PWA_ORGANISATION_TEAM | PWA Organisation Group | Manage access to PWAs for this organisation group | PARENT        |
| PWA_REGULATOR_TEAM    | PWA NSTA Team          | Manage access to PWAs for regulators              | UNIVERSAL_SET |
| PWA_USERS             | PWA Permissions        | Permissions for PWA Access                        | UNIVERSAL_SET |

## Overview
### Entities
- `Person`: View `people`
- `WebUserAccount`: View `user_accounts`
- `PortalTeam`: View `portal_teams`
- `PortalTeamMember`: View `portal_res_members_current`
- `PortalTeamUsage`: View `portal_resource_usages_current`
- `PortalTeamTypeRole`: View `portal_resource_type_roles`
- `PortalTeamMemberRole`: View `portal_res_memb_current_roles`
- `PortalTeamType`: View `portal_team_types`

### Relationships
- `Person` has a one-to-many relationship with `WebUserAccount`
- `PortalTeam` has a one-to-many relationship with `PortalTeamMember`
- `PortalTeamMember` has a many-to-one relationship with `Person` and `PortalTeam`
- `PortalTeamUsage` has a many-to-one relationship with `PortalTeam`
- `PortalTeamMemberRole` has a many-to-one relationship with `PortalTeamMember` and `PortalTeamTypeRole`