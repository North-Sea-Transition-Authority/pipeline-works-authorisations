<#-- @ftlvariable name="pageHeading" type="String" -->
<#-- @ftlvariable name="typeDisplay" type="String" -->
<#-- @ftlvariable name="buttonUrl" type="String" -->
<#-- @ftlvariable name="formattedDuration" type="java.lang.String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading backLink=true>

  <@fdsStartPage.startPage startActionText="Start ${typeDisplay}" startActionUrl=buttonUrl>

    <p class="govuk-body">The Holder must make an application very early in the process regarding any proposed changes to
      the Holder, User, Operator or Owner information for OGA’s consideration.</p>

    <p class="govuk-body">Consent will only be issued following the date of execution of the deed of the licence transfer
      and/or when the OGA is content.</p>

  </@fdsStartPage.startPage>

</@defaultPage>