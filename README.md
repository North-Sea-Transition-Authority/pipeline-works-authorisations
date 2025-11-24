# Pipeline Works Authorisations

## Project setup

### Prerequisites
#### Downloads/Installation
* IntelliJ Ultimate
* Git Bash
* Java 21
* Python v2.7 - ([Official Download](https://www.python.org/downloads/release/python-278/))
* Node LTS + NPM v18 - ([Official Download](https://nodejs.org/download/release/v12.22.12/))
* Docker

#### Permissions / Project Access
* TPM - PWA Project Access - ([Location](https://tpm.fivium.co.uk/index.php/prj/view/95))

### Steps

#### Initialise the Fivium Design System
* In Git Bash, from Project Home Directory
* `git submodule update --init --recursive --remote`    
* `cd fivium-design-system-core`
* `npm install && npx gulp buildAll`
* `cd ..`
Output should claim build successful.

#### Build frontend components
* `npm install`
* `npx gulp buildAll`

#### Configure the following environment variables

##### Regardless of profile

| **Environment Variable**                   | **Description**                                                                                |
|--------------------------------------------|------------------------------------------------------------------------------------------------|
| PWA_EPMQ_SNS_SQS_AWS_ACCESS_KEY_ID         | AWS access key id for SNS/SQS. For local: https://tpm.fivium.co.uk/index.php/pwd/view/2134     |
| PWA_EPMQ_SNS_SQS_AWS_SECRET_ACCESS_KEY     | AWS secret access key for SNS/SQS. For local: https://tpm.fivium.co.uk/index.php/pwd/view/2134 |
| PWA_EPMQ_SNS_SQS_AWS_REGION_ID (optional)  | The AWS region to run in. Defaults to `eu-west-2`                                              |
| PWA_EPMQ_ENVIRONMENT_SUFFIX                | Something unique per environment, e.g. `dev`. For local dev this can be your initials          |

##### Development profile
| Environment Variable        | Description                                                                                                                       |
|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| CONTEXT_SUFFIX              | A unique per developer suffix string to apply to the application context path. E.g. your initials in lower case                   |
| DB_SCHEMA_NAME              | Database schema to connect as. E.g. `PWA_XX` Where XX is recommended to be your initials in upper case                            |
| PWA_GOVUK_NOTIFY_API_KEY    | The API Key for GOV.UK Notify. ([TPM Link](https://tpm.fivium.co.uk/index.php/pwd/view/1384))                                     |
| PWA_TEST_EMAIL_RECIPIENT    | Email address(es) CSV to send test emails to (use your own email address for local dev)                                           |
| PWA_GOVUK_PAY_API_KEY       | The Test GOV.UK Pay Api key for OGA apps. ([TPM Link](https://tpm.fivium.co.uk/index.php/pwd/view/1723))                          |
| ANALYTICS_APP_API_SECRET    | The api secret for the analytics collection endpoint (app) ([TPM Link](https://tpm.fivium.co.uk/index.php/pwd/view/1855))         |
| ANALYTICS_GLOBAL_API_SECRET | The api secret for the analytics collection endpoint (portal-wide) ([TPM Link](https://tpm.fivium.co.uk/index.php/pwd/view/1865)) |

##### Production profile
| Environment Variable           | Description                                                                                                                                                                                                                                                 |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| CONTEXT_SUFFIX                 | A unique per developer suffix string to apply to the application context path. E.g. your initials in lower case                                                                                                                                             |
| DB_SCHEMA_NAME                 | Database schema to connect as. E.g. `PWA_XX` Where XX is recommended to be your initials in upper case                                                                                                                                                      |
| PWA_GOVUK_NOTIFY_API_KEY       | The API Key for GOV.UK Notify. ([TPM Link](https://tpm.fivium.co.uk/index.php/pwd/view/1384))                                                                                                                                                               |
| PWA_TEST_EMAIL_RECIPIENT       | Email address(es) CSV to send test emails to (use your own email address for local dev)                                                                                                                                                                     |
| PWA_GOVUK_PAY_API_KEY          | The Test GOV.UK Pay Api key for OGA apps. ([TPM Link](https://tpm.fivium.co.uk/index.php/pwd/view/1723))                                                                                                                                                    |                                            
| ANALYTICS_APP_API_SECRET       | The api secret for the analytics collection endpoint (app) ([TPM Link](https://tpm.fivium.co.uk/index.php/pwd/view/1855))                                                                                                                                   |
| ANALYTICS_GLOBAL_API_SECRET    | The api secret for the analytics collection endpoint (portal-wide) ([TPM Link](https://tpm.fivium.co.uk/index.php/pwd/view/1865))                                                                                                                           |
| PWA_API_PRE_SHARED_KEY         | The API key used to validate requests from the Energy Portal API                                                                                                                                                                                            |
| PWA_SAML_CERTIFICATE           | The x509 certificate string                                                                                                                                                                                                                                 |
| PWA_ENERGY_PORTAL_LOGOUT_KEY   | The pre-shared logout key for the energy portal.                                                                                                                                                                                                            |
| PWA_ENERGY_PORTAL_API_URL      | The energy portal API URL                                                                                                                                                                                                                                   |
| PWA_ENERGY_PORTAL_API_TOKEN    | Security key for access the energy portal API                                                                                                                                                                                                               |
| S3_ACCESS_TOKEN                | S3 username for document uploads / downloads                                                                                                                                                                                                                |                                                                                                                                                                                              |
| S3_SECRET_TOKEN                | S3 secret for document uploads / downloads                                                                                                                                                                                                                  |                                                                                                                                                                                             |
| S3_DEFAULT_BUCKET              | S3 default bucket for document uploads / downloads                                                                                                                                                                                                          |
| CLAMAV_HOST                    | Virus scanner host location                                                                                                                                                                                                                                 |                                                                                                                                                                                            |
| FILE_UPLOAD_MAX_ALLOWED_SIZE   | Maximum file upload size in bytes                                                                                                                                                                                                                           |                                                                                                                                                                                           |
| FILE_UPLOAD_ALLOWED_EXTENSIONS | Allowed file extensions for document uploads                                                                                                                                                                                                                |
| EPAS_SAML_ENTITY_ID            | The entity ID is either:<ul><li>`energy-portal-accounts-service-dev` for dev/local</li><li>`energy-portal-accounts-service-st` for st</li><li>`energy-portal-accounts-service-uat` for uat</li><li>`energy-portal-accounts-service-prod` for prod</li></ul> |
| EPAS_SAML_LOGIN_URL            | The login url e.g. https://nsta.itportal.dev.fivium.co.uk/accounts/saml/login                                                                                                                                                                               |
| EPAS_LOGOUT_REQUEST_URL        | The logout url e.g https://nsta.itportal.dev.fivium.co.uk/accounts/service-provider-sign-out                                                                                                                                                                |
| EPAS_SAML_BASE_URL             | The base url (e.g. https://nsta.itportal.dev.fivium.co.uk)                                                                                                                                                                                                  |

##### Devtools profile
| Environment Variable   | Description |
|------------------------|-------------|
| MIGRATION_S3_BUCKET    |             |
| MIGRATION_CSV_FILE_KEY |             |

## Logging

PWA can log in either JSON or text mode.

In order to turn on JSON logging, set the profile 'json-logging'. This will automatically include any MDC attributes.

#### Create the Flyway user & Intialise the Database


To create the flyway user for the first time:
* Open Toad and connect to:

  | Connection Credentials |                    |
  |------------------------|--------------------|
  | User schema:           | xviewmgr           |
  | Password:              |                    |
  | Host:                  | db-ogadev1.sb2.dev | 
  | Port:                  | 1521               |
  | SID:                   | OGADEV1            |


* Run the below script to create your user 
* Where `XX` is your DB_SCHEMA_NAME

```oraclesqlplus
CREATE USER pwa_xx_flyway IDENTIFIED BY "dev1"
/

GRANT UNLIMITED TABLESPACE TO pwa_xx_flyway WITH ADMIN OPTION
/

GRANT
  CREATE SESSION, 
  CREATE USER,
  DROP ANY TABLE,
  CREATE ANY TABLE,
  CREATE TABLE, -- Not covered by above grant. They are different.
  CREATE ANY VIEW,
  CREATE ANY INDEX, 
  SELECT ANY TABLE,
  DELETE ANY TABLE,
  LOCK ANY TABLE,
  INSERT ANY TABLE, 
  UPDATE ANY TABLE,
  ALTER ANY TABLE,
  DROP ANY INDEX,
  CREATE ANY SEQUENCE,
  SELECT ANY SEQUENCE,
  DROP ANY SEQUENCE,
  CREATE ANY PROCEDURE,
  GRANT ANY OBJECT PRIVILEGE,
  DROP ANY VIEW
TO pwa_xx_flyway WITH ADMIN OPTION
/
GRANT EXECUTE ON decmgr.contact TO pwa_xx_flyway
/
```
This user must be created before the app runs for the first time on a new DB. All migrations will be run by this flyway user.

If someone has deleted the distributed tables that various PWA migrations expect to exist, run the following script to add them back:

```oraclesqlplus
CREATE TABLE decmgr.pipeline_authorisations (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
);

CREATE TABLE decmgr.xview_pipeline_auth_details (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
  ,pa_id NUMBER NOT NULL
);

CREATE TABLE decmgr.xview_pipeline_company_hist (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
);

CREATE TABLE decmgr.xview_pipelines_history (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
);

CREATE TABLE decmgr.xview_pipeline_app_details (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
  ,status_control VARCHAR(1)
);

CREATE TABLE decmgr.pipeline_authorisation_details (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
);
```

#### Set the active profile
Set the profile to `development, test-harness` in your run configuration

#### Setup Checkstyle
* Install the Checkstyle-IDEA plugin (from third-party repositories)
* Go to Settings > Checkstyle
* Set Checkstyle Version - `8.40`
* Add a "Configuration File"
* "Use a local Checkstyle file"
* Select `ide/checkstyle.xml`
* Check box for "Store relative to project location"
* Leave Checkstyle-Suppression blank
* Check the "Active" box next to the new profile
* On the Git commit dialog tick the 'Scan with Checkstyle' box in the 'Before Commit' section
* After completing the above steps you must exit and restart intellij (This requires closing all associated windows).
  
Note that Checkstyle rules are checked during the build process and any broken rules will fail the build.
    
#### Run local fox engine to enable session sharing
To enable Spring to access the fox session, you must run a local fox instance on your machine. To do this run the compose file provided in `/devtools-pwa/local-dev-compose.yml`.

This will start a fox4 instance listening on `localhost:8080`.

See https://fivium.atlassian.net/wiki/spaces/JAVA/pages/15368483/Java+development+environment+setup#Javadevelopmentenvironmentsetup-Docker if you don't have Docker setup, or don't have the `repo1.dev.fivium.local` registry marked as allowing insecure connections.

#### Run the app
IntelliJ should auto detect the Spring application and create a run configuration.\
Run the project and wait for terminal output similar to the below example:
```
Started PipelineWorksAuthorisationApplication in 26.086 seconds (JVM running for 26.832)
```

You can then reach the work area on http://localhost:8081/engedudev1/xx/work-area (where xx is your `CONTEXT_SUFFIX` (WARNING: case-sensitive)).
You will be redirected to your local fox instance for authentication, and then redirected back to your local PWA instance with an active session.

### GOV.UK Notify setup
To add new or modify email templates you will need to be invited to the GOV.UK notification service (https://www.notifications.service.gov.uk/) by a member of the team who already has access.

## Documentation

The [Architectural Decision Records](https://adr.github.io/) pattern should be used to document important technical decisions and the reasoning behind them. This project uses the [MADR](https://adr.github.io/madr/) format. Records can be found in the `docs/adr` folder. 
