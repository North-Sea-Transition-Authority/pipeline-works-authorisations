<#include '../../layout.ftl'>

<#-- @ftlvariable name="ouMap" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="ogList" type="java.util.List<String>" -->
<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="backUrl" type="String" -->
<#-- @ftlvariable name="ogaServiceDeskEmail" type="String" -->

<@defaultPage htmlTitle="Consent holder" errorItems=errorList>

  <@fdsForm.htmlForm>

    <@fdsSearchSelector.searchSelectorEnhanced labelHeadingClass="govuk-label--l" path="form.holderOuId" labelText="Consent holder" options=ouMap pageHeading=true/>

      <#assign groups>
        <#list ogList as group> ${group} <#sep>, </#list>
      </#assign>
      <@fdsDetails.summaryDetails summaryTitle="The holder organisation is not in the list">
        <p>You can only create a new PWA for organisations within the following groups: ${groups}. </p>
        <p>If the group you need to create a PWA for is not shown above then you must contact the holder to create the new PWA application on your behalf and provide you with access to the application. Alternatively, they can provide you with access to their organisation account. </p>
        <p>If you already have access to the holder group but your legal entity as per companies house is not shown in the list then provide the NSTA with the holder company name, address including postcode and companies house registration number to add to the PWA service: ${ogaServiceDeskEmail} </p>
      </@fdsDetails.summaryDetails>
      <@fdsTextInput.textInput
          inputClass="govuk-visually-hidden"
          path="form.resourceType"
          labelText=""/>

      <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Back to work area" linkSecondaryActionUrl=springUrl(workareaUrl) />



  </@fdsForm.htmlForm>

</@defaultPage>
