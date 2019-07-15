var input; // To store what the user typed in search box
/** Fetches users found and adds them to the page. */
function fetchUsersFound(){
    if (input == 'none') {
      list.innerHTML = 'No results found.';
      return;
    }
    const url = '/search?search=' + input;
    fetch(url).then((response) => {
      return response.json();
    }).then((users) => {
      const list = document.getElementById('list');
      list.innerHTML = '';
      if (users == null || users == '') {
        list.innerHTML = 'No results found.';
        return;
      }

      users.forEach((user) => {
        const userListItem = buildUserListItem(user);
        list.appendChild(userListItem);
      });
    });
  }

/*
  Functions to get the parameters I previously sent through the URL
*/
  function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
  }
  function getUrlParam(parameter, defaultvalue){
    var urlparameter = defaultvalue;
    if(window.location.href.indexOf(parameter) > -1){
        urlparameter = getUrlVars()[parameter];
        }
    return urlparameter;
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

// Send search input through the URL
function sendInput() {
    var input = document.getElementById("search-input").value;
    input = encodeURIComponent(input);
    window.location.href = "search-results.html?search=" + input;
}

function buildSearchPage() {
  input = getUrlParam('search', 'none');
  document.getElementById("userInput").innerText = "'" + unescape(input) + "':";
  fetchUsersFound();

}