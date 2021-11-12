이전 글에서는 의존 자동 주입에 대해 다뤄왔다. 이번에는 의존 자동 주입과 함께 사용하는 추가 기능인 **컴포넌트 스캔**에 대해 간단히 다뤄보려고 한다.
> ### 💡 컴포넌트 스캔이란?
> 스프링이 직접 클래스를 검색해서 빈으로 등록해주는 기능

# @Component
스프링이 검색해서 빈으로 등록할 수 있으려면 클래스에 ```@Component``` 애노테이션을 붙여야 한다. 

```@Component``` 애노테이션은 해당 클래스를 스캔 대상으로 표시한다.

- ```@Component``` 속성값을 따로 주지 않았을 경우: 클래스의 첫 글자를 소문자로 바꾼 값을 빈 이름으로 사용한다.
- ```@Component("beanName")``` 속성값을 줬을 경우: 해당 값을 빈 이름으로 사용한다.

# @ComponentScan
```@Component``` 애노테이션을 붙인 클래스를 스캔해서 스프링 빈으로 등록하려면,
**설정 클래스에 ```@ComponentScan``` 애노테이션을 적용해야 한다.**

```
import org.springframework.context.annotation.ComponentScan
...

@Configuration
@ComponentScan(basePackages = {"spring"})
public class AppCtx {
	...
}
```
```@Component``` 애노테이션을 붙이고 설정 클래스에 ```@ComponentScan```을 붙이면 스프링 컨테이너가 알아서 클래스를 검색하여 빈으로 등록해주기 때문에, 이전의 설정 클래스에 비해 설정 코드가 줄어든다.

```@ComponentScan```애노테이션의 ```basePackages``` 속성은 스캔 대상 패키지 목록을 지정한다. 예제 코드에서는 ```{"spring"}```인데, 이는 spring 패키지와 그 하위 패키지에 속한 클래스를 스캔 대상으로 설정한다. 스캔 대상에 해당하는 클래스 중에서 ```@Component```이 붙은 클래스의 객체를 생성해서 빈으로 등록한다.

> ### 📌 이름으로 빈을 검색하는 코드가 존재할 경우
>```@Component```에 속성값을 주지 않은 채 해당 애노테이션을 붙인 클래스는 나중에 빈 이름으로 _클래스의 맨 앞 글자를 소문자로 바꾼 값_이 사용된다. 만약 이전에 특정 이름으로 빈을 검색하는 코드가 있었을 경우, **타입만으로 검색하도록 변경**한다.
>EX.
>```
>MemberRegisterService regSvc = 
>	ctx.getBean("memberRegSvc", MemberRegisterService.class);
>```
>이를 다음과 같은 코드로 변경하자.
>```
>MemberRegisterService regSvc = 
>	ctx.getBean(MemberRegisterService.class);
>```

# 스캔 대상에서 제외/포함
```excludeFilters``` 속성을 사용하면 스캔할 때 특정 대상을 자동 등록 대상에서 제외할 수 있다.
```
import org.springframework.context.annotation.ComponentScan
...

@Configuration
@ComponentScan(basePackages = {"spring"}, 
	excludeFilters = @Filter(type = FilterType.REGEX,
    				pattern = "spring\\..*Dao"))
public class AppCtxWithExclude {
	...
}
```
 
- ```@Filter```의 ```type``` 속성값, **FilterType.REGEX**: **정규표현식**을 사용해서 제외 대상을 지정한다
- ```pattern```: FilterType에 적용할 값을 설정 
	- 위 코드에서는 "spring."으로 시작하고 Dao로 끝나는 정규표현식을 지정했다.

FilterType.REGEX 대신 **FilterType.ASPECTJ**를 필터 타입으로 사용할 수도 있다. 다음은 FilterType.ASPECTJ를 사용한 코드다.
```
@Configuration
@ComponentScan(basePackages = {"spring"},
	excludeFilters = @Filter(type = FilterType.ASPECTJ,
    				pattern = "spring*Dao"))
public class AppCtxWithExclude {
	...
}
```
AspectJ 패턴에 대해서는 나중에 더 자세히 살펴보겠다. 여기에서는 하나의 FilterType이고 패턴은 저렇게 쓴다는 정도만 알아두자.

