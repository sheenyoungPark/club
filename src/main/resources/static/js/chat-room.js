// WebSocket 및 STOMP 클라이언트 변수
let stompClient = null;
let typingTimeout = null;
let isConnected = false;
let readCheckInterval;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // WebSocket 연결
    connectWebSocket();

    // 이벤트 리스너 설정
    setupEventListeners();

    // 채팅 메시지 로드
    loadMessages();
});

// WebSocket 연결 함수
function connectWebSocket() {
    const socket = new SockJS('/ws-chat');
    stompClient = Stomp.over(socket);

    // 디버깅용 로그 활성화
    stompClient.debug = function(message) {
        console.log('STOMP: ', message);
    };

    stompClient.connect({}, function(frame) {
        console.log('Connected to WebSocket: ' + frame);
        isConnected = true;

        // 연결 상태 업데이트
        updateConnectionStatus(true);

        // 구독 로그 추가
        console.log('구독 시작: /topic/room/' + roomId);
        stompClient.subscribe('/topic/room/' + roomId, function(message) {
            console.log('메시지 수신:', message.body);
            const receivedMessage = JSON.parse(message.body);

            // 메시지 추가
            handleServerMessage(receivedMessage);

            // 읽음 표시 메시지 전송 (상대방 메시지인 경우만)
            if (receivedMessage.senderId !== userId) {
                markAsRead(receivedMessage.messageId);
            }
        });

        // 개인 알림 구독
        stompClient.subscribe('/user/queue/notifications', function(notification) {
            showNotification(JSON.parse(notification.body));
        });

        // 읽음 확인 구독 (개인)
        stompClient.subscribe('/user/queue/read-receipts', function(receipt) {
            console.log('개인 읽음 상태 수신:', receipt.body);
            updateReadReceipt(JSON.parse(receipt.body));
        });

        // 읽음 상태 구독 (방 전체)
        console.log('구독 시작: /topic/read-status/' + roomId);
        stompClient.subscribe('/topic/read-status/' + roomId, function(status) {
            console.log('읽음 상태 업데이트 수신:', status.body);
            updateReadReceipt(JSON.parse(status.body));
        });

        // 타이핑 상태 구독
        stompClient.subscribe('/user/queue/typing.' + roomId, function(status) {
            updateTypingStatus(JSON.parse(status.body));
        });

        // 페이지 로드 후 모든 표시된 메시지에 대해 읽음 표시
        markAllMessagesAsRead();

        // 주기적 읽음 상태 확인 시작
        startPeriodicReadCheck();
    }, function(error) {
        console.error('WebSocket 연결 오류: ', error);
        isConnected = false;
        updateConnectionStatus(false);

        // 연결 재시도
        setTimeout(connectWebSocket, 5000);
    });
}

// 주기적으로 읽음 상태 확인 (30초마다)
function startPeriodicReadCheck() {
    // 이전 인터벌 제거
    if (readCheckInterval) {
        clearInterval(readCheckInterval);
    }

    // 새 인터벌 설정
    readCheckInterval = setInterval(function() {
        if (isConnected) {
            // 마지막 메시지의 읽음 상태 갱신 요청
            const lastMessage = document.querySelector('#chatMessages .message:last-child');
            if (lastMessage && lastMessage.dataset.messageId) {
                console.log('읽음 상태 갱신 요청:', lastMessage.dataset.messageId);
                // 서버에 현재 읽음 상태 요청
                stompClient.send('/app/chat.getReadStatus/' + roomId, {}, JSON.stringify({
                    messageId: lastMessage.dataset.messageId
                }));
            }
        }
    }, 30000); // 30초마다
}

