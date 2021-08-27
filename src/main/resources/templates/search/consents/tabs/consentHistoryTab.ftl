<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pwaConsentHistoryViews" type="java.util.List<uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaConsentApplicationDto>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewUrlFactory" -->

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

        <#local documentIsDownloadable = pwaConsentHistoryView.consentDocumentDownloadable() />
        <#local documentStatusDisplay = pwaConsentHistoryView.getDocStatusDisplay() />

        <td class="govuk-table__cell">
          <#if pwaConsentHistoryView.pwaApplicationId?has_content>
            <#if documentIsDownloadable>
              <@fdsAction.link
                linkText=pwaConsentHistoryView.consentReference
                linkUrl=springUrl(urlFactory.getConsentDocumentsUrl(pwaConsentHistoryView.consentId))
                linkClass="govuk-link"
                linkScreenReaderText="Download consent document"
                role=false
                start=false/>
            <#else>
              <span>${pwaConsentHistoryView.consentReference}
              <#if documentStatusDisplay?has_content>
                <br/>
                ${documentStatusDisplay}
              </#if>
              </span>
            </#if>
          <#else>
            <span>${pwaConsentHistoryView.consentReference}<span>
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
