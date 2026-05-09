package com.ibizabroker.lms.controller;

import com.ibizabroker.lms.dao.BooksRepository;
import com.ibizabroker.lms.entity.Books;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BooksControllerTest {

    private BooksController controller;

    @Mock
    private BooksRepository booksRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new BooksController();
        ReflectionTestUtils.setField(controller, "booksRepository", booksRepository);
    }

    @Test
    void getAllBooksReturnsRepositoryData() {
        Books cleanCode = book(1, "Clean Code", "Robert Martin", "Software", 4);
        Books refactoring = book(2, "Refactoring", "Martin Fowler", "Software", 2);
        when(booksRepository.findAll()).thenReturn(Arrays.asList(cleanCode, refactoring));

        assertEquals(2, controller.getAllBooks().size());
    }

    @Test
    void createBookSavesBook() {
        Books cleanCode = book(null, "Clean Code", "Robert Martin", "Software", 4);
        when(booksRepository.save(cleanCode)).thenReturn(book(1, "Clean Code", "Robert Martin", "Software", 4));

        Books created = controller.createBook(cleanCode);

        assertEquals(1, created.getBookId());
        verify(booksRepository).save(cleanCode);
    }

    @Test
    void updateBookCopiesEditableFields() {
        Books existing = book(1, "Old", "Old Author", "Old", 1);
        Books changes = book(1, "New", "New Author", "Technology", 5);
        when(booksRepository.findById(1)).thenReturn(Optional.of(existing));
        when(booksRepository.save(any(Books.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Books updated = controller.updateBook(1, changes).getBody();

        assertEquals("New", updated.getBookName());
        assertEquals("Technology", updated.getBookGenre());
        assertEquals(5, updated.getNoOfCopies());
    }

    @Test
    void deleteBookReturnsConfirmation() {
        Books existing = book(1, "Clean Code", "Robert Martin", "Software", 1);
        when(booksRepository.findById(1)).thenReturn(Optional.of(existing));

        Boolean deleted = controller.deleteBook(1).getBody().get("deleted");

        assertTrue(deleted);
        verify(booksRepository).delete(existing);
    }

    private Books book(Integer id, String name, String author, String genre, Integer copies) {
        Books book = new Books();
        book.setBookId(id);
        book.setBookName(name);
        book.setBookAuthor(author);
        book.setBookGenre(genre);
        book.setNoOfCopies(copies);
        return book;
    }
}
