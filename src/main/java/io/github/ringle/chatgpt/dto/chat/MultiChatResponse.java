package io.github.ringle.chatgpt.dto.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.ringle.chatgpt.dto.Usage;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiChatResponse {
    private String id;
    private String object;
    @JsonIgnore
    private LocalDate created;
    private String model;
    private List<MultiResponseChoice> choices;
    private Usage usage;
}
