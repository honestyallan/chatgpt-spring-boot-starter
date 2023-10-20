package io.github.ringle.chatgpt.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.github.ringle.chatgpt.dto.ChatRequest;
import io.github.ringle.chatgpt.dto.ChatResponse;
import io.github.ringle.chatgpt.dto.chat.MultiChatMessage;
import io.github.ringle.chatgpt.dto.chat.MultiChatRequest;
import io.github.ringle.chatgpt.dto.chat.MultiChatResponse;
import io.github.ringle.chatgpt.dto.image.ImageFormat;
import io.github.ringle.chatgpt.dto.image.ImageRequest;
import io.github.ringle.chatgpt.dto.image.ImageResponse;
import io.github.ringle.chatgpt.dto.image.ImageSize;
import io.github.ringle.chatgpt.exception.ChatgptException;
import io.github.ringle.chatgpt.property.ChatgptProperties;
import io.github.ringle.chatgpt.service.ChatgptService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;


@Slf4j
@Service
public class DefaultChatgptService implements ChatgptService {

  protected final ChatgptProperties chatgptProperties;

  private final String AUTHORIZATION;

  private final RestTemplate restTemplate = new RestTemplate();

  private WebClient client;

  public DefaultChatgptService(ChatgptProperties chatgptProperties) {
    this.chatgptProperties = chatgptProperties;
    AUTHORIZATION = "Bearer " + chatgptProperties.getApiKey();
  }

  @Override
  public String sendMessage(String message) {
    ChatRequest chatRequest = new ChatRequest(chatgptProperties.getModel(), message, chatgptProperties.getMaxTokens(),
        chatgptProperties.getTemperature(), chatgptProperties.getTopP());
    ChatResponse chatResponse = this.getResponse(this.buildHttpEntity(chatRequest), ChatResponse.class,
        chatgptProperties.getUrl());
    try {
      return chatResponse.getChoices().get(0).getText();
    } catch (Exception e) {
      log.error("parse chatgpt message error", e);
      throw e;
    }
  }

  @Override
  public ChatResponse sendChatRequest(ChatRequest chatRequest) {
    return this.getResponse(this.buildHttpEntity(chatRequest), ChatResponse.class, chatgptProperties.getUrl());
  }

  @Override
  public String multiChat(List<MultiChatMessage> messages) {
    MultiChatRequest multiChatRequest = new MultiChatRequest(chatgptProperties.getMulti().getModel(), messages,
        chatgptProperties.getMulti().getMaxTokens(), chatgptProperties.getMulti().getTemperature(),
        chatgptProperties.getMulti().getTopP());
    MultiChatResponse multiChatResponse = this.getResponse(this.buildHttpEntity(multiChatRequest),
        MultiChatResponse.class, chatgptProperties.getMulti().getUrl());
    try {
      return multiChatResponse.getChoices().get(0).getMessage().getContent();
    } catch (Exception e) {
      log.error("parse chatgpt message error", e);
      throw e;
    }
  }

  @PostConstruct
  public void init() {
    client = WebClient.builder()
                      .baseUrl(chatgptProperties.getMulti().getUrl())
                      .defaultHeader("Authorization", AUTHORIZATION)
                      .build();
  }

  private final ObjectMapper objectMapper = new ObjectMapper().configure(
                                                                  DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                                              .setPropertyNamingStrategy(
                                                                  PropertyNamingStrategies.SNAKE_CASE);

  /**
   * @param messages
   * @description: 流式对接
   * @author: chenli
   * @return: void
   * @date: 2023/3/27 16:44
   */
  @Override
  public Flux<String> consumeServerSentEvent(List<MultiChatMessage> messages) throws JsonProcessingException {
    ParameterizedTypeReference<String> type = new ParameterizedTypeReference<String>() {
    };
    MultiChatRequest multiChatRequest = new MultiChatRequest(chatgptProperties.getMulti().getModel(), messages,
        chatgptProperties.getMulti().getMaxTokens(), chatgptProperties.getMulti().getTemperature(),
        chatgptProperties.getMulti().getTopP(), chatgptProperties.getMulti().getStream());

    String requestValue = objectMapper.writeValueAsString(multiChatRequest);
    return client.post()
                 .contentType(MediaType.APPLICATION_JSON)
                 .bodyValue(requestValue)
                 .accept(MediaType.TEXT_EVENT_STREAM)
                 .retrieve()
                 .bodyToFlux(type)
                 .mapNotNull(event -> {
                   try {
                       return objectMapper.readValue(event, MultiChatResponse.class);
                   } catch (JsonProcessingException | StringIndexOutOfBoundsException e) {
                     return null;
                   }
                 })
                 .filter(event -> !ObjectUtils.isEmpty(event.getChoices().get(0).getDelta().getContent()))
                 .skipUntil(event -> !event.getChoices().get(0).getDelta().getContent().equals("\n"))
                 .map(event -> event.getChoices().get(0).getDelta().getContent());
  }

  @Override
  public MultiChatResponse multiChatRequest(MultiChatRequest multiChatRequest) {
    return this.getResponse(this.buildHttpEntity(multiChatRequest), MultiChatResponse.class,
        chatgptProperties.getMulti().getUrl());
  }

  @Override
  public String imageGenerate(String prompt) {
    ImageRequest imageRequest = new ImageRequest(prompt, null, null, null, null);
    ImageResponse imageResponse = this.getResponse(this.buildHttpEntity(imageRequest), ImageResponse.class,
        chatgptProperties.getImage().getUrl());
    try {
      return imageResponse.getData().get(0).getUrl();
    } catch (Exception e) {
      log.error("parse image url error", e);
      throw e;
    }
  }

  @Override
  public List<String> imageGenerate(String prompt, Integer n, ImageSize size, ImageFormat format) {
    ImageRequest imageRequest = new ImageRequest(prompt, n, size.getSize(), format.getFormat(), null);
    ImageResponse imageResponse = this.getResponse(this.buildHttpEntity(imageRequest), ImageResponse.class,
        chatgptProperties.getImage().getUrl());
    try {
      List<String> list = new ArrayList<>();
      imageResponse.getData().forEach(imageData -> {
        if (format.equals(ImageFormat.URL)) {
          list.add(imageData.getUrl());
        } else {
          list.add(imageData.getB64Json());
        }
      });
      return list;
    } catch (Exception e) {
      log.error("parse image url error", e);
      throw e;
    }
  }

  @Override
  public ImageResponse imageGenerateRequest(ImageRequest imageRequest) {
    return this.getResponse(this.buildHttpEntity(imageRequest), ImageResponse.class,
        chatgptProperties.getImage().getUrl());
  }

  protected <T> HttpEntity<?> buildHttpEntity(T request) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
    headers.add("Authorization", AUTHORIZATION);
    return new HttpEntity<>(request, headers);
  }

  protected <T> T getResponse(HttpEntity<?> httpEntity, Class<T> responseType, String url) {
    log.info("request url: {}, httpEntity: {}", url, httpEntity);
    ResponseEntity<T> responseEntity = restTemplate.postForEntity(url, httpEntity, responseType);
    if (responseEntity.getStatusCode().isError()) {
      log.error("error response status: {}", responseEntity);
      throw new ChatgptException("error response status :" + responseEntity.getStatusCode().value());
    } else {
      log.info("response: {}", responseEntity);
    }
    return responseEntity.getBody();
  }

}
