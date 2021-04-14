<#include '../../../layout.ftl'>

<#-- @ftlvariable name="task" type="uk.co.ogauthority.pwa.model.tasklist.TaskListEntry" -->
<#-- @ftlvariable name="taskGroupNameWarningMessageMap" type="java.util.Map<java.lang.String, java.lang.String>" -->

<#macro tab taskListGroups industryFlag=false taskGroupNameWarningMessageMap=[]>



  <@fdsTaskList.taskList>

      <#list taskListGroups as taskGroup>

          <#assign warningText = ""/>
          <#if taskGroupNameWarningMessageMap[taskGroup.groupName]?has_content>
            <#assign warningText = taskGroupNameWarningMessageMap[taskGroup.groupName]/>
          </#if>

          <@fdsTaskList.taskListSection sectionHeadingText=industryFlag?then("Status", taskGroup.groupName) warningText=warningText>
              <#list taskGroup.taskListEntries as task>

                  <#if task.taskState != "LOCK">
                      <#assign taskUrl = task.route?has_content?then(springUrl(task.route), "") />
                    <#else>
                      <#assign taskUrl = "" />
                  </#if>
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