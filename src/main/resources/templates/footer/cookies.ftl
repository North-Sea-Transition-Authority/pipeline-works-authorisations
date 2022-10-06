<#include '../layout.ftl'>

<@defaultPage htmlTitle="Cookies" pageHeading="Cookies" fullWidthColumn=true topNavigation=false backLink=true phaseBanner=false>

  <#assign essentialCookies>
      <@fdsCookiePreferences.essentialCookieRow name="field_set" purpose="Used for session security" expiry="When you close your browser"/>
      <@fdsCookiePreferences.essentialCookieRow name="p_dti_session_id" purpose="Used to keep you signed in" expiry="When you close your browser"/>
      <@fdsCookiePreferences.essentialCookieRow name="CONTAINER" purpose="Used to deliver the service" expiry="When you close your browser"/>
      <@fdsCookiePreferences.essentialCookieRow name="FOX_ORIGIN_ID" purpose="Used to keep you signed in" expiry="When you close your browser"/>
      <@fdsCookiePreferences.essentialCookieRow name="JSESSIONID" purpose="Used to keep you signed in" expiry="When you close your browser"/>
      <@fdsCookiePreferences.essentialCookieRow name="SESSION" purpose="Used to keep you signed in" expiry="When you close your browser"/>
  </#assign>

  <@fdsCookiePreferences.cookiePreferences serviceName=service.serviceName essentialCookies=essentialCookies>

    <h2 class="govuk-heading-m">Analytics cookies (optional)</h2>

    <p class="govuk-body">
      With your permission, we use Google Analytics to collect data about how you use the '${service.serviceName}' service.
      This information helps us to improve our service.
    </p>

    <p class="govuk-body">
      Google is not allowed to use or share our analytics data with anyone.
    </p>

    <p class="govuk-body">
      Google Analytics stores anonymised information about:
    </p>

    <ul class="govuk-list govuk-list--bullet">
      <li>how you got to the '${service.serviceName}' service</li>
      <li>the pages you visit on this service and how long you spend on them</li>
    </ul>

    <table class="govuk-table">
      <caption class="govuk-visually-hidden">Google Analytics cookies</caption>
      <thead class="govuk-table__head">
      <tr class="govuk-table__row">
        <th class="govuk-table__header">Name</th>
        <th class="govuk-table__header">Purpose</th>
        <th class="govuk-table__header">Expires</th>
      </tr>
      </thead>
      <tbody class="govuk-table__body">
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">
          _ga
        </td>
        <td class="govuk-table__cell govuk-!-width-one-half">
          Checks if you’ve visited this service before. This helps us count how many people use the service.
        </td>
        <td class="govuk-table__cell">
          2 years
        </td>
      </tr>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">
          _ga_${analytics.appTag?replace("G-", "")}
          <br/>
          _ga_${analytics.globalTag?replace("G-", "")}
        </td>
        <td class="govuk-table__cell govuk-!-width-one-half">
          Stores information about how you use this service. This helps us improve the service for other people.
        </td>
        <td class="govuk-table__cell">
          2 years
        </td>
      </tr>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">${analyticsClientIdCookieName}</td>
        <td class="govuk-table__cell govuk-!-width-one-half">
          Stores information about how you use this service. This helps us improve the service for other people.
        </td>
        <td class="govuk-table__cell">When you close your browser</td>
      </tr>
      </tbody>
    </table>

  </@fdsCookiePreferences.cookiePreferences>

</@defaultPage>
