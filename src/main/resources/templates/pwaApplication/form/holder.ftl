<#include '../../layout.ftl'>

<#-- @ftlvariable name="ouMap" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="ogList" type="java.util.List<String>" -->
<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="backUrl" type="String" -->

<@defaultPage htmlTitle="Consent holder" breadcrumbs=hasHolderSet>

  <@fdsError.errorSummary errorItems=errorList />

  <@fdsForm.htmlForm>

    <@fdsSearchSelector.searchSelectorEnhanced labelHeadingClass="govuk-label--l" path="form.holderOuId" labelText="Consent holder" options=ouMap pageHeading=true/>

      <@fdsInsetText.insetText>You can only create a new PWA for organisations within the following groups: <#list ogList as group> ${group} <#sep>, </#list> </@fdsInsetText.insetText>

      <#if hasHolderSet>
        <@fdsAction.submitButtons primaryButtonText="Save" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(taskListUrl) />
      <#else>
        <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Back to work area" linkSecondaryActionUrl=springUrl(workareaUrl) />
      </#if>


  </@fdsForm.htmlForm>

</@defaultPage>