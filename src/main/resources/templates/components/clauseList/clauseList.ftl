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

<#macro list documentView listClass="number" childListClass="lower-alpha">

  <div class="clause-list">

    <#list documentView.sections as section>

      <h2 class="govuk-heading-l">${section.name}</h2>

      <ol class="govuk-list govuk-list--${listClass}">

        <#list section.clauses as clauseView>

          <@clause clauseView=clauseView listClass=childListClass/>

        </#list>

    </#list>

  </div>

</#macro>

<#macro clause clauseView listClass
  headingSize="h3"
  headingClass="m"
  childHeadingSize="h4"
  childHeadingClass="s"
  childListClass="lower-roman">

  <li id="clauseId-${clauseView.id?c}">

    <${headingSize} class="govuk-heading-${headingClass}">
        ${clauseView.name}
        <@fdsAction.link linkText="Edit clause" linkUrl="#" linkClass="govuk-link clause-list__action govuk-!-font-size-19" />
    </${headingSize}>

    <p class="govuk-body">${clauseView.text}</p>

    <#if clauseView.childClauses?has_content>

      <ol class="govuk-list govuk-list--${listClass}">

          <#list clauseView.childClauses as child>

            <@clause clauseView=child headingSize=childHeadingSize headingClass=childHeadingClass listClass=childListClass childHeadingSize="h5"/>

          </#list>

      </ol>

    </#if>

  </li>

</#macro>
