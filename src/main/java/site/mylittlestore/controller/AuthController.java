package site.mylittlestore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.form.auth.MemberLogInForm;
import site.mylittlestore.form.auth.MemberSignUpForm;
import site.mylittlestore.service.MemberService;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;

    private final PasswordEncoder passwordEncoder;

    @GetMapping("/auth/login")
    public String logInForm(Model model) {
        model.addAttribute("memberLogInForm", new MemberLogInForm());
        return "auth/logInForm";
    }

    @PostMapping("/auth/login")
    public String logIn(@Valid MemberLogInForm memberLogInForm, BindingResult result) {
        //잘못된 값이 들어오면, 다시 로그인 폼으로 돌아간다.
        if (result.hasErrors()) {
            return "auth/logInForm";
        }

        return "redirect:/members";
    }

    @GetMapping("auth/signup")
    public String signUpForm(Model model) {
        model.addAttribute("memberSignUpForm", new MemberSignUpForm());
        return "auth/signUpForm";
    }

    @PostMapping("auth/signup")
    public String signUp(@Valid MemberSignUpForm memberSignUpForm, BindingResult result, Model model) {
        //잘못된 값이 들어오면, 다시 로그인 폼으로 돌아간다.
        if (result.hasErrors()) {
            model.addAttribute("memberSignUpForm", memberSignUpForm);
            return "auth/signUpForm";
        }

        Long memberId = memberService.joinMember(MemberCreationDto.builder()
                .name(memberSignUpForm.getName())
                .email(memberSignUpForm.getEmail())
                .password(passwordEncoder.encode(memberSignUpForm.getPassword()))
                .city(memberSignUpForm.getCity())
                .street(memberSignUpForm.getStreet())
                .zipcode(memberSignUpForm.getZipcode())
                .build());

        return "redirect:/auth/login";
    }

    @GetMapping("/auth/oauth2/member/{memberId}/switchrole")
    public String switchRole(@PathVariable Long memberId) {
        memberService.switchRole(memberId);

        return "redirect:/";
    }
}