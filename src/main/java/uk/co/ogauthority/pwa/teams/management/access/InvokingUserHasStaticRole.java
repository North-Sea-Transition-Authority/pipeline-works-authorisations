package uk.co.ogauthority.pwa.teams.management.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InvokingUserHasStaticRole {
  TeamType teamType();
  Role role();
}
