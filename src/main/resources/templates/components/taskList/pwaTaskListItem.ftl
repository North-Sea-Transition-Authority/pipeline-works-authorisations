<#import '/spring.ftl' as spring>
<#include '../../layout.ftl'>

<#-- @ftlvariable name="taskName" type="java.lang.String" -->
<#-- @ftlvariable name="route" type="java.lang.String" -->
<#-- @ftlvariable name="isCompleted" type="java.lang.Boolean" -->
<#-- @ftlvariable name="taskInfoList" type="java.util.List<uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskInfo>" -->

<#macro taskInfoItem taskName taskInfoList route isCompleted linkScreenReaderText="">

  <#assign tagText></#assign>

  <#list taskInfoList as taskInfo>
    <#if taskInfo.count gt 0>
      <#assign tagText>
        <@stringUtils.pluralise count=taskInfo.count word=taskInfo.countType />
      </#assign>
    </#if>
   </#list>

    <@fdsTaskList.taskListItem
      tagText=tagText!""
      tagClass="govuk-tag--grey"
      itemUrl=springUrl(route)
      completed=isCompleted
      showTag=true
      itemText=taskName
      useNotCompletedLabels=true
     />
</#macro>