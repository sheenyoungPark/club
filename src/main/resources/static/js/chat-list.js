// WebSocket 및 STOMP 클라이언트 변수
let stompClient = null;
let isConnected = false;
let unreadRefreshInterval;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // WebSocket 연결
    connectWebSocket();

    // 최초 안 읽은 메시지 수 업데이트
    fetchUnreadCounts();

    // 주기적 알림 업데이트 설정 (30초마다)
    startPeriodicUnreadCheck();

    // 브라우저 알림 권한 요청
    requestNotificationPermission();
});

// 브라우저 알림 권한 요청
function requestNotificationPermission() {
    if ('Notification' in window) {
        if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
            Notification.requestPermission();
        }
    }
}

// WebSocket 연결 함수
function connectWebSocket() {
    const socket = new SockJS('/ws-chat');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected to WebSocket: ' + frame);
        isConnected = true;
        updateConnectionStatus(true);

        // 개인 알림 구독
        stompClient.subscribe('/user/queue/notifications', function(notification) {
            console.log('새 메시지 알림:', notification.body);
            handleNewMessageNotification(JSON.parse(notification.body));
        });

        // 읽음 상태 변경 알림 구독
        stompClient.subscribe('/user/queue/read-receipts', function(receipt) {
            console.log('읽음 상태 업데이트:', receipt.body);
            fetchUnreadCounts();
        });

    }, function(error) {
        console.error('WebSocket 연결 오류: ', error);
        isConnected = false;
        updateConnectionStatus(false);

        // 5초 후 재연결 시도
        setTimeout(connectWebSocket, 5000);
    });
}

// 연결 상태 UI 업데이트
function updateConnectionStatus(connected) {
    console.log('WebSocket 연결 상태:', connected ? '연결됨' : '연결 끊김');
}

// 주기적으로 안 읽은 메시지 수 확인 (5초마다)
function startPeriodicUnreadCheck() {
    if (unreadRefreshInterval) {
        clearInterval(unreadRefreshInterval);
    }

    unreadRefreshInterval = setInterval(function() {
        if (isConnected) {
            fetchUnreadCounts();
        }
    }, 1000);
}

// 안 읽은 메시지 수 업데이트를 위한 API 호출
function fetchUnreadCounts() {
    fetch('/chat/unread-counts')
        .then(response => response.json())
        .then(data => {
            console.log('받은 데이터:', data);

            // 개인 채팅 및 동호회 채팅 안 읽은 메시지 수 업데이트
            updateTabBadge('personal-tab', data.personalUnread);
            updateTabBadge('club-tab', data.clubUnread);
            updateTabBadge('all-tab', data.totalUnread);

            updateRoomUnreadCounts(data.roomUnreadCounts);

        })
        .catch(error => {
            console.error('안 읽은 메시지 수 조회 실패: ', error);
        });
}

// 채팅방 목록 업데이트 (안 읽은 메시지 수 반영)
function updateRoomUnreadCounts(roomUnreadCounts) {
    Object.keys(roomUnreadCounts).forEach(roomId => {
        const unreadCount = roomUnreadCounts[roomId];

        // 모든 탭에서 해당 roomId를 가진 채팅방 찾기
        const roomElements = [
            document.getElementById(`room-${roomId}`),
            document.getElementById(`personal-room-${roomId}`),
            document.getElementById(`club-room-${roomId}`)
        ];

        roomElements.forEach(roomElement => {
            if (roomElement) {
                // 안 읽은 메시지 배지 찾기
                let unreadBadge = roomElement.querySelector('.unread-badge');

                if (unreadCount > 0) {
                    // 배지가 없으면 생성
                    if (!unreadBadge) {
                        const previewContainer = roomElement.querySelector('.d-flex.justify-content-between.align-items-center.mt-1');
                        if (previewContainer) {
                            unreadBadge = document.createElement('div');
                            unreadBadge.className = 'unread-badge';
                            previewContainer.appendChild(unreadBadge);
                        }
                    }

                    // 배지가 있으면 업데이트
                    if (unreadBadge) {
                        unreadBadge.textContent = unreadCount;
                        unreadBadge.style.display = 'flex'; // flex로 변경하여 콘텐츠 정렬
                    }
                } else if (unreadBadge) {
                    // 안 읽은 메시지가 없으면 배지 숨기기
                    unreadBadge.style.display = 'none';
                }
            }
        });
    });
}

