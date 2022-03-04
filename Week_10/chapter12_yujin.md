이전 글에서는 스프링 MVC의 기본적인 컨트롤러 구현 방법을 살펴봤다.
- 요청 매핑 애노테이션을 이용하여 요청 경로 처리 메서드 설정
- 커맨드 객체를 이용하여 폼에 입력한 데이터를 받을 수 있음
- 모델을 통해 뷰가 응답을 생성할 때 필요한 데이터를 전달하는 방법

이 글에서는 메시지를 출력하는 방법과 커맨드 객체의 값을 검증하는 방법에 대해 다룰 것이다.
# <spring:message> 태그로 메시지 출력
사용자 화면에 보일 문자열은 JSP에 직접 코딩한다. 예를 들어 로그인 폼의 일부 코드가 다음과 같다고 하자.
```
<label>이메일</label>
<input tyoe="text" name="email">
```
'**이메일**'과 같은 문자열은 로그인 폼, 회원가입 폼, 회원 정보 수정 폼 등에서 **반복해서 사용**된다.

이렇게 문자열을 직접 하드코딩하면 **동일 문자열을 변경할 때 해당 문자열이 들어간 모든 폼을 찾아 하나하나 변경**해야 한다.

또 다른 문제점이 있다. 만약 전세계를 대상으로 서비스를 제공한다면, 사용자의 언어 설정에 따라 '이메일' 또는 'E-mail'과 같이 **각 언어에 맞게 문자열을 표시**해야 한다. 그런데 여기서 문자열이 하드코딩되어 있다면, 언어별로 뷰 코드를 따로 만드는 번거로운 상황이 발생한다.

하드코딩함으로써 생기는 이 두 가지 문제점을 해결하는 방법은, 

뷰 코드에서 사용할 문자열을 **언어별로 파일에 보관**하고, 
뷰 코드는 **언어에 따라 알맞은 파일에서 문자열을 읽어와 출력**하는 것이다.

스프링은 자체적으로 이 기능을 제공하고 있다. 문자열을 별도 파일에 작성하고 JSP 코드에서 이를 사용하려면 다음 작업을 하면 된다.
- 문자열을 담은 메시지 파일을 작성
- 메시지 파일에서 값을 읽어오는 MessageSource 빈을 설정
- JSP 코드에서 <spring:message> 태그를 사용해서 메시지를 출력

## 1. 메시지 파일 작성
메시지 파일은 자바의 프로퍼티 파일 형식으로 작성한다. 

다음은 메시지 파일인 label.properties 파일의 예시 코드이다.
```
member.register=회원가입

term=약관
term.agree=약관동의
next.btn=다음단계

member.info=회원정보
email=이메일
name=이름
password=비밀번호
password.confirm=비밀번호 확인
register.btn=가입완료

register.done=<strong>{0}님</strong>, 회원 가입을 완료했습니다.

go.main=메인으로 이동
```
## 2. MessageSource 타입의 빈 추가
스프링 설정 클래스 중 하나에 추가해주면 된다.
```java

@Bean
public MessageSource messageSource() {
	ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
    ms.setBasenames("message.label");
    ms.setDefaultEncoding("UTF-8");
    return ms;
}
```
- **setBasenames("message.label")**: message 패키지에 속한 label 프로퍼티 파일로부터 메시지를 읽어온다. setBasenames() 메서드는 가변 인자이므로 사용할 메시지 프로퍼티 목록을 전달할 수 있다.

> ### 주의❗ 
> 빈의 아이디를 **"messageSource"로 지정**해야 한다. 다른 이름을 사용할 경우 정상적으로 동작하지 않는다.

## 3. <spring:message> 태그 사용하여 메시지 출력
위의 커스텀 태그를 사용하려면 태그 라이브러리 설정을 추가해야 한다.
```
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
```

메시지 파일로부터 읽어올 것이므로, 다음 코드를
```
<title>회원가입</title>
```
<spring:message> 태그로 메시지를 출력하기 위해 다음과 같이 바꿔주자.
```
<title><spring:message code="member.register" /></title>
```
- <spring:message> 태그의 code 값은 앞서 작성한 프로퍼티 파일의 프로퍼티 이름과 일치한다. 
- <spring:message> 태그는 MessageSource로부터 코드에 해당하는 메시지를 읽어온다. 앞서 설정한 MessageSource는 label.properties 파일로부터 메시지를 읽어오므로 <spring:message> 태그의 위치에 label.properties에 설정한 프로퍼티의 값이 출력된다.

