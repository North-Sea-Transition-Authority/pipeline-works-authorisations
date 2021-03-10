<#include '../../../layout.ftl'>

<#-- @ftlvariable name="task" type="uk.co.ogauthority.pwa.model.tasklist.TaskListEntry" -->
<#-- @ftlvariable name="taskRequirementToShowWarning" type="uk.co.ogauthority.pwa.service.enums.appprocessing.TaskRequirement -->

<#macro tab taskListGroups industryFlag=false warningText="" showWarning=false taskRequirementToShowWarning=[]>



  <@fdsTaskList.taskList>

      <#list taskListGroups as taskGroup>

          <#assign taskListSectionWarningText = ""/>
          <#if showWarning && taskRequirementToShowWarning.getDisplayName() == taskGroup.groupName>
            <#assign taskListSectionWarningText = warningText/>
          </#if>

          <@fdsTaskList.taskListSection sectionHeadingText=industryFlag?then("Status", taskGroup.groupName) warningText=taskListSectionWarningText>
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