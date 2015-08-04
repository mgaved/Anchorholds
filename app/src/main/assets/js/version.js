if(typeof AndroidVersion !== 'undefined') {
  window.addEventListener('load', displayVersion);
}

function displayVersion() {
  var version = document.getElementById('version');
  if(version) {
    version.innerHTML = version.innerHTML + AndroidVersion.getVersion();
  }
}