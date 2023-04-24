package com.steelrain.springboot.lilac.service;

import com.steelrain.springboot.lilac.common.ICacheService;
import com.steelrain.springboot.lilac.common.PagingUtils;
import com.steelrain.springboot.lilac.datamodel.*;
import com.steelrain.springboot.lilac.datamodel.view.LectureNoteYoutubeVideoDTO;
import com.steelrain.springboot.lilac.datamodel.view.RecommendedPlayListDTO;
import com.steelrain.springboot.lilac.datamodel.view.RecommendedVideoDTO;
import com.steelrain.springboot.lilac.event.VideoListByPlayListEvent;
import com.steelrain.springboot.lilac.event.VideoPlayListSearchEvent;
import com.steelrain.springboot.lilac.exception.LilacServiceException;
import com.steelrain.springboot.lilac.repository.IVideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.steelrain.springboot.lilac.datamodel.KEYWORD_TYPE.LICENSE;
import static com.steelrain.springboot.lilac.datamodel.KEYWORD_TYPE.SUBJECT;

/**
 * 영상 서비스
 * - 영상의 비즈니스 로직을 구현
 * - 영상관련 이벤트를 처리/발행 한다
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoService implements IVideoService {

    private final IVideoRepository m_videoRepository;
    private final ICacheService m_cacheService;


    @Override
    public List<RecommendedVideoDTO> getRecommendedVideoList() {
        return m_videoRepository.findRecommendedVideoList();
    }

    @Override
    public List<RecommendedPlayListDTO> getRecommendedPlayList() {
        return m_videoRepository.findRecommendedPlayList();
    }

    @Override
    public List<YoutubeVideoDTO> getPlayListDetail(Long youtubePlaylistId){
        return m_videoRepository.findPlayListDetail(youtubePlaylistId);
    }

    @Override
    public VideoPlayListSearchResultDTO searchPlayListByKeyword(String searchKeyword, int pageNum, int playlistCount) {
        int pageStart = PagingUtils.calcStartPage(pageNum, playlistCount);
        int totalPlaylistCount = m_videoRepository.selectTotalPlayListCountByKeyword(searchKeyword);
        return VideoPlayListSearchResultDTO.builder()
                                        .requestKeywordCode(0)
                                        .requestKeywordType(KEYWORD_TYPE.NONE)
                                        .searchKeyword(searchKeyword)
                                        .pageDTO(PagingUtils.createPagingInfo(totalPlaylistCount, pageNum, playlistCount))
                                        .playList(m_videoRepository.findPlayListByKeyword(searchKeyword, pageStart, playlistCount))
                                        .build();
    }

    @Override
    public VideoPlayListSearchResultDTO searchPlayListById(int code, int pageNum, int playlistCount, KEYWORD_TYPE codeType){
        int pageStart = PagingUtils.calcStartPage(pageNum, playlistCount);
        int id = extractIdType(code, codeType);
        int totalPlaylistCount = m_videoRepository.selectTotalPlayListCount(id, codeType.getValue());
        return VideoPlayListSearchResultDTO.builder()
                .requestKeywordCode(code)
                .requestKeywordType(codeType)
                .searchKeyword(null)
                .pageDTO(PagingUtils.createPagingInfo(totalPlaylistCount, pageNum, playlistCount))
                .playList(m_videoRepository.findPlayListById(id, codeType.getValue(), pageStart, playlistCount))
                .build();
    }

    private int extractIdType(int id, KEYWORD_TYPE idType){
        int result = 0;
        if(idType == LICENSE){
            result = m_cacheService.getLicenseIdByCode(id);
        }else if(idType == SUBJECT){
            result = m_cacheService.getSubjectIdByCode(id);
        }
        return result;
    }


    @EventListener(VideoPlayListSearchEvent.class)
    public void handleVideoPlayListSearchEvent(VideoPlayListSearchEvent event){
        VideoPlayListSearchResultDTO result = null;
        switch (event.getKeywordType()) {
            case KEYWORD:
                result = searchPlayListByKeyword(event.getKeyword(), event.getPageNum(), event.getPlaylistCount());
                break;
            case LICENSE:
            case SUBJECT:
                result = searchPlayListById(event.getKeywordCode(), event.getPageNum(), event.getPlaylistCount(), event.getKeywordType());
                break;
            default:
                throw new LilacServiceException(String.format("확인할 수 없는 키워드 코드 입니다. 입력된 키워드 코드 : %d", event.getKeywordType()));
        }
        event.setSearchResultDTO(result);
    }

    @EventListener(VideoListByPlayListEvent.class)
    public void handleVideoListByPlayListEvent(VideoListByPlayListEvent event){
        event.setVideoDTOList(getAllVideoIdByPlayList(event.getPlayListId()));
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

    /*
        영상의 재생시간을 처리한다
     */
    @Override
    @Transactional
    public void updateVideoPlaytime(Long lectureVideoId, Long playtime) {
        long progress = m_videoRepository.getProgress(lectureVideoId);
        long duration = m_videoRepository.getDuration(lectureVideoId);
        if(playtime > progress && playtime <= duration){
            m_videoRepository.updateVideoPlaytime(lectureVideoId, playtime);
        }
    }

    @Override
    public List<LectureNoteYoutubeVideoDTO> getPlayListDetailOfLectureNote(Long memberId, Long youtubePlaylistId, Long noteId) {
        return m_videoRepository.findPlayListDetailOfLectureNote(memberId, youtubePlaylistId, noteId);
    }

    @Override
    public Boolean getLikeStatus(Long memberId, Long videoId) {
        return m_videoRepository.findVideoLikeStatus(memberId, videoId).orElse(null);
    }

    /*
        유튜브 영상의 좋아요 처리
     */
    @Override
    @Transactional
    public Map<String, Long> updateLikeVideo(Long videoId, Long memberId) {
        Optional<Boolean> res = m_videoRepository.findVideoLikeStatus(memberId, videoId);
        if(res.isPresent()){
            if(res.get().booleanValue()){
                m_videoRepository.decreaseLikeCount(videoId);
                m_videoRepository.deleteLikeVideo(memberId, videoId);
            }else{
                m_videoRepository.updateLikeVideo(memberId, videoId, true);
                m_videoRepository.increaseLikeCount(videoId);
                m_videoRepository.decreaseDislikeCount(videoId);
            }
        }else{
            m_videoRepository.setLikeStatus(memberId, videoId, true);
            m_videoRepository.increaseLikeCount(videoId);
        }
        return m_videoRepository.selectLikeCountMap(videoId);
    }

    /*
        유튜브 영상의 싫어요 처리
     */
    @Override
    @Transactional
    public Map<String, Long> updateDislikeVideo(Long videoId, Long memberId) {
        Optional<Boolean> res = m_videoRepository.findVideoLikeStatus(memberId, videoId);
        if(res.isPresent()){
            if(res.get().booleanValue()){
                m_videoRepository.updateLikeVideo(memberId, videoId, false);
                m_videoRepository.increaseDislikeCount(videoId);
                m_videoRepository.decreaseLikeCount(videoId);
            }else{
                m_videoRepository.decreaseDislikeCount(videoId);
                m_videoRepository.deleteLikeVideo(memberId, videoId);
            }
        }else{
            m_videoRepository.setLikeStatus(memberId, videoId, false);
            m_videoRepository.increaseDislikeCount(videoId);
        }
        return m_videoRepository.selectLikeCountMap(videoId);
    }
}
