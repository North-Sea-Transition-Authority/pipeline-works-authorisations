$(document).ready(() => {
  // ignore clause hashes in url as these are used when focusing on clauses in the consent doc editor screen
  if (window.location.hash && !window.location.hash.substring(1).includes("clause")) {
    window.location = $('#doc-download-link').attr("href") + `${window.location.hash.substring(1)}`;
  }
});