// 서버에서 받은 메시지 처리
function handleServerMessage(message) {
    // 이미 같은 ID의 메시지가 있는지 확인 (임시 ID로 전송된 경우 때문)
    const existingMessage = document.querySelector(`.message[data-message-id="temp-${message.messageId}"]`);

    if (existingMessage) {
        // 이미 존재하는 메시지의 ID를 실제 ID로 업데이트
        existingMessage.dataset.messageId = message.messageId;

        // 읽지 않은 사용자 수 업데이트
        const timeElement = existingMessage.querySelector('.message-time');
        if (timeElement) {
            // 읽지 않은 사용자 수 계산
            let unreadCount = participants.length - message.readCount - 1;
            unreadCount = unreadCount < 0 ? 0 : unreadCount;

            // 시간 표시 업데이트
            const timeText = timeElement.textContent.split(' ')[0];
            timeElement.innerHTML = unreadCount > 0 ?
                `<span class="text-primary read-count">${unreadCount}</span> ${timeText}` :
                timeText;
        }
    } else {
        // 새 메시지 추가 (이전 메시지와 연속성 확인)
        const lastMessage = document.querySelector('#chatMessages .message:last-child');
        const isNewSender = !lastMessage || lastMessage.dataset.senderId !== message.senderId;

        // 새 메시지 추가
        appendMessage(message, true, isNewSender, true);
    }
}

// 표시된 모든 메시지 읽음 표시
function markAllMessagesAsRead() {
    // 처리할 메시지 찾기 (자신이 보낸 메시지 제외)
    const messages = document.querySelectorAll('#chatMessages .message-received');

    if (messages.length > 0) {
        // 마지막 메시지만 읽음 처리 (최적화)
        const lastMessage = messages[messages.length - 1];
        const messageId = lastMessage.dataset.messageId;

        if (messageId) {
            markAsRead(messageId);
        }
    }
}

// 연결 상태 UI 업데이트
function updateConnectionStatus(connected) {
    const connectionStatus = document.getElementById('connectionStatus');

    if (connected) {
        connectionStatus.classList.remove('disconnected');
        connectionStatus.classList.add('connected');
        connectionStatus.classList.add('d-none');
        connectionStatus.querySelector('span').textContent = '연결됨';
    } else {
        connectionStatus.classList.remove('connected');
        connectionStatus.classList.add('disconnected');
        connectionStatus.classList.remove('d-none');
        connectionStatus.querySelector('span').textContent = '연결 끊김';
    }
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 메시지 전송 버튼
    document.getElementById('sendBtn').addEventListener('click', sendMessage);

    // 메시지 입력창 엔터 키
    document.getElementById('messageInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // 파일 첨부 버튼
    document.getElementById('fileBtn').addEventListener('click', function() {
        document.getElementById('fileInput').click();
    });

    // 파일 선택 시
    document.getElementById('fileInput').addEventListener('change', uploadFile);

    // 이모지 버튼
    document.getElementById('emojiBtn').addEventListener('click', toggleEmojiPicker);

    // 메시지 입력 감지
    document.getElementById('messageInput').addEventListener('input', function() {
        if (typingTimeout) {
            clearTimeout(typingTimeout);
        }

        sendTypingStatus(true);

        // 5초 후 입력 중 상태 해제
        typingTimeout = setTimeout(function() {
            sendTypingStatus(false);
        }, 5000);
    });

    // 메시지 검색 기능
    document.getElementById('searchBtn').addEventListener('click', searchMessages);
    document.getElementById('searchInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            searchMessages();
        }
    });
}

// 메시지 로드
function loadMessages() {
    fetch(`/chat/room/${roomId}`)
        .then(response => response.json())
        .then(data => {
            // 메시지 표시
            if (data.messages && data.messages.length > 0) {
                displayMessages(data.messages);
            }
        })
        .catch(error => {
            console.error('메시지 로드 실패: ', error);
            showAlert('메시지를 불러오는 중 오류가 발생했습니다.');
        });
}

