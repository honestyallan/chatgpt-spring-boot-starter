package io.github.ringle.chatgpt.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiResponseChoice {
    private MultiChatMessage message;
    private MultiChatMessage delta;

    @JsonProperty("finish_reason")
    private String finishReason;

    private Integer index;
}
