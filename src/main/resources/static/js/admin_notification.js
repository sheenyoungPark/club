// admin_notification.js - 관리자 페이지용 알림 시스템 공통 파일

// 전역 변수
let adminStompClient = null;
let isAdminConnected = false;

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 로그인 상태 확인 및 WebSocket 연결
    initializeAdminNotifications();
});

// 알림 시스템 초기화
function initializeAdminNotifications() {
    // currentUserId 변수가 정의되어 있는지 확인 (HTML에서 설정되어야 함)
    if (typeof currentUserId === 'undefined' || !currentUserId) {
        console.log('관리자 로그인 상태가 아니므로 알림 시스템을 초기화하지 않습니다.');
        return;
    }

    console.log('관리자 알림 시스템 초기화. 관리자 ID:', currentUserId);

    // 최초 unread count 가져오기
    fetchAdminUnreadCount();

    // 2초마다 unread count 갱신
    setInterval(fetchAdminUnreadCount, 2000);

    // WebSocket 연결
    connectAdminWebSocket();
}

// WebSocket 연결 함수
function connectAdminWebSocket() {
    // SockJS 및 STOMP 클라이언트 생성
    const socket = new SockJS('/ws-chat');
    adminStompClient = Stomp.over(socket);

    // 디버깅 로그 최소화
    adminStompClient.debug = null;

    // 연결 설정
    adminStompClient.connect({}, function(frame) {
        console.log('관리자 WebSocket 연결 성공');
        isAdminConnected = true;

        // 개인 알림 구독
        adminStompClient.subscribe('/user/queue/notifications', function(notification) {
            console.log('새 메시지 알림 수신');
            fetchAdminUnreadCount();
        });

        // 새 채팅방 알림 구독
        adminStompClient.subscribe('/user/queue/new-room', function(message) {
            console.log('새 채팅방 알림 수신');
            fetchAdminUnreadCount();
        });

        // 읽음 상태 알림 구독
        adminStompClient.subscribe('/user/queue/read-receipts', function(message) {
            console.log('읽음 상태 알림 수신');
            fetchAdminUnreadCount();
        });

    }, function(error) {
        // 연결 오류 처리
        console.error('WebSocket 연결 오류:', error);
        isAdminConnected = false;

        // 5초 후 재연결 시도
        setTimeout(connectAdminWebSocket, 5000);
    });
}

// 안 읽은 메시지 수 가져오는 함수
function fetchAdminUnreadCount() {
    // userId가 없으면 실행하지 않음
    if (typeof currentUserId === 'undefined' || !currentUserId) {
        return;
    }

    fetch('/chat/unread-counts')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok: ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            updateAdminUnreadBadge(data.totalUnread);
        })
        .catch(error => {
            console.error('안 읽은 메시지 수 가져오기 오류:', error);
        });
}

function updateAdminUnreadBadge(count) {
    // 관리자 채팅 메뉴에 있는 배지만 찾기
    const unreadBadge = document.querySelector('.admin-chat-link .admin-chat-badge');

    if (unreadBadge) {
        if (count > 0) {
            unreadBadge.textContent = count;
            unreadBadge.style.display = 'inline-block';
        } else {
            unreadBadge.style.display = 'none';
        }
    }
}

// 현재 페이지가 '관리자 채팅' 페이지인지 확인
function isAdminChatPage() {
    // URL에 'chat/view/rooms'가 포함되어 있는지 확인
    return window.location.href.includes('/chat/view/rooms');
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 현재 페이지가 채팅 페이지가 아닌 경우에만 알림 시스템 초기화
    if (!isAdminChatPage()) {
        initializeAdminNotifications();
    }
});