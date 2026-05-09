package com.ibizabroker.lms.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BooksEntityTest {

    @Test
    void borrowBookDecreasesNumberOfCopies() {
        Books book = new Books();
        book.setNoOfCopies(3);
        book.borrowBook();
        assertEquals(2, book.getNoOfCopies());
    }

    @Test
    void returnBookIncreasesNumberOfCopies() {
        Books book = new Books();
        book.setNoOfCopies(1);
        book.returnBook();
        assertEquals(2, book.getNoOfCopies());
    }
}
