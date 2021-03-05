<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="cancelUrl" type="java.lang.String" -->


<#include '../layout.ftl'>

<@defaultPage htmlTitle="${appRef} withdraw public notice" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList/>
    </#if>

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

    <h2 class="govuk-heading-l">Are you sure you want to withdraw this public notice?</h2>


    <@fdsForm.htmlForm>
        <@fdsTextarea.textarea path="form.withdrawalReason" labelText="Provide a reason for why you are withdrawing this public notice" characterCount=true maxCharacterLength="4000" inputClass="govuk-!-width-two-thirds"/>

        <@fdsAction.submitButtons primaryButtonText="Withdraw public notice" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>


</@defaultPage>
