let userEmail = null;

function fetchUserEmail() {
  $.ajaxSetup({ async: false });
  $.getJSON('/login-status', (loginStatus) => {
    if (loginStatus.isLoggedIn) {
      userEmail = loginStatus.username;
    }
  });
  $.ajaxSetup({ async: true });
}
fetchUserEmail();

function getUsername(email) {
  const url = `/nickname?user=` + email;
  return fetch(url)
    .then(response => {return response.text();})
    .then((nickname) => {
      return (nickname != null && nickname != '') ? nickname : email;
    });
}

function getActivity(email) {
  const url = '/act?user=' + email;
  return fetch(url)
    .then(response => {return response.text();})
    .then((activity) => {
      return (activity != null && activity != '') ? activity : "Gym";
    })
}

// Fetch messages and add them to the page.  
function fetchMessages(){
  const url = '/feed';
  fetch(url).then((response) => {
    return response.json();
  }).then((messages) => {
    const messageContainer = document.getElementById('message-container');
    if(messages.length == 0){
      messageContainer.innerHTML = '<p>There are no posts yet.</p>';
    }
    else{
      messageContainer.innerHTML = '';  
    }
    messages.forEach((message) => {  
      console.log(JSON.stringify(message));
      const messageDiv = buildMessageDiv(message);
      messageContainer.appendChild(messageDiv);
    });
  });
}
  
function buildMessageDiv(message){
  const usernameDiv = document.createElement('div');
  usernameDiv.classList.add("left-align", "font-weight-bold");  
  usernameDiv.id = 'username';
  getUsername(message.user).then((username) => {
    usernameDiv.appendChild(document.createTextNode(username));
  });
  
  const activityDiv = document.createElement('div');
  activityDiv.classList.add("left-align", "activity-div", "text-muted");
  activityDiv.appendChild(document.createTextNode("Activity: "));
  const activityLine = document.createElement('span');
  getActivity(message.user).then((activity) => {
    activityLine.classList.add("font-italic");
    activityLine.innerHTML = activity;
  })
  activityDiv.appendChild(activityLine);
  
  const headerDiv = document.createElement('div');
  headerDiv.classList.add('message-header');
  headerDiv.appendChild(usernameDiv);
  headerDiv.appendChild(activityDiv);
  headerDiv.appendChild(buildTimeDiv(message.timestamp));
  
  const bodyDiv = document.createElement('div');
  bodyDiv.classList.add('message-body');
  bodyDiv.innerHTML = message.text;
  //bodyDiv.appendChild(document.createTextNode(message.text));
  
  const messageDiv = document.createElement('div');
  messageDiv.classList.add("message-div");
  messageDiv.appendChild(headerDiv);
  messageDiv.appendChild(bodyDiv);

  messageDiv.appendChild(buildResponseDiv(message));
  messageDiv.appendChild(buildActionDiv(message));
  messageDiv.appendChild(buildCommentDiv(message.id));
  onCommentPost(message.id);
  onLikePost(message.id);
  return messageDiv;
}

/**
 * Function to show comments when clicking on the comment count
 */
function onClickCommentCount() {
  document.getElementById('comment-container').classList.remove('hidden');
}
/**
 * Function to show how many comments a message has
 * @param {*} message 
 */
function buildCommentCount(message) {
  // if there are no comments show empty string, 1 comment show 1 comment, else get length of comment list
  return (message.commentIDs == null || message.commentIDs.length === 0)
    ? '' : message.commentIDs.length === 1
      ? `<p class="text-muted font-weight-light pr-2 mb-0" data-toggle="collapse" data-target="#comment-container-${message.id}">1 comment</p>`
      : `<p class="text-muted font-weight-light pr-2 mb-0" data-toggle="collapse" data-target="#comment-container-${message.id}">${message.commentIDs.length} comments</p>`;
}

function buildLikeCount(message) {
  const messageCount = (message.likeEmails == null || message.likeEmails.length === 0)
    ? '' : message.likeEmails.length;
  return (message.likeEmails == null || message.likeEmails.length === 0)
    ? '' : `<i class="reaction-icon far fa-thumbs-up ml-1 mr-1"></i>
            <p class="reaction-count font-weight-light mb-0">${messageCount}</p>`;
}

function hasResponse(message) {
  return ((message.commentIDs != null && message.commentIDs.length !== 0)
    || (message.likeEmails != null && message.likeEmails.length !== 0));
}

function toggleResponse(message) {
  const responseDiv = document.getElementById(`response-container-${message.id}`);
  if (hasResponse(message)) {
    responseDiv.classList.remove('hidden');
  } else {
    responseDiv.classList.add('hidden');
  }
}

