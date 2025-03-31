let stompClient = null;
let isConnected = false;

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 로그인 상태 확인 및 WebSocket 연결
    initializeNotifications();
});

// 알림 시스템 초기화
function initializeNotifications() {
    // 로그인 체크 - currentUserId는 HTML에서 Thymeleaf로 설정됨
    if (typeof currentUserId === 'undefined' || !currentUserId) {
        console.log('로그인 상태가 아니므로 알림 시스템을 초기화하지 않습니다.');
        return;
    }

    console.log('알림 시스템 초기화. 사용자 ID:', currentUserId);

    // 최초 unread count 가져오기
    fetchUnreadCounts();

    // 2초마다 unread count 갱신 (2000ms = 2초)
    setInterval(fetchUnreadCounts, 2000);

    // WebSocket 연결
    connectWebSocket();
}

// WebSocket 연결 함수
function connectWebSocket() {
    // SockJS 및 STOMP 클라이언트 생성
    const socket = new SockJS('/ws-chat');
    stompClient = Stomp.over(socket);

    // 디버깅 로그 설정 (필요시 주석 처리)
    stompClient.debug = function(message) {
        //console.debug('STOMP: ' + message);
    };

    // 연결 설정
    stompClient.connect({}, function(frame) {
        console.log('WebSocket 연결 성공:', frame);
        isConnected = true;

        // 개인 알림 구독
        stompClient.subscribe('/user/queue/notifications', function(notification) {
            console.log('새 메시지 알림 수신:', notification);
            handleNewMessageNotification(JSON.parse(notification.body));
        });

        // 새 채팅방 알림 구독
        stompClient.subscribe('/user/queue/new-room', function(message) {
            console.log('새 채팅방 알림 수신:', message);
            handleNewRoomNotification(JSON.parse(message.body));
        });

        // 읽음 상태 알림 구독
        stompClient.subscribe('/user/queue/read-receipts', function(message) {
            console.log('읽음 상태 알림 수신:', message);
            // 안 읽은 메시지 수 갱신
            fetchUnreadCounts();
        });

    }, function(error) {
        // 연결 오류 처리
        console.error('WebSocket 연결 오류:', error);
        isConnected = false;

        // 5초 후 재연결 시도
        setTimeout(connectWebSocket, 5000);
    });
}

// 새 메시지 알림 처리
function handleNewMessageNotification(notification) {
    // 안 읽은 메시지 수 갱신
    fetchUnreadCounts();

    // 선택적: 브라우저 알림 표시
    showBrowserNotification('새 메시지', notification.senderNickname + ': ' + notification.messageContent);
}

// 새 채팅방 알림 처리
function handleNewRoomNotification(roomData) {
    // 안 읽은 메시지 수 갱신
    fetchUnreadCounts();

    // 선택적: 브라우저 알림 표시
    showBrowserNotification('새 채팅방', roomData.room_name + '에 초대되었습니다.');
}

// 안 읽은 메시지 수 가져오기
function fetchUnreadCounts() {
    fetch('/chat/unread-counts')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok: ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            console.log('안 읽은 메시지 수 데이터:', data);
            updateUnreadBadges(data);
        })
        .catch(error => {
            console.error('안 읽은 메시지 수 가져오기 오류:', error);
        });
}

// 안 읽은 메시지 배지 업데이트
function updateUnreadBadges(data) {
    // 상단 메뉴 배지 업데이트
    const headerBadge = document.getElementById('unreadChatCount');
    if (headerBadge) {
        if (data.totalUnread > 0) {
            headerBadge.textContent = data.totalUnread;
            headerBadge.style.display = 'inline-block';
        } else {
            headerBadge.style.display = 'none';
        }
    }

    // 모바일 메뉴 배지 업데이트
    const mobileBadge = document.getElementById('mobileUnreadChatCount');
    if (mobileBadge) {
        if (data.totalUnread > 0) {
            mobileBadge.textContent = data.totalUnread;
            mobileBadge.style.display = 'inline-flex';
        } else {
            mobileBadge.style.display = 'none';
        }
    }
}

// 브라우저 알림 표시
function showBrowserNotification(title, body) {
    // 알림 권한 확인
    if ('Notification' in window && Notification.permission === 'granted') {
        const notification = new Notification(title, {
            body: body,
            icon: '/sources/picture/기본이미지.png'
        });

        // 알림 클릭 시 채팅방 목록으로 이동
        notification.onclick = function() {
            window.focus();
            window.location.href = '/chat/view/rooms';
        };
    }
    // 알림 권한 요청
    else if ('Notification' in window && Notification.permission !== 'denied') {
        Notification.requestPermission();
    }
}

// 페이지 종료 시 연결 정리
window.addEventListener('beforeunload', function() {
    if (stompClient && isConnected) {
        stompClient.disconnect();
        console.log('WebSocket 연결 종료');
    }
});

// 노출 API - 외부에서 호출 가능
window.updateUnreadCount = function(count) {
    const data = { totalUnread: count };
    updateUnreadBadges(data);
};