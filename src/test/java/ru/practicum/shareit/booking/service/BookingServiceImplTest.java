package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.ReceivingBookingDto;
import ru.practicum.shareit.booking.dto.ReturnBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
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
            .item(item)
            .booker(user)
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
    void statusUpdate() {
    }

    @Test
    void statusUpdate_whenInvokedWithUnknownUser_thenReturnNotFoundException() {
    }

    @Test
    void statusUpdate_whenInvokedWithUnknownBooking_thenReturnNotFoundException() {
    }

    @Test
    void statusUpdate_whenUserIsNotOwner_thenReturnNotFoundException() {
    }

    @Test
    void statusUpdate_whenBookingIsAlreadyApproved_thenReturnBadRequestException() {
    }

    @Test
    void statusUpdate_whenBookingStatusRejected_then() {
    }

    @Test
    void statusUpdate() {
    }

    @Test
    void findById() {
    }

    @Test
    void findById() {
    }

    @Test
    void findById() {
    }

    @Test
    void findById() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByBookerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }

    @Test
    void findAllByOwnerId() {
    }
}