<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="ogList" type="java.util.List<String>" -->
<#-- @ftlvariable name="pwaApplicationType" type="uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType" -->
<#-- @ftlvariable name="nonConsentedPwaMap" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="consentedPwaMap" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="showNonConsentedOptions" type="java.lang.Boolean" -->

<@defaultPage htmlTitle="Start PWA application" errorItems=errorList backLink=true backLinkUrl=springUrl(backUrl)>

    <@fdsForm.htmlForm>

        <#assign pageHeader="What PWA is this variation for?"/>
        <#if pwaApplicationType == "DEPOSIT_CONSENT">
            <#assign pageHeader="What PWA is the deposit consent linked to?"/>
        </#if>

         <#if showNonConsentedOptions>
           <@fdsFieldset.fieldset legendHeading=pageHeader legendHeadingClass="govuk-fieldset__legend--l" legendHeadingSize="h2">

             <@fdsSearchSelector.searchSelectorEnhanced path="form.consentedMasterPwaId" labelText="Select a consented PWA" options=consentedPwaMap optionalInputDefault="Select one..." />
             <p class="govuk-body">or</p>
             <@fdsSearchSelector.searchSelectorEnhanced path="form.nonConsentedMasterPwaId" labelText="Select a PWA that has been applied for but is not yet consented" options=nonConsentedPwaMap optionalInputDefault="Select one..." />
           </@fdsFieldset.fieldset>
        <#else>
          <@fdsSearchSelector.searchSelectorEnhanced path="form.consentedMasterPwaId" labelText=pageHeader options=consentedPwaMap pageHeading=true labelHeadingClass="govuk-label--l" />
        </#if>

        <#assign groups>
            <#list ogList as group> ${group} <#sep>, </#list>
        </#assign>
        <@fdsDetails.summaryDetails summaryTitle="The PWA is not in the list">
            <p>You can only access PWAs for organisations within the following groups: ${groups} </p>
            <p>The organisation must be the current holder of the PWA.</p>
            <p>If you do not have access to the PWA then you must contact the holder to create the application on your behalf. Once created they can provide you with access to the application. Alternatively, they can provide you with access to their organisation account. </p>
        </@fdsDetails.summaryDetails>

        <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Change application type" linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>

</@defaultPage>