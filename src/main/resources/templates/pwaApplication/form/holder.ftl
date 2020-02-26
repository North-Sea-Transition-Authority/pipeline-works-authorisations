<#include '../../layout.ftl'>

<#-- @ftlvariable name="ouMap" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="backUrl" type="String" -->

<@defaultPage htmlTitle="Consent holder" pageHeading="Consent holder" breadcrumbs=hasHolderSet>

  <@fdsInsetText.insetText>Consent holders are...</@fdsInsetText.insetText>

  <@fdsError.errorSummary errorItems=errorList />

  <@fdsForm.htmlForm>

    <@fdsSelect.select path="form.holderOuId" labelText="" options=ouMap />

      <#if hasHolderSet>
        <@fdsAction.submitButtons primaryButtonText="Save" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(taskListUrl) />
      <#else>
        <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Back to workarea" linkSecondaryActionUrl=springUrl(workareaUrl) />
      </#if>

  </@fdsForm.htmlForm>

</@defaultPage>