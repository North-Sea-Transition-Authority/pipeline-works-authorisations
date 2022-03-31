$(document).ready(() => {
  function setCookie (name,value,days) {
    let expires = "";
    if (days) {
      const date = new Date();
      date.setTime(date.getTime() + (days*24*60*60*1000));
      expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "")  + expires + "; path=/";
  }

  if (window.FDS.googleAnalytics !== undefined) {
    window.FDS.googleAnalytics._gtag('get', PWA_CONFIG.globalTag, 'client_id', clientId => {
      if (clientId !== 'anonymous_user') {
        setCookie('pwa-ga-client-id', clientId);
      } else {
        window.FDS.cookies.deleteCookie('pwa-ga-client-id');
      }
    })
  }
});

$('body').on('click', '*[data-analytics-event-category]', (e) => {
  // Wherever possible, analytics events should originate in page controllers.
  // Client-side events can be used for interactions which are client-side only (eg mailto links, expanding/collapsing)
  const analyticsAttrPrefix = 'data-analytics-';
  const eventCategoryAttr = 'data-analytics-event-category';
  const category = $(e.target).attr(eventCategoryAttr);

  // GA doesn't allow hyphens in parameters, replace with underscores, also strip prefix
  function constructParamName(input) {
    return input.replace(analyticsAttrPrefix, '').replaceAll('-', '_');
  }

  // support arbitrary event params on elements using format "data-analytics-<paramName>"
  const paramMap = new Map(Array.from($(e.target.attributes))
    .filter(att => att.name.startsWith(analyticsAttrPrefix) && att.name !== eventCategoryAttr)
    .map(att => [constructParamName(att.name), att.nodeValue]));

  $.ajax(
    `${PWA_CONFIG.analyticsMeasurementUrl}`,
    {
      method: 'POST',
      contentType: "application/json; charset=UTF-8",
      data: JSON.stringify({
        "eventCategory": category,
        "paramMap": Object.fromEntries(paramMap)
      })
    }
  )
    .then(
      function success() {
        // Do nothing, the event was collected
      },
      function fail() {
        // Do nothing, the request failed but there's nothing we can do
      }
    );
})
