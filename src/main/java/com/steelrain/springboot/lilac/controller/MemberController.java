package com.steelrain.springboot.lilac.controller;

import com.steelrain.springboot.lilac.common.AlertRedirectUtil;
import com.steelrain.springboot.lilac.common.ICacheService;
import com.steelrain.springboot.lilac.common.SESSION_KEY;
import com.steelrain.springboot.lilac.datamodel.*;
import com.steelrain.springboot.lilac.datamodel.view.MemberLoginDTO;
import com.steelrain.springboot.lilac.datamodel.view.MemberProfileEditDTO;
import com.steelrain.springboot.lilac.datamodel.view.MemberRegDTO;
import com.steelrain.springboot.lilac.exception.DuplicateLilacMemberException;
import com.steelrain.springboot.lilac.exception.LilacRepositoryException;
import com.steelrain.springboot.lilac.service.IMemberService;
import com.steelrain.springboot.lilac.validate.LoginValidationSequence;
import com.steelrain.springboot.lilac.validate.RegistrationValidateSequence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Objects;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("member")
public class MemberController {

    private final IMemberService m_memberService;
    private final ICacheService m_keywordCategoryCacheService;

/*
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
    }*/

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
            log.error("로그인 에러 : {}", bindingResult);
            return "member/login";
        }
        MemberDTO memberDTO = m_memberService.loginMember(loginDTO.getEmail(), loginDTO.getPassword());
        if(memberDTO != null){
            HttpSession session = servletRequest.getSession();
            session.setAttribute(SESSION_KEY.MEMBER_ID, memberDTO.getId());
            session.setAttribute(SESSION_KEY.MEMBER_NICKNAME, memberDTO.getNickname());
            session.setAttribute(SESSION_KEY.MEMBER_EMAIL, memberDTO.getEmail());
            session.setAttribute(SESSION_KEY.MEMBER_GRADE, memberDTO.getGrade());
            session.setAttribute(SESSION_KEY.MEMBER_PROFILE, memberDTO.getProfileSave());
            if(memberDTO.getGrade() == 1){
                return "redirect:/admin/admin-menu-pl";
            }
            return "redirect:" + redirectURL;
        }else{
            bindingResult.addError(new ObjectError("LOGIN-ERR","아이디 또는 비밀번호가 맞지않습니다."));
            return "member/login";
        }
    }

    @GetMapping("/registration")
    public String registerForm(Model model){
        model.addAttribute("memberReg", new MemberRegDTO());
        model.addAttribute("libRegionCodes", m_keywordCategoryCacheService.getLibraryRegionCodeList());
        return "member/registration";
    }

    @PostMapping("/registration")
    public String registerMember(@Validated(RegistrationValidateSequence.class) @ModelAttribute("memberReg") MemberRegDTO memberRegDTO, BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            log.error("회원가입 입력정보 에러 : 입력한 회원정보 - {}, 에러정보 - {}", memberRegDTO, bindingResult);
            // 지역코드 입력에러가 발생해도 지역코드정보를 넘겨줘야 사용자가 지역코드를 선택할 수 있다
            model.addAttribute("libRegionCodes", m_keywordCategoryCacheService.getLibraryRegionCodeList());
            return "member/registration";
        }
        MemberDTO memberDTO = MemberDTO.builder()
                .nickname(memberRegDTO.getNickname())
                .email(memberRegDTO.getEmail())
                .password(memberRegDTO.getPassword())
                .region(Objects.isNull(m_keywordCategoryCacheService.getRegionName(memberRegDTO.getRegion())) ? null : memberRegDTO.getRegion())
                .dtlRegion(Objects.isNull(m_keywordCategoryCacheService.getDetailRegionName(memberRegDTO.getRegion(), memberRegDTO.getDtlRegion())) ? null : memberRegDTO.getDtlRegion())
                .grade(USER_GRADE.NORMAL_MEMBER.getGrade()) // 1번은 관리자, 2번은 일반회원이므로 기본값으로 2를 설정해준다
                .build();
        boolean isRegist = false;
        try{
            isRegist = m_memberService.registerMember(memberDTO);
            return  isRegist ? "redirect:login" : "redirect:registration";
        }catch(DuplicateLilacMemberException dme){
            log.error("회원가입 중복 예외발생 : 회원정보 - {}, 예외정보 - {}", memberRegDTO, dme);
            bindingResult.addError(new ObjectError("memberReg", "이미 가입된 회원입니다. 다른 닉네임 또는 이메일을 입력해주세요"));
            return "member/registration";
        }catch(LilacRepositoryException lre){
            log.error("회원가입 DB 예외발생 : 회원정보 - {}, 예외정보 - {}", memberRegDTO, lre);
            bindingResult.addError(new ObjectError("memberReg", "회원가입에 오류가 발생하였습니다"));
            return "member/registration";
        }
    }

    @GetMapping("/profile")
    public String profileForm(HttpSession session, Model model){
        MemberDTO memberDTO = m_memberService.getMemberInfo((Long)session.getAttribute(SESSION_KEY.MEMBER_ID));
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
        model.addAttribute("libRegionCodes", m_keywordCategoryCacheService.getLibraryRegionCodeList());

        return "member/profile";
    }

    // TODO 에러가 났을경우 프로필 이미지 처리를 프런트에서 처리할 방법을 알아보자
    @PostMapping("/profile")
    public String editMemberProfile(@Validated @ModelAttribute("memberInfo") MemberProfileEditDTO editDTO, BindingResult bindingResult,
                                              HttpSession session, Model model){
        if(bindingResult.hasErrors()){
            log.error("회원정보수정 입력값 에러 : 회원입력정보 - {}, 에러정보 - {}", editDTO, bindingResult);
            // 에러가 발생해도 기존의 프로필 이미지는 보이도록 session에서 원래 프로필이미지가져와서 설정해준다
            editDTO.setProfileSave( (String) session.getAttribute(SESSION_KEY.MEMBER_PROFILE));
            return "member/profile";
        }
        Long memberId = (Long)session.getAttribute(SESSION_KEY.MEMBER_ID);
        try{
            boolean isUpdated = m_memberService.updateMemberInfo(memberId, editDTO);
            if(isUpdated){
                MemberDTO updatedMember = m_memberService.getMemberInfo(memberId);
                // 세션정보도 같이 업데이트 해준다
                session.setAttribute(SESSION_KEY.MEMBER_NICKNAME, updatedMember.getNickname());
                session.setAttribute(SESSION_KEY.MEMBER_EMAIL, updatedMember.getEmail());
                session.setAttribute(SESSION_KEY.MEMBER_PROFILE, updatedMember.getProfileSave());
            }else{
                log.error("회원정보수정 업데이트 에러 : 회원입력정보 - {}, 현재로그인ID - {}", editDTO, memberId);
                return AlertRedirectUtil.alertRedirect("회원정보 수정에 문제가 발생했습니다.", "profile", model);
            }
            return "redirect:profile";
        }catch(DuplicateLilacMemberException dme){
            log.error("회원정보수정 중복 예외 발생 : 회원정보 - {}, 예외정보 - {}", editDTO, dme);
            bindingResult.addError(new ObjectError("memberInfo","이미 존재하는 닉네임 또는 이메일입니다. 다른 닉네임, 이메일을 입력해주세요"));
            // 에러가 발생해도 기존의 프로필 이미지는 보이도록 하기 위해 설정해준다
            editDTO.setProfileSave( (String)session.getAttribute(SESSION_KEY.MEMBER_PROFILE));
            return "member/profile";
        }catch(LilacRepositoryException lre){
            log.error("회원정보수정 DB 예외 발생 : 회원정보 - {}, 예외정보 - {}", editDTO, lre);
            // 에러가 발생해도 기존의 프로필 이미지는 보이도록 하기 위해 설정해준다
            editDTO.setProfileSave( (String)session.getAttribute(SESSION_KEY.MEMBER_PROFILE));
            bindingResult.addError(new ObjectError("memberInfo","회원정보 업데이트 도중 문제가 발행했습니다. 관리자에게 문의하세요"));
            return "member/profile";
        }
    }

    @PostMapping("/delete")
    public String deleteMember(HttpServletRequest servletRequest, Model model){
        HttpSession session = servletRequest.getSession(false);
        if(Objects.isNull(session)){
            return "redirect:login";
        }
        Long memberId = (Long) session.getAttribute(SESSION_KEY.MEMBER_ID);
        try{
            m_memberService.deleteMember(memberId);
            session.invalidate();
            String ctxPath = servletRequest.getContextPath();
            return AlertRedirectUtil.alertRedirect("회원탈퇴를 완료하였습니다", StringUtils.hasText(ctxPath) ? ctxPath : "/", model);
        }catch(Exception ex){
            log.error("회원탈퇴 처리중 예외발생 - 회원정보 : ID - {}, 예외정보 - {}", memberId, ex);
            return AlertRedirectUtil.alertRedirect("회원탈퇴중 예외가 발생했습니다", "profile", model);
        }
    }



    @GetMapping("/duplicated-email/{email}")
    public ResponseEntity<String> checkDuplicatedEmail(@PathVariable("email") String email){
        if(email.length() > 100){
            return new ResponseEntity<>("이메일주소는 100자 이하이어야 합니다.", HttpStatus.BAD_REQUEST);
        }
        return m_memberService.checkDuplicatedEmail(email) ?
                new ResponseEntity<>("중복된 이메일주소", HttpStatus.CONFLICT):
                new ResponseEntity<>("사용가능한 이메일주소", HttpStatus.OK);
    }
    
    @GetMapping("/duplicated-nickname/{nickname}")
    public ResponseEntity<String> checkDuplicatedNickname(@PathVariable("nickname") String nickname) {
        if (nickname.length() < 1 || nickname.length() > 20) {
            return new ResponseEntity<>("닉네임은 1자 이상 20자 이하이어야 합니다.", HttpStatus.BAD_REQUEST);
        }
        return m_memberService.checkDuplicatedNickName(nickname) ?
                new ResponseEntity<>("중복된 닉네임", HttpStatus.CONFLICT) :
                new ResponseEntity<>("사용가능한 닉네임", HttpStatus.OK);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest servletRequest){
        HttpSession session = servletRequest.getSession(false);
        if(Objects.nonNull(session)){
            session.invalidate();
        }
        return "redirect:/";
    }
}
