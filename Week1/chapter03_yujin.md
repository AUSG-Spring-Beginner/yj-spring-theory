# 의존
DI(Dependency injection)은 우리말로 '의존 주입'이라고 한다. 

DI를 이해하려면 먼저 '의존(Dependency)'가 무엇인지 알아야 한다.

>💡 여기서 말하는 **의존** = **객체 간의 의존**

이해를 위해 회원가입 예제와 함께 살펴보자.
```
import java.time.LocalDateTime

public class MemberRegisterService {
    // 의존 객체 직접 생성
    private MemberDao memberDao = new MemberDao();
    
    public void regist(RegisterRequest req) {
        // 이메일로 회원 데이터(Member) 조회
        Member member = memberDao.selectByEmail(req.getEmail());
        
        if(member != null) {
            // 같은 이메일을 가진 회원이 이미 존재하면 익셉션 발생
            throw new DuplicateMemberException("dup email " + req.getEmail());
        }
        // 같은 이메일을 가진 회원이 존재하지 않으면 DB에 삽입
        Member newMember = new Member(
            req.getEmail(),req.getPassword(), req.getName(), 
            LocalDateTime.now()
        );
        memberDao.insert(newMember);
    }
}
```
서로 다른 회원은 동일한 이메일 주소를 사용할 수 없다고 가정했을 때, 이 제약사항을 처리하기 위해 ```MemberRegisterService```는 ```MemberDao``` 객체의 ```selectByEmail()``` 메서드를 이용해서 동일한 이메일을 가진 회원이 존재하는지 확인한다. 

이렇게, **한 클래스가 다른 클래스의 메서드를 실행할 때** 이를 '**의존**'이라 표현한다.

위 코드에서는 ```MemberRegisterService``` 클래스가 ```MemberDao``` 클래스에 의존한다고 표현할 수 있겠다.

> ### 💡 의존은 변경에 의해 영향을 받는 관계를 의미한다.
> 예를 들어 MemberDao의 insert() 메서드 이름을 insertMember()로 변경하면, 이 메서드를 사용하는 MemberRegisterService 클래스의 소스 코드도 같이 변경된다.

## 의존 대상 구하는 방법 2가지
의존하는 대상이 있으면 그 대상을 구하는 방법이 필요하다.
### 1. 의존 대상 객체 직접 생성
앞의 코드에서 사용한 방법으로, 의존 대상을 구하는 방법 중 가장 쉬운 방법이다. 하지만 유지보수 관점에서 문제점을 유발할 수 있다. 
### 2. DI와 서비스 로케이터
의존 대상을 구하는 또 다른 방법이다. 스프링에서는 DI를 주로 다루니, 
서비스 로케이터에 대해서는 간단하게만 알아보고 넘어가도록 하자.

> ### 💡 서비스 로케이터란?
>마틴 파울러에 의해 유명해진 디자인 패턴 중 하나다. 객체를 cache에 담아두고, 객체가 필요할 경우 메모리에서 바로바로 찾아 제공해주는 구조이다. 
> ### ❗ 서비스 로케이터와 DI의 차이점
> - **서비스 로케이터** : 공용 인터페이스를 변경하지 않고 전체 디자인을 느슨하게 만들기 때문에 기존 코드베이스에서 사용하기 쉽다. 하지만 DI를 기반으로 하는 동일한 코드보다는 가독성이 떨어진다.
> - **DI** : 클래스 또는 메소드가 가질 종속성의 서명 이후 명확하게 나타내기 때문에 결과 코드의 가독성이 좋다.

# DI를 통한 의존 처리

DI(Dependency Injection)는 의존하는 객체를 직접 생성하는 대신 **의존 객체를 전달받는 방식**을 사용한다.

예를 들어 앞서 의존 객체를 직접 생성했던 코드에서 DI를 적용하면 다음과 같이 변경할 수 있다. (기존 코드와 똑같은 코드는 생략)
```
import java.time.LocalDateTime;

public class MemberRegisterService {
    private MemberDao memberDao;
    
    // 추가된 코드
    public MemberRegisterService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
    
    // 이하 동일한 코드
    ...

}
```
위에서 추가된 코드는 **의존 객체를 직접 생성하지 않는다.**
대신 **생성자**를 통해 의존 객체를 전달받는다. 

즉, 생성자를 통해 ```MemberRegisterService```가 의존(Dependency)하고 있는 ```MemberDao``` 객체를 주입(Injection)받은 것이다.

DI를 적용한 ```MemberRegisterService``` 클래스를 사용하는 코드는 다음과 같이 ```MemberRegisterService``` 객체를 생성할 때 생성자에 ```MemberDao``` 객체를 전달해야 한다.

```
MemberDao dao = new MemberDao();
// 의존 객체를 생성자를 통해 주입한다.
MemberRegisterService svc = new MemberRegisterService(dao);
```

