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

<#macro list documentView clauseActionsUrlProvider listClass="number" childListClass="lower-alpha" showSectionHeading=true showClauseHeadings=true>

  <div class="pwa-clause-list">

    <#list documentView.sections as section>

      <#assign addAndRemoveClauseAllowed = section.addAndRemoveClauseAllowed()/>

      <#if showSectionHeading>
        <h2 class="govuk-heading-l">${section.name}</h2>
      </#if>

      <ol class="govuk-list govuk-list--${listClass} pwa-clause-list__list pwa-clause-list__list--${listClass}">

        <#list section.clauses as clauseView>

          <#local isLastInList = clauseView?counter == section.clauses?size />

            <@clause
              clauseView=clauseView
              clauseActionsUrlProvider=clauseActionsUrlProvider
              listClass=childListClass
              isLastInList=isLastInList
              addAndRemoveClauseAllowed=addAndRemoveClauseAllowed
              showClauseHeading=showClauseHeadings/>

        </#list>

      </ol>

    </#list>

  </div>

</#macro>

<#macro clause clauseView clauseActionsUrlProvider listClass isLastInList addAndRemoveClauseAllowed
  headingSize="h3"
  headingClass="m"
  childHeadingSize="h4"
  childHeadingClass="s"
  childListClass="lower-roman"
  showClauseHeading=true>

  <#assign clauseActionsFlag = clauseActionsUrlProvider?has_content />

  <li id="clauseId-${clauseView.clauseId?c}" class="pwa-clause-list__list-item">

    <#if showClauseHeading>
      <${headingSize} class="govuk-heading-${headingClass} govuk-!-margin-bottom-2">${clauseView.name}</${headingSize}>
      <#if clauseActionsFlag && addAndRemoveClauseAllowed>
        <@fdsActionDropdown.actionDropdown dropdownButtonText="Clause actions" dropdownButtonClass="govuk-!-margin-bottom-2">
          <@fdsActionDropdown.actionDropdownItem
            actionText="Add clause above"
            linkAction=true
            linkActionUrl=springUrl(clauseActionsUrlProvider.getAddClauseBeforeRoute(clauseView.clauseId))
            linkActionScreenReaderText=clauseView.name/>
          <#if isLastInList>
            <@fdsActionDropdown.actionDropdownItem
              actionText="Add clause below"
              linkAction=true
              linkActionUrl=springUrl(clauseActionsUrlProvider.getAddClauseAfterRoute(clauseView.clauseId))
              linkActionScreenReaderText="after ${clauseView.name}" />
          </#if>
          <#if !clauseView.childClauses?has_content && (clauseView.levelNumber == 1 || clauseView.levelNumber == 2)>
            <@fdsActionDropdown.actionDropdownItem
              actionText="Add sub-clause"
              linkAction=true
              linkActionUrl=springUrl(clauseActionsUrlProvider.getAddSubClauseRoute(clauseView.clauseId))
              linkActionScreenReaderText="for ${clauseView.name}"/>
          </#if>
          <@fdsActionDropdown.actionDropdownItem
            actionText="Edit clause"
            linkAction=true
            linkActionUrl=springUrl(clauseActionsUrlProvider.getEditClauseRoute(clauseView.clauseId))
            linkActionScreenReaderText=clauseView.name />
          <@fdsActionDropdown.actionDropdownItem
            actionText="Remove"
            linkAction=true
            linkActionUrl=springUrl(clauseActionsUrlProvider.getRemoveClauseRoute(clauseView.clauseId))
            linkActionScreenReaderText=clauseView.name />
        </@fdsActionDropdown.actionDropdown>
        <#elseif clauseActionsFlag && !addAndRemoveClauseAllowed>
          <@fdsAction.link
            linkText="Edit clause"
            linkUrl=springUrl(clauseActionsUrlProvider.getEditClauseRoute(clauseView.clauseId))
            linkScreenReaderText=clauseView.name />
      </#if>
    </#if>

    <@multiLineText.multiLineText blockClass="pwa-clause-list__text">
      <#if clauseView.text?has_content>
        ${clauseView.text?no_esc}
      </#if>
    </@multiLineText.multiLineText>

    <#if clauseView.childClauses?has_content>

      <ol class="govuk-list govuk-list--${listClass} pwa-clause-list__list pwa-clause-list__list--${listClass}">

          <#list clauseView.childClauses as child>

            <#local isLast = child?counter == clauseView.childClauses?size />

            <@clause
              clauseView=child
              clauseActionsUrlProvider=clauseActionsUrlProvider
              headingSize=childHeadingSize
              headingClass=childHeadingClass
              listClass=childListClass
              childHeadingSize="h5"
              isLastInList=isLast
              addAndRemoveClauseAllowed=addAndRemoveClauseAllowed
              showClauseHeading=showClauseHeading/>

          </#list>

      </ol>

    </#if>

  </li>

</#macro>
