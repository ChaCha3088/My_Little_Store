package site.mylittlestore.config.auth.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.mylittlestore.config.auth.PrincipalUserDetails;
import site.mylittlestore.domain.member.Member;
import site.mylittlestore.enumstorage.errormessage.MemberErrorMessage;
import site.mylittlestore.exception.member.NoSuchMemberException;
import site.mylittlestore.repository.member.MemberRepository;

@Service
@RequiredArgsConstructor
public class PrincipalUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    //이런 요청이 들어왔는데, 얘 혹시 회원이야?
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //Member를 찾는다.
        Member member = memberRepository.findNotDeletedByEmail(email)
                //없으면, UsernameNotFoundException 발생
                .orElseThrow(() -> new UsernameNotFoundException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()));

        //있으면, PrincipalUserDetails 생성
        return new PrincipalUserDetails(member);
    }
}
