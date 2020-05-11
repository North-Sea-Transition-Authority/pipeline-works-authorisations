<#include '../../../layout.ftl'>
<#import 'blockCrossingsManagement.ftl' as blockCrossingManagement>
<#import 'medianLineCrossingManagement.ftl' as medianLineCrossingManagement>
<#import 'cableCrossingManagement.ftl' as cableCrossingManagement>

<@defaultPage htmlTitle="Crossing agreements" pageHeading="Crossing agreements" breadcrumbs=true>

    <@fdsTaskList.taskList>
        <@fdsTaskList.taskListSection>
            <#list tasks as entry>
                <@fdsTaskList.taskListItem itemUrl=springUrl(entry.route) itemText=entry.taskName completed=entry.completed>
                  <#list entry.labels as label>
                    <span class="govuk-tag govuk-tag--${label.colour.cssName}">${label.displayText}</span>&nbsp;
                  </#list>
                </@fdsTaskList.taskListItem>
            </#list>
        </@fdsTaskList.taskListSection>
    </@fdsTaskList.taskList>

</@defaultPage>