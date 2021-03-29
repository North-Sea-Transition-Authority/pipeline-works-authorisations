<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pwaConsentHistoryViews" type="java.util.List<uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaConsentApplicationDto>" -->
<#-- @ftlvariable name="pwaViewUrlFactory" type="uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewUrlFactory" -->

<#macro tab urlFactory pwaConsentHistoryViews=[]>

  <table class="govuk-table">
    <thead class="govuk-table__head">
      <tr class="govuk-table__row">
        <th class="govuk-table__header" scope="col">Consent document (if available)</th>
        <th class="govuk-table__header" scope="col">Consent date</th>
        <th class="govuk-table__header" scope="col">Application reference </br> Application type </th>
      </tr>
    </thead>

    <tbody class="govuk-table__body">
    <#list pwaConsentHistoryViews as pwaConsentHistoryView>
      <tr class="govuk-table__row">

        <td class="govuk-table__cell">
          <#if pwaConsentHistoryView.pwaApplicationId?has_content>
            <@fdsAction.link linkText=pwaConsentHistoryView.consentReference linkUrl=springUrl(urlFactory.getConsentDocumentUrl(pwaConsentHistoryView.pwaApplicationId, pwaConsentHistoryView.applicationType)) 
            linkClass="govuk-link" linkScreenReaderText="Download consent document" role=false start=false openInNewTab=true/> 
          <#else>
            ${pwaConsentHistoryView.consentReference}
          </#if>
        </td>

        <td class="govuk-table__cell">
          ${pwaConsentHistoryView.getConsentDateDisplay()}
        </td>

        <td class="govuk-table__cell">
          <#if pwaConsentHistoryView.appReference?has_content && pwaConsentHistoryView.applicationType?has_content>
            <div>
              <@fdsAction.link linkText=pwaConsentHistoryView.appReference linkUrl=springUrl(urlFactory.routeCaseManagement(pwaConsentHistoryView.pwaApplicationId, pwaConsentHistoryView.applicationType)) 
              linkClass="govuk-link" linkScreenReaderText="Go to ${pwaConsentHistoryView.appReference} case management screen" role=false start=false openInNewTab=true/> 
              </br>
              ${pwaConsentHistoryView.applicationType.getDisplayName()}
            </div>
          <#else>
            <span> Unknown </span>
          </#if>
        </td>   

      </tr>
    </#list>    
    </tbody>

  </table>

  

</#macro>
