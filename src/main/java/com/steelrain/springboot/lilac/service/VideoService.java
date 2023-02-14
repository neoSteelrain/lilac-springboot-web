package com.steelrain.springboot.lilac.service;

import com.steelrain.springboot.lilac.common.KeywordCategoryCacheService;
import com.steelrain.springboot.lilac.common.PagingUtils;
import com.steelrain.springboot.lilac.datamodel.*;
import com.steelrain.springboot.lilac.datamodel.view.RecommendedVideoDTO;
import com.steelrain.springboot.lilac.exception.LilacException;
import com.steelrain.springboot.lilac.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoService implements IVideoService {

    private final VideoRepository m_videoRepository;
    private final KeywordCategoryCacheService m_keywordCategoryCacheService;


    @Override
    public List<RecommendedVideoDTO> getRecommendedVideoList() {
        return m_videoRepository.findRecommendedVideoList();
    }

    @Override
    public List<YoutubeVideoDTO> getPlayListDetail(Long youtubePlaylistId){
        return m_videoRepository.findPlayListDetail(youtubePlaylistId);
    }

    @Override
    public VideoPlayListSearchResultDTO searchPlayList(int keywordCode, int pageNum, int playlistCount, int keywordType) {
        int pageStart = (pageNum - 1) * playlistCount;
        String keywordStr = parseKeywordCode(keywordCode, keywordType);
        int totalPlaylistCount = m_videoRepository.selectTotalPlayListCountByKeyword(keywordStr);
        return VideoPlayListSearchResultDTO.builder()
                                        .requestKeywordCode(keywordCode)
                                        .requestKeywordType(keywordType)
                                        .pageDTO(PagingUtils.createPagingInfo(totalPlaylistCount, pageNum, playlistCount))
                                        .playList(m_videoRepository.findPlayListByKeyword(keywordStr, pageStart, playlistCount))
                                        .build();
    }

    private String parseKeywordCode(int keywordCode, int keywordType){
        if(keywordType == 1){
            return m_keywordCategoryCacheService.getLicenseKeyword(keywordCode);
        }else if(keywordType == 2){
            return m_keywordCategoryCacheService.getSubjectKeyword(keywordCode);
        }else{
            throw new LilacException(String.format("확인할 수 없는 키워드 코드 입니다. 입력된 키워드 코드 : %d", keywordCode));
        }
    }

    @Override
    public List<Long> getAllVideoIdByPlayList(Long playListId) {
        return m_videoRepository.findAllVideoIdByPlayList(playListId);
    }

    @Override
    public YoutubeVideoDTO getVideoDetail(Long videoId) {
        return m_videoRepository.findVideoDetail(videoId);
    }

    @Override
    public boolean isExistYoutubePlayList(Long playListId){ return m_videoRepository.isExistYoutubePlayList(playListId);}
}