// 메시지 목록 표시
function displayMessages(messages) {
    const chatMessages = document.getElementById('chatMessages');

    // 기존 채팅 정보 외의 내용 저장
    const chatInfo = chatMessages.querySelector('.chat-info');

    // 내용 초기화 (채팅 시작 정보는 유지)
    chatMessages.innerHTML = '';
    if (chatInfo) {
        chatMessages.appendChild(chatInfo);
    }

    // 메시지 그룹화를 위한 변수
    let lastSenderId = null;
    let lastMessageDate = null;

    // 메시지를 날짜별로 그룹화
    const messagesByDate = {};

    messages.forEach(message => {
        const messageDate = new Date(message.sendTime);
        const dateKey = `${messageDate.getFullYear()}-${messageDate.getMonth() + 1}-${messageDate.getDate()}`;

        if (!messagesByDate[dateKey]) {
            messagesByDate[dateKey] = [];
        }

        messagesByDate[dateKey].push(message);
    });

    // 날짜별로 메시지 표시
    Object.keys(messagesByDate).sort().forEach(dateKey => {
        // 날짜 헤더 추가
        const dateHeader = document.createElement('div');
        dateHeader.className = 'chat-info';

        const [year, month, day] = dateKey.split('-');
        const dateObj = new Date(year, month - 1, day);
        const today = new Date();
        const yesterday = new Date(today);
        yesterday.setDate(yesterday.getDate() - 1);

        let dateText;
        if (dateObj.toDateString() === today.toDateString()) {
            dateText = '오늘';
        } else if (dateObj.toDateString() === yesterday.toDateString()) {
            dateText = '어제';
        } else {
            dateText = `${year}년 ${month}월 ${day}일`;
        }

        dateHeader.innerHTML = `<span>${dateText}</span>`;
        chatMessages.appendChild(dateHeader);

        // 해당 날짜의 메시지 표시
        lastSenderId = null; // 날짜가 바뀌면 발신자 연속성 초기화

        messagesByDate[dateKey].forEach((message, index) => {
            const isLastMessage = index === messagesByDate[dateKey].length - 1;
            const isNewSender = lastSenderId !== message.senderId;
            const showTime = shouldShowTime(message, lastMessageDate) || isLastMessage;

            appendMessage(message, false, isNewSender, showTime);

            lastSenderId = message.senderId;
            lastMessageDate = new Date(message.sendTime);
        });
    });

    // 스크롤을 가장 아래로
    scrollToBottom();
}

// 시간 표시 여부 결정
function shouldShowTime(message, lastMessageTime) {
    if (!lastMessageTime) return true;

    const currentMessageTime = new Date(message.sendTime);

    // 분이 다르면 시간 표시
    return currentMessageTime.getMinutes() !== lastMessageTime.getMinutes() ||
        currentMessageTime.getHours() !== lastMessageTime.getHours() ||
        currentMessageTime.getDate() !== lastMessageTime.getDate() ||
        currentMessageTime.getMonth() !== lastMessageTime.getMonth() ||
        currentMessageTime.getFullYear() !== lastMessageTime.getFullYear();
}

