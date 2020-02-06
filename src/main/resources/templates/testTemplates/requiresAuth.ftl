<#include '../layout.ftl'>

<@defaultPage htmlTitle="This page requires auth" pageHeading="This page requires auth" twoThirdsColumn=false>
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