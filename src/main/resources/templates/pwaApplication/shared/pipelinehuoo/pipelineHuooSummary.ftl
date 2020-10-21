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
      <p>Use the change/assign link shown for each group of pipelines to assign or update HUOOs for those pipelines.</p>
      <p>If the group of pipelines you want to assign HUOOs for is not shown then use the "Select pipelines and assign" button to choose which pipelines to assign.</p>
      <p>If you have a pipeline that has different HUOOs responsible for different parts of it you can use the "Define pipeline split" button to define each section with different HUOOs.</p>
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