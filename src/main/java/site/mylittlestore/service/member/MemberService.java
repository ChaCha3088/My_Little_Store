package site.mylittlestore.service.member;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.mylittlestore.domain.member.Member;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.dto.member.MemberFindDto;
import site.mylittlestore.dto.member.MemberPasswordUpdateDto;
import site.mylittlestore.dto.member.MemberUpdateDto;
import site.mylittlestore.enumstorage.errormessage.MemberErrorMessage;
import site.mylittlestore.exception.member.DuplicateMemberException;
import site.mylittlestore.exception.member.MemberPasswordDoesNotMatchException;
import site.mylittlestore.exception.member.NoSuchMemberException;
import site.mylittlestore.repository.member.MemberRepository;
import site.mylittlestore.repository.member.temporarymember.TemporaryMemberRepository;
import site.mylittlestore.repository.store.StoreRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final TemporaryMemberRepository temporaryMemberRepository;

    public MemberFindDto findMemberFindDtoById(Long memberId) throws NoSuchMemberException {
        return memberRepository.findActiveById(memberId)
                //회원이 없으면 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()))
                //Dto로 변환
                .toMemberFindDto();
    }


    public MemberFindDto findMemberFindDtoByEmail(String email) throws NoSuchMemberException {
        return memberRepository.findActiveByEmail(email)
                //해당하는 이메일을 가진 회원이 없으면, 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()))
                //Dto로 변환
                .toMemberFindDto();
    }

    public boolean isEmailValid(String email) {
        return !(memberRepository.findActiveIdByEmail(email).isPresent());
    }

    public List<MemberFindDto> findAllMemberFindDto() {
        return memberRepository.findAll().stream()
                .map(Member::toMemberFindDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long joinMember(MemberCreationDto memberCreationDto) throws DataIntegrityViolationException {
        return memberRepository.save(Member.builder()
                        .name(memberCreationDto.getName())
                        .email(memberCreationDto.getEmail())
                        .password(memberCreationDto.getPassword())
                        .city(memberCreationDto.getCity())
                        .street(memberCreationDto.getStreet())
                        .zipcode(memberCreationDto.getZipcode())
                .build())
            .getId();
    }

    /**
     * 회원의 정보를 수정한다.
     * 이름과 주소 수정 가능
     * @param memberUpdateDto
     * @return
     * @throws NoSuchMemberException
     */
    @Transactional
    public Long updateMember(MemberUpdateDto memberUpdateDto) throws NoSuchMemberException {
        //업데이트하려는 회원이 있는지 검증
        Member findMemberById = findById(memberUpdateDto.getId());

        //회원의 정보 업데이트
        findMemberById.updateMemberName(memberUpdateDto.getName());
        findMemberById.updateMemberAddress(memberUpdateDto.getCity(), memberUpdateDto.getStreet(), memberUpdateDto.getZipcode());

        //저장
        Member savedMember = memberRepository.save(findMemberById);

        return savedMember.getId();
    }

    @Transactional
    public void updateMemberPassword(MemberPasswordUpdateDto memberPasswordUpdateDto) throws NoSuchMemberException, MemberPasswordDoesNotMatchException {
        Member findMemberById = findById(memberPasswordUpdateDto.getId());

        //비밀번호 검증
        if (!findMemberById.getPassword().equals(memberPasswordUpdateDto.getPassword())) {
            throw new MemberPasswordDoesNotMatchException(MemberErrorMessage.PASSWORD_DOES_NOT_MATCH.getMessage());
        }

        //회원의 정보 업데이트
        findMemberById.updateMemberPassword(memberPasswordUpdateDto.getNewPassword());

        //회원의 정보 저장
        memberRepository.save(findMemberById);
    }

    /**
     * 테스트용
     * @param memberId
     */
    @Transactional
    public void switchRole(Long memberId) {
        Member member = memberRepository.findActiveById(memberId)
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));

        member.switchRole();

        memberRepository.save(member);
    }

    private Member findById(Long memberId) throws NoSuchMemberException {
        return memberRepository.findActiveById(memberId)
                //해당하는 Id를 가진 회원이 없으면, 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));
    }
}
