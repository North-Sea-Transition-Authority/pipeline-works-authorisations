<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="nonConsentedPwaMap" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="consentedPwaMap" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="showNonConsentedOptions" type="java.lang.Boolean" -->


<#include '../layout.ftl'>


<@defaultPage htmlTitle="Generate variation application" fullWidthColumn=false wrapperWidth=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList/>
    </#if>
    <#assign pageHeader="What PWA is this variation for?"/>


    <@fdsForm.htmlForm>

        <#if showNonConsentedOptions>
           <@fdsFieldset.fieldset legendHeading=pageHeader legendHeadingClass="govuk-fieldset__legend--l" legendHeadingSize="h2">

             <@fdsSearchSelector.searchSelectorEnhanced path="form.consentedMasterPwaId" labelText="Select a consented PWA" options=consentedPwaMap optionalInputDefault="Select one..." />
             <p class="govuk-body">or</p>
             <@fdsSearchSelector.searchSelectorEnhanced path="form.nonConsentedMasterPwaId" labelText="Select a PWA that has been applied for but is not yet consented" options=nonConsentedPwaMap optionalInputDefault="Select one..." />
           </@fdsFieldset.fieldset>
        <#else>
          <@fdsSearchSelector.searchSelectorEnhanced path="form.consentedMasterPwaId" labelText=pageHeader options=consentedPwaMap pageHeading=true labelHeadingClass="govuk-label--l" />
        </#if>

        <@fdsAction.submitButtons primaryButtonText="Generate variation application" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>



</@defaultPage>



  