package com.steelrain.springboot.lilac.datamodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminPlayListRequest {
    private int plType;
    private int pageNum;
    private int[] licenseIds;
    private int[] subjectIds;
}