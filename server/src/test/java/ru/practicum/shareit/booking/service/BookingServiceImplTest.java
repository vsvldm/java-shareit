package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.ReceivingBookingDto;
import ru.practicum.shareit.booking.dto.ReturnBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.exception.BadRequestException;
import ru.practicum.shareit.exception.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingMapper bookingMapper;
    @InjectMocks
    private BookingServiceImpl bookingService;
    private final long userId = 1L;
    private final long itemId = 1L;
    private final long bookingId = 1L;
    private final Integer from = 0;
    private final Integer size = 10;
    private final User user = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@email.com")
            .build();
    private final Item item = Item.builder()
            .id(1L)
            .name("ItemName")
            .description("ItemDescription")
            .available(true)
            .owner(user)
            .request(ItemRequest.builder().build())
            .build();
    private final ReceivingBookingDto receivingBookingDto = ReceivingBookingDto.builder()
            .id(1L)
            .itemId(item.getId())
            .start(LocalDateTime.now().plusDays(1))
            .end(LocalDateTime.now().plusDays(7))
            .build();
    private final Booking booking = Booking.builder()
            .id(1L)
            .start(LocalDateTime.now().minusDays(7))
            .end(LocalDateTime.now().minusDays(1))
            .item(item)
            .booker(user)
            .status(BookingStatus.WAITING)
            .build();
    private final ReturnBookingDto returnBookingDto = ReturnBookingDto.builder()
            .id(booking.getId())
            .start(booking.getStart())
            .end((booking.getEnd()))
            .item(booking.getItem())
            .booker(booking.getBooker())
            .status(booking.getStatus())
            .build();

    @Test
    void create_whenNormallyInvoked_thenReturnBookingDto() {
        User userForTest = User.builder()
                .id(2L)
                .name("NameForBooker")
                .email("booker@email.com")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(userForTest));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingMapper.toBooking(receivingBookingDto, userForTest, item, BookingStatus.WAITING)). thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toReturnBookingDto(booking)).thenReturn(returnBookingDto);

        ReturnBookingDto actual = bookingService.create(userId, receivingBookingDto);

        assertEquals(returnBookingDto, actual);

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingMapper, times(1)).toBooking(receivingBookingDto, userForTest, item, BookingStatus.WAITING);
        verify(bookingRepository, times(1)).save(any());
        verify(bookingMapper, times(1)).toReturnBookingDto(any());
    }

    @Test
    void create_whenStartAfterEnd_thenReturnBadRequestException() {
        ReceivingBookingDto receivingBookingDtoForTest = ReceivingBookingDto.builder()
                .id(1L)
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(7))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        assertThrows(BadRequestException.class, () -> bookingService.create(userId, receivingBookingDtoForTest));

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void create_whenInvokedWithUnknownUser_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(userId, receivingBookingDto));

        verify(itemRepository, never()).findById(anyLong());
    }

    @Test
    void create_whenInvokedWithUnknownItem_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(userId, receivingBookingDto));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_whenBookerIsOwner_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.create(userId, receivingBookingDto));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_whenItemIsNotAvailable_thenReturnBadRequestException() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(User.builder()
                        .id(2L)
                        .name("Name")
                        .email("email@email.cop")
                        .build()));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(Item.builder()
                        .id(1L)
                        .name("ItemName")
                        .description("ItemDescription")
                        .available(false)
                        .owner(user)
                        .request(ItemRequest.builder().build())
                        .build()));

        assertThrows(BadRequestException.class, () -> bookingService.create(2L, receivingBookingDto));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void statusUpdate_whenInvokedWithUnknownUser_thenReturnBadRequestException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> bookingService.statusUpdate(userId, bookingId, true));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void statusUpdate_whenInvokedWithUnknownBooking_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.statusUpdate(userId, bookingId, true));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void statusUpdate_whenUserIsNotOwner_thenReturnNotFoundException() {
        User notOwner = User.builder()
                .id(2L)
                .name("NameForOwner")
                .email("Owner@email.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(notOwner));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.statusUpdate(userId, bookingId, true));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void statusUpdate_whenBookingIsAlreadyApproved_thenReturnBadRequestException() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(7))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(bookingForTest));

        assertThrows(BadRequestException.class, () -> bookingService.statusUpdate(userId, bookingId, true));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void statusUpdate_whenBookingStatusRejected_thenReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(7))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.REJECTED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(item)
                .booker(user)
                .status(bookingForTest.getStatus())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(bookingForTest);
        when(bookingMapper.toReturnBookingDto(booking)).thenReturn(returnBookingDtoForTest);

        ReturnBookingDto actual = bookingService.statusUpdate(userId, bookingId, false);

        assertEquals(returnBookingDtoForTest,actual);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any());
        verify(bookingMapper, times(1)).toReturnBookingDto(booking);
    }

    @Test
    void statusUpdate_whenBookingStatusApproved_thenReturnBookingDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toReturnBookingDto(booking)).thenReturn(returnBookingDto);

        ReturnBookingDto actual = bookingService.statusUpdate(userId, bookingId, true);

        assertEquals(returnBookingDto,actual);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any());
        verify(bookingMapper, times(1)).toReturnBookingDto(booking);
    }

    @Test
    void findById_whenInvokedWithUnknownUser_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.findById(userId, bookingId));

        verify(bookingRepository, never()).findById(bookingId);
    }

    @Test
    void findById_whenInvokedWithUnknownBooking_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.findById(userId, bookingId));

        verify(bookingMapper, never()).toReturnBookingDto(booking);
    }

    @Test
    void findById_whenUserDoesHaveNotAccess_thenReturnNotFoundException() {
        User notOwner = User.builder()
                .id(2L)
                .name("NameForOwner")
                .email("Owner@email.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(notOwner));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.findById(userId, bookingId));

        verify(bookingMapper, never()).toReturnBookingDto(booking);
    }

    @Test
    void findById_whenNormallyInvoked_thenReturnBookingDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toReturnBookingDto(booking)).thenReturn(returnBookingDto);

        ReturnBookingDto actual = bookingService.findById(userId, bookingId);

        assertEquals(returnBookingDto, actual);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingMapper, times(1)).toReturnBookingDto(booking);
    }

    @Test
    void findAllByBookerId_whenInvokedWithUnknownBooker_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.findAllByBookerId(userId, null, null, null));

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any(), any());
    }

    @Test
    void findAllByBookerId_whenInvokedWithAllStateAndWithoutPagination_thenReturnListWithALLReturnBookingDto() {
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDto);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerOrderByStartDesc(user)).thenReturn(Collections.singletonList(booking));
        when(bookingMapper.toReturnBookingDto(booking)).thenReturn(returnBookingDto);

        List<ReturnBookingDto> actual = bookingService.findAllByBookerId(userId, BookingState.ALL, null, null);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any(), any());
    }

    @Test
    void findAllByBookerId_whenInvokedWithAllStateAndWithPagination_thenReturnListWithALLReturnBookingDto() {
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDto);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerOrderByStartDesc(any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(booking)));
        when(bookingMapper.toReturnBookingDto(booking)).thenReturn(returnBookingDto);

        List<ReturnBookingDto> actual = bookingService.findAllByBookerId(userId, BookingState.ALL, from, size);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any());
    }

    @Test
    void findAllByBookerId_whenInvokedWithCurrentStateAndWithoutPagination_thenReturnListWithCurrentReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(7))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllCurrentByBooker(user)).thenReturn(Collections.singletonList(bookingForTest));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByBookerId(userId, BookingState.CURRENT, null, null);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any(), any());
    }

    @Test
    void findAllByBookerId_whenInvokedWithCurrentStateAndWithPagination_thenReturnListWithCurrentReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(7))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllCurrentByBooker(any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(bookingForTest)));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByBookerId(userId, BookingState.CURRENT, from, size);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any());
    }

    @Test
    void findAllByBookerId_whenInvokedWithPastStateAndWithoutPagination_thenReturnListWithPastReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(7))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerAndEndBeforeOrderByStartDesc(any(), any()))
                .thenReturn(Collections.singletonList(bookingForTest));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByBookerId(userId, BookingState.PAST, null, null);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any(), any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any(), any());
    }

    @Test
    void findAllByBookerId_whenInvokedWithPastStateAndWithPagination_thenReturnListWithPastReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(7))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerAndEndBeforeOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(bookingForTest)));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByBookerId(userId, BookingState.PAST, from, size);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any(), any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any());
    }

    @Test
    void findAllByBookerId_whenInvokedWithFutureStateAndWithoutPagination_thenReturnListWithFutureReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(10))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerAndStartAfterOrderByStartDesc(any(), any()))
                .thenReturn(Collections.singletonList(bookingForTest));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByBookerId(userId, BookingState.FUTURE, null, null);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any(), any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any(), any());
        verify(bookingRepository, never()).findAllByBookerAndStartAfterOrderByStartDesc(any(),any(), any());
    }

    @Test
    void findAllByBookerId_whenInvokedWithFutureStateAndWithPagination_thenReturnListWithFutureReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(10))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerAndStartAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(bookingForTest)));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByBookerId(userId, BookingState.FUTURE, from, size);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any(), any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any(), any());
        verify(bookingRepository, never()).findAllByBookerAndStartAfterOrderByStartDesc(any(),any());
    }

    @Test
    void findAllByBookerId_whenInvokedWithWaitingStateAndWithoutPagination_thenReturnListWithWaitingReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(10))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerAndStatusOrderByStartDesc(user, BookingStatus.WAITING))
                .thenReturn(Collections.singletonList(bookingForTest));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByBookerId(userId, BookingState.WAITING, null, null);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any(), any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any(), any());
        verify(bookingRepository, never()).findAllByBookerAndStartAfterOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndStartAfterOrderByStartDesc(any(),any(), any());
        verify(bookingRepository, never()).findAllByBookerAndStatusOrderByStartDesc(any(),any(), any());
    }

    @Test
    void findAllByBookerId_whenInvokedWithWaitingStateAndWithPagination_thenReturnListWithWaitingReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(10))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerAndStatusOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(bookingForTest)));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByBookerId(userId, BookingState.WAITING, from, size);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any(), any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any(), any());
        verify(bookingRepository, never()).findAllByBookerAndStartAfterOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndStartAfterOrderByStartDesc(any(),any(), any());
        verify(bookingRepository, never()).findAllByBookerAndStatusOrderByStartDesc(any(),any());
    }

    @Test
    void findAllByBookerId_whenInvokedWithRejectedStateAndWithoutPagination_thenReturnListWithRejectedReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(10))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.REJECTED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerAndStatusOrderByStartDesc(user, BookingStatus.REJECTED))
                .thenReturn(Collections.singletonList(bookingForTest));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByBookerId(userId, BookingState.REJECTED, null, null);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any(), any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any(), any());
        verify(bookingRepository, never()).findAllByBookerAndStartAfterOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndStartAfterOrderByStartDesc(any(),any(), any());
        verify(bookingRepository, never()).findAllByBookerAndStatusOrderByStartDesc(user,BookingStatus.WAITING);
        verify(bookingRepository, never()).findAllByBookerAndStatusOrderByStartDesc(any(),any(), any());
    }

    @Test
    void findAllByBookerId_whenInvokedWithRejectedStateAndWithPagination_thenReturnListWithRejectedReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(10))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.REJECTED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerAndStatusOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(bookingForTest)));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByBookerId(userId, BookingState.REJECTED, from, size);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any(), any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any(), any());
        verify(bookingRepository, never()).findAllByBookerAndStartAfterOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndStartAfterOrderByStartDesc(any(),any(), any());
        verify(bookingRepository, never()).findAllByBookerAndStatusOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndStatusOrderByStartDesc(user,BookingStatus.WAITING, PageRequest.of(from / size, size));
    }

    @Test
    void findAllByBookerId_whenInvokedWithUnknownState_thenReturnBadRequestException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> bookingService.findAllByBookerId(userId, BookingState.UNSUPPORTED_STATUS, null, null));

        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByBookerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any());
        verify(bookingRepository, never()).findAllCurrentByBooker(any(), any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndEndBeforeOrderByStartDesc(any(),any(), any());
        verify(bookingRepository, never()).findAllByBookerAndStartAfterOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndStartAfterOrderByStartDesc(any(),any(), any());
        verify(bookingRepository, never()).findAllByBookerAndStatusOrderByStartDesc(any(),any());
        verify(bookingRepository, never()).findAllByBookerAndStatusOrderByStartDesc(any(),any(), any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithUnknownOwner_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.findAllByBookerId(userId, null, null, null));

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithUserWithoutItems_thenReturnNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class, () -> bookingService.findAllByOwnerId(userId, null, null, null));

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithAllStateAndWithoutPagination_thenReturnListWithALLReturnBookingDto() {
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDto);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findAllByItemOwnerOrderByStartDesc(user)).thenReturn(Collections.singletonList(booking));
        when(bookingMapper.toReturnBookingDto(booking)).thenReturn(returnBookingDto);

        List<ReturnBookingDto> actual = bookingService.findAllByOwnerId(userId, BookingState.ALL, null, null);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithAllStateAndWithPagination_thenReturnListWithALLReturnBookingDto() {
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDto);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findAllByItemOwnerOrderByStartDesc(any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(booking)));
        when(bookingMapper.toReturnBookingDto(booking)).thenReturn(returnBookingDto);

        List<ReturnBookingDto> actual = bookingService.findAllByOwnerId(userId, BookingState.ALL, from, size);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithCurrentStateAndWithoutPagination_thenReturnListWithCurrentReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(7))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findAllCurrentByOwner(user)).thenReturn(Collections.singletonList(bookingForTest));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByOwnerId(userId, BookingState.CURRENT, null, null);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any(), any());

    }

    @Test
    void findAllByOwnerId_whenInvokedWithCurrentStateAndWithPagination_thenReturnListWithCurrentReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(7))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findAllCurrentByOwner(any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(bookingForTest)));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByOwnerId(userId, BookingState.CURRENT, from, size);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithPastStateAndWithoutPagination_thenReturnListWithPastReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(7))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any()))
                .thenReturn(Collections.singletonList(bookingForTest));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByOwnerId(userId, BookingState.PAST, null, null);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any(), any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithPastStateAndWithPagination_thenReturnListWithPastReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(7))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any(),any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(bookingForTest)));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByOwnerId(userId, BookingState.PAST, from, size);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithFutureStateAndWithoutPagination_thenReturnListWithFutureReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(10))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findAllByItemOwnerAndStartAfterOrderByStartDesc(any(), any()))
                .thenReturn(Collections.singletonList(bookingForTest));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByOwnerId(userId, BookingState.FUTURE, null, null);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStartAfterOrderByStartDesc(any(), any(), any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithFutureStateAndWithPagination_thenReturnListWithFutureReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(10))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findAllByItemOwnerAndStartAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(bookingForTest)));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByOwnerId(userId, BookingState.FUTURE, from, size);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStartAfterOrderByStartDesc(any(), any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithWaitingStateAndWithoutPagination_thenReturnListWithWaitingReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(10))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(user, BookingStatus.WAITING))
                .thenReturn(Collections.singletonList(bookingForTest));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByOwnerId(userId, BookingState.WAITING, null, null);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStartAfterOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStatusOrderByStartDesc(any(), any(), any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithWaitingStateAndWithPagination_thenReturnListWithWaitingReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(10))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(bookingForTest)));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByOwnerId(userId, BookingState.WAITING, from, size);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStartAfterOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStatusOrderByStartDesc(any(), any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithRejectedStateAndWithoutPagination_thenReturnListWithRejectedReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(10))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.REJECTED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(user, BookingStatus.REJECTED))
                .thenReturn(Collections.singletonList(bookingForTest));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByOwnerId(userId, BookingState.REJECTED, null, null);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStartAfterOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStatusOrderByStartDesc(any(), any(), any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithRejectedStateAndWithPagination_thenReturnListWithRejectedReturnBookingDto() {
        Booking bookingForTest = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(10))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.REJECTED)
                .build();
        ReturnBookingDto returnBookingDtoForTest = ReturnBookingDto.builder()
                .id(bookingForTest.getId())
                .start(bookingForTest.getStart())
                .end((bookingForTest.getEnd()))
                .item(bookingForTest.getItem())
                .booker(bookingForTest.getBooker())
                .status(bookingForTest.getStatus())
                .build();
        List<ReturnBookingDto> expected = Collections.singletonList(returnBookingDtoForTest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(bookingForTest)));
        when(bookingMapper.toReturnBookingDto(bookingForTest)).thenReturn(returnBookingDtoForTest);

        List<ReturnBookingDto> actual = bookingService.findAllByOwnerId(userId, BookingState.REJECTED, from, size);

        assertEquals(expected, actual);

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStartAfterOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStatusOrderByStartDesc(any(), any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithUnknownStateAndWithoutPagination_thenReturnBadRequestException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));

        assertThrows(BadRequestException.class, () -> bookingService.findAllByOwnerId(userId, BookingState.UNSUPPORTED_STATUS, null, null));

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStartAfterOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStatusOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStatusOrderByStartDesc(any(), any(), any());
    }

    @Test
    void findAllByOwnerId_whenInvokedWithAllStateAndWithIncorrectPagination_thenReturnBadRequestException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user)).thenReturn(Collections.singletonList(item));

        assertThrows(BadRequestException.class, () -> bookingService.findAllByOwnerId(userId, BookingState.ALL, -1, 0));

        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any());
        verify(bookingRepository, never()).findAllCurrentByOwner(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(), any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStartAfterOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStatusOrderByStartDesc(any(), any());
        verify(bookingRepository, never()).findAllByItemOwnerAndStatusOrderByStartDesc(any(), any(), any());
    }

}