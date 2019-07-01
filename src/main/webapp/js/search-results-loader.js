/** Fetches users found and adds them to the page. */
function fetchUsersFound(){
    const url = '/search';
    fetch(url).then((response) => {
      return response.json();
    }).then((users) => {
      const list = document.getElementById('list');
      list.innerHTML = '';

      users.forEach((user) => {
        const userListItem = buildUserListItem(user);
        list.appendChild(userListItem);
      });
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


function sendInput() {
    var input = document.getElementById("search-input").value;
    // var GET = {};
    // var query = window.location.search.substring(1).split("&");
    // for (var i = 0, max = query.length; i < max; i++)
    // {
    //     if (query[i] === "") // check for trailing & with no param
    //         continue;

    //     var param = query[i].split("=");
    //     GET[decodeURIComponent(param[0])] = decodeURIComponent(param[1] || "");
    // }
    input = encodeURIComponent(input);
    window.location.href = "search-results.html?search=" + input;
}

// function getUrlVars() {
//     var vars = {};
//     var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
//         vars[key] = value;
//     });
//     return vars;
// }

// function getUrlParam(parameter, defaultvalue){
//     var urlparameter = defaultvalue;
//     if(window.location.href.indexOf(parameter) > -1){
//         urlparameter = getUrlVars()[parameter];
//         }
//     return urlparameter;
// }

function buildSearchPage() {
    fetchUsersFound();

}