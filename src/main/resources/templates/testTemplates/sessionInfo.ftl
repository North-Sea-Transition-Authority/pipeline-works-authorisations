<#include '../layout.ftl'>

<@defaultPage htmlTitle="Session integration test" pageHeading="Session integration test" twoThirdsColumn=false>
  <@fdsCard.card>
    <#if user?has_content>

      <ul class="govuk-list govuk-list--bullet">

        <li>wuaId: ${user.wuaId}</li>

        <li>fname: ${user.forename}</li>

        <li>sname: ${user.surname}</li>

        <li>email: ${user.emailAddress}</li>

        <li>privs:

          <ul class="govuk-list govuk-list--bullet">
            <#list user.userPrivileges as priv>
              <li>${priv.name()}</li>
            </#list>
          </ul>

        </li>

      </ul>

    <#else>
      <p class="govuk-body">user is unauthenticated</p>
    </#if>
  </@fdsCard.card>
</@defaultPage>