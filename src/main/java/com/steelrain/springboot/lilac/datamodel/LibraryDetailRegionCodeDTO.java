package com.steelrain.springboot.lilac.datamodel;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 정보나루 API 검색조건 중 지역구코드정보
 * 예) 종로구, 미추홀구, 용산구
 */
@Getter
@Setter
@ToString
public class LibraryDetailRegionCodeDTO {
    /*
    code	int	NO	PRI
    region_code	smallint	NO	PRI
    name	varchar(10)	NO
    detail_name	varchar(10)	NO
     */

    @NotNull
    @NotEmpty
    private Integer code;

    @NotNull
    @NotEmpty
    private Short regionCode;

    @Size(max=10)
    @NotNull
    @NotEmpty
    private String name;

    @Size(max=10)
    @NotNull
    @NotEmpty
    private String detailName;
}
