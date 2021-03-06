package com.back.snobs.service;

import com.back.snobs.domain.book.Book;
import com.back.snobs.domain.book.BookRepository;
import com.back.snobs.domain.book.snob_book.SnobBook;
import com.back.snobs.domain.book.snob_book.SnobBookId;
import com.back.snobs.domain.book.snob_book.SnobBookRepository;
import com.back.snobs.domain.genre.Genre;
import com.back.snobs.domain.genre.GenreRepository;
import com.back.snobs.domain.genre.snob_genre.SnobGenre;
import com.back.snobs.domain.genre.snob_genre.SnobGenreId;
import com.back.snobs.domain.genre.snob_genre.SnobGenreRepository;
import com.back.snobs.domain.keyword.Keyword;
import com.back.snobs.domain.keyword.KeywordRepository;
import com.back.snobs.domain.keyword.snob_keyword.SnobKeyword;
import com.back.snobs.domain.keyword.snob_keyword.SnobKeywordId;
import com.back.snobs.domain.keyword.snob_keyword.SnobKeywordRepository;
import com.back.snobs.domain.log.Log;
import com.back.snobs.domain.snob.Snob;
import com.back.snobs.domain.snob.SnobDto;
import com.back.snobs.domain.snob.SnobRepository;
import com.back.snobs.domain.snob.dailyLog.DailyLog;
import com.back.snobs.domain.snob.dailyLog.DailyLogRepository;
import com.back.snobs.error.CustomResponse;
import com.back.snobs.error.ResponseCode;
import com.back.snobs.error.exception.NoDataException;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SnobService {
    private final SnobRepository snobRepository;
    private final SnobGenreRepository snobGenreRepository;
    private final SnobKeywordRepository snobKeywordRepository;
    private final GenreRepository genreRepository;
    private final KeywordRepository keywordRepository;
    private final BookRepository bookRepository;
    private final SnobBookRepository snobBookRepository;
    private final DailyLogRepository dailyLogRepository;

    // ?????? ?????? ????????????
    public ResponseEntity<CustomResponse> read(String userEmail) {
        Snob snob = snobRepository.findById(userEmail).orElseThrow(() -> {
            throw new NoDataException("No User Found", ResponseCode.DATA_NOT_FOUND);
        });
        return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS, snob),
                HttpStatus.valueOf(200));
    }

    @Transactional
    public ResponseEntity<CustomResponse> update(SnobDto snobDto, String userEmail) {
        Optional<Snob> temp = snobRepository.findById(userEmail);
        // ?????? ???????????? ???????????? ??????
        if (temp.isPresent()) {
            Snob snob = temp.get();
            snob.SnobUpdate(
                    snobDto.getUserName(),
                    snobDto.getGender(),
                    LocalDate.parse(snobDto.getBirthDate(), DateTimeFormatter.ISO_DATE),
                    snobDto.getAddress(),
                    snobDto.getSelfIntroduction()
            );

            return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS, snobRepository.save(snob)),
                    HttpStatus.valueOf(200));
        } else {
            throw new NoDataException("No such data", ResponseCode.DATA_NOT_FOUND);
        }
    }

    // ?????? ???????????? ??????
    public ResponseEntity<CustomResponse> getGenre(String userEmail) {
        List<SnobGenre> ls = snobGenreRepository.findBySnob_UserEmail(userEmail);
        List<String> genres = new ArrayList<String>();
        for (SnobGenre sg: ls) {
            genres.add(sg.getGenre().getGenreName());
        }

        return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS, genres),
                HttpStatus.valueOf(200));
    }

    // ?????? ???????????? ?????? ??? ??????
    @Transactional
    public ResponseEntity<CustomResponse> setGenre(String userEmail, String genreData) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject)jsonParser.parse(genreData);
        HashMap<String, Object> map = (HashMap<String, Object>) jsonObject.get("genreData");

        Snob snob = snobRepository.findById(userEmail).orElseThrow(
                () -> new NoDataException("No such User", ResponseCode.DATA_NOT_FOUND));

        for(String s: map.keySet()) {
            Optional<SnobGenre> temp = snobGenreRepository
                    .findById(new SnobGenreId(s, userEmail));
            SnobGenre snobGenre;
            Boolean b = Integer.parseInt(String.valueOf(map.get(s))) == 1;
            if (temp.isPresent()) {
                snobGenre = temp.get();
                snobGenre.setPreference(b);
            } else {
                Genre genre = genreRepository.findById(s).orElseThrow(
                        () -> new NoDataException("No such Genre", ResponseCode.DATA_NOT_FOUND));
                snobGenreRepository.save(
                        SnobGenre.builder()
                                .genre(genre)
                                .snob(snob)
                                .preference(b)
                                .build()
                );
            }
        }

        return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS), null, HttpStatus.valueOf(200));
    }

    //?????? ????????? ??????
    public ResponseEntity<CustomResponse> getKeyword(String userEmail) {
        List<SnobKeyword> ls = snobKeywordRepository.findBySnob_UserEmail(userEmail);
        List<String> keywords = new ArrayList<String>();
        for (SnobKeyword sk: ls) {
            keywords.add(sk.getKeyword().getKeywordName());
        }

        return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS, keywords),
                HttpStatus.valueOf(200));
    }

    // ?????? ????????? ?????? ??? ??????
    @Transactional
    public ResponseEntity<CustomResponse> setKeyword(String userEmail, String keywordData) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject)jsonParser.parse(keywordData);
        HashMap<String, Object> map = (HashMap<String, Object>) jsonObject.get("keywordData");

        Snob snob = snobRepository.findById(userEmail).orElseThrow(
                () -> new NoDataException("No such User", ResponseCode.DATA_NOT_FOUND));

        for(String s: map.keySet()) {
            Optional<SnobKeyword> temp = snobKeywordRepository
                    .findById(new SnobKeywordId(s, userEmail));
            SnobKeyword snobKeyword;
            Boolean b = Integer.parseInt(String.valueOf(map.get(s))) == 1;
            if (temp.isPresent()) {
                snobKeyword = temp.get();
                snobKeyword.setPreference(b);
            } else {
                Keyword keyword = keywordRepository.findById(s).orElseThrow(
                        () -> new NoDataException("No such Keyword", ResponseCode.DATA_NOT_FOUND));
                snobKeywordRepository.save(
                        SnobKeyword.builder()
                                .keyword(keyword)
                                .snob(snob)
                                .preference(b)
                                .build()
                );
            }
        }

        return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS), null, HttpStatus.valueOf(200));
    }

    // ????????? ?????? ???, ?????? ?????? ??? ????????????
    public ResponseEntity<CustomResponse> getSnobBook(String userEmail, boolean readed) {
        List<SnobBook> ls = snobBookRepository.findBySnob_UserEmailWithFetchJoin(userEmail, readed);
        List<Book> books = new ArrayList<>();
        for (SnobBook sb: ls) {
            books.add(sb.getBook());
        }

        return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS, books),
                HttpStatus.valueOf(200));
    }

    public ResponseEntity<CustomResponse> getSnobBookTest2(String userEmail) {
        // ????????? ???????????? N + 1?
        PageRequest pageRequest = PageRequest.of(0, 3);
        List<Book> books = bookRepository.findByBookWithJoin(userEmail, true, pageRequest);
        // ??? join fetch??? ???????????? ????????? ??? ?????? ??????????????? ???????????? ????????? ??????????
//        List<Book> books = bookRepository.findByBookWithFetchJoin2(pageRequest);

        return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS, books),
                HttpStatus.valueOf(200));
    }

    // fetch join difference test
    public ResponseEntity<CustomResponse> getSnobBookTest(String userEmail) {
        PageRequest pageRequest = PageRequest.of(0, 3);
        List<SnobBook> ls = snobBookRepository.findBySnob_UserEmail(userEmail, true, pageRequest);
        List<Object> books = new ArrayList<>();

        for (SnobBook sb: ls) {
            // ?????? ?????? ?????? ??????????????? ????????? getBook??????
            // ?????? ????????? ????????? ?????????
//            books.add(sb.getBook().getTitle());
            Book b = sb.getBook();
            books.add(b);
            System.out.println(b);
        }

        return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS, books), HttpStatus.valueOf(200));
    }

    // ?????? ???, ?????? ?????? ??? ??????
    @Transactional
    public ResponseEntity<CustomResponse> setSnobBook(String userEmail, String bookId, Boolean readed) {
        Optional<SnobBook> temp = snobBookRepository
                .findById(new SnobBookId(userEmail, bookId));
        SnobBook snobBook;

        if (temp.isPresent()) {
            snobBook = temp.get();
            snobBook.updateReaded(readed);
            return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS, snobBookRepository.save(snobBook)),
                    HttpStatus.valueOf(200));
        } else {
            return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS,
                        snobBookRepository.save(
                                SnobBook.builder()
                                        .snob(snobRepository.findById(userEmail).orElseThrow(
                                                () -> new NoDataException("No such Data", ResponseCode.DATA_NOT_FOUND)))
                                        .book(bookRepository.findById(bookId).orElseThrow(
                                                () -> new NoDataException("No such Data", ResponseCode.DATA_NOT_FOUND)))
                                        .readed(readed)
                                        .build()
                        )
                    ),
                    HttpStatus.valueOf(200));
        }
    }

    public ResponseEntity<CustomResponse> getSnobDailyLog(String userEmail) {
        List<DailyLog> dailyLogs = dailyLogRepository.findAllBySnob_userEmail(userEmail);
        List<Log> logs = new ArrayList<>();
        for (DailyLog dl: dailyLogs) {
            logs.add(dl.getLog());
        }
        return new ResponseEntity<>(new CustomResponse(ResponseCode.SUCCESS,
                logs),
                HttpStatus.valueOf(200));
    }
}
