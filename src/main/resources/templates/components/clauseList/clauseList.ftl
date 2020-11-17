<#include '../../layout.ftl'>

<#-- @ftlvariable name="documentView" type="uk.co.ogauthority.pwa.model.documents.view.DocumentView" -->

<#macro sidebarSections documentView>

  <#list documentView.sections as section>

      <@fdsSubNavigation.subNavigationSection themeHeading=section.name>

          <#list section.sidebarSectionLinks as sidebarLink>
              <@pwaSidebarSectionLink.renderSidebarLink sidebarLink=sidebarLink/>
          </#list>

      </@fdsSubNavigation.subNavigationSection>

  </#list>

</#macro>

<#macro list documentView clauseActionsUrlFactory listClass="number" childListClass="lower-alpha">

  <div class="clause-list">

    <#list documentView.sections as section>

      <h2 class="govuk-heading-l">${section.name}</h2>

      <ol class="govuk-list govuk-list--${listClass}">

        <#list section.clauses as clauseView>

          <#local isLastInList = clauseView?counter == section.clauses?size />
          <@clause clauseView=clauseView clauseActionsUrlFactory=clauseActionsUrlFactory listClass=childListClass isLastInList=isLastInList/>

        </#list>

    </#list>

  </div>

</#macro>

<#macro clause clauseView clauseActionsUrlFactory listClass isLastInList
  headingSize="h3"
  headingClass="m"
  childHeadingSize="h4"
  childHeadingClass="s"
  childListClass="lower-roman">

  <li id="clauseId-${clauseView.clauseId?c}">

    <${headingSize} class="govuk-heading-${headingClass}">
        ${clauseView.name}
        <@fdsAction.link
        linkText="Add clause above"
        linkUrl=springUrl(clauseActionsUrlFactory.getAddClauseBeforeRoute(clauseView.clauseId))
        linkClass="govuk-link clause-list__action clause-list__action--heading govuk-!-font-size-19"
        linkScreenReaderText=clauseView.name />
        <@fdsAction.link
        linkText="Edit clause"
        linkUrl=springUrl(clauseActionsUrlFactory.getEditClauseRoute(clauseView.clauseId))
        linkClass="govuk-link clause-list__action clause-list__action--heading govuk-!-font-size-19"
        linkScreenReaderText=clauseView.name />
        <@fdsAction.link
        linkText="Remove"
        linkUrl=springUrl(clauseActionsUrlFactory.getRemoveClauseRoute(clauseView.clauseId))
        linkClass="govuk-link clause-list__action clause-list__action--heading govuk-!-font-size-19"
        linkScreenReaderText=clauseView.name />
    </${headingSize}>

    <@multiLineText.multiLineText blockClass="clause-list__text">${clauseView.text}</@multiLineText.multiLineText>

    <#if isLastInList>
        <@fdsAction.link
        linkText="Add clause"
        linkUrl=springUrl(clauseActionsUrlFactory.getAddClauseAfterRoute(clauseView.clauseId))
        linkClass="govuk-link clause-list__action"
        linkScreenReaderText="after ${clauseView.name}" />
    </#if>

    <#if clauseView.childClauses?has_content>

      <ol class="govuk-list govuk-list--${listClass}">

          <#list clauseView.childClauses as child>

            <#local isLast = child?counter == clauseView.childClauses?size />

            <@clause
              clauseView=child
              clauseActionsUrlFactory=clauseActionsUrlFactory
              headingSize=childHeadingSize
              headingClass=childHeadingClass
              listClass=childListClass
              childHeadingSize="h5"
              isLastInList=isLast />

          </#list>

      </ol>

      <#elseif clauseView.levelNumber == 1 || clauseView.levelNumber == 2>

        <@fdsAction.link
        linkText="Add sub-clause"
        linkUrl=springUrl(clauseActionsUrlFactory.getAddSubClauseRoute(clauseView.clauseId))
        linkClass="govuk-link clause-list__action"
        linkScreenReaderText="for ${clauseView.name}"/>

    </#if>

  </li>

</#macro>
