package com.steelrain.springboot.lilac.datamodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminPlayListRequest extends AdminSearchRequest {
    private ADMIN_PLAYLIST_TYPE plType;
}
