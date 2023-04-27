package com.steelrain.springboot.lilac.controller;

import com.steelrain.springboot.lilac.datamodel.AdminPlayListRequest;
import com.steelrain.springboot.lilac.service.IAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final static int PAGE_COUNT = 10;
    private final IAdminService m_adminService;


    @GetMapping("/admin-menu-pl")
    public String adminForm(Model model){
        model.addAttribute("totalPlcnt", m_adminService.getTotalPlayListCount());
        model.addAttribute("todayPlcnt", m_adminService.getTodayPlayListCount());
        model.addAttribute("weekPlcnt", m_adminService.getWeekPlayListCount());
        model.addAttribute("monthPlcnt", m_adminService.getMonthPlayListCount());
        return "admin/admin-menu-pl";
    }

    @GetMapping("/admin-menu-book")
    public String adminBookForm(Model model){
        model.addAttribute("totalBookCnt", m_adminService.getTotalBookCount());
        model.addAttribute("todayBookCnt", m_adminService.getTodayBookCount());
        return "admin/admin-menu-book";
    }

    @PostMapping("/admin-playlist-template")
    public String getAdminPlayListTemplate(@ModelAttribute AdminPlayListRequest request, Model model){
        prePatch(request);
        model.addAttribute("plInfo", m_adminService.getAdminPlayList(request.getPlType(), request.getPageNum(), PAGE_COUNT, request.getLicenseIds(), request.getSubjectIds()));
        return "admin/playlist-template";
    }

    @GetMapping("/candi-pl")
    public String candiPlayListTemplate(Model model){
        model.addAttribute("cpl", m_adminService.getCandiPlayList());
        return "admin/candi-pl-template";
    }

    @PostMapping("/remove-candi-pl")
    public String removeCandiPlayList(@RequestParam("plId") Long playlistId, Model model){
        model.addAttribute("cpl", m_adminService.removeCandiPlayList(playlistId));
        return "admin/candi-pl-template";
    }

    @PostMapping("/update-recommend-pl")
    public String udpateRecommendPlayList(@RequestParam(value="cplArray[]") List<Long> cplList, Model model){
        model.addAttribute("rpl", m_adminService.updateRecommendPlayList(cplList));
        return "admin/recommend-pl-template";
    }

    @GetMapping("/recommend-pl")
    public String recommendPlayListTemplate(Model model){
        model.addAttribute("rpl", m_adminService.getRecommendPlayList());
        return "admin/recommend-pl-template";
    }

    @PostMapping("/remove-recommend-pl")
    public String removeRecommendPlayList(@RequestParam("plId")Long playListId, Model model){
        model.addAttribute("rpl", m_adminService.removeRecommendPlayList(playListId));
        return "admin/recommend-pl-template";
    }
    /*
        - 프런트에서 jQuery ajax로 licenseIds, subjectIds 배열을 넘길때 null 로 설정해서 보냈을때
          컨트롤러에서 null 로 받지않고 길이가 0 인 int 배열로 받아진다.
          : 아직 원인이 무엇인지 파악하지 못했으므로 일단은 길이가 0 일때 null로 세팅해준다.
     */
    private void prePatch(AdminPlayListRequest request){
        if(request.getLicenseIds() != null && request.getLicenseIds().length == 0){
            request.setLicenseIds(null);
        }
        if(request.getSubjectIds() != null && request.getSubjectIds().length == 0){
            request.setSubjectIds(null);
        }
    }
}
