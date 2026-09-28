package com.cdcollaguazo.bsn.api.book;

import com.cdcollaguazo.bsn.api.common.PageResponse;
import com.cdcollaguazo.bsn.api.exception.OperationNotPermittedException;
import com.cdcollaguazo.bsn.api.file.FileStorageService;
import com.cdcollaguazo.bsn.api.history.BookTransactionHistory;
import com.cdcollaguazo.bsn.api.history.BookTransactionHistoryRepository;
import com.cdcollaguazo.bsn.api.user.User;
import com.cdcollaguazo.bsn.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
public class BookService {

    private final BookMapper bookMapper;

    private final BookRepository bookRepository;

    private final BookTransactionHistoryRepository bookTransactionHistoryRepository;

    private final FileStorageService fileStorageService;

    private final UserRepository userRepository;

    public BookService(BookMapper bookMapper,
                       BookRepository bookRepository,
                       BookTransactionHistoryRepository bookTransactionHistoryRepository,
                       FileStorageService fileStorageService,
                       UserRepository userRepository) {
        this.bookMapper = bookMapper;
        this.bookRepository = bookRepository;
        this.bookTransactionHistoryRepository = bookTransactionHistoryRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    public Integer save(BookRequest request, Authentication connectedUser) {
        User user = userRepository.findByKeycloakId(connectedUser.getName()).orElseThrow(
                () -> new EntityNotFoundException("User not found with id " + connectedUser.getName())
        );

        // Book already exists
        if ( request.id() != null ) {
            Book book = bookRepository.findById(request.id()).orElseThrow(
                    () -> new EntityNotFoundException("Book not found with id " + request.id()));
            book.setTitle( request.title() );
            book.setAuthorName( request.authorName() );
            book.setIsbn( request.isbn() );
            book.setSynopsis( request.synopsis() );
            book.setShareable( request.shareable() );
            if ( book.isShareable() ) {
                book.setArchived( false );
            }
            return bookRepository.save(book).getId();
        }

        Book book = bookMapper.toBook(request);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId, Authentication connectedUser) {
        User user =  userRepository.findByKeycloakId(connectedUser.getName()).orElseThrow(
                () ->  new EntityNotFoundException("User not found with id " + connectedUser.getName()));

        return bookRepository.findById(bookId)
                .map(book -> bookMapper.toBookResponse(book, user))
                .orElseThrow(() -> new EntityNotFoundException("No book with the id:" + bookId));
    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, String searchTerm, Authentication connectedUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, searchTerm, connectedUser.getName());
        User user =  userRepository.findByKeycloakId(connectedUser.getName()).orElseThrow(
                () ->  new EntityNotFoundException("User not found with id " + connectedUser.getName()));

        List<BookResponse> bookResponse = books.stream()
                .map( book -> bookMapper.toBookResponse( book, user ))
                .toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAll(BookSpecification.withOwnerId(connectedUser.getName()), pageable);
        User user =  userRepository.findByKeycloakId(connectedUser.getName()).orElseThrow(
                () ->  new EntityNotFoundException("User not found with id " + connectedUser.getName()));
        List<BookResponse> bookResponse = books.stream()
                .map(book -> bookMapper.toBookResponse( book, user) )
                .toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionHistoryRepository.findAllBorrowedBooks(pageable, connectedUser.getName());
        List<BorrowedBookResponse> bookResponse = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionHistoryRepository.findAllReturnedBooks(pageable, connectedUser.getName());
        List<BorrowedBookResponse> bookResponse = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public Integer updateShareableState(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new EntityNotFoundException("No book found with id:" + bookId)
        );

        if(!Objects.equals(connectedUser.getName(), book.getOwner().getKeycloakId())) {
            throw new OperationNotPermittedException("You cannot update books shareable status");
        }
        book.setShareable(!book.isShareable());
        if ( book.isShareable() ) {
            book.setArchived( false );
        }
        bookRepository.save(book);
        return bookId;
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new EntityNotFoundException("No book found with id:" + bookId)
        );

