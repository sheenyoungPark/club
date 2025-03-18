document.addEventListener('DOMContentLoaded', function() {
    // 전역 변수
    let stompClient = null;
    let currentRoomId = null;
    let currentRoomType = null;
    let unsubscribe = null;
    let typing = false;
    let typingTimeout = null;

    // DOM 요소 참조
    const chatRoomList = document.getElementById('chatRoomList');
    const messagesContainer = document.getElementById('messagesContainer');
    const messageInput = document.getElementById('messageInput');
    const sendMessageBtn = document.getElementById('sendMessageBtn');
    const newChatBtn = document.getElementById('newChatBtn');
    const chatPlaceholder = document.getElementById('chatPlaceholder');
    const chatRoom = document.getElementById('chatRoom');
    const currentRoomName = document.getElementById('currentRoomName');
    const tabButtons = document.querySelectorAll('.tab-btn');
    const searchChatInput = document.getElementById('searchChatInput');
    const userSearchInput = document.getElementById('userSearchInput');
    const searchResults = document.getElementById('searchResults');
    const chatInfoBtn = document.getElementById('chatInfoBtn');
    const newChatModal = document.getElementById('newChatModal');
    const chatInfoModal = document.getElementById('chatInfoModal');
    const closeNewChatModal = document.getElementById('closeNewChatModal');
    const closeChatInfoModal = document.getElementById('closeChatInfoModal');
    const chatRoomDetails = document.getElementById('chatRoomDetails');

    // STOMP 연결 설정
    function connectStomp() {
        const socket = new SockJS('/ws-connect');
        stompClient = Stomp.over(socket);
        stompClient.debug = null; // 콘솔 디버그 메시지 비활성화

        stompClient.connect({}, function(frame) {
            console.log('Connected to websocket');

            // 사용자별 구독 (개인 메시지용)
            stompClient.subscribe('/user/queue/messages', function(message) {
                const receivedMessage = JSON.parse(message.body);
                handleReceivedMessage(receivedMessage);
            });
        }, function(error) {
            console.error('STOMP connection error:', error);
            setTimeout(connectStomp, 5000); // 5초 후 재연결 시도
        });
    }

    // 초기화 함수
    function initialize() {
        // WebSocket 연결
        connectStomp();

        // 이벤트 리스너 설정
        setupEventListeners();

        // 탭 초기화
        initializeTabs();

        // 첫 번째 채팅방 자동 선택 (있는 경우)
        autoSelectFirstRoom();
    }

    // 이벤트 리스너 설정
    function setupEventListeners() {
        // 채팅방 클릭 이벤트
        chatRoomList.addEventListener('click', function(e) {
            const chatRoomItem = e.target.closest('.chat-room-item');
            if (chatRoomItem) {
                selectChatRoom(chatRoomItem);
            }
        });

        // 메시지 전송 버튼 클릭 이벤트
        sendMessageBtn.addEventListener('click', sendMessage);

        // 엔터키로 메시지 전송
        messageInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });

        // 입력 시작 감지
        messageInput.addEventListener('input', function() {
            if (!typing) {
                typing = true;
                // typing 상태 서버에 알림 (필요한 경우 구현)
            }

            // 타이핑 타임아웃 재설정
            clearTimeout(typingTimeout);
            typingTimeout = setTimeout(function() {
                typing = false;
                // typing 종료 상태 서버에 알림 (필요한 경우 구현)
            }, 1000); // 1초 동안 입력이 없으면 typing 상태 종료
        });

        // 새 채팅 버튼 클릭 이벤트
        newChatBtn.addEventListener('click', function() {
            newChatModal.style.display = 'flex';
        });

        // 채팅방 정보 버튼 클릭 이벤트
        chatInfoBtn.addEventListener('click', function() {
            if (currentRoomId) {
                loadRoomDetails(currentRoomId);
                chatInfoModal.style.display = 'flex';
            }
        });

        // 모달 닫기 이벤트
        closeNewChatModal.addEventListener('click', function() {
            newChatModal.style.display = 'none';
        });

        closeChatInfoModal.addEventListener('click', function() {
            chatInfoModal.style.display = 'none';
        });

        // 모달 외부 클릭시 닫기
        window.addEventListener('click', function(e) {
            if (e.target === newChatModal) {
                newChatModal.style.display = 'none';
            }
            if (e.target === chatInfoModal) {
                chatInfoModal.style.display = 'none';
            }
        });

        // 검색 입력 이벤트
        searchChatInput.addEventListener('input', function() {
            searchChatRooms(this.value);
        });

        // 사용자 검색 입력 이벤트
        userSearchInput.addEventListener('input', function() {
            if (this.value.length >= 2) {
                searchUsers(this.value);
            } else {
                searchResults.innerHTML = '';
            }
        });
    }

    // 탭 초기화
    function initializeTabs() {
        tabButtons.forEach(button => {
            button.addEventListener('click', function() {
                // 현재 활성 탭 제거
                tabButtons.forEach(btn => btn.classList.remove('active'));

                // 클릭한 탭 활성화
                this.classList.add('active');

                // 탭에 따른 채팅방 목록 로드
                const roomType = this.dataset.type;
                loadChatRooms(roomType);
            });
        });

        // 기본 탭(개인 채팅) 활성화
        document.querySelector('.tab-btn[data-type="PRIVATE"]').classList.add('active');
        loadChatRooms('PRIVATE');
    }

    // 채팅방 선택 처리
    function selectChatRoom(chatRoomItem) {
        // 이전 선택 항목 비활성화
        const prevSelected = document.querySelector('.chat-room-item.selected');
        if (prevSelected) {
            prevSelected.classList.remove('selected');
        }

        // 새 항목 활성화
        chatRoomItem.classList.add('selected');

        // 새 채팅방 정보 설정
        const roomId = chatRoomItem.dataset.roomId;
        const roomName = chatRoomItem.querySelector('.chat-room-name').textContent;
        const roomType = chatRoomItem.dataset.roomType;

        // 현재 채팅방 설정 업데이트
        currentRoomId = roomId;
        currentRoomName.textContent = roomName;
        currentRoomType = roomType;

        // 채팅방 표시 및 메시지 로드
        chatPlaceholder.style.display = 'none';
        chatRoom.style.display = 'flex';

        // 이전 구독 취소 (있는 경우)
        if (unsubscribe) {
            unsubscribe();
        }

        // 새 채팅방 구독
        unsubscribe = subscribeToRoom(roomId);

        // 메시지 기록 로드
        loadMessages(roomId);

        // 읽음 표시 업데이트
        markRoomAsRead(roomId);
    }

    // 채팅방 구독
    function subscribeToRoom(roomId) {
        const subscription = stompClient.subscribe(`/topic/room/${roomId}`, function(message) {
            const receivedMessage = JSON.parse(message.body);
            displayMessage(receivedMessage);
        });

        return function() {
            subscription.unsubscribe();
        };
    }

    // 메시지 전송 함수
    function sendMessage() {
        const messageText = messageInput.value.trim();

        if (messageText && currentRoomId) {
            const message = {
                roomId: currentRoomId,
                content: messageText,
                type: 'TEXT'
            };

            stompClient.send("/app/chat.send", {}, JSON.stringify(message));
            messageInput.value = '';

            // 메시지 미리 표시 (옵션)
            // displayMessage({...message, sender: { id: currentUserId, username: currentUsername }, timestamp: new Date().toISOString()});
        }
    }

    // 수신된 메시지 처리
    function handleReceivedMessage(message) {
        // 알림 메시지인 경우
        if (message.type === 'NOTIFICATION') {
            showNotification(message);
            return;
        }

        // 현재 선택된 채팅방의 메시지인 경우 표시
        if (message.roomId === currentRoomId) {
            displayMessage(message);
            markRoomAsRead(currentRoomId);
        } else {
            // 다른 채팅방의 메시지는 해당 채팅방 항목에 읽지 않음 표시
            updateUnreadCount(message.roomId);
        }
    }

    // 메시지 표시 함수
    function displayMessage(message) {
        const messageElement = createMessageElement(message);
        messagesContainer.appendChild(messageElement);

        // 스크롤을 가장 아래로 이동
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    // 메시지 요소 생성
    function createMessageElement(message) {
        const messageDiv = document.createElement('div');
        messageDiv.className = 'message';

        // 현재 사용자 메시지인지 확인
        const isCurrentUser = message.sender.id === getCurrentUserId();
        if (isCurrentUser) {
            messageDiv.classList.add('my-message');
        } else {
            messageDiv.classList.add('other-message');
        }

        // 시간 포맷팅
        const timestamp = new Date(message.timestamp);
        const formattedTime = timestamp.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});

        // 메시지 내용에 따른 처리
        if (message.type === 'TEXT') {
            messageDiv.innerHTML = `
        <div class="message-content">
          ${!isCurrentUser ? `<span class="sender-name">${message.sender.username}</span>` : ''}
          <p>${message.content}</p>
          <span class="message-time">${formattedTime}</span>
        </div>
      `;
        } else if (message.type === 'IMAGE') {
            messageDiv.innerHTML = `
        <div class="message-content">
          ${!isCurrentUser ? `<span class="sender-name">${message.sender.username}</span>` : ''}
          <img src="${message.content}" alt="Shared image" class="message-image">
          <span class="message-time">${formattedTime}</span>
        </div>
      `;
        }

        return messageDiv;
    }

    // 채팅방 목록 로드
    function loadChatRooms(roomType) {
        fetch(`/api/rooms?type=${roomType}`)
            .then(response => response.json())
            .then(rooms => {
                displayChatRooms(rooms);
            })
            .catch(error => {
                console.error('Error loading chat rooms:', error);
            });
    }

    // 채팅방 목록 표시
    function displayChatRooms(rooms) {
        chatRoomList.innerHTML = '';

        if (rooms.length === 0) {
            const emptyMessage = document.createElement('div');
            emptyMessage.className = 'empty-list-message';
            emptyMessage.textContent = '채팅방이 없습니다.';
            chatRoomList.appendChild(emptyMessage);
            return;
        }

        rooms.forEach(room => {
            const roomElement = createChatRoomElement(room);
            chatRoomList.appendChild(roomElement);
        });
    }

    // 채팅방 요소 생성
    function createChatRoomElement(room) {
        const roomDiv = document.createElement('div');
        roomDiv.className = 'chat-room-item';
        roomDiv.dataset.roomId = room.id;
        roomDiv.dataset.roomType = room.type;

        // 마지막 메시지 및 시간 포맷팅
        let lastMessageText = '새로운 채팅방';
        let lastMessageTime = '';

        if (room.lastMessage) {
            lastMessageText = room.lastMessage.type === 'TEXT'
                ? room.lastMessage.content
                : '이미지를 보냈습니다.';

            const timestamp = new Date(room.lastMessage.timestamp);
            lastMessageTime = formatTimestamp(timestamp);
        }

        // 읽지 않은 메시지 수 표시
        const unreadBadge = room.unreadCount > 0
            ? `<span class="unread-badge">${room.unreadCount}</span>`
            : '';

        roomDiv.innerHTML = `
      <div class="room-avatar">
        ${room.type === 'GROUP' ? '<i class="fas fa-users"></i>' : '<i class="fas fa-user"></i>'}
      </div>
      <div class="room-info">
        <div class="room-header">
          <span class="chat-room-name">${room.name}</span>
          <span class="last-message-time">${lastMessageTime}</span>
        </div>
        <div class="room-footer">
          <span class="last-message">${lastMessageText}</span>
          ${unreadBadge}
        </div>
      </div>
    `;

        return roomDiv;
    }

    // 메시지 기록 로드
    function loadMessages(roomId) {
        messagesContainer.innerHTML = '';

        // 로딩 표시 추가
        const loadingElement = document.createElement('div');
        loadingElement.className = 'loading-messages';
        loadingElement.textContent = '메시지 로딩 중...';
        messagesContainer.appendChild(loadingElement);

        fetch(`/api/rooms/${roomId}/messages`)
            .then(response => response.json())
            .then(messages => {
                messagesContainer.innerHTML = '';

                if (messages.length === 0) {
                    const emptyElement = document.createElement('div');
                    emptyElement.className = 'empty-messages';
                    emptyElement.textContent = '아직 메시지가 없습니다. 첫 메시지를 보내보세요!';
                    messagesContainer.appendChild(emptyElement);
                    return;
                }

                messages.forEach(message => {
                    displayMessage(message);
                });
            })
            .catch(error => {
                console.error('Error loading messages:', error);
                messagesContainer.innerHTML = '<div class="error-message">메시지를 불러오는 중 오류가 발생했습니다.</div>';
            });
    }

    // 채팅방 읽음 표시
    function markRoomAsRead(roomId) {
        fetch(`/api/rooms/${roomId}/read`, { method: 'POST' })
            .then(response => {
                if (response.ok) {
                    // 해당 채팅방의 읽지 않음 표시 제거
                    const roomElement = document.querySelector(`.chat-room-item[data-room-id="${roomId}"]`);
                    if (roomElement) {
                        const unreadBadge = roomElement.querySelector('.unread-badge');
                        if (unreadBadge) {
                            unreadBadge.remove();
                        }
                    }
                }
            })
            .catch(error => {
                console.error('Error marking room as read:', error);
            });
    }

    // 안읽은 메시지 수 업데이트
    function updateUnreadCount(roomId) {
        const roomElement = document.querySelector(`.chat-room-item[data-room-id="${roomId}"]`);
        if (roomElement) {
            let unreadBadge = roomElement.querySelector('.unread-badge');

            if (unreadBadge) {
                // 기존 배지 업데이트
                const count = parseInt(unreadBadge.textContent) + 1;
                unreadBadge.textContent = count;
            } else {
                // 새 배지 생성
                unreadBadge = document.createElement('span');
                unreadBadge.className = 'unread-badge';
                unreadBadge.textContent = '1';

                const roomFooter = roomElement.querySelector('.room-footer');
                roomFooter.appendChild(unreadBadge);
            }

            // 채팅방 항목을 목록 상단으로 이동 (선택 사항)
            chatRoomList.insertBefore(roomElement, chatRoomList.firstChild);
        }
    }

    // 알림 표시
    function showNotification(message) {
        // 사용자가 브라우저 알림을 허용했는지 확인
        if (Notification.permission === 'granted') {
            const notification = new Notification(message.title, {
                body: message.content,
                icon: '/images/notification-icon.png'
            });

            notification.onclick = function() {
                window.focus();
                if (message.roomId) {
                    const roomElement = document.querySelector(`.chat-room-item[data-room-id="${message.roomId}"]`);
                    if (roomElement) {
                        selectChatRoom(roomElement);
                    }
                }
            };
        }

        // 인앱 알림 표시 (선택 사항)
        const notificationElement = document.createElement('div');
        notificationElement.className = 'in-app-notification';
        notificationElement.innerHTML = `
      <div class="notification-title">${message.title}</div>
      <div class="notification-body">${message.content}</div>
    `;

        document.body.appendChild(notificationElement);

        // 5초 후 알림 제거
        setTimeout(() => {
            notificationElement.classList.add('notification-hide');
            setTimeout(() => {
                document.body.removeChild(notificationElement);
            }, 300); // 애니메이션 시간
        }, 5000);
    }

    // 사용자 검색
    function searchUsers(query) {
        fetch(`/api/users/search?query=${encodeURIComponent(query)}`)
            .then(response => response.json())
            .then(users => {
                displaySearchResults(users);
            })
            .catch(error => {
                console.error('Error searching users:', error);
            });
    }

    // 검색 결과 표시
    function displaySearchResults(users) {
        searchResults.innerHTML = '';

        if (users.length === 0) {
            searchResults.innerHTML = '<div class="no-results">검색 결과가 없습니다.</div>';
            return;
        }

        users.forEach(user => {
            const userElement = document.createElement('div');
            userElement.className = 'user-result';
            userElement.innerHTML = `
        <span class="user-name">${user.username}</span>
        <button class="start-chat-btn" data-user-id="${user.id}">대화 시작</button>
      `;

            // 대화 시작 버튼 이벤트
            const chatBtn = userElement.querySelector('.start-chat-btn');
            chatBtn.addEventListener('click', function() {
                createPrivateChat(user.id);
            });

            searchResults.appendChild(userElement);
        });
    }

    // 개인 채팅방 생성
    function createPrivateChat(userId) {
        fetch('/api/rooms/private', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ userId: userId })
        })
            .then(response => response.json())
            .then(room => {
                // 새 채팅방이 생성되면 모달 닫기
                newChatModal.style.display = 'none';

                // 검색 입력 초기화
                userSearchInput.value = '';
                searchResults.innerHTML = '';

                // 채팅방 목록 새로고침
                loadChatRooms(currentRoomType || 'PRIVATE');

                // 생성된 채팅방 선택
                setTimeout(() => {
                    const roomElement = document.querySelector(`.chat-room-item[data-room-id="${room.id}"]`);
                    if (roomElement) {
                        selectChatRoom(roomElement);
                    }
                }, 500);
            })
            .catch(error => {
                console.error('Error creating private chat:', error);
            });
    }

    // 그룹 채팅방 생성
    function createGroupChat(name, userIds) {
        fetch('/api/rooms/group', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                name: name,
                userIds: userIds
            })
        })
            .then(response => response.json())
            .then(room => {
                // 새 채팅방이 생성되면 모달 닫기
                newChatModal.style.display = 'none';

                // 입력 초기화
                document.getElementById('groupNameInput').value = '';
                document.getElementById('selectedUsers').innerHTML = '';

                // 채팅방 목록 새로고침
                loadChatRooms(currentRoomType || 'GROUP');

                // 생성된 채팅방 선택
                setTimeout(() => {
                    const roomElement = document.querySelector(`.chat-room-item[data-room-id="${room.id}"]`);
                    if (roomElement) {
                        selectChatRoom(roomElement);
                    }
                }, 500);
            })
            .catch(error => {
                console.error('Error creating group chat:', error);
            });
    }

    // 채팅방 검색
    function searchChatRooms(query) {
        if (!query) {
            // 검색어가 비어있으면 전체 목록 표시
            loadChatRooms(currentRoomType || 'PRIVATE');
            return;
        }

        const roomElements = chatRoomList.querySelectorAll('.chat-room-item');

        roomElements.forEach(room => {
            const roomName = room.querySelector('.chat-room-name').textContent.toLowerCase();

            if (roomName.includes(query.toLowerCase())) {
                room.style.display = '';
            } else {
                room.style.display = 'none';
            }
        });
    }

    // 채팅방 상세 정보 로드
    function loadRoomDetails(roomId) {
        fetch(`/api/rooms/${roomId}/details`)
            .then(response => response.json())
            .then(details => {
                displayRoomDetails(details);
            })
            .catch(error => {
                console.error('Error loading room details:', error);
                chatRoomDetails.innerHTML = '<div class="error-message">채팅방 정보를 불러오는 중 오류가 발생했습니다.</div>';
            });
    }

    // 채팅방 상세 정보 표시
    function displayRoomDetails(details) {
        chatRoomDetails.innerHTML = '';

        // 채팅방 정보 헤더
        const headerDiv = document.createElement('div');
        headerDiv.className = 'room-details-header';
        headerDiv.innerHTML = `
      <h3>${details.name}</h3>
      <p>${details.type === 'GROUP' ? '그룹 채팅' : '개인 채팅'}</p>
      <p>생성일: ${formatDate(new Date(details.createdAt))}</p>
    `;

        // 채팅방 멤버 목록
        const membersDiv = document.createElement('div');
        membersDiv.className = 'room-members';
        membersDiv.innerHTML = '<h4>채팅방 멤버</h4>';

        const membersList = document.createElement('ul');
        details.members.forEach(member => {
            const memberItem = document.createElement('li');
            memberItem.className = 'member-item';
            memberItem.innerHTML = `
        <span class="member-name">${member.username}</span>
        ${member.isAdmin ? '<span class="admin-badge">관리자</span>' : ''}
      `;
            membersList.appendChild(memberItem);
        });

        membersDiv.appendChild(membersList);

        // 관리 버튼 (그룹 채팅이고 사용자가 관리자인 경우)
        if (details.type === 'GROUP' && details.isCurrentUserAdmin) {
            const adminDiv = document.createElement('div');
            adminDiv.className = 'room-admin-options';

            // 새 멤버 추가 버튼
            const addMemberBtn = document.createElement('button');
            addMemberBtn.className = 'add-member-btn';
            addMemberBtn.textContent = '멤버 추가';
            addMemberBtn.addEventListener('click', function() {
                showAddMemberModal(details.id);
            });

            // 그룹명 변경 버튼
            const changeNameBtn = document.createElement('button');
            changeNameBtn.className = 'change-name-btn';
            changeNameBtn.textContent = '그룹명 변경';
            changeNameBtn.addEventListener('click', function() {
                showChangeNameModal(details.id, details.name);
            });

            // 채팅방 나가기 버튼
            const leaveRoomBtn = document.createElement('button');
            leaveRoomBtn.className = 'leave-room-btn';
            leaveRoomBtn.textContent = '채팅방 나가기';
            leaveRoomBtn.addEventListener('click', function() {
                confirmLeaveRoom(details.id);
            });

            adminDiv.appendChild(addMemberBtn);
            adminDiv.appendChild(changeNameBtn);
            adminDiv.appendChild(leaveRoomBtn);

            // 최종 추가
            chatRoomDetails.appendChild(headerDiv);
            chatRoomDetails.appendChild(membersDiv);
            chatRoomDetails.appendChild(adminDiv);
        } else {
            // 일반 사용자인 경우 채팅방 나가기 버튼만 표시
            const actionDiv = document.createElement('div');
            actionDiv.className = 'room-user-options';

            const leaveRoomBtn = document.createElement('button');
            leaveRoomBtn.className = 'leave-room-btn';
            leaveRoomBtn.textContent = '채팅방 나가기';
            leaveRoomBtn.addEventListener('click', function() {
                confirmLeaveRoom(details.id);
            });

            actionDiv.appendChild(leaveRoomBtn);

            // 최종 추가
            chatRoomDetails.appendChild(headerDiv);
            chatRoomDetails.appendChild(membersDiv);
            chatRoomDetails.appendChild(actionDiv);
        }
    }

    // 멤버 추가 모달 표시
    function showAddMemberModal(roomId) {
        // 모달 생성 및 표시 로직
        // (구현 생략)
    }

    // 그룹명 변경 모달 표시
    function showChangeNameModal(roomId, currentName) {
        // 모달 생성 및 표시 로직
        // (구현 생략)
    }

    // 채팅방 나가기 확인
    function confirmLeaveRoom(roomId) {
        if (confirm('정말로 이 채팅방을 나가시겠습니까?')) {
            leaveRoom(roomId);
        }
    }

    // 채팅방 나가기
    function leaveRoom(roomId) {
        fetch(`/api/rooms/${roomId}/leave`, {
            method: 'POST'
        })
            .then(response => {
                if (response.ok) {
                    // 채팅방 정보 모달 닫기
                    chatInfoModal.style.display = 'none';

                    // 채팅방 목록에서 제거
                    const roomElement = document.querySelector(`.chat-room-item[data-room-id="${roomId}"]`);
                    if (roomElement) {
                        roomElement.remove();
                    }

                    // 현재 선택된 채팅방인 경우 초기화
                    if (currentRoomId === roomId) {
                        currentRoomId = null;
                        chatPlaceholder.style.display = 'flex';
                        chatRoom.style.display = 'none';
                    }

                    // 채팅방 목록 새로고침
                    loadChatRooms(currentRoomType || 'PRIVATE');
                }
            })
            .catch(error => {
                console.error('Error leaving room:', error);
                alert('채팅방을 나가는 중 오류가 발생했습니다.');
            });
    }

    // 첫 번째 채팅방 자동 선택
    function autoSelectFirstRoom() {
        setTimeout(() => {
            const firstRoom = document.querySelector('.chat-room-item');
            if (firstRoom) {
                selectChatRoom(firstRoom);
            }
        }, 300);
    }

    // 시간 포맷 유틸리티 함수
    function formatTimestamp(timestamp) {
        const now = new Date();
        const date = new Date(timestamp);

        // 오늘인 경우 시간만 표시
        if (date.toDateString() === now.toDateString()) {
            return date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
        }

        // 어제인 경우
        const yesterday = new Date(now);
        yesterday.setDate(yesterday.getDate() - 1);
        if (date.toDateString() === yesterday.toDateString()) {
            return '어제';
        }

        // 일주일 이내인 경우 요일 표시
        const weekday = ['일', '월', '화', '수', '목', '금', '토'];
        const dayDiff = Math.floor((now - date) / (1000 * 60 * 60 * 24));
        if (dayDiff < 7) {
            return weekday[date.getDay()] + '요일';
        }

        // 그 외의 경우 날짜 표시
        return `${date.getMonth() + 1}/${date.getDate()}`;
    }

    // 날짜 포맷 함수
    function formatDate(date) {
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    }

    // 현재 사용자 ID 가져오기
    function getCurrentUserId() {
        // 사용자 인증 정보에서 ID 가져오기
        return document.getElementById('currentUserId').value;
    }

    // 브라우저 알림 권한 요청
    function requestNotificationPermission() {
        if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
            Notification.requestPermission();
        }
    }

    // 이미지 업로드 처리
    function setupImageUpload() {
        const imageUploadInput = document.getElementById('imageUploadInput');
        const imageUploadBtn = document.getElementById('imageUploadBtn');

        imageUploadBtn.addEventListener('click', function() {
            imageUploadInput.click();
        });

        imageUploadInput.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                const formData = new FormData();
                formData.append('image', this.files[0]);
                formData.append('roomId', currentRoomId);

                fetch('/api/messages/image', {
                    method: 'POST',
                    body: formData
                })
                    .then(response => response.json())
                    .then(data => {
                        // 이미지 업로드 후 입력 초기화
                        imageUploadInput.value = '';
                    })
                    .catch(error => {
                        console.error('Error uploading image:', error);
                        alert('이미지 업로드 중 오류가 발생했습니다.');
                    });
            }
        });
    }

    // 이모티콘 선택기 설정
    function setupEmojiPicker() {
        const emojiBtn = document.getElementById('emojiBtn');
        const emojiPicker = document.getElementById('emojiPicker');

        emojiBtn.addEventListener('click', function() {
            emojiPicker.style.display = emojiPicker.style.display === 'none' ? 'block' : 'none';
        });

        // 이모티콘 선택 이벤트
        emojiPicker.addEventListener('click', function(e) {
            if (e.target.classList.contains('emoji')) {
                const emoji = e.target.textContent;
                messageInput.value += emoji;
                messageInput.focus();
                emojiPicker.style.display = 'none';
            }
        });

        // 외부 클릭시 닫기
        document.addEventListener('click', function(e) {
            if (!emojiBtn.contains(e.target) && !emojiPicker.contains(e.target)) {
                emojiPicker.style.display = 'none';
            }
        });
    }

    // 초기화 실행
    initialize();
    requestNotificationPermission();
    setupImageUpload();
    setupEmojiPicker();
});