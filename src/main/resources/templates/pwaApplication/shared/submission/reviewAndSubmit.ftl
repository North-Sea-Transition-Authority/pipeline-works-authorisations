<#include '../../../layoutPane.ftl'>

<#assign pageHeading="Review and Submit Application XXX/YYY/ZZZ"/>

<@defaultPagePane htmlTitle=pageHeading phaseBanner=false>

    <@defaultPagePaneSubNav>
        <@fdsSubNavigation.subNavigation>
            <@fdsSubNavigation.subNavigationSection themeHeading="Check your answers of all relevant fields on form">
                <@fdsSubNavigation.subNavigationSectionItem
                linkName="Application contacts"
                currentItemHref="something which doesn match. This wont work on client side options"
                linkAction="#applicationContacts"
                />
                <@fdsSubNavigation.subNavigationSectionItem
                linkName="Field information"
                currentItemHref="something which doesn match. This wont work on client side options"
                linkAction="#fieldInformation"
                />
                <@fdsSubNavigation.subNavigationSectionItem
                linkName="Project information"
                currentItemHref="something which doesn match. This wont work on client side options"
                linkAction="#projectInformation"
                />
            </@fdsSubNavigation.subNavigationSection>

        </@fdsSubNavigation.subNavigation>
    </@defaultPagePaneSubNav>

    <@defaultPagePaneContent pageHeading=pageHeading>
      <!-- This should be supported by the fds component. but it isnt. spent enough time faffing around with LayoutPane.ftl
       to add the same div as in layout.ftl but the css goes nuts and everything breaks.
         Located here to make navigating back from this page easy for basic testing.
         -->
        <@fdsBreadcrumbs.breadcrumbs crumbsList=breadcrumbMap currentPage=currentPage/>

          <h2 id="applicationContacts" class="govuk-heading-l">Application contacts</h2>
          <h3 id="contact1" class="govuk-heading-m">Contact 1 name</h3>
          <p class="govuk-body">filler</p>
          <h3 id="contact1" class="govuk-heading-m">Contact 2 name</h3>
          <p class="govuk-body">filler</p>
          <p class="govuk-body">filler</p>
          <h2 id="fieldInformation" class="govuk-heading-l">Field information</h2>
          <p class="govuk-body">filler</p>
          <p class="govuk-body">filler</p>
          <p class="govuk-body">filler</p>
          <p class="govuk-body">filler</p>
          <h2 id="projectInformation" class="govuk-heading-l">Project information</h2>
          <p class="govuk-body">filler</p>
          <p class="govuk-body">filler</p>
          <p class="govuk-body">filler</p>
          <p class="govuk-body">filler</p>


        <@fdsForm.htmlForm>
            <@fdsAction.button buttonText="Submit" buttonValue="submit" />
        </@fdsForm.htmlForm>
    </@defaultPagePaneContent>




</@defaultPagePane>