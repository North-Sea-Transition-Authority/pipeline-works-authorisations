<#-- @ftlvariable name="pageHeading" type="String" -->
<#-- @ftlvariable name="typeDisplay" type="String" -->
<#-- @ftlvariable name="buttonUrl" type="String" -->
<#-- @ftlvariable name="formattedDuration" type="java.lang.String" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading backLink=true>

  <@fdsStartPage.startPage startActionText="Start ${typeDisplay}" startActionUrl=buttonUrl>

    <p class="govuk-body">The Holder must make an application to <a href="mailto:consents@ogauthority.co.uk"
                                                                    class="govuk-link">consents@ogauthority.co.uk</a> very
      early in the process
      regarding any proposed changes to the Holder, User, Operator or Owner information for OGA’s consideration using the HUOO template.</p>

    <p class="govuk-body">If the OGA is content with the proposed changes the OGA will advise the Holder to resubmit the
      application nearer the execution date. The actual consent will not be issued until the deed has been executed.</p>

    <p class="govuk-body">Where there are no objections, it takes approximately ${formattedDuration} to authorisation.</p>

  </@fdsStartPage.startPage>

</@defaultPage>