package io.github.ringle.chatgpt.dto;

import lombok.Data;

/**
 * @className: Message
 * @description: 消息体
 * @author: chenli
 * @date: 2023/3/20 17:38
 * @copyright (C), 2018-2023
 */
@Data
public class Message {
  String role;
  String content;

}
