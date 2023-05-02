package site.mylittlestore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import site.mylittlestore.config.auth.PrincipalUserDetails;
import site.mylittlestore.dto.member.MemberFindDto;
import site.mylittlestore.service.member.MemberService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {
    private final MemberService memberService;

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        log.info("home controller");

        if (authentication != null) {
            PrincipalUserDetails principal = (PrincipalUserDetails) authentication.getPrincipal();
            model.addAttribute("username", principal.getUsername());
            MemberFindDto memberFindDtoByEmail = memberService.findMemberFindDtoByEmail(principal.getUsername());
            model.addAttribute("memberFindDto", memberFindDtoByEmail);
        }

        return "home";
    }
}
