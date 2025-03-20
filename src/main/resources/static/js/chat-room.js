// WebSocket 연결 설정
let stompClient = null;
let isConnected = false;
let reconnectAttempts = 0;
let maxReconnectAttempts = 10;
let reconnectDelay = 5000;
let connectionStatusElement = null;
let alertModal = null;

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // UI 요소 초기화
    connectionStatusElement = document.getElementById('connectionStatus');
    alertModal = new bootstrap.Modal(document.getElementById('alertModal'));

    // WebSocket 연결
    connectWebSocket();

    // 이벤트 리스너 설정
    document.getElementById('sendBtn').addEventListener('click', sendMessage);
    document.getElementById('messageInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    document.getElementById('fileBtn').addEventListener('click', function() {
        document.getElementById('fileInput').click();
    });

    document.getElementById('fileInput').addEventListener('change', uploadFile);

    document.getElementById('searchBtn').addEventListener('click', searchMessages);

    // 이미지 에러 처리
    document.querySelectorAll('img').forEach(img => {
        img.addEventListener('error', function() {
            if (this.alt === 'Club') {
                this.src = '/sources/picture/기본이미지.png'; // 클럽 기본 이미지
            } else {
                this.src = '/sources/picture/기본이미지.png'; // 사용자 기본 이미지
            }
            this.onerror = null; // 무한 루프 방지
        });
    });

    // 모달 접근성 문제 해결
    const modals = ['alertModal', 'participantsModal', 'searchModal'];

    modals.forEach(modalId => {
        const modalElement = document.getElementById(modalId);
        if (modalElement) {
            // 모달이 열릴 때 aria-hidden 속성 처리
            modalElement.addEventListener('shown.bs.modal', function() {
                // aria-hidden이 true로 설정되어 있으면 제거
                if (modalElement.getAttribute('aria-hidden') === 'true') {
                    modalElement.removeAttribute('aria-hidden');
                }
            });
        }
    });
});

// WebSocket 연결
function connectWebSocket() {
    try {
        // 이전 연결 해제
        if (stompClient !== null && stompClient.connected) {
            stompClient.disconnect();
        }

        // 연결 상태 UI 업데이트
        updateConnectionStatus(false);

        // 새 연결 생성 - 경로 확인
        const socket = new SockJS('/ws-chat');
        stompClient = Stomp.over(socket);

        // 디버깅 활성화 (문제 진단을 위해)
        stompClient.debug = function(str) {
            console.log('STOMP: ' + str);
        };

        // 최소한의 헤더로 시도
        const headers = {};

        // 연결 시도
        stompClient.connect(
            headers,
            frame => {
                console.log('STOMP 연결 성공: ', frame);
                connectCallback(frame);
            },
            error => {
                console.error('STOMP 연결 오류: ', error);
                errorCallback(error);
            }
        );
    } catch (e) {
        console.error('WebSocket 연결 시도 중 예외 발생:', e);
        scheduleReconnect();
    }
}

// 연결 성공 콜백
function connectCallback(frame) {
    try {
        isConnected = true;
        reconnectAttempts = 0; // 재연결 시도 횟수 초기화
        console.log('WebSocket 연결됨: ' + frame);
        updateConnectionStatus(true);

        // 채팅방 메시지 구독
        stompClient.subscribe('/topic/room/' + roomId, function(message) {
            try {
                const messageData = JSON.parse(message.body);
                displayMessage(messageData);

                // 자신이 보낸 메시지가 아니면 읽음 표시 처리
                if (messageData.senderId !== userId) {
                    markAsRead(messageData.messageId);
                }
            } catch (e) {
                console.error('메시지 처리 중 오류:', e);
            }
        });

        // 개인 알림 구독
        stompClient.subscribe('/user/' + userId + '/queue/notifications', function(notification) {
            try {
                console.log('새 메시지 알림: ', notification.body);
                const notificationData = JSON.parse(notification.body);
                if (notificationData.type === 'NEW_MESSAGE') {
                    // 새 메시지 알림 처리
                }
            } catch (e) {
                console.error('알림 처리 중 오류:', e);
            }
        });

        // 읽음 확인 구독
        stompClient.subscribe('/user/' + userId + '/queue/read-receipts', function(receipt) {
            try {
                const messageData = JSON.parse(receipt.body);
                updateReadStatus(messageData.messageId);
            } catch (e) {
                console.error('읽음 확인 처리 중 오류:', e);
            }
        });

        // 연결 성공 후 메시지 로드
        loadMessages();
    } catch (e) {
        console.error('연결 콜백 처리 중 오류:', e);
        scheduleReconnect();
    }
}

