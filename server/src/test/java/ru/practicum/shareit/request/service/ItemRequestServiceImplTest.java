package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.practicum.shareit.exception.exception.BadRequestException;
import ru.practicum.shareit.exception.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private ItemRequestMapper requestMapper;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private ItemRequestServiceImpl requestService;
    private final long userId = 1L;
    private final long requestId = 1L;
    private final User requestor = User.builder()
            .id(userId)
            .name("RequestorName")
            .email("requestorEmail@email.com")
            .build();
    private final ItemRequestDto requestDto = ItemRequestDto.builder()
            .id(1L)
            .description("Description")
            .items(List.of())
            .created(LocalDateTime.now().minusDays(1))
            .build();
    private final ItemRequest request = ItemRequest.builder()
            .id(1L)
            .description("Description")
            .requestor(requestor)
            .created(requestDto.getCreated())
            .build();
    private final List<ItemRequest> requests = Collections.singletonList(request);

    @Test
    void create_whenNormallyInvoked_thenReturnItemRequestDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(requestMapper.toItemRequest(requestDto, requestor)).thenReturn(request);
        when(requestRepository.save(request)).thenReturn(request);
        when(requestMapper.toItemRequestDto(request)).thenReturn(requestDto);

        ItemRequestDto actual = requestService.create(userId, requestDto);

        assertEquals(requestDto, actual);

        verify(userRepository, times(1)).findById(userId);
        verify(requestMapper, times(1)).toItemRequest(requestDto, requestor);
        verify(requestMapper, times(1)).toItemRequest(requestDto, requestor);
        verify(requestRepository, times(1)).save(request);
        verify(requestMapper, times(1)).toItemRequestDto(request);
    }

    @Test
    void create_whenCreatedWithUnknownRequestor_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.create(userId, requestDto));

        verify(userRepository, times(1)).findById(userId);
        verify(requestRepository, never()).save(request);
        verify(requestMapper, never()).toItemRequestDto(request);
        verify(requestMapper, never()).toItemRequest(requestDto, requestor);
    }


    @Test
    void findByUser_whenNormallyInvoked_thenReturnListItemRequestDto() {
        List<ItemRequest> requests = Collections.singletonList(request);

        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(requestRepository.findAllByRequestorOrderByCreatedDesc(requestor)).thenReturn(requests);
        when(requestMapper.toItemRequestDto(request)).thenReturn(requestDto);

        List<ItemRequestDto> actual = requestService.findByUser(userId);

        assertEquals(1, actual.size());
        assertEquals(requestDto, actual.get(0));

        verify(userRepository, times(1)).findById(userId);
        verify(requestRepository, times(1)).findAllByRequestorOrderByCreatedDesc(requestor);
        verify(requestMapper, times(1)).toItemRequestDto(request);
    }

    @Test
    void findByUser_whenFindWithUnknownRequestor_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.findByUser(userId));

        verify(userRepository, times(1)).findById(userId);
        verify(requestRepository, never()).findAllByRequestorOrderByCreatedDesc(requestor);
        verify(requestMapper, never()).toItemRequestDto(request);
    }

    @Test
    void findAll_whenNormallyInvokedWithAllParam_thenReturnListItemRequestDto() {
        long userIdForPageable = 2L;
        Integer from = 0;
        Integer size = 10;
        User user = User.builder()
                .id(2L)
                .build();

        when(userRepository.findById(userIdForPageable)).thenReturn(Optional.of(user));
        when(requestRepository.findAll(PageRequest.of(from / size, size, Sort.by("created"))))
                .thenReturn(new PageImpl<>(requests));
        when(requestMapper.toItemRequestDto(request)).thenReturn(requestDto);

        List<ItemRequestDto> actual = requestService.findAll(userIdForPageable, from, size);

        assertEquals(1, actual.size());
        assertEquals(requestDto, actual.get(0));

        verify(userRepository, times(1)).findById(userIdForPageable);
        verify(requestRepository, times(1)).findAll(PageRequest.of(from / size, size, Sort.by("created")));
        verify(requestMapper, times(1)).toItemRequestDto(request);
    }

    @Test
    void findAll_whenInvokedWithoutFromParam_thenReturnEmptyList() {
        assertEquals(Collections.emptyList(), requestService.findAll(userId, null, 10));

        verify(userRepository, never()).findById(userId);
    }

    @Test
    void findAll_whenInvokedWithoutSizeParam_thenReturnEmptyList() {
        assertEquals(Collections.emptyList(), requestService.findAll(userId, 0, null));

        verify(userRepository, never()).findById(userId);
    }

    @Test
    void findAll_whenInvokedWithFailFromParam_thenReturnBadRequestException() {
        assertThrows(BadRequestException.class, () -> requestService.findAll(userId, -1, 10));

        verify(userRepository, never()).findById(userId);
    }

    @Test
    void findAll_whenInvokedWithFailSizeParam_thenReturnBadRequestException() {
        assertThrows(BadRequestException.class, () -> requestService.findAll(userId, 0, 0));

        verify(userRepository, never()).findById(userId);
    }

    @Test
    void findAll_whenInvokedWithUnknownUser_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.findAll(userId, 0, 10));

        verify(userRepository, times(1)).findById(userId);
        verify(requestMapper, never()).toItemRequestDto(request);
    }

    @Test
    void findById_whenNormallyInvoked_thenReturnItemRequestDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(requestMapper.toItemRequestDto(request)).thenReturn(requestDto);

        ItemRequestDto actual = requestService.findById(userId, requestId);

        assertEquals(requestDto, actual);

        verify(userRepository, times(1)).findById(userId);
        verify(requestRepository, times(1)).findById(requestId);
    }

    @Test
    void findById_whenInvokedWithUnknownUser_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.findById(userId, requestId));

        verify(userRepository, times(1)).findById(userId);
        verify(requestRepository, never()).findById(requestId);
    }

    @Test
    void findById_whenInvokedWithUnknownItemRequest_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.findById(userId, requestId));

        verify(userRepository, times(1)).findById(userId);
        verify(requestRepository, times(1)).findById(requestId);
    }
}