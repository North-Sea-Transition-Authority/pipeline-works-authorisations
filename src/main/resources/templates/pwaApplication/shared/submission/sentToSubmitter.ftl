<#include '../../../layout.ftl'>

<#-- @ftlvariable name="isFirstVersion" type="java.lang.Boolean" -->
<#-- @ftlvariable name="submitterPersonName" type="String" -->
<#-- @ftlvariable name="applicationReference" type="String" -->
<#-- @ftlvariable name="workAreaUrl" type="String" -->

<@defaultPage htmlTitle="Application sent to submitter">

  <div class="govuk-panel govuk-panel--confirmation">
    <h1 class="govuk-panel__title">
      <#if isFirstVersion>Application<#else>Update</#if> sent to
      <br/>
      ${submitterPersonName}
    </h1>
    <div class="govuk-panel__body">
      <p>Your reference number<br><strong>${applicationReference}</strong></p>
    </div>
  </div>

  <p class="govuk-body">
    An email has been sent to ${submitterPersonName} asking them to review and submit your application.
  </p>

  <@fdsAction.link linkClass="govuk-link govuk-!-font-size-19" linkText="Go back to work area" linkUrl="${springUrl(workAreaUrl)}"/>

</@defaultPage>