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

    // 주기적 알림 업데이트 설정 (5초마다)
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

        // 새 채팅방 알림 구독 추가
        stompClient.subscribe('/user/queue/new-room', function(roomData) {
            console.log('새 채팅방 알림:', roomData.body);
            const newRoom = JSON.parse(roomData.body);
            addNewChatRoom(newRoom);
        });

        // 채팅방 업데이트 알림 구독 추가
        stompClient.subscribe('/user/queue/room-update', function(roomData) {
            console.log('채팅방 업데이트 알림:', roomData.body);
            const updatedRoom = JSON.parse(roomData.body);
            updateChatRoom(updatedRoom);
        });

    }, function(error) {
        console.error('WebSocket 연결 오류: ', error);
        isConnected = false;
        updateConnectionStatus(false);

        // 5초 후 재연결 시도
        setTimeout(connectWebSocket, 5000);
    });
}

// 새로운 채팅방을 목록에 추가하는 함수
function addNewChatRoom(room) {
    // 이미 존재하는 방인지 확인
    if (document.getElementById(`room-${room.room_id}`)) {
        console.log(`채팅방 ${room.room_id}는 이미 목록에 존재합니다.`);
        updateChatRoom(room);
        return;
    }

    console.log(`새 채팅방 추가: ${room.room_id} - ${room.room_name}`);

    // 기본 프로필 이미지 URL
    const defaultProfileUrl = '/sources/picture/기본이미지.png';

    // 채팅방 종류에 따른 태그 클래스와 텍스트
    const isClubChat = room.club_id != null;
    const chatTypeTagClass = isClubChat ? 'club-tag' : 'personal-tag';
    const chatTypeText = isClubChat ? '동호회' : '개인';

    // 포맷된 시간
    const formattedTime = room.lastMessageTime ?
        formatDate(new Date(room.lastMessageTime)) : '';

    // 프로필 이미지 URL 처리
    let profileUrl = defaultProfileUrl;

    if (!isClubChat) {
        // 상대방 프로필 정보
        if (room.otherUserProfile) {
            profileUrl = room.otherUserProfile.startsWith('/') ?
                room.otherUserProfile :
                `/profile/${room.otherUserProfile}`;
        } else if (room.participants) {
            // 참가자 목록에서 상대방 찾기
            const currentUserId = document.getElementById('currentUserId')?.value || '';
            for (const p of room.participants) {
                if (p.user_id !== currentUserId && p.userProfile) {
                    profileUrl = p.userProfile.startsWith('/') ?
                        p.userProfile :
                        `/profile/${p.userProfile}`;
                    break;
                }
            }
        }
    }

    // 채팅방 HTML 생성
    const roomHTML = `
        <div id="room-${room.room_id}" class="list-group-item chat-list-item p-3" 
             onclick="location.href='/chat/view/room/${room.room_id}'">
            <div class="d-flex justify-content-between align-items-start">
                <div class="d-flex" style="width: 100%">
                    <div class="me-3">
                        <img src="${isClubChat ? defaultProfileUrl : profileUrl}" 
                             alt="${isClubChat ? 'Club' : 'User'}" class="user-avatar">
                    </div>
                    <div class="flex-grow-1">
                        <div class="d-flex justify-content-between align-items-center">
                            <h5 class="chat-title mb-0">${room.room_name}</h5>
                            <div class="chat-time">${formattedTime}</div>
                        </div>
                        <div class="d-flex justify-content-between align-items-center mt-1">
                            <p class="chat-preview mb-0">${room.lastMessage || '새로운 대화를 시작하세요.'}</p>
                            ${room.unreadCount > 0 ? `<div class="unread-badge">${room.unreadCount}</div>` : ''}
                        </div>
                    </div>
                    <span class="chat-type-tag ${chatTypeTagClass}">${chatTypeText}</span>
                </div>
            </div>
        </div>
    `;

    // 적절한 탭에 추가
    const allChatsContainer = document.querySelector('#all-chats .list-group');
    const personalChatsContainer = document.querySelector('#personal-chats .list-group');
    const clubChatsContainer = document.querySelector('#club-chats .list-group');

    // 모든 채팅 탭에 추가
    if (allChatsContainer) {
        // 빈 상태 메시지 제거
        const emptyState = allChatsContainer.querySelector('.empty-list');
        if (emptyState) {
            emptyState.remove();
        }

        // 목록 맨 앞에 추가
        allChatsContainer.insertAdjacentHTML('afterbegin', roomHTML);
    }

    // 채팅방 유형에 따라 해당 탭에도 추가
    if (isClubChat && clubChatsContainer) {
        const emptyState = clubChatsContainer.querySelector('.empty-list');
        if (emptyState) {
            emptyState.remove();
        }

        // 클럽 탭에 복제된 요소 추가 (ID 변경)
        const clubRoomHTML = roomHTML.replace(`id="room-${room.room_id}"`, `id="club-room-${room.room_id}"`);
        clubChatsContainer.insertAdjacentHTML('afterbegin', clubRoomHTML);
    } else if (!isClubChat && personalChatsContainer) {
        const emptyState = personalChatsContainer.querySelector('.empty-list');
        if (emptyState) {
            emptyState.remove();
        }

        // 개인 탭에 복제된 요소 추가 (ID 변경)
        const personalRoomHTML = roomHTML.replace(`id="room-${room.room_id}"`, `id="personal-room-${room.room_id}"`);
        personalChatsContainer.insertAdjacentHTML('afterbegin', personalRoomHTML);
    }

    // 채팅방 수가 변경되었으므로 탭 배지도 업데이트
    fetchUnreadCounts();
}

