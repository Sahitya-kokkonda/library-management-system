package com.ibizabroker.lms.controller;

import com.ibizabroker.lms.dao.BooksRepository;
import com.ibizabroker.lms.dao.BorrowRepository;
import com.ibizabroker.lms.dao.UsersRepository;
import com.ibizabroker.lms.entity.Books;
import com.ibizabroker.lms.entity.Borrow;
import com.ibizabroker.lms.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BorrowControllerTest {

    private BorrowController controller;

    @Mock
    private BorrowRepository borrowRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private BooksRepository booksRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new BorrowController();
        ReflectionTestUtils.setField(controller, "borrowRepository", borrowRepository);
        ReflectionTestUtils.setField(controller, "usersRepository", usersRepository);
        ReflectionTestUtils.setField(controller, "booksRepository", booksRepository);
    }

    @Test
    void borrowBookDecreasesStockAndStoresBorrowRecord() {
        Users user = getUser();
        Books book = book(20, "Domain-Driven Design", 2);
        Borrow borrow = getBorrow();

        when(usersRepository.findById(10)).thenReturn(Optional.of(user));
        when(booksRepository.findById(20)).thenReturn(Optional.of(book));

        String message = controller.borrowBook(borrow);

        assertEquals(1, book.getNoOfCopies());
        assertTrue(message.contains("Reader One has borrowed one copy"));
        assertNotNull(borrow.getIssueDate());
        assertNotNull(borrow.getDueDate());
        verify(booksRepository).save(book);
        verify(borrowRepository).save(borrow);
    }

    @Test
    void borrowBookDoesNotSaveRecordWhenBookIsOutOfStock() {
        Users user = getUser();
        Books book = book(20, "Unavailable Book", 0);
        Borrow borrow = getBorrow();

        when(usersRepository.findById(10)).thenReturn(Optional.of(user));
        when(booksRepository.findById(20)).thenReturn(Optional.of(book));

        String message = controller.borrowBook(borrow);

        assertEquals("The book \"Unavailable Book\" is out of stock!", message);
        verify(booksRepository, never()).save(any(Books.class));
        verify(borrowRepository, never()).save(any(Borrow.class));
    }

    private Borrow getBorrow() {
        Borrow borrow = new Borrow();
        borrow.setUserId(10);
        borrow.setBookId(20);
        return borrow;
    }

    private Users getUser() {
        Users user = new Users();
        user.setUserId(10);
        user.setUsername("reader1");
        user.setName("Reader One");
        return user;
    }

    private Books book(Integer id, String name, Integer copies) {
        Books book = new Books();
        book.setBookId(id);
        book.setBookName(name);
        book.setBookAuthor("Author");
        book.setBookGenre("Technology");
        book.setNoOfCopies(copies);
        return book;
    }
}
