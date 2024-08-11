package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.ReceivingBookingDto;
import ru.practicum.shareit.booking.dto.ReturnBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookingMapperTest {

    private BookingMapper bookingMapper;
    private Booking booking;
    private ReceivingBookingDto receivingBookingDto;
    private User user;
    private Item item;

    @BeforeEach
    public void setUp() {
        bookingMapper = new BookingMapper();
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();
        item = Item.builder()
                .id(1L)
                .name("Item Name")
                .description("Item Description")
                .available(true)
                .owner(user)
                .build();
        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        receivingBookingDto = ReceivingBookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    public void testToReturnBookingDto() {
        ReturnBookingDto returnBookingDto = bookingMapper.toReturnBookingDto(booking);

        assertEquals(booking.getId(), returnBookingDto.getId());
        assertEquals(booking.getStart(), returnBookingDto.getStart());
        assertEquals(booking.getEnd(), returnBookingDto.getEnd());
        assertEquals(booking.getItem(), returnBookingDto.getItem());
        assertEquals(booking.getBooker(), returnBookingDto.getBooker());
        assertEquals(booking.getStatus(), returnBookingDto.getStatus());
    }

    @Test
    public void testToBooking() {
        Booking mappedBooking = bookingMapper.toBooking(receivingBookingDto, user, item, BookingStatus.APPROVED);

        assertEquals(receivingBookingDto.getId(), mappedBooking.getId());
        assertEquals(receivingBookingDto.getStart(), mappedBooking.getStart());
        assertEquals(receivingBookingDto.getEnd(), mappedBooking.getEnd());
        assertEquals(item, mappedBooking.getItem());
        assertEquals(user, mappedBooking.getBooker());
        assertEquals(BookingStatus.APPROVED, mappedBooking.getStatus());
    }

    @Test
    public void testToBookingDtoForItem() {
        BookingDtoForItem bookingDtoForItem = bookingMapper.toBookingDtoForItem(booking);

        assertEquals(booking.getId(), bookingDtoForItem.getId());
        assertEquals(booking.getStart(), bookingDtoForItem.getStart());
        assertEquals(booking.getEnd(), bookingDtoForItem.getEnd());
        assertEquals(booking.getItem().getId(), bookingDtoForItem.getItemId());
        assertEquals(booking.getBooker().getId(), bookingDtoForItem.getBookerId());
        assertEquals(booking.getStatus(), bookingDtoForItem.getStatus());
    }
}
