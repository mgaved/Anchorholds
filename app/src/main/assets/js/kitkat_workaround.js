if(typeof Android !== 'undefined') {
  //console.log(Android);
  window.addEventListener('load', makeAudioPlayable);
}

function makeAudioPlayable() {
  var sounds = document.getElementsByTagName('audio');
  for (var i = sounds.length - 1; i >= 0; i--) {
    sounds[i].addEventListener("play", playAndroidAudio, true);
    sounds[i].addEventListener("pause", pauseAndroidAudio, true);
  };
}

function playAndroidAudio(AudioElement) {
  //console.log(AudioElement);
  Android.playAudio(AudioElement.target.currentSrc);
}

function pauseAndroidAudio(AudioElement) {
  //console.log(AudioElement.target.currentSrc);
  Android.pauseAudio();
}