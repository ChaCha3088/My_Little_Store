package site.mylittlestore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.enumstorage.errormessage.auth.EmailErrorMessage;
import site.mylittlestore.enumstorage.message.EmailMessage;
import site.mylittlestore.exception.auth.PasswordException;
import site.mylittlestore.exception.member.temporarymember.NoSuchTemporaryMemberException;
import site.mylittlestore.form.auth.MemberLogInForm;
import site.mylittlestore.form.auth.MemberSignUpForm;
import site.mylittlestore.message.Message;
import site.mylittlestore.service.member.MemberService;
import site.mylittlestore.service.member.TemporaryMemberService;
import site.mylittlestore.util.Validator;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;
    private final TemporaryMemberService temporaryMemberService;
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
    public String temporarySignUp(@Valid MemberSignUpForm memberSignUpForm, BindingResult result, Model model) {
        //이메일 규칙 검사
        if (!Validator.isValidEmail(memberSignUpForm.getEmail())) {
            result.addError(new FieldError("memberSignUpForm", "email", EmailErrorMessage.NOT_VALID_EMAIL.getMessage()));
        }

        //비밀번호 규칙 검사
        try
        {
            Validator.isValidPassword(memberSignUpForm.getEmail(), memberSignUpForm.getPassword());
        } catch (PasswordException e) {
            result.addError(new FieldError("memberSignUpForm", "password", e.getMessage()));
        }

        //잘못된 값이 들어오면, 다시 로그인 폼으로 돌아간다.
        if (result.hasErrors()) {
            model.addAttribute("memberSignUpForm", memberSignUpForm);
            return "auth/signUpForm";
        }

        //중복된 이메일이 존재하는지 확인한다.
        if (memberService.isEmailValid(memberSignUpForm.getEmail()))
        //중복된 이메일이 존재하지 않으면,
        {
            try {
                //임시 회원가입을 시도한다.
                Long temporaryMemberId = temporaryMemberService.joinTemporaryMember(MemberCreationDto.builder()
                        .name(memberSignUpForm.getName())
                        .email(memberSignUpForm.getEmail())
                        .password(passwordEncoder.encode(memberSignUpForm.getPassword()))
                        .city(memberSignUpForm.getCity())
                        .street(memberSignUpForm.getStreet())
                        .zipcode(memberSignUpForm.getZipcode())
                        .build());

                //임시 회원가입이 완료되면, 인증 메일 메시지를 띄우고, 로그인 페이지로 이동한다.
                model.addAttribute("messages", Message.builder()
                                .message(EmailMessage.VERIFICATION_EMAIL_SENT.getMessage())
                                .href("/auth/login")
                                        .build());
                return "message/message";

            } catch (DataIntegrityViolationException e) {
                //중복된 이메일은 존재 하지 않지만, 임시 회원가입 기록이 존재하는 경우
                Long idByEmail = temporaryMemberService.findIdByEmail(memberSignUpForm.getEmail());

                model.addAttribute("messages", Message.builder()
                        .message(EmailMessage.CHECK_VERIFICATION_EMAIL.getMessage())
                        .href("/auth/member/verification-email/notice/" + idByEmail)
                                .build());

                return "message/message";
            } catch (UnsupportedEncodingException e) {
                model.addAttribute("messages", Message.builder()
                        .message(e.getMessage())
                        .href("/auth/login")
                        .build());

                return "message/message";
            }
        }

        //중복된 이메일은 존재 하지 않지만, 임시 회원가입 기록이 존재하는 경우
        Long idByEmail = temporaryMemberService.findIdByEmail(memberSignUpForm.getEmail());

        model.addAttribute("messages", Message.builder()
                .message(EmailMessage.CHECK_VERIFICATION_EMAIL.getMessage())
                .href("/auth/member/verification-email/notice/" + idByEmail)
                        .build());

        return "message/message";

////        Long memberId = memberService.joinMember(MemberCreationDto.builder()
////                .name(memberSignUpForm.getName())
////                .email(memberSignUpForm.getEmail())
////                .password(passwordEncoder.encode(memberSignUpForm.getPassword()))
////                .city(memberSignUpForm.getCity())
////                .street(memberSignUpForm.getStreet())
////                .zipcode(memberSignUpForm.getZipcode())
////                .build());
//
//        return "redirect:/auth/login";
    }

    @GetMapping("/auth/member/verification-email/{verificationCode}")
    public String verifyEmail(@PathVariable String verificationCode, Model model) {
        try {
            temporaryMemberService.verifyEmail(verificationCode);
            model.addAttribute("messages", Message.builder()
                    .message(EmailMessage.VERIFICATION_EMAIL_SUCCESS.getMessage())
                    .href("/auth/login")
                            .build());

            return "message/message";

        } catch (NoSuchTemporaryMemberException e) {
            model.addAttribute("messages", Message.builder()
                    .message(e.getMessage())
                    .href("/auth/login")
                            .build());
            return "message/message";
        }
    }

    @GetMapping("/auth/member/verification-email/notice/{temporaryMemberId}")
    public String verificationEmailNotice(@PathVariable Long temporaryMemberId, Model model) {
        model.addAttribute("temporaryMemberId", temporaryMemberId);
        return "auth/member/verificationEmailNotice";
    }

    @GetMapping("/auth/member/verification-email/resend/{temporaryMemberId}")
    public String resendVerificationEmail(@PathVariable Long temporaryMemberId, Model model) {
        try {
            temporaryMemberService.resendVerificationEmail(temporaryMemberId);
            model.addAttribute("messages", Message.builder()
                    .message(EmailMessage.VERIFICATION_EMAIL_RESENT.getMessage())
                    .href("/auth/login")
                            .build());

            return "message/message";
        } catch (NoSuchTemporaryMemberException | UnsupportedEncodingException e) {
            model.addAttribute("messages", Message.builder()
                    .message(e.getMessage())
                    .href("/auth/login")
                    .build());
            return "message/message";
        }
    }

    @GetMapping("/auth/oauth2/member/{memberId}/switchrole")
    public String switchRole(@PathVariable Long memberId) {
        memberService.switchRole(memberId);

        return "redirect:/";
    }
}