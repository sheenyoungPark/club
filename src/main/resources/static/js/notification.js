/**
 * 채팅 알림 모듈
 * 모든 페이지에서 사용되는 실시간 알림 시스템
 */
var ChatNotification = (function() {
    // 비공개 변수
    var stompClient = null;
    var isConnected = false;
    var userId = null;
    var chatCountElement = null;

    // 초기화 함수
    function init() {
        // 사용자 ID 가져오기 (전역 변수 또는 data 속성)
        userId = getUserId();

        if (!userId) {
            console.log('사용자 ID를 찾을 수 없음. 알림 시스템을 초기화하지 않습니다.');
            return;
        }

        // 알림 배지 요소 찾기
        chatCountElement = document.getElementById('unreadChatCount');

        // WebSocket 연결
        connectWebSocket();

        // 브라우저 알림 권한 요청
        requestNotificationPermission();
    }

    // 사용자 ID 가져오기
    function getUserId() {
        // 방법 1: HTML data 속성에서 가져오기
        var userElement = document.querySelector('[data-user-id]');
        if (userElement && userElement.dataset.userId) {
            return userElement.dataset.userId;
        }

        // 방법 2: 전역 변수로 설정된 경우
        if (typeof window.userId !== 'undefined') {
            return window.userId;
        }

        return null;
    }

    // WebSocket 연결
    function connectWebSocket() {
        var socket = new SockJS('/ws-chat');
        stompClient = Stomp.over(socket);

        // 디버그 로그 비활성화
        stompClient.debug = null;

        stompClient.connect({}, function(frame) {
            console.log('채팅 알림 시스템 연결됨');
            isConnected = true;

            // 개인 알림 구독
            stompClient.subscribe('/user/queue/notifications', function(notification) {
                handleNewMessage(JSON.parse(notification.body));
            });

            // 안 읽은 메시지 수 구독
            stompClient.subscribe('/user/queue/total-unread', function(message) {
                updateUnreadCount(JSON.parse(message.body));
            });

        }, function(error) {
            console.log('채팅 알림 시스템 연결 실패:', error);
            isConnected = false;

            // 1초 후 재연결 시도
            setTimeout(connectWebSocket, 1000);
        });
    }

    // 브라우저 알림 권한 요청
    function requestNotificationPermission() {
        if ('Notification' in window) {
            if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
                Notification.requestPermission();
            }
        }
    }

    // 새 메시지 처리
    function handleNewMessage(message) {
        // 브라우저 알림 표시
        showBrowserNotification(message.senderNickname || message.senderId, message.messageContent);

        // 안 읽은 메시지 수 업데이트
        incrementUnreadCount();
    }

    // 브라우저 알림 표시
    function showBrowserNotification(sender, message) {
        if ('Notification' in window && Notification.permission === 'granted') {
            var notification = new Notification(sender + '님의 메시지', {
                body: message,
                icon: '/sources/picture/기본이미지.png'
            });

            // 알림 클릭 시 채팅방으로 이동
            notification.onclick = function() {
                window.focus();
                window.location.href = '/chat/view/rooms';
            };

            // 5초 후 자동으로 닫기
            setTimeout(function() {
                notification.close();
            }, 5000);
        }
    }

    // 안 읽은 메시지 수 증가
    function incrementUnreadCount() {
        if (chatCountElement) {
            var count = parseInt(chatCountElement.textContent || '0');
            count++;
            chatCountElement.textContent = count;
            chatCountElement.style.display = 'inline-block';
        }
    }

    // 안 읽은 메시지 수 업데이트
    function updateUnreadCount(data) {
        if (chatCountElement) {
            var count = 0;

            // data가 숫자인 경우
            if (typeof data === 'number') {
                count = data;
            }
            // data가 객체인 경우 (count 속성 확인)
            else if (data && typeof data.count === 'number') {
                count = data.count;
            }

            chatCountElement.textContent = count;
            chatCountElement.style.display = count > 0 ? 'inline-block' : 'none';
        }
    }

    // 알림음 재생
    function playNotificationSound() {
        var audio = document.getElementById('chatNotificationSound');
        if (audio) {
            audio.play().catch(function(e) {
                console.log('알림음 재생 실패:', e);
            });
        }
    }

    // 공개 메서드
    return {
        init: init,
        updateUnreadCount: updateUnreadCount
    };
})();

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    ChatNotification.init();
});