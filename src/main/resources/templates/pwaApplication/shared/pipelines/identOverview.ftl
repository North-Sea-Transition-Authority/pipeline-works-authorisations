<#include '../../../layout.ftl'>
<#import 'pipelineOverview.ftl' as pipelineOverviewMacro>

<#-- @ftlvariable name="pipelineOverview" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview" -->
<#-- @ftlvariable name="addIdentUrl" type="String" -->

<@defaultPage htmlTitle="${pipelineOverview.pipelineNumber} idents" pageHeading="${pipelineOverview.pipelineNumber} idents" breadcrumbs=true>

    <@pipelineOverviewMacro.header pipeline=pipelineOverview />

    <@fdsAction.link linkText="Add ident" linkUrl=springUrl(addIdentUrl) linkClass="govuk-button govuk-button--blue" />

</@defaultPage>