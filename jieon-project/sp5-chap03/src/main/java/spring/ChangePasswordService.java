package spring;
import lombok.Setter;

@Setter
public class ChangePasswordService {
    private MemberDao memberDao;

    public void changePassword(String email, String oldPwd, String newPwd){
        Member member = memberDao.findByEmail(email);
        if(member == null)
            throw new MemberNotFoundException();

        member.changePassword(oldPwd,newPwd);

        memberDao.update(member);
    }
}
