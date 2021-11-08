package spring;

import org.springframework.transaction.annotation.Transactional;

public class MemberDao {
    private static long nextId = 0;
    private MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member findByEmail(String email) {
        Member entity = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. email=" + email));

        return entity;
    }
}
