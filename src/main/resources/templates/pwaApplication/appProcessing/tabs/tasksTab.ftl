<#include '../../../layout.ftl'>

<#macro tab taskListGroups industryFlag=false>

  <@fdsTaskList.taskList>

      <#list taskListGroups as taskGroup>
          <@fdsTaskList.taskListSection sectionHeadingText=industryFlag?then("Status", taskGroup.groupName)>
              <#list taskGroup.taskListEntries as task>

                  <#assign taskUrl = industryFlag?then("", task.route?has_content?then(springUrl(task.route), "")) />
                  <#assign tagText = task.taskTag?has_content?then(task.taskTag.tagText!, "") />
                  <#assign tagClass = task.taskTag?has_content?then(task.taskTag.tagClass!, "") />

                  <@fdsTaskList.taskListItem
                    itemText=task.taskName
                    itemUrl=taskUrl
                    tagClass=tagClass
                    tagText=tagText />

              </#list>
          </@fdsTaskList.taskListSection>
      </#list>

  </@fdsTaskList.taskList>

</#macro>