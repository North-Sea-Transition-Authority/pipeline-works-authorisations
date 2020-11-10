<#include '../../../layout.ftl'>

<#macro tab taskListGroups industryFlag=false>

  <@fdsTaskList.taskList>

      <#list taskListGroups as taskGroup>
          <@fdsTaskList.taskListSection sectionHeadingText=industryFlag?then("Status", taskGroup.groupName)>
              <#list taskGroup.taskListEntries as task>

                  <#assign taskUrl = industryFlag?then("", springUrl(task.route)) />
                  <#assign tagClass = task.taskStatus?has_content?then(task.taskStatus.tagClass!, "") />
                  <#assign displayText = task.taskStatus?has_content?then(task.taskStatus.displayText!, "") />

                  <@fdsTaskList.taskListItem
                    itemText=task.taskName
                    itemUrl=taskUrl
                    tagClass=tagClass
                    tagText=displayText />

              </#list>
          </@fdsTaskList.taskListSection>
      </#list>

  </@fdsTaskList.taskList>

</#macro>