package io.github.ringle.chatgpt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.ringle.chatgpt.dto.ChatRequest;
import io.github.ringle.chatgpt.dto.ChatResponse;
import io.github.ringle.chatgpt.dto.chat.MultiChatMessage;
import io.github.ringle.chatgpt.dto.chat.MultiChatRequest;
import io.github.ringle.chatgpt.dto.chat.MultiChatResponse;
import io.github.ringle.chatgpt.dto.image.ImageFormat;
import io.github.ringle.chatgpt.dto.image.ImageRequest;
import io.github.ringle.chatgpt.dto.image.ImageResponse;
import io.github.ringle.chatgpt.dto.image.ImageSize;
import java.util.List;
import reactor.core.publisher.Flux;

public interface ChatgptService {

    String sendMessage(String message);

    ChatResponse sendChatRequest(ChatRequest request);

    String multiChat(List<MultiChatMessage> messages);

    Flux<String> consumeServerSentEvent(List<MultiChatMessage> messages) throws JsonProcessingException;

    MultiChatResponse multiChatRequest(MultiChatRequest multiChatRequest);

    /**
     * @param prompt A text description of the desired image(s). The maximum length is 1000 characters.
     * @return generated image url
     */
    String imageGenerate(String prompt);

    /**
     * @param prompt A text description of the desired image(s). The maximum length is 1000 characters.
     * @param n      The number of images to generate. Must be between 1 and 10.
     * @param size   The size of the generated images. Must be one of ImageFormat.SMALL("256x256"), ImageFormat.MEDIUM("512x512"), ImageFormat.LARGE("1024x1024").
     * @param format The format in which the generated images are returned. Must be one of ImageFormat.URL("url"), ImageFormat.BASE64("b64_json").
     * @return image url/base64 list
     */
    List<String> imageGenerate(String prompt, Integer n, ImageSize size, ImageFormat format);

    ImageResponse imageGenerateRequest(ImageRequest imageRequest);

}