## 🎈 메시지 처리를 위한 MessageSource와 <spring:message> 태그
스프링은 로케일(지역)에 상관없이 일관된 방법으로 문자열(메시지)을 관리할 수 있는 MessageSource 인터페이스를 정의하고 있다. 특정 로케일에 해당하는 메시지가 필요한 코드는 MessageSource의 getMessage() 메서드를 이용해서 필요한 메시지를 가져와 사용하는 식이다.

```java
package org.springframework.context;

import java.util.Locale;

public interface MessageSource {
    String getMessage(String code, Object[] args, String defaultMessage, 
    			Locale locale);
    String getMessage(String code, Object[] args, Locale locale)
    			throws NoSuchMessageException;
    ... // 일부 메서드 생략
}
```
getMessage() 메서드의
- code 파라미터: 메시지 구분
- locale 파라미터: 지역 구분

MessageSource의 구현체로는 자바의 프로퍼티 파일로부터 메시지를 읽어오는 **ResourceBundleMessageSource 클래스**를 사용한다. 이 클래스는 메시지 코드와 일치하는 이름을 가진 프로퍼티 값을 메시지로 제공한다. 또한, 해당 프로퍼티 파일이 클래스 패스에 위치해야 한다. 

따라서, <spring:message> 태그를 실행하면 내부적으로 MessageSource의 getMessage() 메서드를 실행해서 필요한 메시지를 구한다.

> <spring:message> 태그의 code 속성에 지정한 메시지가 존재하지 않으면 500 에러가 발생한다.


## 🎈 <spring:message> 태그의 메시지 인자 처리
앞서 작성한 label.properties 파일을 보면 다음과 같은 프로퍼티를 포함하고 있다.
```
register.done=<strong>{0}님</strong>, 회원 가입을 완료했습니다.
```
이 프로퍼티는 값 부분에 {0}을 포함하는데, 이는 인덱스 기반 변수 중 0번 인덱스의 값으로 대치되는 부분이다.

MessageSource의 getMessage()는 인덱스 기반 변수를 전달하기 위해 Object 배열 타입의 파라미터를 사용한다.
```java
Object[] args = new Object[1];
args[0] = "자바";
messageSource.getMessage("register.done", args, Locale.KOREA);
```

<spring:message> 태그를 사용할 때는 arguments 속성을 사용하여 인덱스 기반 변수값을 전달한다. 
Ex.
```
<spring:message code="register.done" arguments="${registerRequest.name}" />
```

### 만약 두 개 이상의 값을 전달해야 할 경우엔?
다음 방법 중 하나를 사용하자.
- 콤마로 구분한 문자열
	- Ex. ```<spring:message code="register.done" arguments="${registerRequest.name},${registerRequest.email}" />```
- 객체 배열
- <spring:argument> 태그 사용
	- Ex. 
    ```
    <spring:message code="register.done">
    	<spring:argument value="${registerRequest.name}" />
        <spring:argument value="${registerRequest.email}" />
    <spring:message>
    ```

# 커맨드 객체의 값 검증과 에러 메시지 처리
위에서 열심히 폼을 만들고 잘못 입력했을 때의 화면을 짠 들, 실제 입력한 값에 대한 검증 처리를 하지 않으면 비정상적인 값을 입력해도 동작해버리는 문제가 발생한다.

또한, (예를 들어) 회원가입 폼에서 중복된 이메일 주소를 입력해 다시 폼을 보여줄 때 왜 실패했는지 이유를 알려주지 않는다면 사용자는 혼란을 겪게 될 것이다.

위에서 언급한 두 가지 문제, **폼 값 검증**과 **에러 메시지 처리**는 어플리케이션을 개발할 때 놓쳐서는 안될 중요한 문제이다. 

따라서 스프링에서는이 두 가지 문제를 처리하기 위해 다음 방법을 제공한다.
- 커맨드 객체를 검증하고 결과를 에러 코드로 저장
- JSP에서 에러 코드로부터 메시지를 출력
## 1. 커맨드 객체 검증과 에러 코드 지정
스프링 MVC에서 커맨드 객체의 값이 올바른지 검사하려면 다음의 두 인터페이스를 사용한다.
- org.springframework.validation.Validator
- org.springframework.validation.Errors

객체를 검증할 때 사용하는 Validator 인터페이스는 다음과 같다.
```java
package org.springframework.validation

public interface Validator {
    boolean supports(Class<?> clazz);
    void validate(Object target, Errors errors);
}
```

