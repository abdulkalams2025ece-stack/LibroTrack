package com.proj.librotrack.Service;

import com.proj.librotrack.Entity.Book;
import com.proj.librotrack.Exception.BusinessRuleException;
import com.proj.librotrack.Exception.ResourceNotFoundException;
import com.proj.librotrack.Repository.BookRepository;
import com.proj.librotrack.Repository.IssueRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final IssueRecordRepository issueRecordRepository;

    public BookService(BookRepository bookRepository,
                       IssueRecordRepository issueRecordRepository) {
        this.bookRepository = bookRepository;
        this.issueRecordRepository = issueRecordRepository;
    }

    // Feature 1: Add book
    public Book addBook(Book book) {
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new BusinessRuleException(
                    "A book with ISBN " + book.getIsbn() + " already exists");
        }
        book.setId(0);                                  // always create new
        book.setAvailableCopies(book.getTotalCopies()); // all copies on shelf
        return bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(int id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id " + id));
    }

    // Feature 1: Update book
    public Book updateBook(int id, Book book) {
        Book existing = getBookById(id);

        // ISBN changed? make sure the new one is not used by another book
        if (!existing.getIsbn().equals(book.getIsbn())
                && bookRepository.existsByIsbn(book.getIsbn())) {
            throw new BusinessRuleException(
                    "A book with ISBN " + book.getIsbn() + " already exists");
        }

        // copies currently with students
        int issuedCount = existing.getTotalCopies() - existing.getAvailableCopies();
        if (book.getTotalCopies() < issuedCount) {
            throw new BusinessRuleException("Total copies cannot be less than "
                    + issuedCount + " because that many copies are currently issued");
        }

        existing.setTitle(book.getTitle());
        existing.setAuthor(book.getAuthor());
        existing.setIsbn(book.getIsbn());
        existing.setCategory(book.getCategory());
        existing.setTotalCopies(book.getTotalCopies());
        existing.setAvailableCopies(book.getTotalCopies() - issuedCount);

        return bookRepository.save(existing);
    }

    public String deleteBook(int id) {
        getBookById(id); // throws 404 if missing
        if (issueRecordRepository.existsByBookId(id)) {
            throw new BusinessRuleException(
                    "Cannot delete book: it has issue history in the library records");
        }
        bookRepository.deleteById(id);
        return "Book deleted successfully";
    }

    // Feature 4: Search by title / author / category
    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> searchByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    public List<Book> searchByCategory(String category) {
        return bookRepository.findByCategoryIgnoreCase(category);
    }
}
