이번 글에서는 세션/인터셉터/쿠키에 관한 내용을, **로그인 기능**을 이용해 설명할 것이므로 로그인과 관련된 몇 가지 필요한 코드들을 살펴보며 진행될 예정이다.

# 컨트롤러에서 HttpSession 사용하기
기본적인 로그인 기능을 구현했다는 전제 하에, 이 **로그인 상태를 유지**하고 싶다. 어떻게 해야할까?

크게 두 가지 방법이 있다.

1. HttpSession을 이용하는 방법
2. 쿠키를 이용하는 방법

외부 데이터베이스에 세션 데이터를 보관하는 방법도 있는데, 큰 틀에서는 위의 두 가지 방법으로 나뉜다. 여기서는 세션을 이용하여 로그인 상태를 유지하는 방법에 대해 알아보자.

## 컨트롤러에서 HttpSession을 사용하려면?
### 1. 요청 매핑 애노테이션 적용 메서드에 HttpSession 파라미터 추가
```java
@PostMapping
public String form(LoginCommand loginCommand, Errors errors, HttpSession session) {
	... // session을 사용하는 코드
}
```
해당 방법을 사용할 경우, 스프링 MVC는 컨트롤러의 메서드를 호출할 때 HttpSession 객체를 파라미터로 전달한다. HttpSession을 생성하기 전이면 새로운 HttpSession을 생성하고, 그렇지 않으면 기존에 존재하는 HttpSession을 전달한다.
### 2. 요청 매핑 애노테이션 적용 메서드에 HttpServletRequest 파라미터를 추가한 뒤 HttpServletRequest를 이용해서 HttpSession을 구한다.
```java
@PostMapping
public String submit(LoginCommand loginCommand, Errors errors, HttpSessionRequest req) {
	HttpSession session = req.getSession();
	... // session을 사용하는 코드
}
```
첫 번째 방법은 항상 HttpSession을 생성한다면, 두 번째 방법은 필요한 시점에만 HttpSession을 생성할 수 있다.

따라서 위 내용을 참고하여 로그인 및 로그아웃 기능을 구현할 경우 다음과 같이 코드를 작성한다.
- 로그인: 로그인 성공 시 HttpSession의 특정 속성(예. "authInfo" 속성)에 인증 정보 객체(예. authInfo 객체)를 저장
```java
AuthInfo authInfo = authService.authenticate(
			loginCommand.getEmail(),
            loginCommand.getPassword());
session.setAttribute("authInfo", authInfo);
```
- 로그아웃: HttpSession 제거 → ```session.invalidate()```

위와 같이 저장된 인증 정보를 활용하여 비밀번호 변경 등의 기능 또한 구현할 수 있다.
# 인터셉터
실제 웹 어플리케이션에서는 비밀번호 변경 등 많은 기능에서 로그인 여부를 확인해야한다. 하지만 각 기능을 구현한 컨트롤러 코드마다 세션 확인 코드를 삽입하는 것은 많은 중복을 일으킨다.

이렇게 다수의 컨트롤러에 대해 동일한 기능을 적용해야 할 때마다 사용할 수 있는 것이 **HandlerInterceptor**이다.
## HandlerInterceptor 인터페이스 구현
```org.springframework.web.HandlerInterceptor``` 인터페이스를 사용하면 다음의 세 시점에 공통 기능을 넣을 수 있다.
- 컨트롤러(핸들러) 실행 전
- 컨트롤러(핸들러) 실행 후, 아직 뷰를 실행하기 전
- 뷰를 실행한 이후

