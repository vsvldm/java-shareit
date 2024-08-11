package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    private final User booker = User.builder()
            .name("UserName")
            .email("email@mail.com")
            .build();
    private final User owner = User.builder()
            .name("OwnerName")
            .email("email@gmail.com")
            .build();
    private final  User requestor = User.builder()
            .name("RequestorName")
            .email("requestor@email.ru")
            .build();
    private final ItemRequest request = ItemRequest.builder()
            .description("DescriptionTest1")
            .requestor(requestor)
            .created(LocalDateTime.now())
            .build();
    private final Item item = Item.builder()
            .name("ItemName")
            .description("ItemDescription")
            .available(true)
            .owner(owner)
            .request(request)
            .build();
    private final Booking currentBooking = Booking.builder()
            .start(LocalDateTime.now().minusDays(7))
            .end(LocalDateTime.now().plusDays(1))
            .item(item)
            .booker(booker)
            .status(BookingStatus.APPROVED)
            .build();
    private final Booking pastBooking = Booking.builder()
            .start(LocalDateTime.now().minusDays(7))
            .end(LocalDateTime.now().minusDays(1))
            .item(item)
            .booker(booker)
            .status(BookingStatus.REJECTED)
            .build();
    private final Booking futureBooking = Booking.builder()
            .start(LocalDateTime.now().plusDays(7))
            .end(LocalDateTime.now().plusDays(8))
            .item(item)
            .booker(booker)
            .status(BookingStatus.CANCELED)
            .build();
    private final Booking waitingBooking = Booking.builder()
            .start(LocalDateTime.now().plusDays(1))
            .end(LocalDateTime.now().plusDays(2))
            .item(item)
            .booker(booker)
            .status(BookingStatus.WAITING)
            .build();

    @BeforeEach
    void setUp() {
        userRepository.saveAll(List.of(booker, owner, requestor));
        itemRequestRepository.save(request);
        itemRepository.save(item);
        bookingRepository.saveAll(List.of(currentBooking, pastBooking, futureBooking, waitingBooking));
    }

    @Test
    void findAllByBookerOrderByStartDesc_whenInvokedWithoutPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(futureBooking, waitingBooking, currentBooking, pastBooking);
        Collection<Booking> actual = bookingRepository.findAllByBookerOrderByStartDesc(booker);

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void testFindAllByBookerOrderByStartDesc() {
        Collection<Booking> expected = List.of(futureBooking, waitingBooking, currentBooking, pastBooking);
        Collection<Booking> actual = bookingRepository.findAllByBookerOrderByStartDesc(booker, PageRequest.of(0, 10)).getContent();

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllCurrentByBooker_whenInvokedWithoutPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(currentBooking);
        Collection<Booking> actual = bookingRepository.findAllCurrentByBooker(booker);

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllCurrentByBooker_whenInvokedWithPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(currentBooking);
        Collection<Booking> actual = bookingRepository.findAllCurrentByBooker(booker, PageRequest.of(0, 10)).getContent();

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByBookerAndEndBeforeOrderByStartDesc_whenInvokedWithoutPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(pastBooking);
        Collection<Booking> actual = bookingRepository.findAllByBookerAndEndBeforeOrderByStartDesc(booker, LocalDateTime.now());

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByBookerAndEndBeforeOrderByStartDesc_whenInvokedWithPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(pastBooking);
        Collection<Booking> actual = bookingRepository.findAllByBookerAndEndBeforeOrderByStartDesc(booker, LocalDateTime.now(), PageRequest.of(0, 10)).getContent();

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByBookerAndStartAfterOrderByStartDesc_whenInvokedWithoutPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(futureBooking, waitingBooking);
        Collection<Booking> actual = bookingRepository.findAllByBookerAndStartAfterOrderByStartDesc(booker, LocalDateTime.now());

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByBookerAndStartAfterOrderByStartDesc_whenInvokedWithPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(futureBooking, waitingBooking);
        Collection<Booking> actual = bookingRepository.findAllByBookerAndStartAfterOrderByStartDesc(booker, LocalDateTime.now(), PageRequest.of(0, 10)).getContent();

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByBookerAndStatusOrderByStartDesc_whenInvokedWithoutPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(waitingBooking);
        Collection<Booking> actual = bookingRepository.findAllByBookerAndStatusOrderByStartDesc(booker, BookingStatus.WAITING);

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByBookerAndStatusOrderByStartDesc_whenInvokedWithPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(waitingBooking);
        Collection<Booking> actual = bookingRepository.findAllByBookerAndStatusOrderByStartDesc(booker, BookingStatus.WAITING, PageRequest.of(0, 10)).getContent();

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByItemOwnerOrderByStartDesc_whenInvokedWithoutPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(pastBooking);
        Collection<Booking> actual = bookingRepository.findAllByBookerAndStatusOrderByStartDesc(booker, BookingStatus.REJECTED);

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByItemOwnerOrderByStartDesc_whenInvokedWithPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(pastBooking);
        Collection<Booking> actual = bookingRepository.findAllByBookerAndStatusOrderByStartDesc(booker, BookingStatus.REJECTED, PageRequest.of(0, 10)).getContent();

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllCurrentByOwner_whenInvokedWithoutPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(currentBooking);
        Collection<Booking> actual = bookingRepository.findAllCurrentByOwner(owner);

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllCurrentByOwner_whenInvokedWithPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(currentBooking);
        Collection<Booking> actual = bookingRepository.findAllCurrentByOwner(owner, PageRequest.of(0, 10)).getContent();

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByItemOwnerAndEndBeforeOrderByStartDesc_whenInvokedWithoutPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(pastBooking);
        Collection<Booking> actual = bookingRepository.findAllByItemOwnerAndEndBeforeOrderByStartDesc(owner, LocalDateTime.now());

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByItemOwnerAndEndBeforeOrderByStartDesc_whenInvokedWithPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(pastBooking);
        Collection<Booking> actual = bookingRepository.findAllByItemOwnerAndEndBeforeOrderByStartDesc(owner, LocalDateTime.now(), PageRequest.of(0, 10)).getContent();

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByItemOwnerAndStartAfterOrderByStartDesc_whenInvokedWithoutPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(futureBooking, waitingBooking);
        Collection<Booking> actual = bookingRepository.findAllByItemOwnerAndStartAfterOrderByStartDesc(owner, LocalDateTime.now());

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByItemOwnerAndStartAfterOrderByStartDesc_whenInvokedWithPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(futureBooking, waitingBooking);
        Collection<Booking> actual = bookingRepository.findAllByItemOwnerAndStartAfterOrderByStartDesc(owner, LocalDateTime.now(), PageRequest.of(0, 10)).getContent();

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByItemOwnerAndStatusOrderByStartDesc_whenInvokedWithoutPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(waitingBooking);
        Collection<Booking> actual = bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(owner, BookingStatus.WAITING);

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findAllByItemOwnerAndStatusOrderByStartDesc_whenInvokedWithPagination_thenReturnCollectionBooking() {
        Collection<Booking> expected = List.of(waitingBooking);
        Collection<Booking> actual = bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(owner, BookingStatus.WAITING, PageRequest.of(0, 10)).getContent();

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual);
    }

    @Test
    void findTop1ByItemAndBookerAndEndBefore() {
        Booking actual = bookingRepository.findTop1ByItemAndBookerAndEndBefore(item, booker, LocalDateTime.now()).get();

        assertEquals(pastBooking, actual);
    }

    @Test
    void findTop1ByItemAndStartAfterAndStatusOrderByStartAsc() {
        Booking actual = bookingRepository.findTop1ByItemAndStartAfterAndStatusOrderByStartAsc(item, LocalDateTime.now(), BookingStatus.WAITING).get();

        assertEquals(waitingBooking, actual);
    }

    @Test
    void findTop1ByItemAndStartBeforeAndStatusOrderByEndDesc() {
        Booking actual = bookingRepository.findTop1ByItemAndStartBeforeAndStatusOrderByEndDesc(item, LocalDateTime.now(), BookingStatus.REJECTED).get();

        assertEquals(pastBooking, actual);
    }
}