<#include '../../../layout.ftl'>
<#import 'blockCrossingsManagement.ftl' as blockCrossingManagement>
<#import 'medianLineCrossingManagement.ftl' as medianLineCrossingManagement>
<#import 'cableCrossingManagement.ftl' as cableCrossingManagement>

<#-- @ftlvariable name="errorMessage" type="String" -->

<@defaultPage htmlTitle="Blocks and crossing agreements" pageHeading="Blocks and crossing agreements" breadcrumbs=true>

    <#if errorMessage?has_content>
      <@fdsError.singleErrorSummary errorMessage=errorMessage />
    </#if>

    <@fdsTaskList.taskList>
        <@fdsTaskList.taskListSection>
            <#list tasks as entry>
                <@fdsTaskList.taskListItem itemUrl=springUrl(entry.route) itemText=entry.taskName completed=entry.completed>
                  <#list entry.taskInfoList as taskInfo>
                    <span class="govuk-tag">${taskInfo.count} ${taskInfo.countType}</span>&nbsp;
                  </#list>
                </@fdsTaskList.taskListItem>
            </#list>
        </@fdsTaskList.taskListSection>
    </@fdsTaskList.taskList>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true linkSecondaryActionUrl=springUrl(backUrl) primaryButtonText="Complete" secondaryLinkText="Back to task list"/>
    </@fdsForm.htmlForm>

</@defaultPage>