// 탭의 읽지 않은 메시지 수 업데이트
function updateTabBadge(tabId, unreadCount) {
    const tabElement = document.getElementById(tabId);
    if (tabElement) {
        let badge = tabElement.querySelector('.badge');
        if (unreadCount > 0) {
            if (!badge) {
                badge = document.createElement('span');
                badge.className = 'badge bg-danger rounded-pill ms-1';
                tabElement.appendChild(badge);
            }
            badge.textContent = unreadCount;
        } else if (badge) {
            badge.remove();
        }
    }
}

// 새 메시지 알림 처리
function handleNewMessageNotification(message) {
    console.log('새 메시지 처리:', message);

    // 알림이 온 채팅방 찾기
    const roomId = message.roomId;
    const roomElements = [
        document.getElementById(`room-${roomId}`),
        document.getElementById(`personal-room-${roomId}`),
        document.getElementById(`club-room-${roomId}`)
    ];

    roomElements.forEach(roomElement => {
        if (roomElement) {
            // 채팅방이 목록에 있으면 정보 업데이트
            const previewElement = roomElement.querySelector('.chat-preview');
            if (previewElement) {
                previewElement.textContent = message.messageContent;
            }

            // 시간 업데이트
            const timeElement = roomElement.querySelector('.chat-time');
            if (timeElement) {
                const messageDate = new Date();
                const formattedTime = `${String(messageDate.getMonth() + 1).padStart(2, '0')}/${String(messageDate.getDate()).padStart(2, '0')} ${String(messageDate.getHours()).padStart(2, '0')}:${String(messageDate.getMinutes()).padStart(2, '0')}`;
                timeElement.textContent = formattedTime;
            }

            // 안 읽은 메시지 수 증가 - 여기가 수정되어야 하는 부분
            let unreadBadge = roomElement.querySelector('.unread-badge');
            const unreadCount = unreadBadge ? parseInt(unreadBadge.textContent) : 0;

            // 안 읽은 메시지 배지가 없으면 생성
            if (!unreadBadge) {
                const previewContainer = roomElement.querySelector('.d-flex.justify-content-between.align-items-center.mt-1');
                if (previewContainer) {
                    unreadBadge = document.createElement('div');
                    unreadBadge.className = 'unread-badge';
                    previewContainer.appendChild(unreadBadge);
                }
            }

            // 배지 업데이트
            if (unreadBadge) {
                unreadBadge.textContent = unreadCount + 1;
                unreadBadge.style.display = 'flex'; // flex로 변경하여 콘텐츠 정렬
            }

            // 채팅방 목록에서 맨 위로 이동
            const parentList = roomElement.parentElement;
            if (parentList && parentList.firstChild !== roomElement) {
                parentList.insertBefore(roomElement, parentList.firstChild);
            }
        }
    });

    // 브라우저 알림 표시
    showBrowserNotification(message);
}

// 브라우저 알림 표시
function showBrowserNotification(message) {
    if ('Notification' in window && Notification.permission === 'granted') {
        const notification = new Notification(message.senderNickname || message.senderId, {
            body: message.messageContent,
            icon: message.senderProfile || '/sources/picture/기본이미지.png'
        });

        notification.onclick = function() {
            window.focus();
            window.location.href = `/chat/view/room/${message.roomId}`;
        };
    } else if ('Notification' in window && Notification.permission !== 'denied') {
        Notification.requestPermission();
    }
}

// 페이지 종료 시 WebSocket 정리
window.addEventListener('beforeunload', function() {
    if (unreadRefreshInterval) {
        clearInterval(unreadRefreshInterval);
    }

    if (stompClient !== null && isConnected) {
        stompClient.disconnect();
    }
});