// 연결 오류 콜백
function errorCallback(error) {
    console.error('WebSocket 연결 실패: ', error);
    isConnected = false;
    updateConnectionStatus(false);
    scheduleReconnect();
}

// 연결 종료 콜백
function closeCallback() {
    console.log('WebSocket 연결 종료');
    isConnected = false;
    updateConnectionStatus(false);
    scheduleReconnect();
}

// 재연결 스케줄링
function scheduleReconnect() {
    if (reconnectAttempts < maxReconnectAttempts) {
        reconnectAttempts++;
        // 지수 백오프로 재연결 지연 시간 계산 (최대 30초)
        const delay = Math.min(reconnectDelay * Math.pow(1.5, reconnectAttempts - 1), 30000);
        console.log(`${delay}ms 후 재연결 시도 (${reconnectAttempts}/${maxReconnectAttempts})...`);
        setTimeout(connectWebSocket, delay);
    } else {
        console.error('최대 재연결 시도 횟수 초과. 페이지를 새로고침하세요.');
        showAlert('연결 오류', '서버 연결에 실패했습니다. 페이지를 새로고침하세요.');

        // 연결 실패 시 대체 경로 시도
        attemptAlternativeConnections();
    }
}

// WebSocket 디버깅 도우미 함수
function checkBackendEndpoints() {
    console.log('서버 엔드포인트 확인 중...');

    // 서버와의 기본 연결 확인
    fetch('/ping', {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (response.ok) {
                console.log('서버 상태: 정상 (ping 성공)');
            } else {
                console.warn('서버 상태: 응답 오류 (ping 실패) -', response.status);
            }
        })
        .catch(error => {
            console.error('서버 연결 실패:', error);
        });
}

// 연결 테스트 함수 - 다양한 경로로 WebSocket 연결 시도
function attemptAlternativeConnections() {
    // 기존 WebSocket 연결 시도가 모두 실패한 경우에만 실행
    if (reconnectAttempts >= maxReconnectAttempts) {
        console.log('대체 WebSocket 경로 시도 중...');

        // 일반적인 Spring Boot WebSocket 경로들 시도
        const alternativePaths = [
            '/ws',              // 기본 경로
            '/websocket',       // 다른 일반 경로
            '/socket',          // 또 다른 일반 경로
            '/chat',            // 채팅 관련 경로
            '/stomp',           // STOMP 프로토콜 경로
            '/app/ws-chat'      // ApplicationDestinationPrefix가 적용된 경로
        ];

        console.log('다음 경로가 가능한 WebSocket 엔드포인트일 수 있습니다:', alternativePaths);
    }
}

// 연결 상태 UI 업데이트
function updateConnectionStatus(connected) {
    if (!connectionStatusElement) return;

    if (connected) {
        connectionStatusElement.classList.remove('disconnected');
        connectionStatusElement.classList.add('connected');
        connectionStatusElement.classList.add('d-none');
        connectionStatusElement.querySelector('span').textContent = '연결됨';
    } else {
        connectionStatusElement.classList.remove('connected');
        connectionStatusElement.classList.remove('d-none');
        connectionStatusElement.classList.add('disconnected');
        connectionStatusElement.querySelector('span').textContent = '연결 끊김';
    }
}

