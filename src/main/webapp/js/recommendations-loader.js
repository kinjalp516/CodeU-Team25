// Get ?user=XYZ parameter value
const urlParams = new URLSearchParams(window.location.search);
const parameterUsername = urlParams.get('user');

/** Fetches users found and adds them to the page. */
function fetchUsersFound(){
    const url = '/recommend?user=' + parameterUsername;
    fetch(url).then((response) => {
      return response.json();
    }).then((users) => {
      // const list = document.getElementById('activityBuddiesList');
      // list.innerHTML = '';
      const noCard = document.getElementById('userCards');
      if (users == null || users == '') {
        noCard.innerHTML = 'No users found with same activity.';
        return;
      }

      users.forEach((user) => {
        if (user != parameterUsername) {
          var card = document.createElement("div");
          card.className = "card";
          card.style = "width:300px";

          var image = document.createElement("img");
          image.className = "card-img-top";
          const url = '/avatar?user=' + user;
          fetch(url).then((response) => {
            return response.text();
          }).then((avatar) => {const avatarContainer = document.getElementById('avatar-img');
            if(avatar != ''){
              image.src = avatar;
            }
            else {
              image.src = "/images/avatar-placeholder.gif";
            }
          });
          card.appendChild(image);

          var cardBody = document.createElement("div");
          cardBody.className = "card-body";

          var cardTitle = document.createElement("div");
          cardTitle.className = "card-title";
          const url2 = '/nickname?user=' + user;
          fetch(url2).then((response) => {
            return response.text();
          }).then((nickname) => {
            if(nickname == ''){
              cardTitle.innerHTML = user;
            }
            else {
              cardTitle.innerHTML = nickname;            }
          });

          var cardText = document.createElement("p");
          cardText.className = "card-text";
          const url3 = '/lvl?user=' + user;
          fetch(url3).then((response) => {
            return response.text();
          }).then((skillLevel) => {
            if(skillLevel == ''){
              cardText.innerHTML = 'Beginner';            }    
            else {
              cardText.innerHTML = skillLevel;            }
          });

          var profileButton = document.createElement("a");
          profileButton.className = "btn btn-primary";
          profileButton.href = "/profile.html?user=" + user;
          profileButton.innerHTML = "View Profile";

          cardBody.appendChild(cardTitle);
          cardBody.appendChild(cardText);
          cardBody.appendChild(profileButton);
          card.appendChild(cardBody);
          document.getElementById("userCards").appendChild(card);

          // const userListItem = buildUserListItem(user);
          // list.appendChild(userListItem);
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