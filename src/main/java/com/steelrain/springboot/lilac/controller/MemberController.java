package com.steelrain.springboot.lilac.controller;

import com.steelrain.springboot.lilac.common.ICacheService;
import com.steelrain.springboot.lilac.common.SESSION_KEY;
import com.steelrain.springboot.lilac.datamodel.LibraryRegionCodeDTO;
import com.steelrain.springboot.lilac.datamodel.LicenseCodeDTO;
import com.steelrain.springboot.lilac.datamodel.SubjectCodeDTO;
import com.steelrain.springboot.lilac.datamodel.view.MemberLoginDTO;
import com.steelrain.springboot.lilac.datamodel.MemberDTO;
import com.steelrain.springboot.lilac.datamodel.view.MemberProfileEditDTO;
import com.steelrain.springboot.lilac.datamodel.view.MemberRegDTO;
import com.steelrain.springboot.lilac.service.IMemberService;
import com.steelrain.springboot.lilac.validate.LoginValidationSequence;
import com.steelrain.springboot.lilac.validate.RegistrationValidateSequence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final IMemberService m_memberService;
    private final ICacheService m_keywordCategoryCacheService;


    @ModelAttribute("subjectCodes")
    public List<SubjectCodeDTO> getSubjectCodes(){
        return m_keywordCategoryCacheService.getSubjectCodeList();
    }

    @ModelAttribute("libRegionCodes")
    public List<LibraryRegionCodeDTO> getLibRegionCodes(){
        return m_keywordCategoryCacheService.getLibraryRegionCodeList();
    }

    @ModelAttribute("licenseCodes")
    public List<LicenseCodeDTO> getLicenseCodes(){
        return m_keywordCategoryCacheService.getLicenseCodeList();
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "redirectURL", defaultValue = "/") String redirectURL, Model model){
        model.addAttribute("memberLogin", new MemberLoginDTO());
        model.addAttribute("redirectURL", redirectURL);
        return "member/login";
    }

    @PostMapping("/login")
    public String login(@Validated(LoginValidationSequence.class) @ModelAttribute("memberLogin") MemberLoginDTO loginDTO, BindingResult bindingResult,
                        @RequestParam(value = "redirectURL", defaultValue = "/") String redirectURL,
                        HttpServletRequest servletRequest){

        if(bindingResult.hasErrors()){
            log.info("????????? ?????? : {}", bindingResult);
            return "/member/login";
        }
        MemberDTO memberDTO = m_memberService.loginMember(loginDTO.getEmail(), loginDTO.getPassword());
        if(memberDTO != null){
            HttpSession session = servletRequest.getSession();
            session.setAttribute(SESSION_KEY.LOGIN_MEMBER, memberDTO);
            return "redirect:" + redirectURL;
        }else{
            return "/member/login";
        }
    }

    @GetMapping("/registration")
    public String registerForm(Model model){
        model.addAttribute("memberReg", new MemberRegDTO());
        return "member/registration";
    }

    @PostMapping("/registration")
    public String registerMember(@Validated(RegistrationValidateSequence.class) @ModelAttribute("memberReg") MemberRegDTO memberRegDTO, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            log.info("???????????? ?????? : {}", bindingResult);
            return "/member/registration";
        }

        MemberDTO memberDTO = MemberDTO.builder()
                .nickname(memberRegDTO.getNickname())
                .email(memberRegDTO.getEmail())
                .password(memberRegDTO.getPassword())
                .build();
        return  m_memberService.registerMember(memberDTO) ? "redirect:/member/registration" : "redirect:/";
    }

    @GetMapping("/profile")
    public String profileForm(HttpServletRequest servletRequest, Model model){
        HttpSession session = servletRequest.getSession(false);
        if(session == null){
            return "redirect:/";
        }
        MemberDTO memberDTO = (MemberDTO) session.getAttribute(SESSION_KEY.LOGIN_MEMBER);
        MemberProfileEditDTO memberEditDTO = new MemberProfileEditDTO();
        memberEditDTO.setNickname(memberDTO.getNickname());
        memberEditDTO.setEmail(memberDTO.getEmail());
        memberEditDTO.setRegion(memberDTO.getRegion());
        memberEditDTO.setDtlRegion(memberDTO.getDtlRegion());
        memberEditDTO.setDescription(memberDTO.getDescription());
        memberEditDTO.setProfileOriginal(memberDTO.getProfileOriginal());
        memberEditDTO.setProfileSave(memberDTO.getProfileSave());
        memberEditDTO.setRegDate(memberDTO.getRegDate());
        model.addAttribute("memberInfo", memberEditDTO);

        return "member/profile";
    }

    @PostMapping("/profile")
    public RedirectView editMemberProfile(@Validated @ModelAttribute("memberInfo") MemberProfileEditDTO editDTO, BindingResult bindingResult,
                                          HttpServletRequest servletRequest, RedirectAttributes attributes){
        if(bindingResult.hasErrors()){
            log.error("???????????? ?????? ?????? : {}", bindingResult);
            return new RedirectView("/member/profile");
        }
        HttpSession session = servletRequest.getSession(false);
        if(session == null){
            return new RedirectView("/");
        }
        MemberDTO memberDTO = (MemberDTO) session.getAttribute(SESSION_KEY.LOGIN_MEMBER);
        m_memberService.updateMemberInfo(memberDTO, editDTO);

        return new RedirectView("/member/profile");
    }

    @GetMapping("/duplicated-email/{email}")
    public ResponseEntity<String> checkDuplicatedEmail(@PathVariable("email") String email){
        if(email.length() > 100){
            return new ResponseEntity<>("?????????????????? 100??? ??????????????? ?????????.", HttpStatus.BAD_REQUEST);
        }
        return m_memberService.checkDuplicatedEmail(email) ?
                new ResponseEntity<>("????????? ???????????????", HttpStatus.CONFLICT):
                new ResponseEntity<>("??????????????? ???????????????", HttpStatus.OK);
    }
    
    @GetMapping("/duplicated-nickname/{nickname}")
    public ResponseEntity<String> checkDuplicatedNickname(@PathVariable("nickname") String nickname) {
        if (nickname.length() < 1 || nickname.length() > 20) {
            return new ResponseEntity<>("???????????? 1??? ?????? 20??? ??????????????? ?????????.", HttpStatus.BAD_REQUEST);
        }
        return m_memberService.checkDuplicatedNickName(nickname) ?
                new ResponseEntity<>("????????? ?????????", HttpStatus.CONFLICT) :
                new ResponseEntity<>("??????????????? ?????????", HttpStatus.OK);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest servletRequest){
        HttpSession session = servletRequest.getSession(false);
        if(session != null){
            session.invalidate();
        }
        return "redirect:/";
    }
}
