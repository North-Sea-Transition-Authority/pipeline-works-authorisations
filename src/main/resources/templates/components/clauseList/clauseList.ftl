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

<#macro list documentView clauseActionsUrlFactory listClass="number" childListClass="lower-alpha" showSectionHeading=true>

  <div class="clause-list">

    <#list documentView.sections as section>

      <#if showSectionHeading>
        <h2 class="govuk-heading-l">${section.name}</h2>
      </#if>

      <ol class="govuk-list govuk-list--${listClass} clause-list__list clause-list__list--${listClass}">

        <#list section.clauses as clauseView>

          <#local isLastInList = clauseView?counter == section.clauses?size />
          <@clause clauseView=clauseView clauseActionsUrlFactory=clauseActionsUrlFactory listClass=childListClass isLastInList=isLastInList/>

        </#list>

      </ol>

    </#list>

  </div>

</#macro>

<#macro clause clauseView clauseActionsUrlFactory listClass isLastInList
  headingSize="h3"
  headingClass="m"
  childHeadingSize="h4"
  childHeadingClass="s"
  childListClass="lower-roman">

  <#assign clauseActionsFlag = clauseActionsUrlFactory?has_content />

  <li id="clauseId-${clauseView.clauseId?c}" class="clause-list__list-item">

    <${headingSize} class="govuk-heading-${headingClass} govuk-!-margin-bottom-2">${clauseView.name}</${headingSize}>
    <#if clauseActionsFlag>
      <@fdsActionDropdown.actionDropdown dropdownButtonText="Clause actions" dropdownButtonClass="govuk-!-margin-bottom-2">
        <@fdsActionDropdown.actionDropdownItem
          actionText="Add clause above"
          linkAction=true
          linkActionUrl=springUrl(clauseActionsUrlFactory.getAddClauseBeforeRoute(clauseView.clauseId))
          linkActionScreenReaderText=clauseView.name/>
        <#if isLastInList>
          <@fdsActionDropdown.actionDropdownItem
            actionText="Add clause below"
            linkAction=true
            linkActionUrl=springUrl(clauseActionsUrlFactory.getAddClauseAfterRoute(clauseView.clauseId))
            linkActionScreenReaderText="after ${clauseView.name}" />
        </#if>
        <#if !clauseView.childClauses?has_content && (clauseView.levelNumber == 1 || clauseView.levelNumber == 2)>
          <@fdsActionDropdown.actionDropdownItem
            actionText="Add sub-clause"
            linkAction=true
            linkActionUrl=springUrl(clauseActionsUrlFactory.getAddSubClauseRoute(clauseView.clauseId))
            linkActionScreenReaderText="for ${clauseView.name}"/>
        </#if>
        <@fdsActionDropdown.actionDropdownItem
          actionText="Edit clause"
          linkAction=true
          linkActionUrl=springUrl(clauseActionsUrlFactory.getEditClauseRoute(clauseView.clauseId))
          linkActionScreenReaderText=clauseView.name />
        <@fdsActionDropdown.actionDropdownItem
          actionText="Remove"
          linkAction=true
          linkActionUrl=springUrl(clauseActionsUrlFactory.getRemoveClauseRoute(clauseView.clauseId))
          linkActionScreenReaderText=clauseView.name />
      </@fdsActionDropdown.actionDropdown>
    </#if>

    <@multiLineText.multiLineText blockClass="clause-list__text">${clauseView.text!}</@multiLineText.multiLineText>

    <#if clauseView.childClauses?has_content>

      <ol class="govuk-list govuk-list--${listClass} clause-list__list clause-list__list--${listClass}">

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

    </#if>

  </li>

</#macro>
