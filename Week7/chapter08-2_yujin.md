# 스프링의 익셉션 변환 처리
JDBC API를 사용하는 과정에서 ```SQLException```이 발생하면,

**이 익셉션을 알맞은 ```DataAccessException```으로 변환해서 발생한다.**

> ## 🤔 변환하는 이유
> 왜 ```SQLException```을 그대로 전파하지 않고 굳이 ```DataAccessException```으로 변환하는 걸까?
>
> **연동 기술에 상관없이 동일하게 익셉션을 처리할 수 있도록 하기 위해**  익셉션을 변환한다. 
>
>스프링은 JDBC뿐만 아니라 JPA, 하이버네이트 등에 대한 연동을 지원하고 MyBatis는 자체적으로 스프링 연동 기능을 지원한다. 만약 이 각각의 구현기술마다 익셉션을 다르게 처리해야 한다면 그에 대한 익셉션 처리 코드를 기술마다 작성해야 하는 번거로움이 생긴다.
>
> 따라서, 각 연동 기술에 따라 발생하는 익셉션을 스프링이 제공하는 익셉션으로 변환하여 동일한 코드로 익셉션을 처리할 수 있게 된다.

```DataAccessException```은 ```RuntimeException```이기 때문에,
JDBC를 직접 이용하는 경우처럼 try~catch를 사용하여 익셉션을 처리할 필요없이, **필요한 경우에만 익셉션을 처리**하면 된다.

# 트랜잭션 처리
## 1. 트랜잭션이란?
두 개 이상의 쿼리를 한 작업으로 실행해야 할 떼 사용하는 것이 트랜잭션이다. 

트랜잭션은 여러 쿼리를 논리적으로 하나의 작업으로 묶어준다. 한 트랜잭션으로 묶인 쿼리 중 하나라도 실패하면 전체 쿼리를 실패로 간주하고 실패 이전에 실행한 쿼리를 취소한다. 

트랜잭션을 시작하면 트랜잭션을 커밋하거나 롤백할 때까지 실행한 쿼리들이 하나의 작업 단위가 된다.
> ### 💡 용어 정리
> - 롤백(rollback): 쿼리 실행 결과를 취소하고 DB를 기존 상태로 되돌리는 것
> - 커밋(commit): 트랜잭션으로 묶인 모든 쿼리가 성공해서 쿼리 결과를 DB에 실제로 반영하는 것

## 트랜잭션이 왜 필요한데?
예시와 함께 보자. 회원정보에서 이메일을 수정하고 인증상태를 변경하는 다음과 같은 두 쿼리가 있다고 가정하자.

```
jdbcTemplate.update("update MEMBER set EMAIL = ?", email);
jdbcTemplate.update("insert into EMAIL_AUTH values (?, 'T')", email);
```

여기서 만약,

1. 두번째 쿼리문에서의 테이블 이름 등이 잘못된 경우
2. 중복된 값이 존재하여 두번째 쿼리인 INSERT 쿼리 실행에 실패할 경우
3. 두번째 쿼리가 실패했음에도 불구하고 첫번째 쿼리 실행 결과가 DB에 반영되어버릴 경우

이런 경우, 사용자의 이메일 주소는 인증되지 않은 채로 계속 남아있게 될 것이다. 올바른 상태를 유지하기 위해서는, 두번째 쿼리문이 실패했을 때 첫번째 쿼리도 취소해야한다.

위와 같이 DB에 의도하지 않은 결과로 반영되는 것을 막기 위해, 여러 쿼리를 하나의 작업으로 묶어주고 하나라도 실패 시 전체 쿼리를 실패로 간주하고 그 이전으로 돌릴 수 있도록 하는 트랜잭션이 필요하다.

## 2. @Transactional
스프링이 제공하는 @Transactional 애노테이션을 사용하면 트랜잭션 범위를 매우 쉽게 지정할 수 있다. 트랜잭션 범위에서 실행하고 싶은 메서드에 해당 애노테이션만 붙이면 된다.

### 💡 @Transactional 애노테이션이 동작하기 위한 조건
- 플랫폼 트랜잭션 매니저(PlatformTransactionManager) 빈 설정
- @Transactional 애노테이션 활성화 설정