# 스프링의 DI 설정
스프링을 사용하려면 먼저 스프링이 어떤 객체를 생성하고, 의존을 어떻게 주입할지를 정의한 **설정 정보**를 작성해야 한다.

```
package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import spring.ChangePasswordService;
import spring.MemberDao;
import spring.MemberRegisterService;

@Configuration
public class AppCtx {

	@Bean
	public MemberDao memberDao() {
		return new MemberDao();
	}
	
	@Bean
	// MemberRegisterService 생성자를 호출할 때 memberDao() 메서드 호출
	// 즉, memberDao()가 생성한 객체를 
	// MemberRegisterService 생성자를 통해 주입 (DI)
	public MemberRegisterService memberRegSvc() {
		return new MemberRegisterService(memberDao());
	}
	
	@Bean
	public ChangePasswordService changePwdSvc() {
		ChangePasswordService pwdSvc = new ChangePasswordService();
		pwdSvc.setMemberDao(memberDao());
		return pwdSvc;
	}
}
```

- ```@Configuration```: 스프링 설정 클래스
- ```@Bean```: 해당 메서드가 생성한 객체

설정 클래스만 만들었다고 끝이 아니다❗ 

이 설정 클래스를 이용해 객체를 생성하고 의존 객체를 주입해줄 "컨테이너"를 생성해야 한다.

```
// 컨테이너 생성
ApplicationContent ctx = new AnnotationConfigApplicationContext(AppCtx.class);
```
위와 같이 컨테이너를 생성했으면 ```getBean()``` 메서드를 이용해서 사용할 객체를 구할 수 있다.

```
// 컨테이너에서 이름이 memberRegSvc인 빈 객체를 구한다.
MemberRegisterService regSvc = ctx.getBean("memberRegSvc", MemberRegisterService.class);
```

# DI 방식

## 1. 생성자 방식
```
public class SampleClass {
	private SampleObject obj;
    
    public SampleClass(SampleObject object) {
    	this.obj = object;
    }
}
```
앞서 사용했던 것처럼 생성자를 통해 의존 객체를 주입받는 방식이다.

## 2. 세터 메서드 방식
```
public class SampleClass {
	private SampleObject obj;
    
    public SampleObject getObj() {
    	return this.obj;
    }
    
    public void setObj(SampleObject object) {
    	this.obj = object;
    }
}
```
```
public class Main {
	public static void main(String[] args) {
    	SampleClass sc = new SampleClass();
        SampleObject so = new SampleObject();
        
        sc.setObj(so); // setter 메서드를 통한 DI
```
생성자 외에 세터 메서드를 이용해 객체를 주입받기도 한다. 이 방식은 개발자가 원할 때 의존 객체를 바꿀 수 있다는 장점이 있다.
## 3. 필드 방식
```
@Component
public class SampleClass {
    @Autowired
    private SampleObject sampleObj;
}
```
변수 선언부에 @Autowired 애노테이션을 붙인다.
## 생성자 vs 세터 메서드
- **생성자** 방식의 장점 : 빈 객체를 생성하는 시점에 모든 의존 객체가 주입된다.
- **생성자** 방식의 단점 : 생성자의 파라미터 개수가 많은 경우 각 인자가 어떤 의존 객체를 설정하는지 알아내려면 생성자의 코드를 확인해야 한다.  
```

```
- **세터 메서드** 방식의 장점 : 세터 메서드 이름을 통해 어떤 의존 객체가 주입되는지 알 수 있다.
- **세터 메서드** 방식의 단점 : 세터 메서드를 사용해서 필요한 의존 객체를 전달하지 않아도 빈 객체가 생성되기 때문에 객체를 사용하는 시점에 ```NullPointerException```이 발생할 수 있다.

### 🤷‍♂️ 생성자, 필드, 세터 메서드 중에 뭐가 더 좋을까?

Spring Framework reference에서 권장하는 방법은 **생성자 방식**이다. 생성자를 통한 주입 방식을 쓰면 필수적으로 사용해야하는 의존성 없이는 인스턴스를 만들지 못하도록 강제할 수 있기 때문이다.

### 🤔 그럼 필드나 세터 메서드는 필요가 없는 걸까?

필드 방식이나 세터 메서드 방식이 필요한 상황도 있다.

> ### 💡 순환 참조
> A가 B를 참조하고 B가 A를 참조하는 상태

A 클래스와 B 클래스가 순환 참조 관계이고 둘 다 생성자 주입을 사용한다면 A와 B중 **어떤 인스턴스도 생성할 수 없고 결과적으로 어플리케이션이 실행조차 되지 않는다.**

가급적이면 순환 참조를 피하는게 좋지만 어쩔 수 없는 상황이라면 필드나 setter 주입 방법을 사용할 수 있다.
