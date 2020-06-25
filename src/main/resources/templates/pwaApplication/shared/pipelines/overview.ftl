<#include '../../../layout.ftl'>
<#import 'pipelineOverview.ftl' as pipelineOverview>

<#-- @ftlvariable name="pipelineTaskListItems" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineTaskListItem>" -->
<#-- @ftlvariable name="bundleSummaryViews" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadBundleSummaryView>" -->
<#-- @ftlvariable name="bundleValidationFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.BundleValidationFactory" -->
<#-- @ftlvariable name="pipelineUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineUrlFactory" -->
<#-- @ftlvariable name="errorMessage" type="String" -->
<#-- @ftlvariable name="taskListUrl" type="String" -->
<#-- @ftlvariable name="canShowAddBundleButton" type="Boolean" -->

<#macro linkButtonBlue text url>
    <@fdsAction.link linkText=text linkUrl=url linkClass="govuk-button govuk-button--blue" />
</#macro>

<@defaultPage htmlTitle="Pipelines and pipeline bundles" pageHeading="Pipelines and pipeline bundles" fullWidthColumn=true breadcrumbs=true>

  <h2 class="govuk-heading-l">Pipelines</h2>

    <#if !pipelineTaskListItems?has_content>
        <@fdsInsetText.insetText>No pipelines have been added yet.</@fdsInsetText.insetText>
    </#if>

    <@linkButtonBlue text="Add pipeline" url=springUrl(pipelineUrlFactory.getAddPipelineUrl()) />
    <#if canShowAddBundleButton>
        <@linkButtonBlue text="Add pipeline bundle" url=springUrl(pipelineUrlFactory.getAddBundleUrl()) />
    </#if>

    <#list pipelineTaskListItems as pipeline>

        <@fdsCard.card>

          <span class="govuk-caption-l">${pipeline.length}m ${pipeline.pipelineType.displayName}</span>
            <@fdsCard.cardHeader cardHeadingText="${pipeline.pipelineNumber}" />

          <hr class="govuk-section-break govuk-section-break--m"/>

            <@fdsTaskList.taskList>
                <#list pipeline.getTaskList() as task>
                    <#if task.taskInfoList?has_content>
                        <@pwaTaskListItem.taskInfoItem taskName=task.taskName taskInfoList=task.taskInfoList route=task.route/>
                    <#else>
                        <@fdsTaskList.taskListItem itemText=task.taskName itemUrl=springUrl(task.route) completed=task.completed/>
                    </#if>
                </#list>
            </@fdsTaskList.taskList>
        </@fdsCard.card>

    </#list>

    <#if pipelineTaskListItems?size gt 4>
        <@linkButtonBlue text="Add pipeline" url=springUrl(pipelineUrlFactory.getAddPipelineUrl()) />
    </#if>

    <#if pipelineTaskListItems?has_content>
      <hr class="govuk-section-break govuk-section-break--l"/>
    </#if>

  <h2 class="govuk-heading-l">Pipeline bundles</h2>
  <!-- Show bundles if not empty in case there's a validation error -->
    <#if canShowAddBundleButton || bundleSummaryViews?size gt 0>

        <#if canShowAddBundleButton>
          <@linkButtonBlue text="Add pipeline bundle" url=springUrl(pipelineUrlFactory.getAddBundleUrl()) />
        </#if>

        <#list bundleSummaryViews as bundle>
            <@fdsCard.card cardClass=(!bundleValidationFactory?has_content || bundleValidationFactory.isValid(bundle))?string("", "fds-card--error")>
                <#if bundleValidationFactory?has_content && !bundleValidationFactory.isValid(bundle)>
                  <span class="govuk-error-message">${bundleValidationFactory.getErrorMessage(bundle)}</span>
                </#if>
                <@fdsCard.cardHeader cardHeadingText=bundle.bundleName>
                    <@fdsCard.cardAction cardLinkText="Edit" cardLinkUrl=springUrl(pipelineUrlFactory.getEditBundleUrl(bundle.bundleId)) />
                    <@fdsCard.cardAction cardLinkText="Remove" cardLinkUrl=springUrl(pipelineUrlFactory.getRemoveBundleUrl(bundle.bundleId)) />
                </@fdsCard.cardHeader>

              <br/><h3 class="govuk-heading-s">Pipelines</h3>

              <ul class="govuk-list">
                  <#list bundle.pipelineReferences as pipelineReference>
                    <li>${pipelineReference}</li>
                  </#list>
              </ul>
            </@fdsCard.card>

        </#list>
    <#else>
        <@fdsInsetText.insetText>
          At least two pipelines must be added before creating a pipeline bundle.
        </@fdsInsetText.insetText>
    </#if>

    <#if bundleSummaryViews?has_content>
      <hr class="govuk-section-break govuk-section-break--l"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(taskListUrl) errorMessage=errorMessage!/>
    </@fdsForm.htmlForm>

</@defaultPage>