function buildResponseDiv(message) {
  const responseDiv = document.createElement('div');
  responseDiv.id = `response-container-${message.id}`;
  responseDiv.classList.add('response-container', 'd-flex', 'justify-content-between', 'mt-2', 'pb-2', 'border-bottom');

  // if (!hasResponse(message)) {
  //   responseDiv.classList.add('hidden');
  // }
  // else {
  //   responseDiv.classList.remove('hidden');
  // }

  responseDiv.innerHTML = `<span class="like-count-container d-flex flex-row" id="like-count-container-${message.id}">
                            ${buildLikeCount(message)}
                          </span>
                          <div class="comment-box d-flex flex-row">
                            <div id="comment-count-container-${message.id}">
                              ${buildCommentCount(message)}
                            </div>
                          </div>`;
  return responseDiv;
}

function buildLikeAction(message) {
  let iconHtml;
  if (message.likeEmails != null && message.likeEmails.includes(userEmail)) {
    iconHtml = '<i class="fas fa-thumbs-up mr-1"></i>Like';
  } else {
    iconHtml = '<i class="far fa-thumbs-up mr-1"></i>Like';
  }
  return iconHtml;
}

function onClickLikeButton(messageID) {
  if (userEmail != null) {
    const data = { userEmail, messageID };
    $.ajax({
      contentType: 'application/json',
      data: JSON.stringify(data),
      processData: false,
      type: 'POST',
      url: '/like',
    }).done(() => {
      fetch(`/message?messageID=${messageID}`)
        .then(response => response.json())
        .then((message) => {
          const likeCountContainers = document.querySelectorAll(`[id='like-count-container-${messageID}']`);
          likeCountContainers.forEach((div) => {
            // eslint-disable-next-line no-param-reassign
            div.innerHTML = buildLikeCount(message);
          });
          const likeActionContainers = document.querySelectorAll(`[id='like-action-container-${messageID}']`);
          likeActionContainers.forEach((div) => {
            // eslint-disable-next-line no-param-reassign
            div.innerHTML = buildLikeAction(message);
          });
          toggleResponse(message);
        });
    });
  } else {
    $('#instructUserToLoginModal').modal('show');
  }
}

function buildActionDiv(message) {
  const actionDiv = document.createElement('div');
  actionDiv.innerHTML = `<div id="action-container" class="action-container d-flex justify-content-between mt-2 pb-2">
                          <button id="like-action-container-${message.id}" class="btn btn-light btn-sm font-weight-light ml-1" onclick="onClickLikeButton('${message.id}');">
                            ${buildLikeAction(message)}
                          </button>
                          <button id="comment-action-container-${message.id}" class="btn btn-light btn-sm font-weight-light mr-1" data-toggle="collapse" data-target="#comment-container-${message.id}">
                            <i class="far fa-comment-alt mr-1"></i>
                            Comment
                          </button>
                         </div>`;
  return actionDiv;
}

function autoGrow(element) {
  // eslint-disable-next-line no-param-reassign
  element.style.height = '5px';
  // eslint-disable-next-line no-param-reassign
  element.style.height = `${element.scrollHeight}px`;
}

function enablePostButton(commentInputTextArea, messageID) {
  const commentPostButton = document.getElementById(`comment-post-button-${messageID}`);

  commentInputTextArea.addEventListener('input', () => {
    commentPostButton.disabled = true;
    if (commentInputTextArea.value) {
      const commentText = commentInputTextArea.value.toString().trim();
      if (commentText !== '') {
        commentPostButton.disabled = false;
      }
    }
  });
}

function getUserAvatarUrl(email) {
  if (email === null || email === undefined || email === '') {
    return Promise.resolve('./images/avatar-placeholder.gif');
  }
  const url = `/avatar?user=${email}`;
  return fetch(url)
    .then(response => {return response.text();})
    .then((avatar) => {
      return avatar;
    });
}

function buildCommentInput(messageID) {
  return getUserAvatarUrl(userEmail).then((userProfileImageUrl) => {
    const commentFormHtml = `<li class="media">
                            <a class="mr-3 my-2" href="#">
                              <img src="${userProfileImageUrl}" class="comment-image rounded-circle" alt="...">
                            </a>
                            <div class="media-body">
                              <div id="comment-input-container" class="comment-input-container">
                                <div class="input-group input-group-sm mt-2">
                                  <textarea
                                    name="comment-input-textarea-${messageID}"
                                    id="comment-input-textarea-${messageID}"
                                    class=form-control
                                    type=text
                                    placeholder="Add a comment"
                                    onblur="this.placeholder='Add a comment'"
                                    onfocus="this.placeholder=''"
                                    onkeyup="autoGrow(this)"
                                    oninput="enablePostButton(this, '${messageID}')"
                                    ></textarea>
                                  <div class="input-group-append">
                                    <button class="btn btn-light comment-post-button border" 
                                            disabled="true"
                                            type="button" 
                                            id="comment-post-button-${messageID}"
                                            onclick="onClickCommentPostButton('${messageID}');">
                                            Post
                                    </button>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </li>`;
    return commentFormHtml;
  });
}

