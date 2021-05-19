<#include '../../layoutPane.ftl'>

<#-- @ftlvariable name="documentSpec" type="uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec" -->
<#-- @ftlvariable name="clauseActionsUrlProvider" type="uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceClauseActionsUrlProvider" -->
<#-- @ftlvariable name="docView" type="uk.co.ogauthority.pwa.model.documents.view.DocumentView" -->

<#assign pageHeading = documentSpec.displayName />

<@defaultPagePane htmlTitle=pageHeading phaseBanner=false topNavigation=true>

    <@defaultPagePaneSubNav>
      <@fdsSubNavigation.subNavigation>
          <@pwaClauseList.sidebarSections documentView=docView />
      </@fdsSubNavigation.subNavigation>
    </@defaultPagePaneSubNav>

    <@defaultPagePaneContent breadcrumbs=true>

      <h1 class="govuk-heading-xl">${documentSpec.displayName}</h1>

      <@pwaClauseList.list documentView=docView clauseActionsUrlProvider=""/>

    </@defaultPagePaneContent>

</@defaultPagePane>