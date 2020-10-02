<#include '../../layout.ftl'>

<#-- @ftlvariable name="documentView" type="uk.co.ogauthority.pwa.model.documents.view.DocumentView" -->

<#macro renderSidebarLink sidebarLink>

    <#local linkUrl = sidebarLink.isAnchorLink?then(sidebarLink.link, springUrl(sidebarLink.link) )>

    <@fdsSubNavigation.subNavigationSectionItem
    linkName=sidebarLink.displayText
    currentItemHref="#top"
    linkAction=linkUrl />

</#macro>

<#macro sidebarSections documentView>

  <#list documentView.sections as section>

      <@fdsSubNavigation.subNavigationSection themeHeading=section.name>

          <#list section.sidebarSectionLinks as sidebarLink>
              <@renderSidebarLink sidebarLink=sidebarLink/>
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
        linkClass="govuk-link clause-list__action clause-list__action--heading govuk-!-font-size-19" />
    </${headingSize}>

    <p class="govuk-body">${clauseView.text}</p>

    <#if isLastInList>
        <@fdsAction.link
        linkText="Add clause"
        linkUrl=springUrl(clauseActionsUrlFactory.getAddClauseAfterRoute(clauseView.clauseId))
        linkClass="govuk-link clause-list__action" />
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
        linkClass="govuk-link clause-list__action" />

    </#if>

  </li>

</#macro>
