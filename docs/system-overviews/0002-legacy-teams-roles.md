# System Overview: Legacy Teams, Roles and their Privileges, and Permissions
Author(s): Harshid Dattani  
Created: 11-02-2025  
Updated: 07-03-2025  

## Overview

The system’s security model is organised into four layers:

1. **Teams:** Users are grouped by their function or organisation.
2. **Roles:** Within each team, users are assigned specific roles.
3. **Privileges:** Each role comes with a set of privileges that are associated with that role from Fox.
4. **Permissions:** High-level permissions (both core and processing) are derived by combining the associated privileges and contextual information (such as application status or involvement).

This document outlines the teams present in the system, the roles within each team, the privileges associated with those roles, and the permissions that those privileges help grant.

## 1. Regulator Team
All Regulator roles have:
- the `PWA_REGULATOR`, `PWA_WORKAREA` privileges. `PWA_APPLICATION_SEARCH` available to all but the `TEMPLATE_CLAUSE_MANAGER` role. 
- the `VIEW_PWA_PIPELINE`, `VIEW_PWA`, `SHOW_PWA_NAVIGATION` PwaPermissions
- the `CASE_MANAGEMENT_OGA`, `VIEW_CONSENT_DOCUMENT`, PwaAppProcessingPermissions
- the `VIEW` PwaApplicationPermission

| **Role (`PwaRegulatorRole`)** | **Privileges (`PwaUserPrivilege`)** | **Processing permissions (`PwaAppProcessingPermission`)**                                                                                                                                                                                                                                                 |
|-------------------------------|-------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `TEAM_ADMINISTRATOR`          | `PWA_REGULATOR_ADMIN`               |                                                                                                                                                                                                                                                                                                           |
| `ORGANISATION_MANAGER`        | `PWA_REG_ORG_MANAGE`                |                                                                                                                                                                                                                                                                                                           |
| `PWA_MANAGER`                 | `PWA_MANAGER`                       | `ACCEPT_INITIAL_REVIEW`, `ASSIGN_CASE_OFFICER`, `CONSENT_REVIEW`, `ADD_CASE_NOTE`, `SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY`, `CHANGE_OPTIONS_APPROVAL_DEADLINE`, `OGA_EDIT_PUBLIC_NOTICE`, `APPROVE_PUBLIC_NOTICE`, `EDIT_CONSENT_DOCUMENT`, `WITHDRAW_APPLICATION`, `CANCEL_PAYMENT`                         |
| `CASE_OFFICER`                | `PWA_CASE_OFFICER`                  | `ADD_CASE_NOTE`, `CLOSE_OUT_OPTIONS`, `OGA_EDIT_PUBLIC_NOTICE`, `FINALISE_PUBLIC_NOTICE`, `APPROVE_PUBLIC_NOTICE`, `CASE_OFFICER_REVIEW`, `CONFIRM_SATISFACTORY_APPLICATION`, `EDIT_CONSULTATIONS`, `WITHDRAW_CONSULTATION`, `SEND_CONSENT_FOR_APPROVAL`, `EDIT_CONSENT_DOCUMENT`, `WITHDRAW_APPLICATION` |
| `CONSENT_VIEWER`              | `PWA_CONSENT_SEARCH`                |                                                                                                                                                                                                                                                                                                           |
| `AS_BUILT_NOTIFICATION_ADMIN` | `PWA_ASBUILT_WORKAREA`              |                                                                                                                                                                                                                                                                                                           |
| `TEMPLATE_CLAUSE_MANAGER`     | `PWA_TEMPLATE_CLAUSE_MANAGE`        |                                                                                                                                                                                                                                                                                                           |


## 2. Organisation (Holder) Team
All Organisation roles have:
- the `PWA_INDUSTRY`, `PWA_WORKAREA`, `PWA_APPLICATION_SEARCH` privileges.
- the `VIEW_PWA_PIPELINE`, `VIEW_PWA`, `SHOW_PWA_NAVIGATION` PwaPermissions
- the `UPDATE_PUBLIC_NOTICE_DOC`, `VIEW_CONSENT_DOCUMENT`, `VIEW_PAYMENT_DETAILS_IF_EXISTS`, `CASE_MANAGEMENT_INDUSTRY` PwaAppProcessingPermissions
- the `VIEW` PwaApplicationPermission

| **Role (`PwaOrganisationRole`)**  | ** Additional Privileges (`PwaUserPrivilege`)** | **Additional Processing permissions (`PwaAppProcessingPermission`)** | **Additional Application permissions (`PwaApplicationPermission`)** |
|-----------------------------------|-------------------------------------------------|----------------------------------------------------------------------|---------------------------------------------------------------------|
| `TEAM_ADMINISTRATOR`              | `PWA_CONSENT_SEARCH`, `PWA_ORG_ADMIN`           |                                                                      |                                                                     |
| `APPLICATION_CREATOR`             | `PWA_CONSENT_SEARCH`, `PWA_APPLICATION_CREATE`  |                                                                      | `MANAGE_CONTACTS`                                                   |
| `APPLICATION_SUBMITTER`           | `PWA_CONSENT_SEARCH`, `PWA_APPLICATION_SUBMIT`  |                                                                      | `MANAGE_CONTACTS`, `SUBMIT`                                         |
| `FINANCE_ADMIN`                   |                                                 | `PAY_FOR_APPLICATION`                                                |                                                                     |
| `AS_BUILT_NOTIFICATION_SUBMITTER` | `PWA_ASBUILT_WORKAREA`                          |                                                                      |                                                                     |


## 3. Consultee Group Team
All Consultee roles have:
- the `PWA_CONSULTEE`, `PWA_WORKAREA`, `PWA_APPLICATION_SEARCH` privileges.
- the `UPDATE_PUBLIC_NOTICE_DOC`, `VIEW_CONSENT_DOCUMENT`, `VIEW_PAYMENT_DETAILS_IF_EXISTS` PwaAppProcessingPermissions
- the `VIEW` PwaApplicationPermission

| **Role (`ConsulteeGroupMemberRole`)** | **Additional Privileges (`PwaUserPrivilege`)** | **Additional Processing permissions (`PwaAppProcessingPermission`)** |
|---------------------------------------|------------------------------------------------|----------------------------------------------------------------------|
| `ACCESS_MANAGER`                      | `PWA_CONSULTEE_GROUP_ADMIN`                    |                                                                      |
| `RECIPIENT`                           |                                                | `ASSIGN_RESPONDER`,                                                  |
| `RESPONDER`                           |                                                | `ASSIGN_RESPONDER`, `CONSULTATION_RESPONDER`                         |


## 4. Application Contacts
_(Not a system-wide team, scoped to an application)_
All Contact roles have:
- the `PWA_INDUSTRY`, `PWA_WORKAREA` privileges.
- the `VIEW_PAYMENT_DETAILS_IF_EXISTS` PwaAppProcessingPermission
- the `VIEW` PwaApplicationPermission

| **Role (`PwaContactRole`)** | **Additional Processing permissions (`PwaAppProcessingPermission`)** | **Additional Application permissions (`PwaApplicationPermission`)** |
|-----------------------------|----------------------------------------------------------------------|---------------------------------------------------------------------|
| `ACCESS_MANAGER`            | `PAY_FOR_APPLICATION`, `MANAGE_APPLICATION_CONTACTS`                 | `MANAGE_CONTACTS`                                                   |
| `PREPARER`                  | `PAY_FOR_APPLICATION`, `UPDATE_APPLICATION`                          | `EDIT`, `SET_PIPELINE_REFERENCE`                                    |
| `VIEWER`                    |                                                                      |                                                                     |