// 알림 모달 표시
function showAlert(title, message) {
    if (!alertModal) return;

    document.getElementById('alertModalTitle').textContent = title;
    document.getElementById('alertModalBody').textContent = message;
    alertModal.show();
}

// 기존 메시지 로드
function loadMessages() {
    // 로딩 표시
    const chatMessages = document.getElementById('chatMessages');
    const loadingIndicator = document.createElement('div');
    loadingIndicator.className = 'chat-info';
    loadingIndicator.innerHTML = '<div class="spinner-border spinner-border-sm text-primary me-2" role="status"></div>메시지 로드 중...';
    chatMessages.appendChild(loadingIndicator);

    fetch('/chat/room/' + roomId)
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 응답 오류: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            if (!data || !data.messages) {
                throw new Error('메시지 데이터가 없거나 형식이 잘못되었습니다.');
            }

            // 기존 메시지 제거 (시작 정보는 유지)
            const infoMessage = chatMessages.querySelector('.chat-info:first-child');
            chatMessages.innerHTML = '';
            if (infoMessage) {
                chatMessages.appendChild(infoMessage.cloneNode(true));
            }

            // 메시지 표시
            if (data.messages.length === 0) {
                const emptyMsg = document.createElement('div');
                emptyMsg.className = 'chat-info';
                emptyMsg.innerText = '아직 메시지가 없습니다. 첫 메시지를 보내보세요!';
                chatMessages.appendChild(emptyMsg);
            } else {
                data.messages.forEach(message => {
                    displayMessage(message);
                });
            }

            // 스크롤을 가장 아래로 이동
            scrollToBottom();
        })
        .catch(error => {
            console.error('메시지 로드 실패: ', error);

            // 로딩 표시 제거
            if (chatMessages.contains(loadingIndicator)) {
                chatMessages.removeChild(loadingIndicator);
            }

            // 사용자에게 오류 메시지 표시
            const errorMsg = document.createElement('div');
            errorMsg.className = 'chat-info text-danger';
            errorMsg.innerText = '메시지를 불러오는 데 실패했습니다. 페이지를 새로고침해 주세요.';
            chatMessages.appendChild(errorMsg);
        });
}

// 메시지 전송
function sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const content = messageInput.value.trim();

    const now = new Date();
    const koreaTime = new Date(now.getTime() + (9 * 60 * 60 * 1000));

    if (content && isConnected) {
        try {
            const message = {
                roomId: roomId,
                senderId: userId,
                senderType: userType,
                messageContent: content,
                messageType: 'TEXT',
                sendTime: koreaTime
            };

            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
            messageInput.value = '';
            messageInput.focus();
        } catch (error) {
            console.error('메시지 전송 실패: ', error);
            showAlert('전송 실패', '메시지를 전송하는 데 실패했습니다. 다시 시도해 주세요.');
        }
    } else if (!isConnected) {
        showAlert('연결 끊김', '서버와의 연결이 끊어졌습니다. 잠시 후 다시 시도해 주세요.');
    }
}

