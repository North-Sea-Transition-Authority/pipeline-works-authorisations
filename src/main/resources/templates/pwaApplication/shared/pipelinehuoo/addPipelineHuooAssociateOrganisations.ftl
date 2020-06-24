<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pageHeading" type="java.lang.String" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="submitButtonText" type="java.lang.String" -->
<#-- @ftlvariable name="pickablePipelineOptions" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickablePipelineOption>" -->
<#-- @ftlvariable name="pickableOrgDetails" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PickableOrganisationUnitDetail>" -->

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading breadcrumbs=false fullWidthColumn=true>

        <@fdsDetails.summaryDetails summaryTitle="Show selected pipelines">
            <@pwaPipelineTableSelection.pickablePipelineTableSelection
            path="form.pickedPipelineStrings"
            pickablePipelineOptions=pickablePipelineOptions
            readOnlySelected=true/>

        </@fdsDetails.summaryDetails>

    <@fdsForm.htmlForm>

        <@pwaOrgDetailTableSelection.pickableOrgDetailsTableSelection path="form.organisationUnitIds" pickableOrgDetailOptions=pickableOrgDetails/>

        <@fdsAction.submitButtons
        primaryButtonText=submitButtonText
        secondaryButtonText="Back to pipeline selection"
        />
    </@fdsForm.htmlForm>

</@defaultPage>