<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pageHeading" type="java.lang.String" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="markCompleteErrorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PipelineHuooUrlFactory" -->
<#-- @ftlvariable name="orgGroupViews" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineAndOrgRoleGroupViewsByRole" -->


<#macro pipelineRoleGroup pipelineNumberList organisationNameList linkText="Placeholder params as links not functional" >
  <@fdsCard.card>
      <#local joinedPipelineNumbers=pipelineNumberList?join(", ")/>
    <@fdsCard.cardHeader cardHeadingText=joinedPipelineNumbers cardHeadingSize="h3" cardHeadingClass="govuk-heading-s" />
    <ol class="govuk-list">
      <#list organisationNameList as orgName>
        <li>${orgName}</li>
      </#list>
    </ol>
    <@fdsAction.link linkText=linkText linkClass="govuk-link" linkUrl=springUrl("/#") linkScreenReaderText=linkText + ": " + joinedPipelineNumbers />
  </@fdsCard.card>
</#macro>

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading breadcrumbs=true fullWidthColumn=true>
  <h2 class="govuk-heading-m">Holders</h2>
    <#list orgGroupViews.getHolderGroups() as holderGroupView >
      <@pipelineRoleGroup
        pipelineNumberList=holderGroupView.pipelineNumbers
        organisationNameList=holderGroupView.organisationNames
        linkText="Change holders for these pipelines"/>
    </#list>
    <@fdsAction.link linkText="Select pipelines and assign holders"  linkUrl=springUrl(urlFactory.getAddHolderPipelineRoleUrl()) linkClass="govuk-button govuk-button--blue"/>
  <h2 class="govuk-heading-m">Users</h2>
    <#list orgGroupViews.getUserGroups() as userGroupView >
      <@pipelineRoleGroup
        pipelineNumberList=userGroupView.pipelineNumbers
        organisationNameList=userGroupView.organisationNames
        linkText="Change users for these pipelines" />
    </#list>
    <@fdsAction.link linkText="Select pipelines and assign users"  linkUrl=springUrl(urlFactory.getAddUserPipelineRoleUrl()) linkClass="govuk-button govuk-button--blue"/>
  <h2 class="govuk-heading-m">Operators</h2>
    <#list orgGroupViews.getOperatorGroups() as operatorGroupView >
      <@pipelineRoleGroup
        pipelineNumberList=operatorGroupView.pipelineNumbers
        organisationNameList=operatorGroupView.organisationNames
        linkText="Change operators for these pipelines" />
    </#list>
    <@fdsAction.link linkText="Select pipelines and assign operators"  linkUrl=springUrl(urlFactory.getAddOperatorPipelineRoleUrl()) linkClass="govuk-button govuk-button--blue"/>
  <h2 class="govuk-heading-m">Owners</h2>
    <#list orgGroupViews.getOwnerGroups() as ownerGroupView >
      <@pipelineRoleGroup
        pipelineNumberList=ownerGroupView.pipelineNumbers
        organisationNameList=ownerGroupView.organisationNames
        linkText="Change owners for these pipelines" />
    </#list>
    <@fdsAction.link linkText="Select pipelines and assign owners"  linkUrl=springUrl(urlFactory.getAddOwnerPipelineRoleUrl()) linkClass="govuk-button govuk-button--blue"/>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons
        errorMessage=markCompleteErrorMessage!""
        primaryButtonText="Complete"
        linkSecondaryAction=true
        secondaryLinkText="Back to task list"
        linkSecondaryActionUrl=springUrl(backUrl)
        />
    </@fdsForm.htmlForm>

</@defaultPage>