# DI 란?
>의존 객체를 생성자를 통해 주입

의존하는 객체를 new 연산자를 통해 직접 생성하면, 의존하는 객체를 상속하는 개체를 받아야 할 경우 모든 new 연산자를 고쳐야 한다. 하지만 `DI` 방식을 통해 생성자를 통해 주입받으면 주입하는 객체만 수정하면 되므로 편리하다.

# 예제 프로젝트

## Member.java

```java
package spring;


import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String name;
    private LocalDateTime registerDateTime;

    public void changePassword(String oldPassword, String newPassword){
        if(!password.equals(oldPassword))
            throw new WrongIdPasswordException();
        this.password = newPassword;
    }


}

```

* Lombok 을 사용해서 Setter,Getter,AllArgsConstructor 로 코드를 간결하게 했다.

## MemberDao.java

```java
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

```

* 야매로 JPA 를 사용해 데이터베이스에 연결할 준비를 해 놓았다.

## MemberRegisterService
```java
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

```

## MemberRepository
```java
package spring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    @Query("SELECT p FROM Member p ORDER BY p.email DESC")
    Optional<Member> findByEmail(String email);
}

```

* 책에는 SelectBy~ 로 되어있지만 귀찮은 나머지 JPA의 findBy로 구현하였다.

# 기타

* final: 변수를 선언한 후 재할당하지 못하게 함.
* 롬복 Constructor
    * AllArgsConstructor: 모든 필드에 대한 생성자를 만든다.
    * RequiredArgsConstructor: final, @Notnull 필드에 대한 생성자를 만든다.
    * NoArgsConstructor: 파라미터가 없는 기본 생성자를 만든다.

