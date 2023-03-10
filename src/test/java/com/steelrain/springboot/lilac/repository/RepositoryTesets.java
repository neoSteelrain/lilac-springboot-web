package com.steelrain.springboot.lilac.repository;

import com.steelrain.springboot.lilac.datamodel.api.KakaoBookSearchResponseDTO;
import com.steelrain.springboot.lilac.datamodel.api.LicenseScheduleResponseDTO;
import com.steelrain.springboot.lilac.datamodel.api.NaruBookExistResposeDTO;
import com.steelrain.springboot.lilac.datamodel.api.NaruLibSearchByBookResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
public class RepositoryTesets {

    @Autowired
    private IKaKoBookRepository kaKoBookRepository;
    @Autowired
    private INaruRepository naruRepository;
    @Autowired
    private ILicenseRespository licenseRespository;


    @Test
    public void testSearchBookfromKakao(){
        /*String tmp = "https://dapi.kakao.com/v3/search/book?query=정보처리기사 2022&page=1&size=5&target=title";
        char ch = tmp.charAt(50);
        System.out.println("ch : " + ch);*/

        KakaoBookSearchResponseDTO res = kaKoBookRepository.searchBookFromKakao("정보처리기사 2022", 1, 6);

        assertThat(res != null);

        System.out.println("res : " + res.toString());
    }

    @Test
    public void testGetLibraryByBook(){
        // 테스트 ISBN 9791162243077
        NaruLibSearchByBookResponseDTO res = naruRepository.getLibraryByBook(9791162243077L, (short)23, 23090);

        assertThat(res != null);

        System.out.println("res : " + res.toString());
        res.getResponse().getLibs().stream().forEach(lib -> System.out.println(lib.getLib().getLibname()));
    }

    @Test
    public void testByGabageGetLibraryByBook(){
        NaruLibSearchByBookResponseDTO res = naruRepository.getLibraryByBook(9791162243077L, (short)23, 23040);

        assertThat(res != null);

        for(NaruLibSearchByBookResponseDTO.Libs ls : res.getResponse().getLibs()){
            System.out.println("Library : " + ls.getLib().getLibname());
        }

//        System.out.println("res : " + res.getResponse().getError());
        //res.getResponse().getLibs().stream().forEach(lib -> System.out.println(lib.getLib().getLibname()));
    }

    @Test
    public void testCheckBookExist(){
        NaruBookExistResposeDTO res = naruRepository.checkBookExist(01020, (short)23090);

        assertThat(res != null);

        // NaruBookExistResposeDTO res = naruRepository.checkBookExist(9791162243077L, (short)1111);
        System.out.println("res : " + res.getResponse().getError());
    }

    @Test
    public void testLicenseRepository(){
        LicenseScheduleResponseDTO res = licenseRespository.getLicenseSchedule(2290);

        assertThat(res != null);

        System.out.println("시험일정 갯수 : " + res.getBody().getScheduleList().size());
    }

    /*@Test
    public void testGetLicenseNama(){
        Optional<LicenseDTO> res = licenseRespository.getLicenseInfo(2290);
        assertThat(res != null && res.isPresent() ? StringUtils.hasText(res.get().getLicenseName()) : false);

        System.out.println("lic name : " + res.get().getLicenseName());
    }*/
}
