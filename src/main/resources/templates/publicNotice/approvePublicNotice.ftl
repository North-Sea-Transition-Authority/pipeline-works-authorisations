<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="coverLetter" type="java.lang.String" -->
<#-- @ftlvariable name="requestReason" type="java.lang.String" -->


<#include '../layout.ftl'>

<@defaultPage htmlTitle="${appRef} review public notice request" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

    <@fdsForm.htmlForm>
        <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

        <h2 class="govuk-heading-l">Review public notice request</h2>


        <h3 class="govuk-heading-m"> Cover letter </h3>
        <p class="govuk-body"> ${coverLetter} </p>

        <h3 class="govuk-heading-m"> Public notice request reason </h3>
        <p class="govuk-body"> ${requestReason} </p>
    
    
        <@fdsRadio.radioGroup path="form.requestApproved" labelText="Respond to the public notice approval request" hiddenContent=true>
            <@fdsRadio.radioItem path="form.requestApproved" itemMap={"true" : "Approve"} isFirstItem=true/>
            <@fdsRadio.radioItem path="form.requestApproved" itemMap={"false" : "Reject"}>
                <@fdsTextarea.textarea path="form.requestRejectedReason" nestingPath="form.requestApproved" labelText="Rejection reason" characterCount=true maxCharacterLength="4000" inputClass="govuk-!-width-two-thirds"/>
            </@fdsRadio.radioItem>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons primaryButtonText="Submit" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>


</@defaultPage>