다음은 그 설정 예이다.
```
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class AppCtx {
    
    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        DataSource ds = new DataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://localhost/spring5fs?characterEncoding=utf8");
        ...
        return ds;
    
    }
    
    @Bean
    public PlatformTransactionManager transactionManager() {
        DatasourceTransactionManager tm = new DataSourceTransactionManager();
        tm.setDataSource(dataSource());
        return tm;
    }
    
    @Bean
    public MemberDao memberDao() {
        return new MemberDao(dataSource());
    }
}
```

> ### 💡 용어 정리
> - **PlatformTransactionManager**: 스프링이 제공하는 트랜잭션 매니저 인터페이스. 스프링에서 구현기술과 상관없이 동일한 방식으로 트랜잭션을 처리하기 위해 사용한다. dataSource 프로퍼티를 이용해서 트랜잭션 연동에 사용할 DataSource를 지정한다.
> - **@EnableTransactionManagement**: @Transactional 애노테이션이 붙은 메서드를 트랜잭션 범위에서 실행하는 기능을 활성화한다. 등록된 PlatformTransactionManager 빈을 사용해서 트랜잭션을 적용한다.

트랜잭션 처리를 위한 설정을 완료하면 트랜잭션 범위에서 실행하고 싶은 스프링 빈 객체의 메서드에 @Transactional 애노테이션을 붙이면 된다.

> 💡 트랜잭션이 시작되고 커밋되는지 확인하고 싶다면, LogBack를 사용해보자.
>
> 🤔 **LogBack?** 스프링에서 사용하는 로깅 모듈

## 3. @Transactional과 프록시
앞서 얘기했던 LogBack을 사용하면 트랜잭션의 롤백 및 커밋에 대한 메시지를 출력할 수 있다. 

그런데, **트랜잭션을 시작하고 커밋하고 롤백하는 건 누가 어떻게 처리하는 걸까?**

### ✔️ 프록시
앞서 **여러 빈 객체에 공통으로 적용되는 기능을 구현하는 방법**으로 **AOP**에 관해 다뤘었는데, **트랜잭션도 공통 기능 중 하나이다.**

스프링은 @Transactional 애노테이션을 이용해서 트랜잭션을 처리하기 위해 내부적으로 AOP를 사용한다. 따라서, 트랜잭션 처리도 **프록시**를 통해서 이루어진다.

1. 스프링이 트랜잭션 기능을 적용한 프록시 객체를 생성한다.
2. PlatformTransactionManager를 사용해서 트랜잭션을 시작한다.
3. 실제 객체의 메서드를 호출하여 핵심 기능을 수행한다.
4. 성공적으로 실행되면 트랜잭션을 커밋한다.
	4.1 실행하는 도중 익셉션(SQLException 제외)이 발생하면 롤백한다.

> ## 🤔 왜 SQLException은 제외할까?
> 프록시는 익셉션이 RuntimeException일 때 트랜잭션을 롤백한다.
>
> JdbcTemplate는 DB 연동 과정 중에 문제가 발생하면 DataAccessException을 발생시키는데, DataAccessException은 RuntimeException을 상속받고 있다. 
> 
> 하지만, SQLException은 RuntimeException을 상속받고 있지 않기 때문에, 별도의 설정을 하지 않는다면 SQLException이 발생해도 트랜잭션을 롤백하지 않는다.
>
> ### 💡 SQLException이 발생해도 롤백하고 싶다면?
> @Transactional의 **rollbackFor 속성**을 사용하면 된다!
> ```
> @Transactional(rollbackFor = SQLException.class)
> public void someMethod() {
> 	...
> }
> ```
> 📌 noRollbackFor: rollbackFor 속성과 반대로, 지정한 익셉션이 발생해도 롤백시키지 않고 커밋할 익셉션 타입을 지정할 때 쓰인다.

