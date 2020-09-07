<#include '../../../layout.ftl'>

<#macro tab taskListGroups industryFlag=false>

  <@fdsTaskList.taskList>

      <#list taskListGroups as taskGroup>
          <@fdsTaskList.taskListSection sectionHeadingText=industryFlag?then("", taskGroup.groupName)>
              <#list taskGroup.taskListEntries as task>
                  <@pwaTaskListItem.taskInfoItem taskName=task.taskName taskInfoList=task.taskInfoList route=task.route isCompleted=task.completed/>
              </#list>
          </@fdsTaskList.taskListSection>
      </#list>

  </@fdsTaskList.taskList>

</#macro>