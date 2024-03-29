package com.back.snobs.service;

import com.back.snobs.domain.chatroom.ChatRoom;
import com.back.snobs.domain.chatroom.ChatRoomRepository;
import com.back.snobs.domain.reaction.Reaction;
import com.back.snobs.domain.reaction.ReactionRepository;
import com.back.snobs.domain.snob.Snob;
import com.back.snobs.error.CustomResponse;
import com.back.snobs.error.ResponseCode;
import com.back.snobs.error.exception.ChatRoomDuplicationException;
import com.back.snobs.error.exception.NoDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ReactionRepository reactionRepository;
    private final JdbcTemplate jdbcTemplate;
    private final int batchSize = 500;
    private final int CHAT_ROOM_MAX_SIZE = 10;

//    public ResponseEntity<CustomResponse> getChatRoom(String userEmail) {
//        PageRequest pr = PageRequest.of(0, 500);
//        List<ChatRoom> cr1 = chatRoomRepository.findAllChatRoomByMeWithFetchJoin(userEmail, pr);
//        List<ChatRoom> cr2 = chatRoomRepository.findAllChatRoomByYouWithFetchJoin(userEmail, pr);
//        List<ChatRoom> result = new ArrayList<>();
//        result.addAll(cr1);
//        result.addAll(cr2);
//        result.sort((o1, o2) -> (int) (o1.getChatRoomIdx() - o2.getChatRoomIdx()));
//
//        // 페이징이 적용되기 전이라, 테스트 용으로 일단 결과 리스트를 잘라서 보냄
//        if (result.size() >= CHAT_ROOM_MAX_SIZE) {
//            return new ResponseEntity<>(new CustomResponse(
//                    ResponseCode.SUCCESS, result.subList(0, CHAT_ROOM_MAX_SIZE)), HttpStatus.valueOf(200)
//            );
//        }
//        return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS, result), HttpStatus.valueOf(200));
//    }

    public ResponseEntity<CustomResponse> getChatRoom(String userEmail) {
        PageRequest pr = PageRequest.of(0, 500);
        List<ChatRoom> cr = chatRoomRepository.findAllChatRoomByUserEmail(userEmail, pr);

        return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS, cr), HttpStatus.valueOf(200));
    }

    public List<ChatRoom> findAll() {
        return chatRoomRepository.findAll();
    }

    public List<ChatRoom> findTop300() {
        PageRequest pr = PageRequest.of(0, 300);
        return chatRoomRepository.findAll(pr).getContent();
    }

    public void updateAllByJDBC(List<ChatRoom> chatRoomList) {
        int batchCount = 0;
        List<ChatRoom> subItems = new ArrayList<>();
        for(int i = 0; i < chatRoomList.size(); i++) {
            subItems.add(chatRoomList.get(i));
            if((i + 1) % batchSize == 0) {
                batchCount = batchUpdate(batchCount, subItems);
            }
        }
        if(!subItems.isEmpty()) {
            batchCount = batchUpdate(batchCount, subItems);
        }
    }

    private int batchUpdate(int batchCount, List<ChatRoom> atMsgs) {
        String query = "UPDATE chat_room SET `last_message_saved` = ? where `chat_room_idx` = ?";
        jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ChatRoom chatRoom = atMsgs.get(i);
                ps.setLong(1, chatRoom.getLastMessageSaved());
                ps.setLong(2, chatRoom.getChatRoomIdx());
            }

            @Override
            public int getBatchSize() {
                return atMsgs.size();
            }
        });
        atMsgs.clear();
        batchCount++;
        return batchCount;
    }

    // Chatroom create
    @Transactional
    public ResponseEntity<CustomResponse> createChatRoom(Long reactionIdx) {
        Reaction reaction = reactionRepository.findById(reactionIdx).orElseThrow(() ->
                new NoDataException("No Such Data", ResponseCode.DATA_NOT_FOUND));
        Snob receiverSnob = reaction.getReceiverSnob();
        Snob senderSnob = reaction.getSenderSnob();
        if(chatRoomRepository.existsBySenderSnob_userEmailAndReceiverSnob_userEmail(senderSnob.getUserEmail(), receiverSnob.getUserEmail()) ||
                chatRoomRepository.existsBySenderSnob_userEmailAndReceiverSnob_userEmail(receiverSnob.getUserEmail(), senderSnob.getUserEmail())) {
            throw new ChatRoomDuplicationException("ChatRoom Duplicated", ResponseCode.CHATROOM_DUPLICATION);
        }

        return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS, chatRoomRepository.save(
                ChatRoom.builder()
                        .receiverSnob(receiverSnob)
                        .senderSnob(senderSnob)
                        .lastMessageSaved(System.currentTimeMillis())
                        .build()
        )), HttpStatus.valueOf(200));
    }
}