// 파일 업로드
function uploadFile() {
    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];

    if (file && isConnected) {
        // 파일 크기 제한 (10MB)
        if (file.size > 10 * 1024 * 1024) {
            showAlert('파일 크기 초과', '파일 크기는 10MB를 초과할 수 없습니다.');
            fileInput.value = '';
            return;
        }

        // 업로드 중 표시
        const uploadingMsg = document.createElement('div');
        uploadingMsg.id = 'uploading-msg';
        uploadingMsg.className = 'message message-sent';
        uploadingMsg.innerHTML = `
      <div class="d-flex align-items-end justify-content-end">
        <div>
          <div class="message-bubble">
            <div class="d-flex align-items-center">
              <div class="spinner-border spinner-border-sm text-light me-2" role="status"></div>
              <span>${file.name} 업로드 중...</span>
            </div>
          </div>
        </div>
      </div>
    `;
        document.getElementById('chatMessages').appendChild(uploadingMsg);
        scrollToBottom();

        const formData = new FormData();
        formData.append('file', file);
        formData.append('roomId', roomId);

        fetch('/chat/upload', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('서버 응답 오류: ' + response.status);
                }
                return response.json();
            })
            .then(message => {
                console.log('파일 업로드 성공: ', message);
                // 업로드 중 메시지 제거 (메시지는 WebSocket을 통해 자동으로 수신됨)
                const uploadingElement = document.getElementById('uploading-msg');
                if (uploadingElement) {
                    uploadingElement.remove();
                }
            })
            .catch(error => {
                console.error('파일 업로드 실패: ', error);
                // 업로드 중 메시지 제거
                const uploadingElement = document.getElementById('uploading-msg');
                if (uploadingElement) {
                    uploadingElement.remove();
                }
                showAlert('업로드 실패', '파일 업로드에 실패했습니다. 다시 시도해 주세요.');
            });

        fileInput.value = '';
    } else if (!isConnected) {
        showAlert('연결 끊김', '서버와의 연결이 끊어졌습니다. 잠시 후 다시 시도해 주세요.');
        fileInput.value = '';
    }
}

// 메시지 표시
function displayMessage(message) {
    try {
        const chatMessages = document.getElementById('chatMessages');
        const messageElement = document.createElement('div');
        messageElement.id = 'msg-' + message.messageId;
        messageElement.className = 'message ' + (message.senderId === userId ? 'message-sent' : 'message-received');

        // 메시지 기본 구조
        let messageHTML = `
      <div class="d-flex align-items-end ${message.senderId === userId ? 'justify-content-end' : ''}">
    `;

        // 받은 메시지인 경우 프로필 이미지 표시
        if (message.senderId !== userId) {
            messageHTML += `
        <img src="${message.senderProfile || '/sources/picture/기본이미지.png'}" alt="Profile" class="avatar me-2" style="width: 30px; height: 30px;"
            onerror="this.src='/sources/picture/기본이미지.png'; this.onerror=null;">
      `;
        }

        // 메시지 버블
        messageHTML += `
      <div>
        ${message.senderId !== userId ? `<div class="mb-1">${message.senderNickname || message.senderId}</div>` : ''}
        <div class="message-bubble">
    `;

        // 메시지 타입에 따른 내용 표시
        if (message.messageType === 'TEXT') {
            messageHTML += escapeHTML(message.messageContent);
        } else if (message.messageType === 'IMAGE') {
            messageHTML += `<img src="C:/${message.filePath}" alt="이미지" style="max-width: 200px; max-height: 200px;"
                        onerror="this.src='/sources/picture/image-error.png'; this.onerror=null;">`;
        } else if (message.messageType === 'FILE') {
            messageHTML += `
        <div>
          <i class="fas fa-file"></i>
          <a href="${message.filePath}" target="_blank">${escapeHTML(message.filePath.split('/').pop())}</a>
        </div>
      `;
        }

        // 시간 및 읽음 상태
        const messageTime = formatTime(message.sendTime);
        messageHTML += `
          </div>
          <div class="d-flex align-items-center justify-content-${message.senderId === userId ? 'end' : 'start'} mt-1">
              <span class="message-time">${messageTime}</span>
              ${message.senderId === userId ? `
                  <span class="ms-1 read-status" data-msg-id="${message.messageId}">
                      ${message.readCount > 0 ? '<i class="fas fa-check-double text-primary"></i>' : '<i class="fas fa-check"></i>'}
                  </span>
              ` : ''}
          </div>
      </div>
    </div>
    `;

        messageElement.innerHTML = messageHTML;
        chatMessages.appendChild(messageElement);

        // 스크롤을 가장 아래로 이동
        scrollToBottom();
    } catch (e) {
        console.error('메시지 표시 중 오류:', e);
    }
}

// HTML 이스케이프 처리
function escapeHTML(str) {
    if (!str) return '';
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;')
        .replace(/\n/g, '<br>');
}

