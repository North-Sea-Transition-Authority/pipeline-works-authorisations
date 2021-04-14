<#include '../../../layout.ftl'>
<#import 'pipelineHuooTableSelection.ftl' as pwaPipelineHuooTableSelection/>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="pageHeading" type="java.lang.String" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="submitButtonText" type="java.lang.String" -->
<#-- @ftlvariable name="pickableHuooPipelineOptions" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableHuooPipelineOption>" -->
<#-- @ftlvariable name="availableTreatyOptions" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="pickableOrgDetails" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PickableOrganisationUnitDetail>" -->

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading breadcrumbs=false fullWidthColumn=true errorItems=errorList>

    <@fdsInsetText.insetText>
        You must select at least one option to assign to the selected pipelines
    </@fdsInsetText.insetText>

    <@fdsDetails.summaryDetails summaryTitle="Show selected pipelines">
        <@pwaPipelineHuooTableSelection.pickablePipelineTableSelection
            path="form.pickedPipelineStrings"
            pickableHuooPipelineOptions=pickableHuooPipelineOptions
            readOnlySelected=true/>



    </@fdsDetails.summaryDetails>

    <@fdsForm.htmlForm>
        <#if availableTreatyOptions?hasContent>
            <@fdsCheckbox.checkboxes
                path="form.treatyAgreements"
                checkboxes=availableTreatyOptions
                fieldsetHeadingText="Select treaties"
                smallCheckboxes=true/>
        </#if>

        <#if pickableOrgDetails?hasContent>
            <@pwaOrgDetailTableSelection.pickableOrgDetailsTableSelection path="form.organisationUnitIds" pickableOrgDetailOptions=pickableOrgDetails caption="Select organisations"/>
        </#if>

        <@fdsAction.submitButtons
            primaryButtonText=submitButtonText
            secondaryButtonText="Back to pipeline selection"
        />
    </@fdsForm.htmlForm>

</@defaultPage>