package com.steelrain.springboot.lilac.config;

import lombok.Getter;

/**
 * 라일락이 사용하는 모든 외부 API 들의 인증키, 서비스 URL 정보를 담고 있는 클래스
 */
@Getter
public class APIConfig {

    private String youtubeKey;
    private String kakaoRestKey;
    private String naruLibraryByBookApiKey;
    private String kakaoRestUrl;
    private String naruLibraryByBookUrl;
    private String naruBookExistUrl;
    private String licenseSchdApikey;
    private String licenseSchdUrl;
    private String licenseMajorApiKey;
    private String licenseMajorApiUrl;
    private String libraryByRegionUrl;
    private String awsS3accessKey;
    private String awsS3SecretKey;
    private String awsS3Bucket;
    private String awsS3BaseDir;


    public APIConfig(String youtubeApikey,
                     String kakaoRestApikey,
                     String naruLibraryByBookApiKey,
                     String licenseSchdApikey,
                     String licenseMajorApiKey,
                     String kakaoRestUrl,
                     String naruLibraryByBookUrl,
                     String naruBookExistUrl,
                     String licenseSchdUrl,
                     String licenseMajorApiUrl,
                     String libraryByRegionUrl,
                     String awsS3accessKey,
                     String awsS3SecretKey,
                     String awsS3Bucket,
                     String awsS3BaseDir){
        this.youtubeKey = youtubeApikey;
        this.kakaoRestKey = kakaoRestApikey;
        this.licenseSchdApikey = licenseSchdApikey;
        this.naruLibraryByBookApiKey = naruLibraryByBookApiKey;
        this.licenseMajorApiKey = licenseMajorApiKey;
        this.kakaoRestUrl = kakaoRestUrl;
        this.naruLibraryByBookUrl = naruLibraryByBookUrl;
        this.naruBookExistUrl = naruBookExistUrl;
        this.licenseSchdUrl = licenseSchdUrl;
        this.licenseMajorApiUrl = licenseMajorApiUrl;
        this.libraryByRegionUrl = libraryByRegionUrl;
        this.awsS3accessKey = awsS3accessKey;
        this.awsS3SecretKey = awsS3SecretKey;
        this.awsS3Bucket = awsS3Bucket;
        this.awsS3BaseDir = awsS3BaseDir;
    }
}