// 시간 포맷팅
function formatTime(timeStr) {
    try {
        const date = new Date(timeStr);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch (e) {
        console.error('시간 포맷팅 오류:', e);
        return '';
    }
}

// 스크롤을 가장 아래로 이동
function scrollToBottom() {
    try {
        const chatMessages = document.getElementById('chatMessages');
        chatMessages.scrollTop = chatMessages.scrollHeight;
    } catch (e) {
        console.error('스크롤 이동 중 오류:', e);
    }
}

// 메시지 읽음 표시
function markAsRead(messageId) {
    if (isConnected) {
        try {
            stompClient.send("/app/chat.markAsRead/" + roomId, {}, JSON.stringify({
                userId: userId,
                messageId: messageId
            }));
        } catch (e) {
            console.error('읽음 표시 중 오류:', e);
        }
    }
}

// 읽음 상태 업데이트
function updateReadStatus(messageId) {
    try {
        const readStatus = document.querySelector(`.read-status[data-msg-id="${messageId}"]`);
        if (readStatus) {
            readStatus.innerHTML = '<i class="fas fa-check-double text-primary"></i>';
        }
    } catch (e) {
        console.error('읽음 상태 업데이트 중 오류:', e);
    }
}

// 메시지 검색
function searchMessages() {
    const keyword = document.getElementById('searchInput').value.trim();
    if (keyword) {
        // 검색 결과 영역 초기화 및 로딩 표시
        const searchResults = document.getElementById('searchResults');
        searchResults.innerHTML = '<div class="text-center my-3"><div class="spinner-border spinner-border-sm text-primary" role="status"></div> 검색 중...</div>';

        fetch('/chat/room/' + roomId + '/search?keyword=' + encodeURIComponent(keyword))
            .then(response => {
                if (!response.ok) {
                    throw new Error('서버 응답 오류: ' + response.status);
                }
                return response.json();
            })
            .then(messages => {
                searchResults.innerHTML = '';

                if (messages.length === 0) {
                    searchResults.innerHTML = '<div class="list-group-item text-center text-muted">검색 결과가 없습니다.</div>';
                } else {
                    messages.forEach(message => {
                        try {
                            const messageDate = new Date(message.sendTime);
                            const formattedDate = messageDate.toLocaleDateString() + ' ' + messageDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

                            const messageElement = document.createElement('a');
                            messageElement.href = '#msg-' + message.messageId;
                            messageElement.className = 'list-group-item list-group-item-action';
                            messageElement.setAttribute('data-bs-dismiss', 'modal');
                            messageElement.onclick = function() {
                                setTimeout(() => {
                                    const msgElement = document.getElementById('msg-' + message.messageId);
                                    if (msgElement) {
                                        msgElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
                                        msgElement.classList.add('highlighted-message');
                                        setTimeout(() => {
                                            msgElement.classList.remove('highlighted-message');
                                        }, 2000);
                                    } else {
                                        showAlert('메시지 찾기 실패', '메시지를 찾을 수 없습니다.');
                                    }
                                }, 300);
                            };

                            messageElement.innerHTML = `
                <div class="d-flex w-100 justify-content-between">
                  <h6 class="mb-1">${escapeHTML(message.senderNickname || message.senderId)}</h6>
                  <small>${formattedDate}</small>
                </div>
                <p class="mb-1">${escapeHTML(message.messageContent)}</p>
              `;

                            searchResults.appendChild(messageElement);
                        } catch (e) {
                            console.error('검색 결과 렌더링 오류:', e);
                        }
                    });
                }
            })
            .catch(error => {
                console.error('메시지 검색 실패: ', error);
                searchResults.innerHTML = '<div class="list-group-item text-center text-danger">검색 중 오류가 발생했습니다.</div>';
            });
    }
}

// 페이지 언로드 시 WebSocket 연결 해제
window.addEventListener('beforeunload', function() {
    if (stompClient !== null && stompClient.connected) {
        stompClient.disconnect();
    }
});