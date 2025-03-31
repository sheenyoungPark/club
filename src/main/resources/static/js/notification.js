// WebSocket 연결을 위한 변수
let notificationStompClient = null;
let isNotificationConnected = false;

// 페이지 로드 시 WebSocket 연결 설정
document.addEventListener('DOMContentLoaded', function() {
    // WebSocket 연결 설정
    connectNotificationWebSocket();

    // 30초마다 안 읽은 메시지 수 갱신 (백업 메커니즘)
    setInterval(fetchUnreadChatCount, 1000);
});

// WebSocket 연결 함수
function connectNotificationWebSocket() {
    // userId가 정의되어 있는지 확인 (로그인한 사용자만)
    if (typeof userId === 'undefined' || !userId) {
        console.log('notification.js: 로그인 상태가 아니므로 WebSocket 연결을 설정하지 않습니다.');
        return;
    }

    console.log('notification.js: WebSocket 연결 시도...');
    const socket = new SockJS('/ws-chat');
    notificationStompClient = Stomp.over(socket);

    // 디버깅 메시지 최소화 (필요시 주석 처리)
    notificationStompClient.debug = null;

    notificationStompClient.connect({}, function(frame) {
        console.log('notification.js: WebSocket 연결 성공');
        isNotificationConnected = true;

        // 개인 메시지 알림 구독
        notificationStompClient.subscribe('/user/queue/notifications', function(notification) {
            console.log('notification.js: 새 메시지 알림 수신');
            // 새 메시지가 오면 안 읽은 메시지 수 갱신
            fetchUnreadChatCount();
        });

        // 새 채팅방 알림 구독
        notificationStompClient.subscribe('/user/queue/new-room', function(roomData) {
            console.log('notification.js: 새 채팅방 알림 수신');
            // 새 채팅방이 생성되면 안 읽은 메시지 수 갱신
            fetchUnreadChatCount();
        });

        // 읽음 상태 변경 알림 구독
        notificationStompClient.subscribe('/user/queue/read-receipts', function(receipt) {
            console.log('notification.js: 읽음 상태 알림 수신');
            // 읽음 상태가 변경되면 안 읽은 메시지 수 갱신
            fetchUnreadChatCount();
        });

        // 초기 안 읽은 메시지 수 가져오기
        fetchUnreadChatCount();

    }, function(error) {
        console.error('notification.js: WebSocket 연결 오류:', error);
        isNotificationConnected = false;

        // 연결 재시도 (5초 후)
        setTimeout(connectNotificationWebSocket, 5000);
    });
}

// 안 읽은 메시지 수 가져오는 함수
function fetchUnreadChatCount() {
    if (typeof userId === 'undefined' || !userId) {
        return; // 로그인하지 않은 경우 실행하지 않음
    }

    fetch('/chat/unread-counts')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok: ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            console.log('notification.js: 안 읽은 메시지 수 가져옴:', data.totalUnread);
            updateUnreadBadge(data.totalUnread);
        })
        .catch(error => {
            console.error('notification.js: 안 읽은 메시지 수 가져오기 오류:', error);
        });
}

// 안 읽은 메시지 배지 업데이트 함수
function updateUnreadBadge(count) {
    // 메인 헤더의 배지 업데이트
    const unreadBadge = document.getElementById('unreadChatCount');
    if (unreadBadge) {
        // 카운트가 있으면 표시, 없으면 숨김
        if (count > 0) {
            unreadBadge.textContent = count;
            unreadBadge.style.display = 'inline-block';
        } else {
            unreadBadge.style.display = 'none';
        }
    }

    // 모바일 메뉴의 배지도 함께 업데이트
    const mobileUnreadBadge = document.getElementById('mobileUnreadChatCount');
    if (mobileUnreadBadge) {
        if (count > 0) {
            mobileUnreadBadge.textContent = count;
            mobileUnreadBadge.style.display = 'inline-flex';
        } else {
            mobileUnreadBadge.style.display = 'none';
        }
    }
}

// 전역 함수로 노출하여 다른 스크립트에서도 사용할 수 있게 함
window.updateUnreadCount = updateUnreadBadge;

// 페이지 종료 시 WebSocket 정리
window.addEventListener('beforeunload', function() {
    if (notificationStompClient !== null && isNotificationConnected) {
        notificationStompClient.disconnect();
        console.log('notification.js: WebSocket 연결 종료');
    }
});

// 브라우저 알림 권한 요청 (선택적)
function requestNotificationPermission() {
    if ('Notification' in window) {
        if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
            Notification.requestPermission();
        }
    }
}

// 브라우저 알림 표시 (선택적)
function showBrowserNotification(title, body, icon) {
    if ('Notification' in window && Notification.permission === 'granted') {
        const notification = new Notification(title, {
            body: body,
            icon: icon || '/sources/picture/기본이미지.png'
        });

        notification.onclick = function() {
            window.focus();
            if (body.includes('메시지')) {
                window.location.href = '/chat/view/rooms';//1111
            }
        };
    }
}