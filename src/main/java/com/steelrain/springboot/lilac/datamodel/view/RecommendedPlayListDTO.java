package com.steelrain.springboot.lilac.datamodel.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedPlayListDTO {
    private Long id;
    private String videoId;
    private Long channelId;
    private Integer licenseId;
    private Integer subjectId;
    private Long youtubePlaylistId;
    private String title;
    private String description;
    private Timestamp publishDate;
    private Timestamp regDate;
    private String thumbnailMedium;
    private String playlistId;

    private String channelTitle;
}
