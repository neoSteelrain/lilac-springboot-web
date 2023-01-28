package com.steelrain.springboot.lilac.service;

import com.steelrain.springboot.lilac.datamodel.LibraryDetailRegionCodeDTO;
import com.steelrain.springboot.lilac.datamodel.LibraryRegionCodeDTO;
import com.steelrain.springboot.lilac.datamodel.LicenseCodeDTO;
import com.steelrain.springboot.lilac.datamodel.SubjectCodeDTO;
import com.steelrain.springboot.lilac.mapper.SearchMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KeywordCategoryCacheService implements ICacheService {

    private List<SubjectCodeDTO> m_subjectCodeList = null;
    private List<LibraryRegionCodeDTO> m_libRegionCodeList = null;
    private List<LicenseCodeDTO> m_licenseCodeList;
    private Map<Short, Object> m_libDetailRegionCodeMap = null;
    private final SearchMapper m_searchMapper;

    public KeywordCategoryCacheService(SearchMapper sm){
        this.m_searchMapper = sm;

        initKeywordMap();
    }

    @Override
    public List<SubjectCodeDTO> getSubjectCodeList(){
        if(m_subjectCodeList == null){
            return new ArrayList<>(0);
        }
        return m_subjectCodeList;
    }

    @Override
    public List<LibraryRegionCodeDTO> getLibraryRegionCodeList(){
        if(m_libRegionCodeList == null){
            return new ArrayList<>(0);
        }
        return m_libRegionCodeList;
    }

    @Override
    public List<LibraryDetailRegionCodeDTO> getLibraryDetailRegionCodeList(short regionCode){
        if(m_libDetailRegionCodeMap == null){
            return new ArrayList<>(0);
        }
        return m_libDetailRegionCodeMap.containsKey(regionCode) ?
                (List<LibraryDetailRegionCodeDTO>) m_libDetailRegionCodeMap.get(regionCode) : new ArrayList<>(0);
    }

    @Override
    public List<LicenseCodeDTO> getLicenseCodeList(){
        if(m_licenseCodeList == null){
            return new ArrayList<>(0);
        }
        return m_licenseCodeList;
    }

    private void initKeywordMap(){
        // 주제분류 초기화
        m_subjectCodeList = m_searchMapper.getSubjectCodes();
        // 지역코드 초기화
        m_libRegionCodeList = m_searchMapper.getLibRegionCodes();
        // 자격증분류 초기화
        m_licenseCodeList = m_searchMapper.getLicenseCodes();
        // 세부지역코드 초기화
        List<Short> codes = m_libRegionCodeList.stream()
                .map(LibraryRegionCodeDTO::getCode)
                .sorted()
                .collect(Collectors.toList());
        m_libDetailRegionCodeMap = new HashMap<>(codes.size());

        List<LibraryDetailRegionCodeDTO> allDtlRegionCodeList = m_searchMapper.getAllLibDetailRegionCodes();
        for(Short code : codes){
            List<LibraryDetailRegionCodeDTO> tmp = new ArrayList<>(20);
            for(LibraryDetailRegionCodeDTO dto : allDtlRegionCodeList){
                if(code.shortValue() == dto.getRegionCode().shortValue()){
                    tmp.add(dto);
                }
            }
            m_libDetailRegionCodeMap.put(code, tmp);
        }
    }
}