- **supports()**: Validator가 검증할 수 있는 타입인지 검사
- **validate()**: 첫 번째 파라미터로 전달받은 객체를 검증하고 오류 결과를 Errors에 담는 기능 정의
	- 해당 메서드는 보통 다음과 같이 구현한다.
    1. 검사 대상 객체의 특정 프로퍼티나 상태가 올바른지 검사
    2. 올바르지 않다면 Errors의 rejectValue() 메서드를 이용해서 에러 코드 저장

## 2. Errors와 ValidationUtil 클래스의 주요 메서드
### Errors 인터페이스가 제공하는 에러 코드 추가 메서드
- reject(String errorCode)
- reject(String errorCode, String defaultMessage)
- reject(String errorCode, Object[] errorArgs, String defaultMessage)
- rejectValue(String field. String errorCode)
- rejectValue(String field. String errorCode, String defaultMessage)
- rejectValue(String field. String errorCode, Object[] errorArgs, String defaultMessage)
> - 에러 코드에 해당하는 메시지가 인덱스 기반 변수를 포함하는 경우 Object 배열 타입의 errorArgs 파라미터를 이용해서 변수에 삽입될 값을 전달한다.
> - defaultMessage 파라미터를 가진 메서드를 사용하면 에러 코드에 해당하는 메시지가 존재하지 않을 때, **익셉션을 발생시키는 대신 defaultMessage를 출력**한다.

### ValidationUtil 클래스가 제공하는 메서드
- rejectIfEmpty(Errors errors, String field, Stirng errorCode)
- rejectIfEmpty(Errors errors, String field, Stirng errorCode, Object[] errorArgs)
- rejectIfEmptyOrWhiteSpace(Errors errors, String field, Stirng errorCode)
- rejectIfEmptyOrWhiteSpace(Errors errors, String field, Stirng errorCode, Object[] errorArgs)

> - **rejectIfEmpty()** : (field에 해당하는 프로퍼티 값이) _null이거나 빈 문자열("")인 경우_ 에러 코드로 errorCode를 추가한다.
> - **rejectIfEmptyOrWhiteSpace()** : (field에 해당하는 프로퍼티 값이) _null이거나 빈 문자열인 경우, 그리고 공백 문자(스페이스, 탭 등)로만 값이 구성된 경우_ 에러 코드 추가

## 3. 커맨드 객체의 에러 메시지 출력
에러 코드를 지정한 이유는 알맞은 에러 메시지를 출력하기 위함이다. 

Errors에 에러 코드를 추가하면 JSP는 스프링이 제공하는 <form:errors> 태그를 사용해서 에러에 해당하는 메시지를 출력할 수 있다.

> 🎈 **<form:errors> 태그의 path 속성**: 에러 메시지를 출력할 프로퍼티 이름 지정

### 📌 에러 코드에 해당하는 메시지 코드 찾을 시 규칙(순서)
1. 에러코드 + "." + 커맨드객체이름 + "." + 필드명
2. 에러코드 + "." + 필드명
3. 에러코드 + "." + 필드타입
4. 에러코드

### 📌 프로퍼티 타입이 List나 목록인 경우 메시지 코드 생성 순서
1. 에러코드 + "." + 커맨드객체이름 + "." + 필드명[인덱스].중첩필드명
2. 에러코드 + "." + 커맨드객체이름 + "." + 필드명.중첩필드명
3. 에러코드 + "." + 필드명[인덱스].중첩필드명
4. 에러코드 + "." + 필드명.중첩필드명 
5. 에러코드 + "." + 중첩필드명 
6. 에러코드 + "." + 필드타입
7. 에러코드

### 📌 커맨드 객체에 추가한 글로벌 에러 코드 메시지 코드 검색 순서
1. 에러코드 + "." + 커맨드객체이름
2. 에러코드

## 4. <form:errors> 태그의 주요 속성
1. **element** : 각 에러 메시지를 **출력**할 때 사용할 HTML 태그. 기본 값은 span
2. **delimiter** : 각 에러 메시지를 **구분**할 때 사용할 HTML 태그. 기본 값은 <br/>

# 글로벌 범위 Validator와 컨트롤러 범위 Validator
스프링 MVC는 

1. 모든 컨트롤러에 적용할 수 있는 글로벌 Validator
2. 단일 컨트롤러에 적용할 수 있는 Validator

를 설정하는 방법을 제공한다.

