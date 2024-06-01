package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Collection<Booking> findAllByBookerOrderByStartDesc(User booker);

    @Query("select b " +
            "from Booking b " +
            "where b.booker = ?1 " +
            "and current_timestamp between b.start and b.end " +
            "order by b.start desc")
    Collection<Booking> findAllCurrentByBooker(User booker);

    Collection<Booking> findAllByBookerAndEndBeforeOrderByStartDesc(User booker, LocalDateTime end);

    Collection<Booking> findAllByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime currentTime);

    Collection<Booking> findAllByBookerAndStatusOrderByStartDesc(User booker, BookingStatus status);

    Collection<Booking> findAllByItemOwnerOrderByStartDesc(User owner);

    @Query("select b " +
            "from Booking b " +
            "join Item i on b.item.id = i.id " +
            "where i.owner = ?1 " +
            "and current_timestamp between b.start and b.end " +
            "order by b.start desc")
    Collection<Booking> findAllCurrentByOwner(User owner);

    Collection<Booking> findAllByItemOwnerAndEndBeforeOrderByStartDesc(User owner, LocalDateTime currentTime);

    Collection<Booking> findAllByItemOwnerAndStartAfterOrderByStartDesc(User owner, LocalDateTime currentTime);

    Collection<Booking> findAllByItemOwnerAndStatusOrderByStartDesc(User owner, BookingStatus status);

    Optional<Booking> findTop1ByItemAndBookerAndEndBefore(Item item, User booker, LocalDateTime currentTime);

    Optional<Booking> findTop1ByItemAndStartAfterAndStatusOrderByStartAsc(Item item, LocalDateTime currentTime, BookingStatus status);

    Optional<Booking> findTop1ByItemAndStartBeforeAndStatusOrderByEndDesc(Item item, LocalDateTime currentTime, BookingStatus status);
}

