<#-- @ftlvariable name="startUrl" type="String" -->
<#-- @ftlvariable name="formattedDuration" type="java.lang.String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Start new PWA" pageHeading="Start new PWA" backLink=true>

  <@fdsStartPage.startPage startActionText="Start" startActionUrl=startUrl>

    <p class="govuk-body">A pipeline works authorisation or variation should be in place before any pipeline or pipeline system construction or modification works of begins.</p>

    <p class="govuk-body">Where there are no objections, it takes approximately ${formattedDuration} from receipt of a satisfactory application to issuing the authorisation.</p>

  </@fdsStartPage.startPage>

</@defaultPage>