// 채팅방 업데이트 처리 함수
function updateChatRoom(room) {
    console.log('채팅방 업데이트:', room);

    // 채팅방 요소 찾기 (모든 탭에서)
    const roomElements = [
        document.getElementById(`room-${room.room_id}`),
        document.getElementById(`personal-room-${room.room_id}`),
        document.getElementById(`club-room-${room.room_id}`)
    ];

    // 존재하지 않는 채팅방이면 새로 추가
    if (!roomElements[0] && !roomElements[1] && !roomElements[2]) {
        // 채팅방 정보 요청 후 추가
        fetch(`/chat/room/${room.room_id}`)
            .then(response => response.json())
            .then(data => {
                if (data.room) {
                    addNewChatRoom(data.room);
                }
            })
            .catch(error => {
                console.error('채팅방 정보 요청 실패:', error);
                // 기본 정보로라도 추가
                addNewChatRoom(room);
            });
        return;
    }

    // 포맷된 시간
    const formattedTime = room.lastMessageTime ?
        formatDate(new Date(room.lastMessageTime)) : '';

    roomElements.forEach(roomElement => {
        if (roomElement) {
            // 마지막 메시지 업데이트
            const previewElement = roomElement.querySelector('.chat-preview');
            if (previewElement && room.lastMessage) {
                previewElement.textContent = room.lastMessage;
            }

            // 시간 업데이트
            const timeElement = roomElement.querySelector('.chat-time');
            if (timeElement && formattedTime) {
                timeElement.textContent = formattedTime;
            }

            // 안 읽은 메시지 배지 업데이트
            let unreadBadge = roomElement.querySelector('.unread-badge');

            if (room.unreadCount && room.unreadCount > 0) {
                if (!unreadBadge) {
                    // 배지가 없으면 새로 생성
                    unreadBadge = document.createElement('div');
                    unreadBadge.className = 'unread-badge';

                    // 적절한 위치에 배지 추가
                    const previewContainer = roomElement.querySelector('.d-flex.justify-content-between.align-items-center.mt-1');
                    if (previewContainer) {
                        previewContainer.appendChild(unreadBadge);
                    }
                }
                // 배지 업데이트
                unreadBadge.textContent = room.unreadCount;
                unreadBadge.style.display = 'flex';
            } else if (unreadBadge) {
                // 안 읽은 메시지가 없으면 배지 숨김
                unreadBadge.style.display = 'none';
            }

            // 채팅방 목록에서 맨 위로 이동
            const parentList = roomElement.parentElement;
            if (parentList && parentList.firstChild !== roomElement) {
                parentList.insertBefore(roomElement, parentList.firstChild);
            }
        }
    });

    // 탭의 안 읽은 메시지 수 업데이트
    fetchUnreadCounts();
}

