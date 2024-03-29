package com.steelrain.springboot.lilac.service;


import com.steelrain.springboot.lilac.datamodel.view.BookDetailDTO;
import com.steelrain.springboot.lilac.datamodel.view.LicenseBookListDTO;
import com.steelrain.springboot.lilac.datamodel.NaruLibraryDTO;
import com.steelrain.springboot.lilac.datamodel.view.RecommendedBookListDTO;
import com.steelrain.springboot.lilac.datamodel.view.SubjectBookListDTO;
import com.steelrain.springboot.lilac.exception.NaruAPIQuotaOverException;

import java.util.List;

/**
 * 도서 서비스 인터페이스
 */
public interface IBookService {
    LicenseBookListDTO getLicenseBookList(int licenseCode, short regionCode, int detailRegionCode, int pageNum, int bookCount) throws NaruAPIQuotaOverException;
    List<NaruLibraryDTO> getLibraryByRegionList(short region, int detailRegion) throws NaruAPIQuotaOverException;
    SubjectBookListDTO getSubjectBookList(int subjectCode, int pageNum, int bookCount);
    //SubjectBookListDTO getSubjectBookList(String keyword, int pageNum, int bookCount);
    BookDetailDTO getBookDetailInfo(Long isbn, short regionCode, int detailRegionCode);
    RecommendedBookListDTO getRecommendedBookList();
}
