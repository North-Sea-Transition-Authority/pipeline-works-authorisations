<#include '../../../layout.ftl'>
<#import 'pipelineHuooRoleSummaryViewTemplate.ftl' as pwaPipelineHuooSummaryView>

<#-- @ftlvariable name="pageHeading" type="java.lang.String" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="markCompleteErrorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PipelineHuooUrlFactory" -->
<#-- @ftlvariable name="holderSummary" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineHuooRoleSummaryView" -->
<#-- @ftlvariable name="userSummary" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineHuooRoleSummaryView" -->
<#-- @ftlvariable name="operatorSummary" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineHuooRoleSummaryView" -->
<#-- @ftlvariable name="ownerSummary" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineHuooRoleSummaryView" -->
<#-- @ftlvariable name="summaryValidationResult" type="uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult" -->


<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading breadcrumbs=true fullWidthColumn=true>

    <@validationResult.singleErrorSummary summaryValidationResult=summaryValidationResult! />
    <@validationResult.errorSummary summaryValidationResult=summaryValidationResult! />

    <@fdsInsetText.insetText>
      <p>Groups of pipelines with associated HUOOs are shown below.</p>
      <p>Use the change/assign link for a group of pipelines to update associated HUOOs for those pipelines.</p>
      <p>Use the "Select pipelines and assign" button to choose specific pipelines to assign to HUOOs.</p>
      <p>Use the "Define pipeline split" button to define or remove sections of a pipeline associated with different HUOOs.</p>
    </@fdsInsetText.insetText>

    <@pwaPipelineHuooSummaryView.pipelineHuooRoleSummary summaryView=holderSummary urlFactory=urlFactory errorKeyPrefix="HOLDER" summaryValidationResult=summaryValidationResult!  />

    <@pwaPipelineHuooSummaryView.pipelineHuooRoleSummary summaryView=userSummary urlFactory=urlFactory errorKeyPrefix="USER" summaryValidationResult=summaryValidationResult! />

    <@pwaPipelineHuooSummaryView.pipelineHuooRoleSummary summaryView=operatorSummary urlFactory=urlFactory errorKeyPrefix="OPERATOR" summaryValidationResult=summaryValidationResult! />

    <@pwaPipelineHuooSummaryView.pipelineHuooRoleSummary summaryView=ownerSummary urlFactory=urlFactory errorKeyPrefix="OWNER" summaryValidationResult=summaryValidationResult! />

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons
        primaryButtonText="Complete"
        linkSecondaryAction=true
        secondaryLinkText="Back to task list"
        linkSecondaryActionUrl=springUrl(backUrl)
        />
    </@fdsForm.htmlForm>

</@defaultPage>