## 4. @Transactional의 주요 속성
보통 이들 속성을 사용할 일은 별로 없지만 간혹 필요할 경우가 생길 수 있으니 이런 게 있다는 정도로만 알아두자. 
![](https://images.velog.io/images/nanaeu/post/bd2079d5-056f-4f28-8350-682c76feeb0c/image.png)

Propagation 열거 타입에 정의되어 있는 값들과 Isolation 열거 타입에 정의되어 있는 값은 검색해보자 ^^

## 5. @EnableTransactionManagement의 주요 속성
- **proxyTargetClass**: 클래스를 이용해서 프록시를 생성할지 여부를 지정한다. 기본값은 false로서 인터페이스를 이용해서 프록시를 생성한다.
- **order**: AOP 적용 순서를 지정한다. 기본값은 가장 낮은 우선순위에 해당하는 int의 최댓값이다.
## 6. 트랜잭션 전파 
Propagation 열거 타입 값 목록에서 REQUIRED 값의 설명은 다음과 같다.
- 메서드를 수행하는 데 트랜잭션이 필요하다는 것을 의미한다. **현재 진행중인 트랜잭션이 존재하면 해당 트랜잭션을 사용**한다. 존재하지 않으면 새로운 트랜잭션을 생성한다.

이 설명을 완벽히 이해하려면 트랜잭션 전파가 무엇인지 알아야 한다.

예시와 함께 보자. 

1. 임의의 두 클래스 
```
public class SomeClass {
    private AnyClass anyClass;
    
    @Transactional
    public void some() {
        anyClass.any();
    }
    
    public void setAnyClass(AnyClass ac) {
        this.anyClass = ac;
    }
}

public class AnyClass {
    @Transactional
    public void any() { ... }
}
```
2. 설정 클래스
```
@Configuration
@EnableTransactionManagement
public class Config {
    
    @Bean
    public SomeClass some() {
        SomeClass some = new SomeClass();
        some.setAnyClass(any());
        return some;
    }
    
    @Bean
    public AnyClass any() {
        return new AnyClass();
    }
    
    // DataSourceTransactionManager 빈 설정
    // DataSource 설정
}
```

```SomeClass```와 ```AnyClass``` 둘 다 @Transactional 애노테이션을 적용하고 있다. 따라서 두 클래스에 대해 프록시가 생성된다.

즉, ```SomeClass```의 some() 메서드를 호출하면 트랜잭션이 시작되고, ```AnyClass```의 any() 메서드를 호출해도 트랜잭션이 시작된다. 

그런데, some() 메서드는 내부에서 any() 메서드를 호출하고 있다. 이 경우엔 트랜잭션이 어떻게 될까?

별도로 설정하지 않았을 경우, @Transactional의 propagation 속성의 기본값은 Propagation.REQUIRED이다. 위에서 설명한 바와 같이, 현재 트랜잭션이 존재할 경우 해당 트랜잭션을 사용하고 존재하지 않으면 새로운 트랜잭션을 시작한다. 

따라서 some() 메서드를 호출하면 새로운 트랜잭션을 시작하고, some() 메서드 내부에서 any() 메서드가 호출될 때는 현재 진행 중인 트랜잭션이 존재하기 때문에 새로운 트랜잭션을 생성하지 않고 존재하는 트랜잭션을 그대로 사용한다. 

즉, **some() 메서드와 any() 메서드를 한 트랜잭션으로 묶어서 실행한다.**

위와 같은 상황을 **트랜잭션이 전파되었다**고 한다.

또 다른 상황을 보자.

```
public class ChangePasswordService {
	...
    
    @Transactional
    public void changePassword(String email, String oldPwd, String newPwd) {
        Member member = memberDao.selectByEmail(email);
        
        if(member == null) throw new MemberNotFoundException();
        
        member.changePassword(oldPwd, newPwd);
        
        memberDao.update(member);
    }
}

public class MemberDao {
    private JdbcTemplate jdbcTemplate;
    ...
    
    // @Transactional 없음
    public void update(Member member) {
        jdbcTemplate.update(
        	"update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?",
            	member.getName(), member.getPassword(), member.getEmail());
    }
}
```

changePassword() 메서드는 MemberDao의 update() 메서드를 호출하고 있다. 이 때, update() 메서드에는 @Transactional이 붙어있지 않다. 이 경우에는 트랜잭션이 어떻게 될까?

update() 메서드에 @Transactional이 붙어있지 않지만, JdbcTemplate 클래스 덕에 트랜잭션 범위에서 쿼리를 실행할 수 있게 된다.

**JdbcTemplate는 진행 중인 트랜잭션이 존재할 경우 해당 트랜잭션 범위에서 쿼리를 실행한다.**
