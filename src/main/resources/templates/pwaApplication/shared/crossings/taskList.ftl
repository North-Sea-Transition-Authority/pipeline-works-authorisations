<#include '../../../layout.ftl'>
<#import 'blockCrossingsManagement.ftl' as blockCrossingManagement>
<#import 'medianLineCrossingManagement.ftl' as medianLineCrossingManagement>
<#import 'cableCrossingManagement.ftl' as cableCrossingManagement>

<#-- @ftlvariable name="errorMessage" type="String" -->
<#-- @ftlvariable name="tasks" type="java.util.List<uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry>" -->

<@defaultPage htmlTitle="Blocks and crossing agreements" breadcrumbs=true>

    <#if errorMessage?has_content>
      <@fdsError.singleErrorSummary errorMessage=errorMessage />
    </#if>

    <h1 class="govuk-heading-xl">Blocks and crossing agreements</h1>

    <@fdsTaskList.taskList>
        <@fdsTaskList.taskListSection>
            <#list tasks as entry>
                <@pwaTaskListItem.taskInfoItem taskName=entry.taskName taskInfoList=entry.taskInfoList route=entry.route isCompleted=entry.completed/>
            </#list>
        </@fdsTaskList.taskListSection>
    </@fdsTaskList.taskList>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true linkSecondaryActionUrl=springUrl(backUrl) primaryButtonText="Complete" secondaryLinkText="Back to task list"/>
    </@fdsForm.htmlForm>

</@defaultPage>