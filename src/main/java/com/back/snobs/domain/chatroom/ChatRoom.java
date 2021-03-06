package com.back.snobs.domain.chatroom;

import com.back.snobs.domain.BaseTimeEntity;
import com.back.snobs.domain.snob.Snob;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Getter
@RequiredArgsConstructor
@Entity
public class ChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatRoomIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Snob receiverSnob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Snob senderSnob;

    private Long lastMessageSaved;

    @Builder
    public ChatRoom(Snob receiverSnob, Snob senderSnob, Long lastMessageSaved) {
        this.receiverSnob = receiverSnob;
        this.senderSnob = senderSnob;
        this.lastMessageSaved = lastMessageSaved;
    }

    public void setLastMessageSaved(Long modifiedTime) {
        this.lastMessageSaved = modifiedTime;
    }
}