// 메시지 추가 함수 수정
function appendMessage(message, animate = true, isNewSender = true, showTime = true) {
    const chatMessages = document.getElementById('chatMessages');
    const isCurrentUser = message.senderId === userId;

    // 메시지 요소 생성
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${isCurrentUser ? 'message-sent' : 'message-received'} d-flex ${isCurrentUser ? 'justify-content-end' : 'justify-content-start'} mb-2`;
    messageDiv.dataset.messageId = message.messageId;
    messageDiv.dataset.senderId = message.senderId;

    // 시간 포맷팅
    let messageTime;
    if (typeof message.sendTime === 'string') {
        messageTime = new Date(message.sendTime);
    } else if (message.sendTime instanceof Date) {
        messageTime = message.sendTime;
    } else {
        messageTime = new Date(); // 기본값으로 현재 시간 사용
    }

    // 클라이언트의 현지 시간대로 형식화
    const formattedTime = messageTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    // 프로필 이미지 - 기본 이미지로 고정
    const defaultProfile = '/sources/picture/기본이미지.png';

    // 메시지 HTML 구성
    let messageHtml = '';

    // 받은 메시지인 경우 프로필 이미지와 이름 표시 (새 발신자인 경우만)
    if (!isCurrentUser && isNewSender) {
        messageHtml += `
            <div class="me-2">
                <img src="${defaultProfile}" alt="Profile" class="avatar">
            </div>
            <div class="message-content-wrapper">
                <div class="small text-muted mb-1">${message.senderNickname || message.senderId}</div>
        `;
    } else if (!isCurrentUser && !isNewSender) {
        // 같은 발신자가 연속으로 보낸 메시지인 경우 여백만 추가
        messageHtml += `
            <div class="me-2 avatar-placeholder"></div>
            <div class="message-content-wrapper">
        `;
    } else {
        // 내가 보낸 메시지
        messageHtml += `<div class="message-content-wrapper">`;
    }

    // 메시지 본문
    messageHtml += `
        <div class="message-bubble">
            ${message.messageType === 'TEXT' ? message.messageContent : ''}
            ${message.messageType === 'IMAGE' ? `<img src="${message.filePath}" alt="Image" class="img-fluid rounded">` : ''}
            ${message.messageType === 'FILE' ? `<a href="${message.filePath}" target="_blank" class="btn btn-sm btn-light"><i class="fas fa-file"></i> 파일 다운로드</a>` : ''}
        </div>
    `;

    // 시간 및 읽음 상태 표시 (마지막 메시지이거나 시간이 변경된 경우만)
    if (showTime) {
        // 읽지 않은 사용자 수 계산 (전체 참여자 수 - 읽은 사용자 수 - 1(발신자))
        let unreadCount = 0;
        if (participants && participants.length > 0) {
            unreadCount = participants.length - message.readCount - 1;
            // 0보다 작으면 0으로 설정 (읽음 카운트 오류 방지)
            unreadCount = unreadCount < 0 ? 0 : unreadCount;
        }

        // 읽음 상태 표시 (내가 보낸 메시지는 시간 앞에, 상대방이 보낸 메시지는 시간 뒤에)
        if (isCurrentUser) {
            // 내가 보낸 메시지: 읽음 표시가 시간 앞에
            const readStatus = unreadCount > 0 ?
                `<span class="text-primary read-count me-1">${unreadCount}</span>` : '';

            messageHtml += `
                <div class="message-time-wrapper text-end">
                    ${readStatus}<span class="message-time">${formattedTime}</span>
                </div>
            `;
        } else {
            // 상대방이 보낸 메시지: 읽음 표시가 시간 뒤에
            const readStatus = unreadCount > 0 ?
                `<span class="text-primary read-count ms-1">${unreadCount}</span>` : '';

            messageHtml += `
                <div class="message-time-wrapper text-start">
                    <span class="message-time">${formattedTime}</span>${readStatus}
                </div>
            `;
        }
    }

    // </div> 태그 닫기
    messageHtml += `</div>`;

    // HTML 설정
    messageDiv.innerHTML = messageHtml;

    // 애니메이션 클래스 추가
    if (animate) {
        messageDiv.classList.add('highlighted-message');
    }

    // 메시지 목록에 추가
    chatMessages.appendChild(messageDiv);

    // 스크롤을 가장 아래로
    if (animate) {
        scrollToBottom();
    }
}


// 스크롤을 가장 아래로
function scrollToBottom() {
    const chatMessages = document.getElementById('chatMessages');
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// 메시지 전송
function sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const messageContent = messageInput.value.trim();

    if (messageContent && isConnected) {
        // 입력 필드 초기화 (먼저 초기화하여 중복 전송 방지)
        messageInput.value = '';

        // WebSocket으로 메시지 전송 (UI에는 표시하지 않음)
        stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
            roomId: roomId,
            senderId: userId,
            senderType: userType,
            messageContent: messageContent,
            messageType: 'TEXT'
        }));

        // 입력 중 상태 해제
        if (typingTimeout) {
            clearTimeout(typingTimeout);
        }
        sendTypingStatus(false);
    } else if (!isConnected) {
        showAlert('연결이 끊겼습니다. 페이지를 새로고침해 주세요.');
    }
}

// 파일 업로드
function uploadFile() {
    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];

    if (file && isConnected) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('roomId', roomId);
        formData.append('senderId', userId);
        formData.append('senderType', userType);

        fetch('/chat/upload', {
            method: 'POST',
            body: formData
        })
            .then(response => response.json())
            .then(message => {
                // 업로드된 파일 정보를 사용하여 메시지 전송
                stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
            })
            .catch(error => {
                console.error('파일 업로드 실패: ', error);
                showAlert('파일 업로드에 실패했습니다.');
            });

        // 파일 입력 초기화
        fileInput.value = '';
    } else if (!isConnected) {
        showAlert('연결이 끊겼습니다. 페이지를 새로고침해 주세요.');
    }
}

// 이모지 선택기 표시/숨김
function toggleEmojiPicker() {
    // 이모지 선택기 라이브러리 통합 필요
    alert('이모지 기능은 현재 개발 중입니다.');
}

// 입력 중 상태 전송
function sendTypingStatus(isTyping) {
    if (isConnected) {
        stompClient.send('/app/chat.typing.' + roomId, {}, JSON.stringify({
            userId: userId,
            isTyping: isTyping
        }));
    }
}

// 입력 중 상태 업데이트
function updateTypingStatus(status) {
    const userTyping = document.getElementById('userTyping');

    if (status.isTyping) {
        userTyping.classList.remove('d-none');
        userTyping.querySelector('span').textContent = `${status.userNickname || status.userId}님이 입력 중입니다...`;
    } else {
        userTyping.classList.add('d-none');
    }
}

// 메시지 읽음 표시
function markAsRead(messageId) {
    if (isConnected) {
        console.log('읽음 표시 메시지 전송:', messageId);
        stompClient.send('/app/chat.markAsRead/' + roomId, {}, JSON.stringify({
            userId: userId,
            messageId: messageId
        }));
    }
}

// 읽음 확인 업데이트 함수 수정
function updateReadReceipt(message) {
    console.log('읽음 상태 업데이트:', message);

    // 해당 메시지 ID를 가진 메시지를 찾아서 읽음 상태 업데이트
    const messageElements = document.querySelectorAll(`.message[data-message-id="${message.messageId}"]`);

    if (messageElements.length > 0) {
        // 메시지를 찾으면 상태 업데이트
        messageElements.forEach(messageElement => {
            const isCurrentUserMessage = messageElement.classList.contains('message-sent');
            const timeWrapper = messageElement.querySelector('.message-time-wrapper');

            if (timeWrapper) {
                // 읽지 않은 사용자 수 계산 (전체 참여자 수 - 읽은 사용자 수 - 1(발신자))
                let unreadCount = 0;
                if (participants && participants.length > 0) {
                    unreadCount = participants.length - message.readCount - 1;
                    // 0보다 작으면 0으로 설정 (읽음 카운트 오류 방지)
                    unreadCount = unreadCount < 0 ? 0 : unreadCount;
                }

                // 시간 요소 찾기
                const timeElement = timeWrapper.querySelector('.message-time');
                const timeText = timeElement ? timeElement.textContent : '';

                // 내가 보낸 메시지와 받은 메시지에 대해 다른 구조 적용
                if (isCurrentUserMessage) {
                    // 내가 보낸 메시지
                    if (unreadCount > 0) {
                        timeWrapper.innerHTML = `<span class="text-primary read-count me-1">${unreadCount}</span><span class="message-time">${timeText}</span>`;
                    } else {
                        timeWrapper.innerHTML = `<span class="message-time">${timeText}</span>`;
                    }
                } else {
                    // 상대방이 보낸 메시지
                    if (unreadCount > 0) {
                        timeWrapper.innerHTML = `<span class="message-time">${timeText}</span><span class="text-primary read-count ms-1">${unreadCount}</span>`;
                    } else {
                        timeWrapper.innerHTML = `<span class="message-time">${timeText}</span>`;
                    }
                }
            }
        });
    }
}

// 알림 표시
function showNotification(message) {
    // 브라우저 알림 권한 요청 및 표시
    if (Notification.permission === 'granted') {
        const notification = new Notification(message.senderNickname || message.senderId, {
            body: message.messageContent,
            icon: message.senderProfile || '/sources/picture/기본이미지.png'
        });

        notification.onclick = function() {
            window.focus();
        };
    } else if (Notification.permission !== 'denied') {
        Notification.requestPermission();
    }
}

// 메시지 검색
function searchMessages() {
    const keyword = document.getElementById('searchInput').value.trim();
    const searchResults = document.getElementById('searchResults');

    if (keyword) {
        // 로딩 상태 표시
        searchResults.innerHTML = '<div class="text-center p-3"><div class="spinner-border spinner-border-sm text-primary" role="status"></div> 검색 중...</div>';

        fetch(`/chat/room/${roomId}/search?keyword=${encodeURIComponent(keyword)}`)
            .then(response => response.json())
            .then(messages => {
                displaySearchResults(messages);
            })
            .catch(error => {
                console.error('검색 실패: ', error);
                searchResults.innerHTML = '<div class="alert alert-danger">검색 중 오류가 발생했습니다.</div>';
            });
    } else {
        searchResults.innerHTML = '<div class="text-center text-muted p-3">검색어를 입력하세요.</div>';
    }
}

// 검색 결과 표시
function displaySearchResults(messages) {
    const searchResults = document.getElementById('searchResults');
    searchResults.innerHTML = '';

    if (messages.length === 0) {
        searchResults.innerHTML = '<div class="text-center text-muted p-3">검색 결과가 없습니다.</div>';
        return;
    }

    messages.forEach(message => {
        const resultItem = document.createElement('div');
        resultItem.className = 'list-group-item search-result-item';
        resultItem.dataset.messageId = message.messageId;
        resultItem.style.cursor = 'pointer';

        // 시간 포맷팅
        const messageTime = new Date(message.sendTime);
        const formattedDate = `${messageTime.getFullYear()}-${(messageTime.getMonth() + 1).toString().padStart(2, '0')}-${messageTime.getDate().toString().padStart(2, '0')} ${messageTime.getHours().toString().padStart(2, '0')}:${messageTime.getMinutes().toString().padStart(2, '0')}`;

        resultItem.innerHTML = `
            <div class="d-flex justify-content-between align-items-start">
                <div>
                    <div class="mb-1">
                        <strong>${message.senderNickname || message.senderId}:</strong> ${message.messageContent}
                    </div>
                    <small class="text-muted">${formattedDate}</small>
                </div>
            </div>
        `;

        // 검색 결과 클릭 시 해당 메시지로 스크롤
        resultItem.addEventListener('click', function() {
            const modal = bootstrap.Modal.getInstance(document.getElementById('searchModal'));
            modal.hide();

            highlightMessage(message.messageId);
        });

        searchResults.appendChild(resultItem);
    });
}

// 메시지 강조 표시
function highlightMessage(messageId) {
    const messageElement = document.querySelector(`.message[data-message-id="${messageId}"]`);

    if (messageElement) {
        // 먼저 스크롤
        messageElement.scrollIntoView({ behavior: 'smooth', block: 'center' });

        // 애니메이션 클래스 추가/제거로 강조 효과
        messageElement.classList.add('highlighted-message');
        setTimeout(() => {
            messageElement.classList.remove('highlighted-message');
        }, 2000);
    }
}

// 알림 모달 표시
function showAlert(message) {
    const alertModal = new bootstrap.Modal(document.getElementById('alertModal'));
    document.getElementById('alertModalBody').textContent = message;
    alertModal.show();
}