// 날짜 포맷팅 헬퍼 함수
function formatDate(date) {
    if (!date) return '';

    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${month}/${day} ${hours}:${minutes}`;
}

// 연결 상태 UI 업데이트
function updateConnectionStatus(connected) {
    console.log('WebSocket 연결 상태:', connected ? '연결됨' : '연결 끊김');
}

// 주기적으로 안 읽은 메시지 수 확인 (1초마다)
function startPeriodicUnreadCheck() {
    if (unreadRefreshInterval) {
        clearInterval(unreadRefreshInterval);
    }

    unreadRefreshInterval = setInterval(function() {
        if (isConnected) {
            fetchUnreadCounts();
        }
    }, 1000); // 1초마다 업데이트 (기존 5초에서 변경)
}

// 안 읽은 메시지 수 업데이트를 위한 API 호출
function fetchUnreadCounts() {
    fetch('/chat/unread-counts')
        .then(response => response.json())
        .then(data => {
            // 개인 채팅 및 동호회 채팅 안 읽은 메시지 수 업데이트
            updateTabBadge('personal-tab', data.personalUnread);
            updateTabBadge('club-tab', data.clubUnread);
            updateTabBadge('all-tab', data.totalUnread);

            // 각 채팅방의 unreadCount 업데이트
            updateRoomUnreadCounts(data.roomUnreadCounts);
        })
        .catch(error => {
            console.error('안 읽은 메시지 수 조회 실패: ', error);
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

    const roomId = message.roomId;
    const roomElements = [
        document.getElementById(`room-${roomId}`),
        document.getElementById(`personal-room-${roomId}`),
        document.getElementById(`club-room-${roomId}`)
    ];

    // 채팅방이 존재하지 않으면 새로운 채팅방 정보를 요청
    if (!roomElements[0] && !roomElements[1] && !roomElements[2]) {
        console.log('새 메시지가 도착했지만 채팅방이 목록에 없습니다:', roomId);
        // 채팅방 정보 요청
        fetch(`/chat/room/${roomId}`)
            .then(response => response.json())
            .then(data => {
                if (data.room) {
                    // 채팅방 정보를 사용하여 목록에 추가
                    const room = data.room;
                    room.lastMessage = message.messageContent;
                    room.lastMessageTime = message.sendTime || new Date();
                    room.unreadCount = 1; // 최소한 현재 메시지는 읽지 않음
                    addNewChatRoom(room);
                    // 알림 표시
                    showBrowserNotification(message);
                }
            })
            .catch(error => {
                console.error('채팅방 정보 요청 실패:', error);
            });
        return;
    }

    roomElements.forEach(roomElement => {
        if (roomElement) {
            // 미리보기 & 시간 업데이트
            const previewElement = roomElement.querySelector('.chat-preview');
            if (previewElement) {
                previewElement.textContent = message.messageContent;
            }

            const timeElement = roomElement.querySelector('.chat-time');
            if (timeElement) {
                const messageDate = new Date(message.sendTime || new Date());
                const formattedTime = formatDate(messageDate);
                timeElement.textContent = formattedTime;
            }

            // 안 읽은 메시지 배지 업데이트
            let unreadBadge = roomElement.querySelector('.unread-badge');
            const unreadCount = unreadBadge ? parseInt(unreadBadge.textContent) : 0;

            if (!unreadBadge) {
                unreadBadge = document.createElement('div');
                unreadBadge.className = 'unread-badge';

                const previewContainer = roomElement.querySelector('.d-flex.justify-content-between.align-items-center.mt-1');
                if (previewContainer) {
                    previewContainer.appendChild(unreadBadge);
                }
            }

            // 배지 업데이트
            unreadBadge.textContent = unreadCount + 1;
            unreadBadge.style.display = 'flex';

            // 채팅방 목록에서 맨 위로 이동
            const parentList = roomElement.parentElement;
            if (parentList && parentList.firstChild !== roomElement) {
                parentList.insertBefore(roomElement, parentList.firstChild);
            }
        }
    });

    // 브라우저 알림 표시
    showBrowserNotification(message);

    // 채팅방의 미읽음 메시지 수를 갱신하기 위해 API 호출
    fetchUnreadCounts();
}

// 채팅방 목록 업데이트 (안 읽은 메시지 수 반영) - 중복 함수 제거하고 하나로 통합
function updateRoomUnreadCounts(roomUnreadCounts) {
    if (!roomUnreadCounts) return;

    Object.keys(roomUnreadCounts).forEach(roomId => {
        const unreadCount = roomUnreadCounts[roomId];

        // 채팅방 요소 찾기 (모든 탭에서)
        const roomElements = [
            document.getElementById(`room-${roomId}`),
            document.getElementById(`personal-room-${roomId}`),
            document.getElementById(`club-room-${roomId}`)
        ];

        roomElements.forEach(roomElement => {
            if (roomElement) {
                // 기존 안 읽은 메시지 배지 찾기
                let unreadBadge = roomElement.querySelector('.unread-badge');

                if (unreadCount > 0) {
                    if (!unreadBadge) {
                        // 배지가 없으면 새로 생성
                        unreadBadge = document.createElement('div');
                        unreadBadge.className = 'unread-badge';

                        // 미리보기 컨테이너 찾기
                        const previewContainer = roomElement.querySelector('.d-flex.justify-content-between.align-items-center.mt-1');
                        if (previewContainer) {
                            previewContainer.appendChild(unreadBadge);
                        } else {
                            // 미리보기 컨테이너가 없는 경우 최선의 위치 찾기
                            const chatInfo = roomElement.querySelector('.chat-info');
                            if (chatInfo) {
                                chatInfo.appendChild(unreadBadge);
                            }
                        }
                    }

                    // 배지 업데이트
                    unreadBadge.textContent = unreadCount;
                    unreadBadge.style.display = 'flex';
                } else if (unreadBadge) {
                    // 안 읽은 메시지가 없으면 배지 숨김
                    unreadBadge.style.display = 'none';
                }
            }
        });
    });
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