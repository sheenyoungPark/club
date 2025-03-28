package com.spacedong.beans;

import lombok.Data;

/**
 * 채팅방 생성 결과를 담는 클래스
 * 채팅방 객체와 함께 새로 생성되었는지, 새 참가자가 추가되었는지 정보를 포함
 */
@Data
public class ChatRoomCreationResult {
    private ChatRoomBean room;
    private boolean isNewRoom;
    private boolean isNewParticipant;

    public ChatRoomCreationResult(ChatRoomBean room, boolean isNewRoom, boolean isNewParticipant) {
        this.room = room;
        this.isNewRoom = isNewRoom;
        this.isNewParticipant = isNewParticipant;
    }
}