세 시점을 처리하기 위해 HandlerInterceptor 인터페이스는 다음 메서드를 정의하고 있다.
- **boolean preHandle**(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
	- preHandle() 메서드는 _컨트롤러(핸들러) 객체를 실행하기 전_ 필요한 기능을 구현할 때 사용한다. handler 파라미터는 웹 요청을 처리할 컨트롤러(핸들러) 객체이다. 이 메서드를 사용하면 다음 작업들이 가능하다.
    1. 로그인하지 않은 경우 컨트롤러를 실행하지 않는다.
    2. 컨트롤러를 실행하기 전에 컨트롤러에서 필요로 하는 정보를 생성한다.
    - preHandle() 메서드가 false를 리턴할 경우 컨트롤러(또는 다음 HandlerInterceptor)를 실행하지 않는다.
- **void postHandle**(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception;
	- _컨트롤러(핸들러)가 정상적으로 실행된 이후에 추가 기능을 구현할 때_ 사용한다. 컨트롤러가 익셉션을 발생하면 postHandle() 메서드는 실행하지 않는다.
- **void afterCompletion**(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception;
	- _뷰가 클라이언트에 응답을 전송한 뒤_에 실행된다. 컨트롤러 실행과정에서 익셉션이 발생하면 이 메서드의 네 번째 파라미터로 전달되고, 발생하지 않으면 네 번째 파라미터는 null이 되기 때문에 실행 이후의 익셉션을 로그로 남기는 등 _후처리를 하기에 적합한 메서드_이다.

해당 인터페이스의 각 메서드는 아무 기능도 구현하지 않은 자바 8의 디폴트 메서드이기 때문에 이 메서드들을 모두 구현할 필요는 없고, 필요한 메서드만 재정의하면 된다.

## HandlerInterceptor 설정
HandlerInterceptor를 구현하면, 이제 이 HandlerInterceptor를 어디에 적용할 지 설정해야 한다. 다음은 설정 클래스에 추가한 예시 코드이다.
```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
	registry.addInterceptor(authCheckInterceptor())
    	.addPathPatterns("/edit/**");
```
InterceptorRegistry의 addInterceptor() 메서드는 HandlerInterceptor 객체를 설정하고 InterceptorRegistration 객체를 리턴하는데, 이 객체의 addPathPatterns() 메서드는 인터셉터를 적용할 경로 패턴을 지정한다. 이 때 경로는 **Ant 경로** 패턴을 사용한다.

> ## 💡 Ant 경로패턴
> *, **, ?의 세 가지 특수 문자를 이용해서 경로를 표현하는 패턴이다. 각 문자는 다음의 의미를 갖는다.
> - *: 0개 또는 그 이상의 글자
> - **: 0개 또는 그 이상의 폴더 경로
> - ?: 1개 글자
>
> 이들 문자를 사용한 경로 표현 예는 다음과 같다.
> - ```/member/?*.info```: /member/로 시작하고 확장자가 .info로 끝나는 모든 경로
> - ```/faq/f?OO.fq```: /faq/f로 시작하고 OO.fq로 끝나는 모든 경로
> - ```/folders/**/files```: /folders/로 시작하고, 중간에 0개 이상의 중간 경로가 존재하고 /files로 끝나는 모든 경로

# 컨트롤러에서 쿠키 사용하기
사용자 편의를 위해 아이디를 기억해두었다가 다음에 로그인할 때 아이디를 자동으로 넣어주는 사이트들이 많다. 이런 기능들을 구현할 대 쿠키를 사용한다. 이번 글에서는 이메일 기억하기 기능으로 쿠키 사용에 대한 내용을 다룰 것이다.

기본적인 이메일 기억하기 기능 구현 방식은 다음과 같다.
- 로그인 폼에 '이메일 기억하기' 옵션 추가
- 로그인 시에 '이메일 기억하기' 옵션을 선택했으면 로그인 성공 후 쿠키에 이메일을 저장하고, 이때 웹 브라우저를 닫더라도 쿠키가 삭제되지 않도록 유효시간을 길게 설정
- 이후 로그인 폼을 보여줄 때 이메일을 저장한 쿠키가 존재하면 입력 폼에 이메일을 보여줌

스프링 MVC에서 쿠키를 사용하는 방법 중 하나는 @CookieValue 애노테이션을 사용하는 것이다.

## @CookieValue
@CookieValue 애노테이션은 요청 매핑 애노테이션 적용 메서드의 Cookie 타입 파라미터에 적용하며, 이를 통해 쉽게 쿠키를 Cookie 파라미터로 전달받을 수 있다. 다음은 @CookieValue 애노테이션을 사용한 예시 코드이다. 이 코드는 사용방식 파악 정도로만 보자.
```java
@GetMapping
public String form(LoginCommand loginCommand, 
@CookieValue(value = "REMEMBER", required = false) Cookie rCookie) {
	if(rCookie != null) {
    	loginCommand.setEmail(rCookie.getValue());
        loginCommand.setRememberEmail(true);
    }
    return "login/loginForm";
}
```

@CookieValue 애노테이션의 value 속성은 쿠키의 이름을 지정한다. 또한 지정한 이름을 가진 쿠키가 존재하지 않을 수도 있다면 required 속성값은 false로 지정한다. 참고로 required의 기본 속성값은 true이다.