이를 사용하면 @Valid 애노테이션을 사용해서 커맨드 객체에 검증 기능을 적용할 수 있다.
## 1. 글로벌 범위 Validator 설정과 @Valid 애노테이션
글로벌 범위 Validator는 모든 컨트롤러에 적용할 수 있는 Validator이다. 글로벌 범위 Validator를 적용하려면 다음 두 가지를 설정하면 된다.

### 1) 설정 클래스에서 WebMvcConfigurer의 getValidator() 메서드가 Validator 구현 객체를 리턴하도록 구현
```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public Validator getValidator() {
        return new RegisterRequestValidation();
    }
    ...
}
```

스프링 MVC는 getValidator() 메서드가 리턴한 객체를 글로벌 범위 Validator로 사용한다. 이렇게 글로벌 범위 Validator를 지정하면 @Valid 애노테이션을 사용해서 Validator를 적용할 수 있다.

### 2) 글로벌 범위 Validator가 검증할 커맨드 객체에 @Valid 애노테이션 적용
위와 같이 글로벌 범위 Validator를 지정하고 나면 @Valid 애노테이션을 사용해서 Validator를 적용할 수 있다.

Ex. 
```java
import javax.validation.Valid;
...

@Controller
public class RegisterController {

	...
    
    @PostMapping("/register/step3")
    public String handleStep3(@Valid RegisterRequest regReq, Errors errors) {
    	...
    }
    ...
}
```
이렇게 커맨드 객체에 해당하는 파라미터에 @Valid 애노테이션을 붙이면 글로벌 범위 Validator가 해당 타입을 검증할 수 있는지 확인한다. 이때 검증 수행 결과는 Errors 타입 파라미터로 받는다.

> ### ❗ @Valid 애노테이션 사용시 주의할 점
> Errors 타입 파라미터가 없으면 검증 실패 시 400 에러를 응답한다.

## 2. @InitBinder 애노테이션을 이용한 컨트롤러 범위 Validator
@InitBinder 애노테이션을 이용하면 컨트롤러 범위 Validator를 설정할 수 있다. 
Ex.
```java
@Controller
public class RegisterController {
	...
    
    @PostMapping("/register/step3")
    public String handleStep3(@Valid RegisterRequest regReq, Errors errors) {
    	...
    }
    
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
    	binder.setValidator(new RegisterRequestValidator());
    }
}
```
위 코드를 보면 커맨드 객체 파라미터에 @Valid 애노테이션을 적용하고 있다. 글로벌 범위 Validator를 사용할 때와 마찬가지로 handleStep3() 메서드에는 Validator 객체의 validate() 메서드를 호출하는 코드가 없다.

**어떤 Validator가 커맨드 객체를 검증할지는 initBinder() 메서드가 결정**한다. 

> @InitBinder 애노테이션을 적용한 메서드는 WebDataBinder 타입 파라미터를 갖는데, WebDataBinder.setValidator() 메서드를 이용해서 컨트롤러 범위에 적용할 Validator를 설정할 수 있다.

@InitBinder가 붙은 메서드는 컨트롤러의 요청 처리 메서드를 실행하기 전에 **매번** 실행된다. 

# Bean Validation을 이용한 값 검증 처리
Bean Validation 스펙에 @Valid, @NotNull, @Digits, @Size 등의 애노테이션이 정의되어 있다. 이 애노테이션들을 사용하면 Validator 작성 없이 애노테이션만으로 커맨드 객체의 값 검증을 처리할 수 있다.

### Bean Validation이 제공하는 애노테이션으로 커맨드 객체의 값 검증 방법
1. Bean Validation과 관련된 의존을 설정에 추가
2. OptionalValidatorFactoryBean 클래스를 빈으로 등록
	2.1) @EnableWebMvc 애노테이션을 사용하면 알아서 OptionalValidatorFactoryBean을 글로벌 범위 Validator로 등록하므로 추가로 설정해줄 필요 없다.
3. 커맨드 객체에 애노테이션을 이용해서 검증 규칙 설정

> ❗ 글로벌 범위 Validator를 따로 설정했다면 **해당 설정은 삭제하자.**

스프링 MVC는 별도로 설정한 글로벌 범위 Validator가 없을 때, OptionalValidatorFactoryBean을 글로벌 범위 Validator로 사용한다.

## Bean Validation의 주요 애노테이션
1. Bean Validation 1.1
https://docs.jboss.org/hibernate/beanvalidation/spec/1.1/api/javax/validation/constraints/package-summary.html
2. Bean Validation 2.0
https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/javax/validation/constraints/package-summary.html