        if(!Objects.equals(connectedUser.getName(), book.getOwner().getKeycloakId())) {
            throw new OperationNotPermittedException("You cannot update books shareable status");
        }
        book.setArchived(!book.isArchived());
        if ( book.isArchived() ) {
            book.setShareable( false );
        }
        bookRepository.save(book);
        return bookId;
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        User user = userRepository.findByKeycloakId(connectedUser.getName()).orElseThrow(
                () -> new EntityNotFoundException("User not found with id " + connectedUser.getName())
        );

        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new EntityNotFoundException("No book found with id:" + bookId)
        );
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not shareable");
        }

        if (Objects.equals(connectedUser.getName(), book.getOwner().getKeycloakId())) {
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }

        final boolean isAlreadyBorrowed = bookTransactionHistoryRepository.isAlreadyBorrowedByUser(bookId, connectedUser.getName());
        if (isAlreadyBorrowed) {
            throw new OperationNotPermittedException("The request book is already borrowed");
        }

        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();

        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new EntityNotFoundException("No book found with id:" + bookId)
        );
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not shareable");
        }

        if (Objects.equals(connectedUser.getName(), book.getOwner().getKeycloakId())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }
        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndUserId(bookId, connectedUser.getName())
                .orElseThrow(() -> new OperationNotPermittedException("You didn't borrow this book"));
        bookTransactionHistory.setReturned(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }


    public Integer approveReturnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new EntityNotFoundException("No book found with id:" + bookId)
        );
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not shareable");
        }

        if (!Objects.equals(connectedUser.getName(), book.getOwner().getKeycloakId())) {
            throw new OperationNotPermittedException("You cannot approve the return of a book that you don't own");
        }
        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndOwnerId(bookId, connectedUser.getName())
                .orElseThrow(() -> new OperationNotPermittedException("The book is not returned yet so you can't approve its return"));
        bookTransactionHistory.setReturnApproved(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new EntityNotFoundException("No book found with id:" + bookId)
        );

        String bookCover = fileStorageService.saveFile(file, connectedUser.getName());
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }

    public PageResponse<BookResponse> getWaitingList(int page, int size, Authentication connectedUser) {
        User user =  userRepository.findByKeycloakId(connectedUser.getName()).orElseThrow(
                () ->  new EntityNotFoundException("User not found with id " + connectedUser.getName()));

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findLikedBooksByUser(pageable, user.getKeycloakId());

        List<BookResponse> bookResponse = books.stream()
                .map( book -> bookMapper.toBookResponse( book, user ))
                .toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public void addBookToWaitingList( Integer bookId, Authentication connectedUser ) {
        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new EntityNotFoundException("No book found with id:" + bookId)
        );

        User user = userRepository.findByKeycloakId(connectedUser.getName()).orElseThrow(
                () -> new EntityNotFoundException("User not found with id " + connectedUser.getName())
        );

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be added to your waiting list" +
                    " since it is archived or not shareable");
        }

        if (Objects.equals(connectedUser.getName(), book.getOwner().getKeycloakId())) {
            throw new OperationNotPermittedException("You cannot add your own book to your waiting list");
        }

        if ( user.getLikedBooks().contains(book) ) {
            throw new OperationNotPermittedException("You cannot add the same book to your waiting list");
        }

        user.getLikedBooks().add(book);
        userRepository.save(user);
    }


    public void removeBookFromWaitingList( Integer bookId, Authentication connectedUser ) {
        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new EntityNotFoundException("No book found with id:" + bookId)
        );

        User user = userRepository.findByKeycloakId(connectedUser.getName()).orElseThrow(
                () -> new EntityNotFoundException("User not found with id " + connectedUser.getName())
        );

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be added to your waiting list" +
                    " since it is archived or not shareable");
        }

        if (Objects.equals(connectedUser.getName(), book.getOwner().getKeycloakId())) {
            throw new OperationNotPermittedException("You cannot add your own book to your waiting list");
        }

        if ( !user.getLikedBooks().contains(book) ) {
            throw new OperationNotPermittedException("You cannot remove a non liked book from your waiting list");
        }

        user.getLikedBooks().remove(book);
        userRepository.save(user);
    }

}