function buildCommentItem(comment) {
  return getUsername(comment.user).then(username => getUserAvatarUrl(comment.user)
    .then((userProfileUrl) => {
      const commentHtml = `<li class="media">
            <a class="mr-3 my-2" href="/user-page.html?user=${comment.user}">
              <img src="${userProfileUrl}" class="comment-image rounded-circle" alt="...">
            </a>
            <div class="media-body">
              <div class="d-flex justify-content-between mt-1">
                <a href="/user-page.html?user=${comment.user}"><p class="mb-0 font-weight-normal comment-username">${username}</p></a>
                <p class="card-text mb-0 comment-time-container">
                  <small class="text-muted">${getTimeText(comment.timestamp)}</small>  
                </p>
              </div>
              <div class="d-flex justify-content-between mt-1">
                <p class="font-weight-light comment-text mb-0">${comment.text}</p>
              </div>
            </div>
          </li>`;

      return commentHtml;
    }));
}

function getHourDiffFromNow(timeStamp) {
  const duration = moment.duration(moment(new Date()).diff(moment(timeStamp)));
  const hours = duration.asHours();
  return hours;
}

function getTimeText(timestamp) {
  return getHourDiffFromNow(timestamp) < 24 ? moment(timestamp).fromNow()
    : getHourDiffFromNow(timestamp) < (24 * 7) ? moment(timestamp).calendar()
      : moment(timestamp).format('ll');
}

function buildTimeDiv(timestamp) {
  const timeDiv = document.createElement('p');
  timeDiv.classList.add('card-text', 'mb-0');

  const timeText = document.createElement('small');
  timeText.classList.add('text-muted');

  timeText.innerHTML = getTimeText(timestamp);

  timeDiv.appendChild(timeText);
  return timeDiv;
}

function buildCommentHtml(messageID) {
  let commentHtml = `<ul class="list-unstyled comment-list mb-0" id="comment-list-${messageID}">`;

  return buildCommentInput(messageID).then((commentInput) => {
    commentHtml += commentInput;

    const url = `/comments?messageID=${messageID}`;
    return fetch(url)
      .then(response => response.json())
      .then((comments) => {
        let commentSequence = Promise.resolve();
        comments.forEach((comment) => {
          commentSequence = commentSequence.then(() => buildCommentItem(comment))
            .then((commentItemHtml) => {
              commentHtml += commentItemHtml;
            });
        });

        return commentSequence.then(() => {
          commentHtml += '</ul>';
          return commentHtml;
        });
      });
  });
}

function onCommentPost(messageId) {
  fetch(`/message?messageID=${messageId}`)
    .then(response => response.json())
    .then((message) => {
      $(`#comment-count-container-${messageId}`).html(
        buildCommentCount(message),
      );
      toggleResponse(message);
    });
}

function onLikePost(messageId) {
  fetch(`/message?messageID=${messageId}`)
        .then(response => response.json())
        .then((message) => {
          const likeCountContainers = document.querySelectorAll(`[id='like-count-container-${messageId}']`);
          likeCountContainers.forEach((div) => {
            // eslint-disable-next-line no-param-reassign
            div.innerHTML = buildLikeCount(message);
          });
          const likeActionContainers = document.querySelectorAll(`[id='like-action-container-${messageId}']`);
          likeActionContainers.forEach((div) => {
            // eslint-disable-next-line no-param-reassign
            div.innerHTML = buildLikeAction(message);
          });
          toggleResponse(message);
        });
}

function onClickCommentPostButton(messageId) {
  const commentInputTextarea = document.getElementById(`comment-input-textarea-${messageId}`);

  const comment = {
    messageId: messageId,
    userText: commentInputTextarea.value,
  };
  console.log(JSON.stringify(comment));
  $.ajax({
    contentType: 'application/json',
    data: JSON.stringify(comment),
    processData: false,
    type: 'POST',
    url: '/comments',
  }).done((response) => {
    if (response.toString().trim() !== '') {
      $('#instructUserToLoginModal').modal('show');
    } else {
      buildCommentHtml(messageId).then((commentHtml) => {
        $(`#comment-container-${messageId}`).html(commentHtml);
      });

      onCommentPost(messageId);
    }
  });
}

function buildCommentDiv(messageId) {
  const commentDiv = document.createElement('div');
  commentDiv.classList.add('px-2', 'py-1', 'border-top', 'collapse', 'comment-container');
  commentDiv.id = `comment-container-${messageId}`;

  buildCommentHtml(messageId).then((commentHtml) => {
    commentDiv.innerHTML = commentHtml;
  });

  return commentDiv;
}


// Fetch data and populate the UI of the page.
function buildUI(){
  fetchUserList();
  fetchMessages();
}