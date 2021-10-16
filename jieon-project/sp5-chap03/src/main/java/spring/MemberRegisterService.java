package spring;

import java.time.LocalDateTime;

public class MemberRegisterService {
    private MemberDao memberDao;
    public MemberRegisterService(MemberDao memberDao){
        this.memberDao = memberDao;
    }

    Long curId  = 0L;

    public Long regist(RegisterRequest req){
        Member member = memberDao.findByEmail(req.getEmail());
        if(member != null){
            throw new DuplicateMemberException("dup email "+req.getEmail());
        }
        Member newMember = new Member(curId, req.getEmail(),req.getPassword(),
                req.getName(), LocalDateTime.now());
        memberDao.insert(newMember);
        curId += 1L;
        return newMember.getId();
    }
}
