# Pipeline Works Authorisations

## Project setup

### Prerequisites
* Java 11
* Node LTS + NPM
* IntelliJ Ultimate

### Steps

#### Initialise the Fivium Design System
* `git submodule update --init --recursive`    
* `cd fivium-design-system-core && npm install && npx gulp build && cd ..`

#### Build frontend components
* `npm install`
* `npx gulp buildAll`

#### Configure the following environment variables


| Environment Variable | Description |
| -------------------- |-------------|
| DB_SCHEMA_NAME | Database schema to connect as. E.g. `PWA_XX` This schema will be created for you by Flyway|
| CONTEXT_SUFFIX | A unique per developer suffix string to apply to the application context path. E.g. your initials |


#### Create the Flyway user

This must be your DB_SCHEMA_NAME with '_flyway' appended to the end.


```oraclesqlplus
CREATE USER pwa_xx_flyway IDENTIFIED BY "dev1"
/

GRANT UNLIMITED TABLESPACE TO pwa_xx_flyway WITH ADMIN OPTION
/

GRANT
  CREATE SESSION, 
  CREATE USER,
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
  CREATE ANY SEQUENCE,
  SELECT ANY SEQUENCE,
  CREATE ANY PROCEDURE,
  GRANT ANY OBJECT PRIVILEGE
TO pwa_xx_flyway WITH ADMIN OPTION
/ 

GRANT EXECUTE ON decmgr.contact TO pwa_xx_flyway
/
```
This user must be created before the app runs for the first time on a new DB. All migrations will be run by this flyway user.

#### Set the active profile
Set the profile to `development` in your run configuration

#### Setup Checkstyle
* Install the Checkstyle-IDEA plugin (from third-party repositories)
* Go to Settings > Checkstyle
* Add a "Configuration File"
* "Use a local Checkstyle file"
* Select `ide/checkstyle.xml`
* Check box for "Store relative to project location" 
* Check the "Active" box next to the new profile
* On the Git commit dialog tick the 'Scan with Checkstyle' box in the 'Before Commit' section
  
  Note that Checkstyle rules are checked during the build process and any broken rules will fail the build.
    
#### Proxy routes to enable session sharing (Skip to 7 if you don't need to access authenticated pages)

To enable Spring to access the fox session, you must access your local instance under the same hostname and application context (edu-app3.decc.local/engedudev1/).

The easiest way to do this is to add a ProxyPass rule to Apache running on edu-dev-app3.
 
Edit the puppet yaml at `//Infrastructure/Puppet/fiv-lemms1/environments/development/hieradata/edu-dev-app3.decc.local.yaml` and add a ProxyPass rule forwarding traffic under your CONTEXT_SUFFIX to your local machine e.g.

`ProxyPass /engedudev1/jb/ http://fivium-jbarnett.fivium.local:8081/engedudev1/jb/`

This rule MUST be placed before the Fox proxypass `ProxyPass /engedudev1/ ajp://localhost:8041/engedudev1/`  because the Fox route has a higher scope which
would capture your local route request.

Run `puppet agent -t` on  edu-dev-app3 to apply your change. You can then access your local instance at `http://edu-app3.decc.local/engedudev1/CONTEXT_SUFFIX/session-info`

#### Run the app
IntelliJ should auto detect the Spring application and create a run configuration.
Run the project and navigate to `localhost:8081/engedudev1/CONTEXT_SUFFIX/session-info` or `edu-dev-app3.decc.local/engedudev1/CONTEXT_SUFFIX/session-info` if you have set up the proxy
There are some debug endpoints to test your session integration:

* `/session-info` - Shows the current session authentication status, and user account info.
* `/requires-auth` - Same as `session-info` but requires authentication. Use this to test the Fox login/callback redirection process. 