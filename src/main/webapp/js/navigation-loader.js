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

/**
 * Adds a login or logout link to the page, depending on whether the user is
 * already logged in.
 */
function addLoginOrLogoutLinkToNavigation() {
  const navigationElement = document.getElementById('navigation');
  if (!navigationElement) {
    console.warn('Navigation element not found!');
    return;
  }

  fetch('/login-status')
      .then((response) => {
        return response.json();
      })
      .then((loginStatus) => {
        if (loginStatus.isLoggedIn) {
          if (window.location.pathname == '/user-page.html?user=' + loginStatus.username) {
              navigationElement.appendChild(createActiveListItem(createLink(
                '/user-page.html?user=' + loginStatus.username, 'Your Page')));
          }
          else {
            navigationElement.appendChild(createListItem(createLink(
              '/user-page.html?user=' + loginStatus.username, 'Your Page')));
          }          

          navigationElement.appendChild(
              createListItem(createLink('/logout', 'Logout')));
        } else {
          navigationElement.appendChild(
              createButtonItem(createListItem(createLink('/login', 'Login'))));
        }
      });
}

/**
 * Creates a button element.
 * @param {Element} childElement
 * @return {Element} button element
 */
function createButtonItem(childElement) {
  const listButtonElement = document.createElement('button');
  listButtonElement.appendChild(childElement);
  listButtonElement.className = "navButton";
  return listButtonElement;
}

/**
 * Creates an li element.
 * @param {Element} childElement
 * @return {Element} li element
 */
function createListItem(childElement) {
  const listItemElement = document.createElement('li');
  listItemElement.appendChild(childElement);
  listItemElement.className = "nav-item";
  return listItemElement;
}

/**
 * Creates an active nav element.
 * @param {Element} childElement
 * @return {Element} li element
 */
function createActiveListItem(childElement) {
  const listItemElement = document.createElement('li');
  listItemElement.appendChild(childElement);
  listItemElement.className = "nav-item active";
  return listItemElement;
}

/**
 * Creates an anchor element.
 * @param {string} url
 * @param {string} text
 * @return {Element} Anchor element
 */
function createLink(url, text) {
  const linkElement = document.createElement('a');
  linkElement.appendChild(document.createTextNode(text));
  linkElement.href = url;
  linkElement.className = "nav-link font";
  return linkElement;
}

// listen for whenever the window loads.
window.addEventListener('load', function() {
  addLoginOrLogoutLinkToNavigation();
})