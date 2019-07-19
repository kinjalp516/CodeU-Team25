// Get ?user=XYZ parameter value
const urlParams = new URLSearchParams(window.location.search);
const parameterUsername = urlParams.get('user');
/** Fetches users found and adds them to the page. */
function fetchUsersFound(){
    const url = '/recommend?user=' + parameterUsername;
    fetch(url).then((response) => {
      return response.json();
    }).then((users) => {
      const list = document.getElementById('activityBuddiesList');
      list.innerHTML = '';
      if (users == null || users == '') {
        list.innerHTML = 'No users found with same activity.';
        return;
      }

      users.forEach((user) => {
        if (user != parameterUsername) {
          const userListItem = buildUserListItem(user);
          list.appendChild(userListItem);
        }
      });
    });
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
    activityContainer.innerHTML = activity;

  });
}

  /**
    * Builds a list element that contains a link to a user page, e.g.
    * <li><a href="/user-page.html?user=test@example.com">test@example.com</a></li>
    */
  function buildUserListItem(user){
    const userLink = document.createElement('a');
    userLink.setAttribute('href', '/user-page.html?user=' + user);

    const url = '/nickname?user=' + user;
    fetch(url).then((response) => {
        return response.text();
    }).then((nickname) => {
        if(nickname == ''){
            userLink.appendChild(document.createTextNode(user));
        }
        else {
            userLink.appendChild(document.createTextNode(nickname + ' - ' + user));
        }
    });

   
    const userListItem = document.createElement('li');
    userListItem.appendChild(userLink);
    return userListItem;
  }

function buildRecommendationsPage() {
  fetchActivity();
  fetchUsersFound();
}