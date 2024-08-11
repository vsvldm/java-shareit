package ru.practicum.shareit.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.ReceivingBookingDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> create(long userId, ReceivingBookingDto bookingDto) {
        return post("", userId, bookingDto);
    }

    public ResponseEntity<Object> statusUpdate(long userId, long bookingId, boolean approved) {
        Map<String, Object> parameters = Map.of(
                "bookingId", bookingId,
                "approved", approved
        );
        return patch("/{bookingId}?approved={approved}", userId, parameters, null);
    }

    public ResponseEntity<Object> findById(long userId, long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> findAllByBookerId(long bookerId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = new HashMap<>();
        StringBuilder url = new StringBuilder("");

        if (state != null) {
            url.append("?state={state}");
            parameters.put("state", state);
        }

        if (from != null) {
            if(url.length() > 0) {
                url.append("&from={from}");
            } else {
                url.append("?from={from}");
            }
            parameters.put("from", from);
        }

        if (size != null) {
            url.append("&size={size}");
            parameters.put("size", size);
        }
        return get(url.toString(), bookerId, parameters);
    }


    public ResponseEntity<Object> findAllByOwnerId(long ownerId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("state", state.name());
        if (from != null) {
            parameters.put("from", from);
        }
        if (size != null) {
            parameters.put("size", size);
        }

        StringBuilder url = new StringBuilder("/?state={state}");
        if (from != null) {
            url.append("&from={from}");
        }
        if (size != null) {
            url.append("&size={size}");
        }

        return get(url.toString(), ownerId, parameters);
    }
}
