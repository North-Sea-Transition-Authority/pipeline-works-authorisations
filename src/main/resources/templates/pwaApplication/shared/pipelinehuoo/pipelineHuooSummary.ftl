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


<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading breadcrumbs=true fullWidthColumn=true>
  <h2 class="govuk-heading-m">Holders</h2>
    <@pwaPipelineHuooSummaryView.pipelineHuooRoleSummary summaryView=holderSummary />

    <@fdsAction.link linkText="Select pipelines and assign holders"  linkUrl=springUrl(urlFactory.getAddHolderPipelineRoleUrl()) linkClass="govuk-button govuk-button--blue"/>
  <h2 class="govuk-heading-m">Users</h2>
    <@pwaPipelineHuooSummaryView.pipelineHuooRoleSummary summaryView=userSummary />

    <@fdsAction.link linkText="Select pipelines and assign users"  linkUrl=springUrl(urlFactory.getAddUserPipelineRoleUrl()) linkClass="govuk-button govuk-button--blue"/>
  <h2 class="govuk-heading-m">Operators</h2>
    <@pwaPipelineHuooSummaryView.pipelineHuooRoleSummary summaryView=operatorSummary />

    <@fdsAction.link linkText="Select pipelines and assign operators"  linkUrl=springUrl(urlFactory.getAddOperatorPipelineRoleUrl()) linkClass="govuk-button govuk-button--blue"/>
  <h2 class="govuk-heading-m">Owners</h2>
    <@pwaPipelineHuooSummaryView.pipelineHuooRoleSummary summaryView=ownerSummary />

    <@fdsAction.link linkText="Select pipelines and assign owners"  linkUrl=springUrl(urlFactory.getAddOwnerPipelineRoleUrl()) linkClass="govuk-button govuk-button--blue"/>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons
        errorMessage=markCompleteErrorMessage!""
        primaryButtonText="Complete"
        linkSecondaryAction=true
        secondaryLinkText="Back to task list"
        linkSecondaryActionUrl=springUrl(backUrl)
        />
    </@fdsForm.htmlForm>

</@defaultPage>