AspectJ 패턴이 동작하려면 의존 대상에 aspectjweaver라는 모듈을 추가해야한다.

## 특정 애노테이션을 붙인 타입을 제외하려면
예를 들어 ```@NoProduct```나 ```@ManualBean``` 애노테이션을 붙인 클래스를 스캔 대상에서 제외하고 싶다고 해보자.
```
@Retention(RUNTIME)
@Target(TYPE)
public @interface NoProduct {
}

@Retention(RUNTIME)
@Target(TYPE)
public @interface ManualBean {
}
```
이 두 클래스를 제외하려면 다음과 같이 ```excludeFilters``` 속성을 설정한다.
```
@Configuration
@ComponentScan(basePackages = {"spring"},
	excludeFilters = @Filter(type = FilterType.ANNOTATION,
    			classes = {NoProduct.class, ManualBean.class}))
public class AppCtxWithExclude {
	...
}
```
##  특정 타입이나 그 하위 타입을 제외하려면
```ASSIGNABLE_TYPE```을 FilterType으로 사용한다.
```
@Configuration
@ComponentScan(basePackages = {"spring"},
	excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE,
    			classes = MemberDao.class))
public class AppCtxWithExclude {
	...
}
```
classes에는 제외할 타입 목록을 지정한다.
## 설정할 필터가 두 개 이상이라면
```@ComponentScan```의 ```excludeFilters``` 속성에 배열을 사용해서 ```@Filter``` 목록을 전달하면 된다.
```
@Configuration
@ComponentScan(basePackages = {"spring"},
	excludeFilters = {
    		@Filter(type = FilterType.ANNOTATION,
    			classes = {NoProduct.class, ManualBean.class}),
                @Filter(type = FilterType.REGEX,
                	pattern = "spring2\\..*")
        })
public class AppCtxWithExclude {
	...
}
```
### 기본 스캔 대상
```@Component``` 애노테이션을 붙인 클래스뿐만 아니라, 다음 애노테이션을 붙인 클래스도 컴포넌트 스캔 대상에 포함된다.

- @Component (org.springframework.stereotype 패키지)
- @Controller (org.springframework.stereotype 패키지)
- @Service (org.springframework.stereotype 패키지)
- @Repository (org.springframework.stereotype 패키지)
- @Aspect (org.aspectj.lang.annotation 패키지)
- @Configuration (org.springframework.context.annotation 패키지)
 
# 컴포넌트 스캔에 따른 충돌 처리
컴포넌트 스캔 기능을 사용해서 자동으로 빈을 등록할 때에는 충돌을 주의해야 한다. 충돌에는 크게 **빈 이름 충돌**과 **수동 등록한 빈과의 충돌**로 두 가지가 있다.
## 1. 빈 이름 충돌
컴포넌트 스캔 과정에서 서로 다른 타입인데 같은 빈 이름을 사용하는 경우 ```ConflictingBeanDefinitionException```과 같은 익셉션이 발생한다. 
이런 경우 **둘 중 하나에 명시적으로 빈 이름을 지정**해서 이름 충돌을 피해야 한다.
## 2. 수동 등록한 빈과 충돌
### 🤷‍♂️ 자동 등록한 빈 이름이랑 수동 등록한 빈 이름이랑 같으면 어떡하지?
예를 들어, MemberDao라는 클래스에 ```@Component``` 애노테이션을 붙여 "memberDao"라는 이름으로 빈을 자동 등록해두었는데, 설정 클래스에서 직접 MemberDao 클래스를 "memberDao"라는 이름의 빈으로 등록하면 어떻게 될까?

이 경우, **수동 등록한 빈이 우선**한다. 즉, MemberDao 타입 빈은 설정 클래스에서 정의한 한 개만 존재한다.

### 🤔 그렇다면 설정 클래스에서 "memberDao2"라는 다른 이름으로 등록했다면 어떻게 될까?

이 경우, 스캔을 통해 자동 등록한 "memberDao" 빈과 수동 등록한 "memberDao2" 빈이 **모두 존재**한다. 
이때는 **MemberDao 타입 빈이 두 개가 생성**되므로, 자동 주입하는 코드에서는 ```@Qualifier```를 사용하여 알맞은 빈을 선택해야한다.
