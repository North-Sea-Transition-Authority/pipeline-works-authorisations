/*
  The principal name in the spring_sessions table is currently the hash code of the authentication object.
  This isn't helpful when we move to the account service as PWA will need to delete sessions
  based on WUA_ID being sent from the IDP. We are forcing a delete of all session
  data so we all new sessions register the principal as expected.
*/
DELETE FROM ${datasource.user}.spring_session_attributes;

DELETE FROM ${datasource.user}.spring_session;