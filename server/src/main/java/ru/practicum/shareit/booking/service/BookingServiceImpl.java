package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public ReturnBookingDto create(Long userId, ReceivingBookingDto receivingBookingDto) {
        log.info("BookingService: Beginning of method execution create().");
        log.info("create(): Checking the Start and End date of the booking.");
        if (!receivingBookingDto.getStart().isBefore(receivingBookingDto.getEnd())) {
            log.error("create(): Start date must be before End date.");
            throw new BadRequestException("Start date must be before End date.");
        }
        log.info("create(): Checking the existence of the booker and the item.");
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("create(): User with id = {} not found", userId);
                    return new NotFoundException(String.format("User with id = %d not found", userId));
                });
        Item item = itemRepository.findById(receivingBookingDto.getItemId())
                .orElseThrow(() -> {
                    log.error("create(): Item with id = {} not found", receivingBookingDto.getItemId());
                    return new NotFoundException(String.format("Item with id = %d not found.", receivingBookingDto.getItemId()));
                });

        log.info("create(): Checking that the booker is not the owner of the item");
        if (booker.equals(item.getOwner())) {
            log.error("create(): Booker is the owner of the item.");
            throw new NotFoundException(String.format("The owner with id = %d cannot reserve his item.", userId));
        }
        log.info("create(): Checking availability item.");
        if (!item.isAvailable()) {
            log.error("create(): Item is not available.");
            throw new BadRequestException(String.format("Item with id = %d is not available.", receivingBookingDto.getItemId()));
        }
        log.info("create(): Saving the booking in the database.");
        Booking booking = bookingRepository.save(bookingMapper.toBooking(receivingBookingDto, booker, item, BookingStatus.WAITING));

        log.info("create(): The booking was successfully saved in the database");
        return bookingMapper.toReturnBookingDto(booking);
    }

    @Override
    @Transactional
    public ReturnBookingDto statusUpdate(Long userId, Long bookingId, boolean approved) {
        log.info("BookingService: Beginning of method execution statusUpdate().");

        log.info("statusUpdate(): Checking the existence of the owner with id = {}.", userId);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("statusUpdate(): User with id = {} does not exist.", userId);
                    return new BadRequestException(String.format("User with id = %d does not exist.", userId));
                });
        log.info("statusUpdate(): Checking the existence of the booking with id = {}.", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("statusUpdate(): Booking with id = {} not found.", bookingId);
                    return new NotFoundException(String.format("Booking with id = %d not found.", bookingId));
                });

        log.info("statusUpdate(): Checking if the user with id = {} is the owner for the item with id = {}.", userId, booking.getItem().getId());
        if (!booking.getItem().getOwner().equals(owner)) {
            log.error("statusUpdate(): The user with id = {} is not the owner of the item with id = {}.", userId, booking.getItem().getId());
            throw new NotFoundException(String.format("The user with id = %d is not the owner of the item with id = %d.", userId, booking.getItem().getId()));
        }

        log.info("statusUpdate(): Checking booking status.");
        if (booking.getStatus() == BookingStatus.APPROVED && approved) {
            log.error("statusUpdate(): The booking with id = {} is already approved.", bookingId);
            throw new BadRequestException(String.format("Booking with id = %d is already approved.", bookingId));
        }

        log.info("statusUpdate(): Change booking status.");
        if (approved) {
            log.info("statusUpdate(): The booking with id = {} is approved.", bookingId);
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            log.info("statusUpdate(): The booking with id = {} is rejected.", bookingId);
            booking.setStatus(BookingStatus.REJECTED);
        }
        bookingRepository.save(booking);

        log.info("statusUpdate(): The booking status was successfully update.");
        return bookingMapper.toReturnBookingDto(booking);
    }

    @Override
    public ReturnBookingDto findById(Long userId, Long bookingId) {
        log.info("BookingService: Beginning of method execution findById().");
        log.info("findById(): Checking the existence of the owner with id = {}.", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("findById(): User with id = {} not found", userId);
                    return new NotFoundException(String.format("User with id = %d not found", userId));
                });
        log.info("findById(): Checking the existence of the booking with id = {}.", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("findById(): Booking with id = {} not found", bookingId);
                    return new NotFoundException(String.format("Booking with id = %d not found.", bookingId));
                });

        log.info("findById(): Checking access to information.");
        if (!booking.getItem().getOwner().equals(user) && !booking.getBooker().equals(user)) {
            log.error("findById(): User with id = {} does not have access to booking with id = {}.", userId, bookingId);
            throw new NotFoundException(String.format("User with id = %d does not have access to booking with id = %d.", userId, bookingId));
        }

        log.info("findById(): Booking with id = {} successfully found.", bookingId);
        return bookingMapper.toReturnBookingDto(booking);
    }

    @Override
    public List<ReturnBookingDto> findAllByBookerId(Long bookerId, BookingState state, Integer from, Integer size) {
        log.info("BookingService: Beginning of method execution findAllByBookerId().");
        log.info("findAllByBookerId(): Checking the existence of the booker with id = {}.", bookerId);
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> {
            log.error("findAllByBookerId(): User with id = {} not found", bookerId);
            return new NotFoundException(String.format("User with id = %d not found", bookerId));
        });

        log.info("findAllByBookerId(): Searching bookings for user with id = {} by state = {}.", bookerId, state);
        switch (state) {
            case ALL:
                log.info("findAllByBookerId(): Searching successfully completed.");
                if (isPaginationEnabled(from, size)) {
                    return bookingRepository.findAllByBookerOrderByStartDesc(booker, PageRequest.of(from / size, size)).stream()
                            .map(bookingMapper::toReturnBookingDto)
                            .collect(Collectors.toList());
                }

                return bookingRepository.findAllByBookerOrderByStartDesc(booker).stream()
                        .map(bookingMapper::toReturnBookingDto)
                        .collect(Collectors.toList());

            case CURRENT:
                log.info("findAllByBookerId(): Searching successfully completed.");
                if (isPaginationEnabled(from, size)) {
                    return bookingRepository.findAllCurrentByBooker(booker, PageRequest.of(from / size, size)).stream()
                            .map(bookingMapper::toReturnBookingDto)
                            .collect(Collectors.toList());
                }

                return bookingRepository.findAllCurrentByBooker(booker).stream()
                        .map(bookingMapper::toReturnBookingDto)
                        .collect(Collectors.toList());

            case PAST:
                log.info("findAllByBookerId(): Searching successfully completed.");
                if (isPaginationEnabled(from, size)) {
                    return bookingRepository.findAllByBookerAndEndBeforeOrderByStartDesc(booker, LocalDateTime.now(), PageRequest.of(from / size, size)).stream()
                            .map(bookingMapper::toReturnBookingDto)
                            .collect(Collectors.toList());
                }

                return bookingRepository.findAllByBookerAndEndBeforeOrderByStartDesc(booker, LocalDateTime.now()).stream()
                        .map(bookingMapper::toReturnBookingDto)
                        .collect(Collectors.toList());

            case FUTURE:
                log.info("findAllByBookerId(): Searching successfully completed.");
                if (isPaginationEnabled(from, size)) {
                    return bookingRepository.findAllByBookerAndStartAfterOrderByStartDesc(booker, LocalDateTime.now(), PageRequest.of(from / size, size)).stream()
                            .map(bookingMapper::toReturnBookingDto)
                            .collect(Collectors.toList());
                }

                return bookingRepository.findAllByBookerAndStartAfterOrderByStartDesc(booker, LocalDateTime.now()).stream()
                        .map(bookingMapper::toReturnBookingDto)
                        .collect(Collectors.toList());

            case WAITING:
                log.info("findAllByBookerId(): Searching successfully completed.");
                if (isPaginationEnabled(from, size)) {
                    return bookingRepository.findAllByBookerAndStatusOrderByStartDesc(booker, BookingStatus.WAITING, PageRequest.of(from / size, size)).stream()
                            .map(bookingMapper::toReturnBookingDto)
                            .collect(Collectors.toList());
                }

                return bookingRepository.findAllByBookerAndStatusOrderByStartDesc(booker, BookingStatus.WAITING).stream()
                        .map(bookingMapper::toReturnBookingDto)
                        .collect(Collectors.toList());

            case REJECTED:
                log.info("findAllByBookerId(): Searching successfully completed.");
                if (isPaginationEnabled(from, size)) {
                    return bookingRepository.findAllByBookerAndStatusOrderByStartDesc(booker, BookingStatus.REJECTED, PageRequest.of(from / size, size)).stream()
                            .map(bookingMapper::toReturnBookingDto)
                            .collect(Collectors.toList());
                }

                return bookingRepository.findAllByBookerAndStatusOrderByStartDesc(booker, BookingStatus.REJECTED).stream()
                        .map(bookingMapper::toReturnBookingDto)
                        .collect(Collectors.toList());

            default:
                log.error("findAllByBookerId(): Unknown state = {}.", state);
                throw new BadRequestException(String.format("Unknown state: %s", state));
        }
    }

    @Override
    public List<ReturnBookingDto> findAllByOwnerId(Long ownerId, BookingState state, Integer from, Integer size) {
        log.info("BookingService: Beginning of method execution findAllByOwnerId().");
        log.info("findAllByOwnerId(): Checking the existence of the owner with id = {}.", ownerId);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.error("findAllByOwnerId(): User with id = {} not found", ownerId);
                    return new NotFoundException(String.format("User with id = %d not found", ownerId));
                });

        log.info("findAllByOwnerId(): Checking the existence of items by owner with id = {}.", ownerId);
        if (itemRepository.findAllByOwner(owner).isEmpty()) {
            log.error("findAllByOwnerId(): User with id = {} does not own any of the existing items.", ownerId);
            throw new NotFoundException(String.format("User with id = %d does not own any of the existing items.", ownerId));
        }

        log.info("findAllByOwnerId(): Searching bookings for user with id = {} by state = {}.", ownerId, state);
        switch (state) {
            case ALL:
                log.info("findAllByBookerId(): Searching successfully completed.");
                if (isPaginationEnabled(from, size)) {
                    return bookingRepository.findAllByItemOwnerOrderByStartDesc(owner, PageRequest.of(from / size, size)).stream()
                            .map(bookingMapper::toReturnBookingDto)
                            .collect(Collectors.toList());
                }

                return bookingRepository.findAllByItemOwnerOrderByStartDesc(owner).stream()
                        .map(bookingMapper::toReturnBookingDto)
                        .collect(Collectors.toList());

            case CURRENT:
                log.info("findAllByOwnerId(): Searching successfully completed.");
                if (isPaginationEnabled(from, size)) {
                    return bookingRepository.findAllCurrentByOwner(owner, PageRequest.of(from / size, size)).stream()
                            .map(bookingMapper::toReturnBookingDto)
                            .collect(Collectors.toList());
                }

                return bookingRepository.findAllCurrentByOwner(owner).stream()
                        .map(bookingMapper::toReturnBookingDto)
                        .collect(Collectors.toList());

            case PAST:
                log.info("findAllByOwnerId(): Searching successfully completed.");
                if (isPaginationEnabled(from, size)) {
                    return bookingRepository.findAllByItemOwnerAndEndBeforeOrderByStartDesc(owner, LocalDateTime.now(), PageRequest.of(from / size, size)).stream()
                            .map(bookingMapper::toReturnBookingDto)
                            .collect(Collectors.toList());
                }

                return bookingRepository.findAllByItemOwnerAndEndBeforeOrderByStartDesc(owner, LocalDateTime.now()).stream()
                        .map(bookingMapper::toReturnBookingDto)
                        .collect(Collectors.toList());

            case FUTURE:
                log.info("findAllByOwnerId(): Searching successfully completed.");
                if (isPaginationEnabled(from, size)) {
                    return bookingRepository.findAllByItemOwnerAndStartAfterOrderByStartDesc(owner, LocalDateTime.now(), PageRequest.of(from / size, size)).stream()
                            .map(bookingMapper::toReturnBookingDto)
                            .collect(Collectors.toList());
                }

                return bookingRepository.findAllByItemOwnerAndStartAfterOrderByStartDesc(owner, LocalDateTime.now()).stream()
                        .map(bookingMapper::toReturnBookingDto)
                        .collect(Collectors.toList());

            case WAITING:
                log.info("findAllByOwnerId(): Searching successfully completed.");
                if (isPaginationEnabled(from, size)) {
                    return bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(owner, BookingStatus.WAITING, PageRequest.of(from / size, size)).stream()
                            .map(bookingMapper::toReturnBookingDto)
                            .collect(Collectors.toList());
                }

                return bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(owner, BookingStatus.WAITING).stream()
                        .map(bookingMapper::toReturnBookingDto)
                        .collect(Collectors.toList());

            case REJECTED:
                log.info("findAllByOwnerId(): Searching successfully completed.");
                if (isPaginationEnabled(from, size)) {
                    return bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(owner, BookingStatus.REJECTED, PageRequest.of(from / size, size)).stream()
                            .map(bookingMapper::toReturnBookingDto)
                            .collect(Collectors.toList());
                }

                return bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(owner, BookingStatus.REJECTED).stream()
                        .map(bookingMapper::toReturnBookingDto)
                        .collect(Collectors.toList());

            default:
                log.error("findAllByOwnerId(): Unknown state = {}.", state);
                throw new BadRequestException(String.format("Unknown state: %s", state));
        }
    }

    private Boolean isPaginationEnabled(Integer from, Integer size) {
        if (from != null && size != null) {
            if (from < 0 || size < 1) {
                log.error("findAllByOwnerId(): Invalid request parameters.");
                throw new BadRequestException("Invalid request parameters.");
            }
            return true;
        }
        return false;
    }
}
