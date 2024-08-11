package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.HashMap;
import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> create(long userId, ItemDto itemDto) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> update(long userId, long itemId, ItemDto itemDto) {
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> findById(long userId, long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> findByOwner(long userId, Integer from, Integer size) {
        Map<String, Object> parameters = new HashMap<>();
        if (from != null) {
            parameters.put("from", from);
        }
        if (size != null) {
            parameters.put("size", size);
        }

        StringBuilder url = new StringBuilder("");
        if (from != null) {
            url.append("?from={from}");
        }
        if (size != null) {
            if (url.length() > 0) {
                url.append("&size={size}");
            } else {
                url.append("?size={size}");
            }
        }

        return get(url.toString(), userId, parameters);
    }



    public ResponseEntity<Object> search(long userId, String text, Integer from, Integer size) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("text", text);
        if (from != null) {
            parameters.put("from", from);
        }
        if (size != null) {
            parameters.put("size", size);
        }

        StringBuilder url = new StringBuilder("/search?text={text}");
        if (from != null) {
            url.append("&from={from}");
        }
        if (size != null) {
            url.append("&size={size}");
        }

        return get(url.toString(), userId, parameters);
    }



    public ResponseEntity<Object> createComment(long userId, long itemId, CommentDto commentDto) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }
}
