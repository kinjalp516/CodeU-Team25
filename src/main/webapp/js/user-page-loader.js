/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Get ?user=XYZ parameter value
const urlParams = new URLSearchParams(window.location.search);
const parameterUsername = urlParams.get('user');
// Variables to store current values
let currentNickname;
let currentActivity;
let currentSkillLevel;
let currentAboutMe;
let map;

// URL must include ?user=XYZ parameter. If not, redirect to homepage.
if (!parameterUsername) {
  window.location.replace('/');
}

/** Sets the page title based on the URL parameter username. */
function setPageTitle() {
  const url = '/nickname?user=' + parameterUsername;
  fetch(url).then((response) => {
    return response.text();
  }).then((nickname) => {
    if(nickname == ''){
      document.getElementById('page-title').innerText = parameterUsername;
      document.title = parameterUsername + ' - User Page';
      currentNickname = parameterUsername;
    }
    else {
      document.getElementById('page-title').innerText = nickname;
      document.title = nickname + ' - User Page';
      currentNickname = nickname;
    }
  });
}

/**
 * Shows the message form if the user is logged in and viewing their own page.
 */
function showMessageFormIfViewingSelf() {
  fetch('/login-status')
      .then((response) => {
        return response.json();
      })
      .then((loginStatus) => {
        if (loginStatus.isLoggedIn &&
            loginStatus.username == parameterUsername) {
          const messageForm = document.getElementById('message-form');
          messageForm.classList.remove('hidden');          
        }
      });
}

/** Fetches messages and add them to the page. */
function fetchMessages() {
  const url = '/messages?user=' + parameterUsername;
  fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((messages) => {
        const messagesContainer = document.getElementById('message-container');
        if (messages.length == 0) {
          messagesContainer.innerHTML = '<p>This user has no posts yet.</p>';
        } else {
          messagesContainer.innerHTML = '';
        }
        messages.forEach((message) => {
          const messageDiv = buildMessageDiv(message);
          messagesContainer.appendChild(messageDiv);
        });
      });
}

/**
 * Builds an element that displays the message.
 * @param {Message} message
 * @return {Element}
 */
function buildMessageDiv(message) {
  const headerDiv = document.createElement('div');
  headerDiv.classList.add('message-header');
  headerDiv.appendChild(document.createTextNode(
      message.user + ' - ' + new Date(message.timestamp)));

  const bodyDiv = document.createElement('div');
  bodyDiv.classList.add('message-body');
  bodyDiv.innerHTML = message.text;

  const messageDiv = document.createElement('div');
  messageDiv.classList.add('message-div');
  messageDiv.appendChild(headerDiv);
  messageDiv.appendChild(bodyDiv);

  return messageDiv;
}

/** Fetches data and populates the UI of the page. */
function buildUI() {
  setPageTitle();
  fetchAvatar();
  showMessageFormIfViewingSelf();
  fetchMessages();
  fetchActivity();
  fetchSkillLevel();
  fetchAboutMe();
  createMap();
}

/** Fetches activity specified by the user. */
function fetchActivity() {
  const url = '/act?user=' + parameterUsername;
  fetch(url).then((response) => {
    return response.text();
  }).then((activity) => {
    const activityContainer = document.getElementById('activity');
    if(activity == ''){
      activity = 'Gym';
    }
    
    currentActivity = activity;
    activityContainer.innerHTML = activity;

  });
}

/** Fetches skill level specified by the user. */
function fetchSkillLevel() {
  const url = '/lvl?user=' + parameterUsername;
  fetch(url).then((response) => {
    return response.text();
  }).then((skillLevel) => {
    const skillLevelContainer = document.getElementById('skill-level');
    if(skillLevel == ''){
      skillLevel = 'Beginner';
    }
    
    currentSkillLevel = skillLevel;
    skillLevelContainer.innerHTML = skillLevel;

  });
}

/** Fetches about me information provided by the user. */
function fetchAboutMe(){
  const url = '/about?user=' + parameterUsername;
  fetch(url).then((response) => {
    return response.text();
  }).then((aboutMe) => {
    const aboutMeContainer = document.getElementById('about-me-container');
    if(aboutMe == ''){
      aboutMe = 'This user has not entered any information yet.';
      currentAboutMe = 'This user has not entered any information yet.';
    }
    else {
      currentAboutMe = aboutMe;
    }
    
    aboutMeContainer.innerHTML = aboutMe;

  });
}

/**
 * Shows the edit profile button if the user is logged in and viewing their own page.
 */
function showEditButton() {
  fetch('/login-status')
      .then((response) => {
        return response.json();
      })
      .then((loginStatus) => {
        if (loginStatus.isLoggedIn &&
            loginStatus.username == parameterUsername) {
          const editButton = document.getElementById('edit-profile');
          editButton.style.display = "block";
        }
      });
}

function editProfile() {
  // Show edit form
  document.getElementById("submit-all").style.display = "block";
  // Hide edit profile button, activity container and skill container
  document.getElementById("edit-profile").style.display = "none";
  document.getElementById("activityContainer").style.display = "none";
  document.getElementById("skillContainer").style.display = "none";
  document.getElementById("aboutMeContainer").style.display = "none";
  // Fill form with existing values
  document.getElementById("nickname").value = currentNickname;  
  document.getElementById("about-me").value = currentAboutMe;
  document.getElementById("currAct").value = currentActivity;
  document.getElementById("currAct").innerHTML = currentActivity;
  document.getElementById("currLvl").value = currentSkillLevel;  
}

function editAvatar() {
  // Show file form
  document.getElementById("change-avatar").style.display = "block";
}

function fetchAvatar() {
  const url = '/avatar?user=' + parameterUsername;
  fetch(url).then((response) => {
    return response.text();
  }).then((avatar) => {
    const avatarContainer = document.getElementById('avatar-img');
    if(avatar != ''){
      avatarContainer.src = avatar;
    }
    else {
      avatarContainer.src = "images/avatar-placeholder.gif";
    }
  });
}

function createMap(){
  map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 38.5949, lng: -94.8923},
    zoom: 4
  });
  // When the user clicks in the map, show a marker with a text box the user can edit.
  map.addListener('click', (event) => {
    createMarkerForEdit(event.latLng.lat(), event.latLng.lng());
  });
  fetchMarkers();
}

/** Fetches markers from the backend and adds them to the map. */
function fetchMarkers(){
  const url = '/markers?user=' + parameterUsername;
fetch(url).then((response) => {
  return response.json();
}).then((markers) => {
  markers.forEach((marker) => {
   createMarkerForDisplay(marker.lat, marker.lng, marker.content)
  });
});
}
/** Creates a marker that shows a read-only info window when clicked. */
function createMarkerForDisplay(lat, lng, content){
  let url = "http://maps.google.com/mapfiles/ms/icons/yellow-dot.png";
const marker = new google.maps.Marker({
  position: {lat: lat, lng: lng},
  map: map,
  icon: url
});
var infoWindow = new google.maps.InfoWindow({
  content: content
});
marker.addListener('click', () => {
  infoWindow.open(map, marker);
});
}

// Activate tooltips
$(function () {
  $('[data-toggle="tooltip"]').tooltip()
})