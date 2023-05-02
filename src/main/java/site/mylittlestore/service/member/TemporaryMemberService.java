package site.mylittlestore.service.member;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.mylittlestore.domain.member.Member;
import site.mylittlestore.domain.member.TemporaryMember;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.enumstorage.errormessage.member.temporarymember.TemporaryMemberErrorMessage;
import site.mylittlestore.enumstorage.message.EmailMessage;
import site.mylittlestore.exception.member.temporarymember.NoSuchTemporaryMemberException;
import site.mylittlestore.repository.member.MemberRepository;
import site.mylittlestore.repository.member.temporarymember.TemporaryMemberRepository;
import site.mylittlestore.service.email.EmailService;
import site.mylittlestore.util.CodeGenerator;
import site.mylittlestore.util.email.Email;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TemporaryMemberService {
    private final EmailService emailService;
    private final MemberRepository memberRepository;
    private final TemporaryMemberRepository temporaryMemberRepository;

    public Long findIdByEmail(String email) throws NoSuchTemporaryMemberException {
        return temporaryMemberRepository.findIdByEmail(email)
                .orElseThrow(() -> new NoSuchTemporaryMemberException(TemporaryMemberErrorMessage.NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_EMAIL.getMessage()));
    }

    @Transactional
    public Long joinTemporaryMember(MemberCreationDto memberCreationDto) throws DataIntegrityViolationException, UnsupportedEncodingException {
        //verificationCode 생성
        String verificationCode = CodeGenerator.generateCode(20);

        //temporaryMember 생성
        Long temporaryMemberId = temporaryMemberRepository.save(TemporaryMember.builder()
                        .name(memberCreationDto.getName())
                        .email(memberCreationDto.getEmail())
                        .password(memberCreationDto.getPassword())
                        .city(memberCreationDto.getCity())
                        .street(memberCreationDto.getStreet())
                        .zipcode(memberCreationDto.getZipcode())
                        .verificationCode(verificationCode)
                        .build())
                .getId();

        //이메일 발송
        emailService.sendMail(Email.builder()
                .subject(EmailMessage.VERIFICATION_EMAIL_SUBJECT.getMessage())
                .receiver(memberCreationDto.getEmail())
                .message(EmailMessage.VERIFICATION_EMAIL_MESSAGE.getMessage() +
                        EmailMessage.VERIFICATION_EMAIL_LINK.getMessage() +
                        URLEncoder.encode(verificationCode, "UTF-8"))
                .build());

        return temporaryMemberId;
    }

    @Transactional
    public void resendVerificationEmail(Long id) throws NoSuchTemporaryMemberException, UnsupportedEncodingException {
        TemporaryMember temporaryMember = temporaryMemberRepository.findById(id)
                .orElseThrow(() -> new NoSuchTemporaryMemberException(TemporaryMemberErrorMessage.NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_ID.getMessage()));

        //verificationCode 생성
        String verificationCode = CodeGenerator.generateCode(20);

        //temporaryMember의 verificationCode 변경
        temporaryMember.updateVerificationCode(verificationCode);

        //이메일 발송
        emailService.sendMail(Email.builder()
                .subject(EmailMessage.VERIFICATION_EMAIL_SUBJECT.getMessage())
                .receiver(temporaryMember.getEmail())
                .message(EmailMessage.VERIFICATION_EMAIL_MESSAGE.getMessage() +
                        EmailMessage.VERIFICATION_EMAIL_LINK.getMessage() +
                        URLEncoder.encode(verificationCode, "UTF-8"))
                .build());
    }

    @Transactional
    public void verifyEmail(String verificationCode) throws NoSuchTemporaryMemberException {
        TemporaryMember temporaryMember = temporaryMemberRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new NoSuchTemporaryMemberException(TemporaryMemberErrorMessage.NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_VERIFICATION_CODE.getMessage()));

        //verificationCode가 일치하면
        if (verificationCode.equals(temporaryMember.getVerificationCode())) {
            //member 생성
            memberRepository.save(Member.builder()
                            .name(temporaryMember.getName())
                            .email(temporaryMember.getEmail())
                            .password(temporaryMember.getPassword())
                            .city(temporaryMember.getAddress().getCity())
                            .street(temporaryMember.getAddress().getStreet())
                            .zipcode(temporaryMember.getAddress().getZipcode())
                            .build());

            //temporaryMember 삭제
            temporaryMemberRepository.delete(temporaryMember);
        } else {
            throw new NoSuchTemporaryMemberException(TemporaryMemberErrorMessage.NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_VERIFICATION_CODE.getMessage());
        }
    }

}
