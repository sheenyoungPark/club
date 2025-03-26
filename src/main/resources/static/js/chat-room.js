// WebSocket 및 STOMP 클라이언트 변수
let stompClient = null;
let typingTimeout = null;
let isConnected = false;
let readCheckInterval;
let room = null;

// 발신자별 마지막 메시지 시간을 저장할 객체
let lastMessageDateBySender = {};

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

// 스크롤 시 읽음 처리를 위한 함수 추가
function setupScrollReadDetection() {
    const chatMessages = document.getElementById('chatMessages');

    // 스크롤 이벤트 리스너 추가
    chatMessages.addEventListener('scroll', debounce(function() {
        checkVisibleMessages();
    }, 300)); // 300ms 디바운스

    // 초기 화면에 표시된 메시지 확인
    setTimeout(checkVisibleMessages, 2000);
}

// 디바운스 함수 (스크롤 이벤트 최적화)
function debounce(func, wait) {
    let timeout;
    return function() {
        const context = this, args = arguments;
        clearTimeout(timeout);
        timeout = setTimeout(function() {
            func.apply(context, args);
        }, wait);
    };
}

// 화면에 보이는 메시지 확인 및 읽음 처리
function checkVisibleMessages() {
    if (!isConnected) return;

    const chatContainer = document.getElementById('chatMessages');
    const messages = chatContainer.querySelectorAll('.message-received');

    // 화면 내에 보이는 메시지만 확인
    let visibleMessages = [];
    let lastVisibleMessageId = null;

    messages.forEach(message => {
        const rect = message.getBoundingClientRect();
        // 메시지가 화면에 보이는지 확인
        if (rect.top >= 0 && rect.bottom <= window.innerHeight) {
            visibleMessages.push(message);
            lastVisibleMessageId = message.dataset.messageId;
        }
    });

    // 보이는 메시지 중 가장 최신 메시지 ID가 있으면 읽음 처리
    if (lastVisibleMessageId) {
        console.log('화면에 보이는 마지막 메시지 읽음 처리:', lastVisibleMessageId);
        markAsRead(lastVisibleMessageId);
    }
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

// 웹소켓으로 새 메시지 수신 처리 (완전히 수정된 버전)
function handleServerMessage(message) {
    console.log("서버에서 받은 메시지:", message);

    if (!message || !message.messageId) {
        console.error("잘못된 메시지 형식:", message);
        return;
    }

    // 이미 같은 ID의 메시지가 있는지 확인 (임시 ID로 전송된 경우 때문)
    const tempSelector = `.message[data-message-id="temp-${message.messageId}"]`;
    const normalSelector = `.message[data-message-id="${message.messageId}"]`;

    let existingMessage = document.querySelector(tempSelector);
    if (!existingMessage) {
        existingMessage = document.querySelector(normalSelector);
    }

    const messageId = message.messageId.toString();

    if (existingMessage) {
        // 이미 존재하는 메시지의 ID를 실제 ID로 업데이트
        existingMessage.dataset.messageId = messageId;

        // 읽지 않은 사용자 수 업데이트
        updateReadCount(existingMessage, message);
    } else {
        // 새 메시지의 시간 정보 가져오기
        const messageTime = new Date(message.sendTime);
        const messageHour = messageTime.getHours();
        const messageMinute = messageTime.getMinutes();

        console.log(`새 메시지 시간: ${messageHour}:${messageMinute}`);

        // 같은 발신자의 마지막 메시지 찾기
        const senderMessages = document.querySelectorAll(`.message[data-sender-id="${message.senderId}"]`);
        let isNewGroup = true;
        let prevLastMessageInGroup = null;

        if (senderMessages.length > 0) {
            // 같은 발신자의 마지막 메시지
            const lastMessage = senderMessages[senderMessages.length - 1];

            // 마지막 메시지의 시간 요소 찾기
            const lastTimeElement = lastMessage.querySelector('.message-time');

            if (lastTimeElement) {
                // 시간 텍스트에서 시와 분 추출 (예: "14:30" -> 14시 30분)
                const timeText = lastTimeElement.textContent;
                const timeParts = timeText.split(':');

                if (timeParts.length === 2) {
                    const lastHour = parseInt(timeParts[0]);
                    const lastMinute = parseInt(timeParts[1]);

                    console.log(`마지막 메시지 시간: ${lastHour}:${lastMinute}`);

                    // 시와 분이 같은지 확인
                    if (lastHour === messageHour && lastMinute === messageMinute) {
                        isNewGroup = false;
                        prevLastMessageInGroup = lastMessage;
                    }
                }
            }
        }

        if (isNewGroup) {
            // 새 그룹으로 처리 - 항상 시간 표시
            console.log("새 그룹으로 처리 - 시간 표시");
            appendMessage(message, true, true, true, true);
        } else {
            // 같은 그룹으로 처리 - 이전 메시지 시간 숨기고 현재 메시지에 시간 표시
            console.log("같은 그룹으로 처리 - 이전 메시지 시간 숨기고 현재 메시지에 시간 표시");

            // 이전 그룹의 마지막 메시지 시간 숨기기
            if (prevLastMessageInGroup) {
                const timeWrapper = prevLastMessageInGroup.querySelector('.message-time-wrapper');
                if (timeWrapper) {
                    timeWrapper.style.display = 'none';
                }
            }

            // 새 메시지 추가 (그룹의 마지막 메시지로, 시간 표시)
            appendMessage(message, true, false, true, true);
        }

        // 발신자별 마지막 메시지 시간 업데이트
        lastMessageDateBySender[message.senderId] = messageTime;
    }
}

// 읽지 않은 사용자 수 업데이트 함수 (코드 중복 방지)
function updateReadCount(messageElement, message) {
    const timeWrapper = messageElement.querySelector('.message-time-wrapper');
    if (!timeWrapper) return;

    // 읽지 않은 사용자 수 계산
    let unreadCount = 0;
    if (participants && participants.length > 0) {
        unreadCount = participants.length - (message.readCount+1 || 1);
        unreadCount = unreadCount < 0 ? 0 : unreadCount;
    }

    // 현재 메시지가 내가 보낸 메시지인지 확인
    const isSentByMe = messageElement.classList.contains('message-sent');

    // 시간 요소 찾기
    const timeElement = timeWrapper.querySelector('.message-time');
    if (!timeElement) return;

    const timeText = timeElement.textContent;

    // 완전히 새로운 HTML로 내용 교체
    if (isSentByMe) {
        // 내가 보낸 메시지
        timeWrapper.innerHTML = '';

        if (unreadCount > 0) {
            const countSpan = document.createElement('span');
            countSpan.className = 'text-primary read-count me-1';
            countSpan.textContent = unreadCount;
            timeWrapper.appendChild(countSpan);
        }

        const newTimeSpan = document.createElement('span');
        newTimeSpan.className = 'message-time';
        newTimeSpan.textContent = timeText;
        timeWrapper.appendChild(newTimeSpan);
    } else {
        // 상대방이 보낸 메시지
        timeWrapper.innerHTML = '';

        const newTimeSpan = document.createElement('span');
        newTimeSpan.className = 'message-time';
        newTimeSpan.textContent = timeText;
        timeWrapper.appendChild(newTimeSpan);

        if (unreadCount > 0) {
            const countSpan = document.createElement('span');
            countSpan.className = 'text-primary read-count ms-1';
            countSpan.textContent = unreadCount;
            timeWrapper.appendChild(countSpan);
        }
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

    // 모든 메시지 읽음 상태 업데이트를 서버에 전송
    if (isConnected) {
        stompClient.send('/app/chat.markAllAsRead/' + roomId, {}, JSON.stringify({
            userId: userId
        }));
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
            // 참가자 정보 확인 로그 추가
            console.log("채팅방 정보 로드:", data);

            if (data.room) {
                room = data.room;
                console.log("룸 정보 업데이트:", room);
            }
            // 참가자 정보가 있으면 전역 변수 업데이트
            if (data.participants && Array.isArray(data.participants)) {
                // 기존 참가자 배열 비우고 새로운 참가자로 채우기
                participants.length = 0;
                data.participants.forEach(p => participants.push(p));

                console.log("업데이트된 참가자 목록:", participants);
                console.log("현재 참가자 수:", participants.length);
            }

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

    // 날짜별로 메시지 그룹화
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
        addDateHeader(chatMessages, dateKey);

        // 해당 날짜의 메시지를 발신자별, 시간별로 그룹화
        const messagesOfDay = messagesByDate[dateKey];
        const messageGroups = groupMessagesBySender(messagesOfDay);

        // 각 발신자 그룹마다 메시지 표시
        messageGroups.forEach(group => {
            // 그룹의 첫 메시지는 발신자 정보 표시
            let isFirstInGroup = true;

            // 그룹 내 메시지 표시
            group.messages.forEach((message, index) => {
                // 그룹의 마지막 메시지만 시간 표시
                const isLastInGroup = index === group.messages.length - 1;
                appendMessage(message, false, isFirstInGroup, isLastInGroup, true);
                isFirstInGroup = false;
            });
        });
    });

    // 스크롤을 가장 아래로
    scrollToBottom();

    // 스크롤 읽음 감지 설정
    setupScrollReadDetection();
}

// 발신자별, 분 단위로 메시지 그룹화
function groupMessagesBySender(messages) {
    const groups = [];
    let currentGroup = null;

    messages.forEach(message => {
        const messageTime = new Date(message.sendTime);
        // 시와 분을 추출 (초는 무시)
        const messageHour = messageTime.getHours();
        const messageMinute = messageTime.getMinutes();

        // 새 그룹 시작 여부 (다른 발신자이거나, 같은 발신자라도 분이 다르면)
        if (!currentGroup ||
            currentGroup.senderId !== message.senderId ||
            currentGroup.hour !== messageHour ||
            currentGroup.minute !== messageMinute) {

            // 이전 그룹이 있으면 저장
            if (currentGroup) {
                groups.push(currentGroup);
            }

            // 새 그룹 시작
            currentGroup = {
                senderId: message.senderId,
                messages: [message],
                hour: messageHour,
                minute: messageMinute
            };
        } else {
            // 현재 그룹에 메시지 추가
            currentGroup.messages.push(message);
        }
    });

    // 마지막 그룹 추가
    if (currentGroup) {
        groups.push(currentGroup);
    }

    return groups;
}

// 발신자별 시간 표시 여부 결정 (수정된 함수)
function shouldShowTime(message, lastMessageTime) {
    // 해당 발신자의 첫 메시지인 경우 시간 표시
    if (!lastMessageTime) return true;

    const currentMessageTime = new Date(message.sendTime);

    // 분이 다르면 시간 표시
    return currentMessageTime.getMinutes() !== lastMessageTime.getMinutes() ||
        currentMessageTime.getHours() !== lastMessageTime.getHours() ||
        currentMessageTime.getDate() !== lastMessageTime.getDate() ||
        currentMessageTime.getMonth() !== lastMessageTime.getMonth() ||
        currentMessageTime.getFullYear() !== lastMessageTime.getFullYear();
}

function appendMessage(message, animate = true, isFirstInGroup = true, isLastInGroup = true, showTime = true) {
    const chatMessages = document.getElementById('chatMessages');
    const isCurrentUser = message.senderId === userId;
    const isClubChat = room && room.room_type === 'CLUB'; // 클럽 채팅인지 확인

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
    if (!isCurrentUser && isFirstInGroup) {
        messageHtml += `
            <div class="me-2">
                <img src="${defaultProfile}" alt="Profile" class="avatar">
            </div>
            <div class="message-content-wrapper">
                <div class="small text-muted mb-1">${message.senderNickname || message.senderId}</div>
        `;
    } else if (!isCurrentUser && !isFirstInGroup) {
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

    // 시간 및 읽음 상태 표시 - 항상 실제 시간 사용
    // 그룹의 마지막 메시지인 경우에만 시간 표시
    if (isLastInGroup && showTime) {
        // 클럽 채팅일 경우 시간만 표시
        if (isClubChat) {
            messageHtml += `
                <div class="message-time-wrapper text-${isCurrentUser ? 'end' : 'start'}">
                    <span class="message-time">${formattedTime}</span>
                </div>
            `;
        } else {
            // 1:1 채팅일 경우 읽음 상태도 함께 표시
            // 읽지 않은 사용자 수 계산
            let unreadCount = 0;
            if (participants && participants.length > 0) {
                unreadCount = participants.length - (message.readCount+1 || 1);
                // 0보다 작으면 0으로 설정 (읽음 카운트 오류 방지)
                unreadCount = unreadCount < 0 ? 0 : unreadCount;
            }

            // 읽음 상태 표시 (내가 보낸 메시지는 시간 앞에, 상대방이 보낸 메시지는 시간 뒤에)
            if (isCurrentUser) {
                // 내가 보낸 메시지: 읽음 표시가 시간 앞에
                messageHtml += `
                    <div class="message-time-wrapper text-end">
                        ${unreadCount > 0 ? `<span class="text-primary read-count me-1">${unreadCount}</span>` : ''}
                        <span class="message-time">${formattedTime}</span>
                    </div>
                `;
            } else {
                // 상대방이 보낸 메시지: 읽음 표시가 시간 뒤에
                messageHtml += `
                    <div class="message-time-wrapper text-start">
                        <span class="message-time">${formattedTime}</span>
                        ${unreadCount > 0 ? `<span class="text-primary read-count ms-1">${unreadCount}</span>` : ''}
                    </div>
                `;
            }
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

// 날짜 헤더 추가
function addDateHeader(container, dateKey) {
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
    container.appendChild(dateHeader);
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

// 읽음 표시 처리 개선 (성능 최적화)
function markAsRead(messageId) {
    if (!isConnected || !messageId) return;

    // 세션 스토리지에서 읽은 메시지 목록 가져오기
    const readMessageIds = JSON.parse(sessionStorage.getItem('readMessageIds') || '[]');

    // 이미 읽은 메시지인지 확인
    if (readMessageIds.includes(messageId)) {
        return; // 이미 읽은 메시지면 처리하지 않음
    }

    console.log('읽음 표시 전송:', messageId);

    // 서버에 읽음 표시 전송
    stompClient.send('/app/chat.markAsRead/' + roomId, {}, JSON.stringify({
        userId: userId,
        messageId: messageId
    }));

    // 읽은 메시지 목록에 추가
    readMessageIds.push(messageId);
    sessionStorage.setItem('readMessageIds', JSON.stringify(readMessageIds));
}

// 페이지 가시성 변경 감지 (브라우저 탭 전환 등)
document.addEventListener('visibilitychange', function() {
    if (document.visibilityState === 'visible') {
        // 페이지가 다시 보이면 현재 화면에 표시된 메시지 읽음 처리
        setTimeout(checkVisibleMessages, 1000);
    }
});

// updateReadReceipt 함수 수정
function updateReadReceipt(message) {
    console.log('읽음 상태 업데이트:', message);

    if (!message || !message.messageId) {
        console.error("잘못된 메시지 형식:", message);
        return;
    }

    // 클럽 채팅인지 확인
    const isClubChat = room.room_type === 'CLUB';

    // 클럽 채팅일 경우 읽음 표시를 업데이트하지 않음
    if (isClubChat) {
        return;
    }

    // 읽지 않은 사용자 수 계산
    let unreadCount = 0;

    if (participants && participants.length > 0) {
        unreadCount = participants.length - (message.readCount+1 || 1);

        // 안 읽은 수가 0보다 작으면 0으로 설정
        unreadCount = unreadCount < 0 ? 0 : unreadCount;

        // 디버깅 로그 추가
        console.log(`메시지 ID: ${message.messageId}, 참가자 수: ${participants.length}, 읽은 사용자 수: ${message.readCount+1 || 1}, 안 읽은 사용자 수: ${unreadCount}`);
    }

    // 읽음 표시가 0이 되면 해당 메시지와 이전 메시지 모두 읽음 처리
    if (unreadCount === 0) {
        updateAllPreviousMessages(message.messageId, 0);
    } else {
        // 해당 메시지 ID만 업데이트
        updateSingleMessage(message.messageId, unreadCount);
    }
}

// 단일 메시지 읽음 상태 업데이트
function updateSingleMessage(messageId, unreadCount) {
    const messageElements = document.querySelectorAll(`.message[data-message-id="${messageId}"]`);

    if (messageElements.length > 0) {
        messageElements.forEach(messageElement => {
            updateMessageTimeDisplay(messageElement, unreadCount);
        });
    } else {
        console.warn(`메시지 ID ${messageId}에 해당하는 요소를 찾을 수 없습니다.`);
    }
}

// 메시지 시간과 읽음 상태 표시 업데이트 (중복 코드 제거를 위한 헬퍼 함수)
function updateMessageTimeDisplay(messageElement, unreadCount) {
    const timeWrapper = messageElement.querySelector('.message-time-wrapper');
    if (!timeWrapper) return;

    const isCurrentUserMessage = messageElement.classList.contains('message-sent');
    const timeElement = timeWrapper.querySelector('.message-time');

    if (!timeElement) {
        console.warn('시간 요소를 찾을 수 없습니다:', timeWrapper);
        return;
    }

    const timeText = timeElement.textContent;

    // 완전히 새로운 HTML로 내용 교체
    timeWrapper.innerHTML = '';

    if (isCurrentUserMessage) {
        // 내가 보낸 메시지
        if (unreadCount > 0) {
            const countSpan = document.createElement('span');
            countSpan.className = 'text-primary read-count me-1';
            countSpan.textContent = unreadCount;
            timeWrapper.appendChild(countSpan);
        }

        const newTimeSpan = document.createElement('span');
        newTimeSpan.className = 'message-time';
        newTimeSpan.textContent = timeText;
        timeWrapper.appendChild(newTimeSpan);
    } else {
        // 상대방이 보낸 메시지
        const newTimeSpan = document.createElement('span');
        newTimeSpan.className = 'message-time';
        newTimeSpan.textContent = timeText;
        timeWrapper.appendChild(newTimeSpan);

        if (unreadCount > 0) {
            const countSpan = document.createElement('span');
            countSpan.className = 'text-primary read-count ms-1';
            countSpan.textContent = unreadCount;
            timeWrapper.appendChild(countSpan);
        }
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

// 알림 표시 (모달 대신 토스트 사용)
function showAlert(message) {
    // 기존 토스트가 있으면 제거
    const existingToast = document.getElementById('alertToast');
    if (existingToast) {
        existingToast.remove();
    }

    // 새 토스트 생성
    const toastHtml = `
        <div id="alertToast" class="toast align-items-center text-white bg-primary border-0 position-fixed bottom-0 end-0 m-3" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', toastHtml);
    const toastElement = document.getElementById('alertToast');
    const toast = new bootstrap.Toast(toastElement, { autohide: true, delay: 3000 });
    toast.show();
}

// 특정 메시지와 그 이전 메시지의 읽음 상태를 모두 업데이트
function updateAllPreviousMessages(messageId, unreadCount) {
    // 현재 메시지 찾기
    const currentMessage = document.querySelector(`.message[data-message-id="${messageId}"]`);
    if (!currentMessage) return;

    // 현재 메시지와 그 이전의 모든 메시지들 찾기
    const allMessages = Array.from(document.querySelectorAll('#chatMessages .message'));
    const currentIndex = allMessages.indexOf(currentMessage);

    // 현재 메시지를 포함하여 그 이전의 모든 메시지 처리
    for (let i = 0; i <= currentIndex; i++) {
        const message = allMessages[i];
        const timeWrapper = message.querySelector('.message-time-wrapper');

        if (timeWrapper) {
            const isCurrentUserMessage = message.classList.contains('message-sent');
            const timeElement = timeWrapper.querySelector('.message-time');

            if (timeElement) {
                const timeText = timeElement.textContent;

                // 메시지 시간과 읽음 상태 업데이트
                timeWrapper.innerHTML = '';

                if (isCurrentUserMessage) {
                    // 내가 보낸 메시지
                    if (unreadCount > 0) {
                        const countSpan = document.createElement('span');
                        countSpan.className = 'text-primary read-count me-1';
                        countSpan.textContent = unreadCount;
                        timeWrapper.appendChild(countSpan);
                    }

                    const newTimeSpan = document.createElement('span');
                    newTimeSpan.className = 'message-time';
                    newTimeSpan.textContent = timeText;
                    timeWrapper.appendChild(newTimeSpan);
                } else {
                    // 상대방이 보낸 메시지
                    const newTimeSpan = document.createElement('span');
                    newTimeSpan.className = 'message-time';
                    newTimeSpan.textContent = timeText;
                    timeWrapper.appendChild(newTimeSpan);

                    if (unreadCount > 0) {
                        const countSpan = document.createElement('span');
                        countSpan.className = 'text-primary read-count ms-1';
                        countSpan.textContent = unreadCount;
                        timeWrapper.appendChild(countSpan);
                    }
                }
            }
        }
    }
}