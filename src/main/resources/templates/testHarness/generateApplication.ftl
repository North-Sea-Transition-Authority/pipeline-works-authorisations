<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="applicationTypeMap" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="appTypesForPipelines" type="java.lang.String" -->
<#-- @ftlvariable name="appStatusesForCaseOfficer" type="java.lang.String" -->



<#include '../layout.ftl'>


<@defaultPage htmlTitle="Generate application" pageHeading="Generate application" fullWidthColumn=false wrapperWidth=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList/>
    </#if>


    <@fdsForm.htmlForm>

        <@fdsSearchSelector.searchSelectorEnhanced path="form.applicationType" labelText="Select an application type" options=applicationTypeMap labelHeadingClass="govuk-label--l" />

        <@fdsSearchSelector.searchSelectorEnhanced path="form.resourceType" labelText="Select a resource type" options=resourceTypeMap labelHeadingClass="govuk-label--l" />

        <@fdsSearchSelector.searchSelectorEnhanced path="form.applicationStatus" labelText="Select an application status" options=applicationStatusMap labelHeadingClass="govuk-label--l" />

        <@fdsTextInput.textInput path="form.pipelineQuantity" labelText="How many pipelines do you want to create?" hintText="Only required for: ${appTypesForPipelines}" inputClass="govuk-input--width-10"/>

        <@fdsSearchSelector.searchSelectorEnhanced path="form.assignedCaseOfficerId" options=caseOfficerCandidates labelText="Select a case officer" hintText="Only required for: ${appStatusesForCaseOfficer}" />

        <@fdsSearchSelector.searchSelectorEnhanced path="form.applicantWuaId" options=applicantUsersMap labelText="Select an applicant" />


        <@fdsAction.submitButtons primaryButtonText="Generate application" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>



</@defaultPage>



