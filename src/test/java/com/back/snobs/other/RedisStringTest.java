package com.back.snobs.other;

import com.back.snobs.domain.chatroom.chatmessage.ChatMessage;
import com.back.snobs.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@SpringBootTest
public class RedisStringTest {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

//    @Test
    void testStrings() {
        ChatMessage chatMessage = ChatMessage.builder()
                .message("hello")
                .chatRoomIdx(31337L)
                .userIdx(2L)
                .createDate(System.currentTimeMillis())
                .build();

        ListOperations<String, Object> listOperations = redisTemplate.opsForList();
        listOperations.rightPush(RedisUtils.getChatRoomKey(chatMessage.getChatRoomIdx()), chatMessage);
        System.out.println(listOperations.range(RedisUtils.getChatRoomKey(chatMessage.getChatRoomIdx()), 0, -1));
    }

//    @Test
    void insertDummyDataToRedis() {
        ChatMessage chatMessage1 = ChatMessage.builder()
                .message("hello")
                .chatRoomIdx(31337L)
                .userIdx(1L)
                .createDate(System.currentTimeMillis())
                .build();

        ChatMessage chatMessage2 = ChatMessage.builder()
                .message("hello")
                .chatRoomIdx(31337L)
                .userIdx(2L)
                .createDate(System.currentTimeMillis())
                .build();

        Random r = new Random();
        List<ChatMessage> tmp = new ArrayList<>();
        for (int i = 0; i <= 1000000; i++) {
            int flag = r.nextInt(2);
            if (flag == 0) {
                tmp.add(chatMessage1);
            }
            else {
                tmp.add(chatMessage2);
            }
        }

        System.out.println("dummy data created. from now on insert...");
        ListOperations<String, Object> listOperations = redisTemplate.opsForList();
//        for (int i = 0; i <= 31; i++) {
//            for (ChatMessage chatMessage : tmp) {
//                listOperations.rightPush(RedisUtils.getChatRoomKey(2L), chatMessage);
//            }
//            System.out.println("done... " + i);
//        }
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<ChatMessage> valueSerializer = new Jackson2JsonRedisSerializer<>(ChatMessage.class);
        for (int i = 0; i < 3; i++) {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (ChatMessage chatMessage : tmp) {
                    connection.rPush(Objects.requireNonNull(keySerializer.serialize("CHATROOM2")),
                            valueSerializer.serialize(chatMessage));
                }

                return null;
            });
        }
    }
}
