package com.steelrain.springboot.lilac.mapper;

import com.steelrain.springboot.lilac.datamodel.YoutubePlayListDTO;
import com.steelrain.springboot.lilac.datamodel.YoutubeVideoDTO;
import com.steelrain.springboot.lilac.datamodel.RecommendedVideoDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoMapper {

    List<RecommendedVideoDTO> findRecommendedVideoList();
    List<YoutubeVideoDTO> findPlayListDetail(Long youtubePlaylistId);
    List<YoutubePlayListDTO> findPlayListByKeyword(@Param("keyword") String keyword, @Param("offset") int offset, @Param("count") int count);


    @Select("SELECT count(id) FROM tbl_youtube_playlist WHERE title LIKE concat('%',#{keyword},'%')")
    int selectTotalPlayListCountByKeyword(@Param("keyword") String keyword);
}
