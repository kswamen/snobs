package com.back.snobs.service.chatmessage;

import com.back.snobs.domain.chatroom.chatmessage.ChatMessageDto;
import com.back.snobs.error.CustomResponse;
import org.springframework.http.ResponseEntity;

public interface ChatMessageServiceInterface {
    ChatMessageDto saveMessage(ChatMessageDto chatMessageDto);
    ResponseEntity<CustomResponse> getMessage(Long chatRoomIdx);
}
