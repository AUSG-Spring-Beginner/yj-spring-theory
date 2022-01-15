스프링 MVC를 사용해서 웹 어플리케이션을 개발한다는 것은 결국 컨트롤러와 뷰 코드를 구현한다는 것을 뜻한다. 대부분 설정은 개발 초기에 완성된다.

이번 11장에서는 기본적인 컨트롤러와 뷰의 구현 방법에 대해 다룰 것이다.

# 요청 매핑 애노테이션을 이용한 경로 매핑
웹 어플리케이션을 개발하는 것은 다음 코드를 작성하는 것이다.
- 특정 요청 URL을 처리할 코드
- 처리 결과를 HTML과 같은 형식으로 응답하는 코드

이 중, 특정 요청 URL을 처리할 코드는 @Controller 애노테이션을 사용한 컨트롤러 클래스를 이용해서 구현한다.

컨트롤러 클래스는 요청 매핑 애노테이션을 사용해서 메서드가 처리할 요청 경로를 지정한다. 
요청 매핑 애노테이션에는 @RequestMapping, @GetMapping, @PostMapping 등이 있다.

## 🎈 요청 매핑 애노테이션을 적용한 메서드를 두 개 이상 정의할 수도 있다!
여러 단계를 거쳐 하나의 기능이 완성되는 경우 관련 요청 경로를 한 개의 컨트롤러 클래스에서 처리할 수 있다.

예를 들어 회원 가입 과정이 
1. 약관 동의
2. 회원 정보 입력
3. 가입 완료

라면, 다음과 같이 컨트롤러 클래스를 작성할 수 있겠다.
```java
@Controller
public class RegistController {

  @RequestMapping("/register/step1")
  public String handleStep1() {
    return "register/step1";
  }
  
  @RequestMapping("/register/step2")
  public String handleStep2() {
    ...
  }
  
  @RequestMapping("/register/step3")
  public String handleStep3() {
    ...
  }
}
```

여기서, 각 요청 매핑 애노테이션 경로가 "/register"로 시작한다. 
이 경우, **공통되는 부분의 경로를 담은 @RequestMapping 애노테이션을 클래스에 적용**하고, 각 메서드는 나머지 경로를 값으로 갖는 요청 매핑 애노테이션을 적용할 수 있다.
```java
@Controller
@RequestMapping("/register")
public class RegistController {

  @RequestMapping("/step1")
  public String handleStep1() {
    return "register/step1";
  }
  
  @RequestMapping("/step2")
  public String handleStep2() {
    ...
  }
  
  @RequestMapping("step3")
  public String handleStep3() {
    ...
  }
}
```
스프링 MVC는 클래스에 적용한 요청 매핑 애노테이션의 경로 + 메서드에 적용한 요청 매핑 애노테이션의 경로로 찾기 때문에,
위 코드에서 handleStep1() 메서드가 처리하는 경로는 "/step1"이 아닌 "/register/step1"이다.

# GET과 POST 구분: @GetMapping, @PostMapping
주로 폼(form)을 전송할 때 POST 방식을 사용하는데, 스프링 MVC는 별도 설정이 없으면 GET과 POST 방식에 상관없이 @RequestMapping에 지정한 경로와 일치하는 요청을 처리한다.

따라서 만약 POST 방식 요청으로만 처리하고 싶다면, 다음과 같이 @PostMapping 애노테이션을 사용해서 제한할 수 있다.
```java
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {

  @PostMapping("/register/step2")
  public String handleStep2() {
    return "register/step2";
  } 
  ...
}
```
위와 같이 설정하면 handleStep2() 메서드는 POST 방식의 "/register/step2" 요청 경로만 처리하면, GET 방식으로는 처리하지 않는다.

마찬가지로 GET 방식 요청으로만 처리하고 싶다면 @GetAMapping 애노테이션으로 제한할 수 있다.

만약 **같은 경로에 대해 POST와 GET 방식 각각 다르게 처리**하고 싶다면 다음과 같이 메서드를 따로 만들어 처리할 수 있다.
```java
@Controller
public class LoginController {

  @GetMapping("/member/login")
  public String form() { ... }
  
  @PostMapping("/member/login")
  public String login() { ... }
  
  ...
}
```
> 💡@GetMapping 애노테이션과 @PostMapping 애노테이션은 스프링 4.3버전에 추가된 것으로, 이전 버전까지는 @RequestMapping 애노테이션의 method 속성을 사용해 HTTP 방식을 제한했다.
> 
> EX. @RequestMapping(value="/member/login", method = RequestMethod.GET)

# 요청 파라미터 접근
컨트롤러 메서드에서 요청 파라미터를 사용하는 방법에는 2가지가 있다.

## 1. HttpServletRequest를 직접 이용
컨트롤러 처리 메서드의 파라미터로 HttpServletRequest 타입을 사용하고, HttpServletRequest의 getParameter() 메서드를 이용해 파라미터의 값을 구할 수 있다.

## 2. @RequestParam 애노테이션 사용
요청 파라미터의 개수가 많지 않다면 이 애노테이션을 사용해 간단하게 요청 파라미터의 값을 구할 수 있다.

Ex.
```java
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {
  ...
  @PostMapping("/register/step2")
  public String handleStep2(
      @RequestParam(value="agree", defaultValue="false") Boolean agreeVal) {
    if(!agree) {
      return "register/step1";
    }
    
    return "register/step2";
  }
  
}
```

### 🎈 @RequestParam 애노테이션의 속성
| 속성 | 타입 | 설명 |
| --- | --- | --- |
| value | String | HTTP 요청 파라미터의 이름 지정 |
| required | boolean | 필수 여부 지정. 이 값이 true면서 해당 요청 파라미터에 값이 없으면 익셉션 발생. 기본값은 true |
| defaultValue | String | 요청 파라미터가 값이 없을 때 사용할 문자열 값 지정. 기본값은 없다. |

@RequestParam은 Boolean 타입 외에 int, long, Integer, Long 등 기본 데이터 타입과 래퍼 타입에 대한 변환을 지원한다.

예제로, 회원가입 과정 중 약관 동의가 안되었을 시 다시 약관 동의 폼을 보여주고 싶고, 동의가 되었을 시에 회원 정보 입력 폼을 보여주고 싶다면 다음과 같이 코드를 작성하면 된다.
```java
package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    ...

    @PostMapping("/register/step2")
    public String handleStep2(
            @RequestParam(value="agree", defaultValue = "false") Boolean agree, Model model) {
        if(!agree) {
            return "register/step1";
        }
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register/step2";
    }
}

```

> 💡 자바 코드를 수정한 뒤에는 서버를 재시작해야 변경 내용이 반영된다!

# 리다이렉트 처리

우리가 원하는 회원가입 과정은
1. 약관 동의
2. 회원 정보 입력
3. 가입 오나료

인데, 처음부터 2단계로 들어가려고 링크를 입력하면 handleStep2() 메서드는 POST 방식만 처리하기 때문에 405 에러가 뜨게 된다.

우리가 원하는 건 에러 화면이 뜨는 것보다는 회원가입의 맨 첫 단계 화면을 보여주는 것이다. 이 경우 어떻게 해야할까?

알맞은 경로로 리다이렉트하려면, **"redirect:경로"**를 뷰 이름으로 리턴하면 된다.

Ex.
```java
@GetMapping("/register/step2")
public String handleStep2Get() {
    return "redirect:/register/step1";
}
```
> ### 🤔 redirect 뒤에 경로를 어떻게 적을까?
> redirect 뒤의 문자열이
> 1. '/'로 시작 → 웹 어플리케이션을 기준으로 이동 경로 생성
> 2. '/'로 시작 X → 현재 경로를 기준으로 상대 경로 사용
> 3. 완전한 URL → 해당 경로로 리다이렉트

# 커맨드 객체를 이용해서 요청 파라미터 사용하기
회원가입에서 다음과 같은 정보들을 입력했다고 하자.
- email(이메일)
- name(이름)
- password(비밀번호)
- confirmPassword(비밀번호 확인)

폼 전송 요청을 처리하는 컨트롤러 코드는 각 파라미터의 값을 구하기 위해 다음과 같은 코드를 사용할 수 있다.
```java
@PostMapping("/register/step3")
public String handleStep3(HttpServletRequest req) {
  String email = req.getParameter("email");
  String name = req.getParameter("namel");
  String password = req.getParameter("password");
  String confirmPassword = req.getParameter("confirmPassword");
  
  RegisterRequest regReq = new RegisterRequest();
  regReq.setEmail(email);
  regReq.setName(name);
  ...
}
```

위 코드가 올바르게는 동작하지만, 만약 파라미터들이 너무 많아진다면 코드 길이도 함께 늘어갈 것이고, 코드가 더욱 복잡해질 것이다.

스프링은 이런 불편함을 줄이기 위해 **요청 파라미터의 값을 커맨드(command) 객체에 담아주는 기능**을 제공한다. 
특별한 코드를 작성해야 하는 건 아니고, 그저 요청 파라미터의 값을 전달받을 수 있는 세터 메서드를 포함하는 객체를 커맨드 객체로 사용하면 된다.

커맨드 객체는 요청 매핑 애노테티션이 적용된 메서드의 파라미터에 위치한다.
```java
@PostMapping("/register/step3")
public String handleStep3(RegisterRequest regReq) {
  ...
}
```
스프링 MVC가 해당 메서드에 전달할 커맨드 객체를 생성하고 그 객체의 세터 메서드를 이용해서 일치하는 요청 파라미터의 값을 전달한다.

# 뷰 JSP 코드에서 커맨드 객체 사용하기

회원 가입이 끝나면 가입할 때 사용한 이메일 주소나 이름을 화면에 띄워주면 더 좋을 거 같다는 생각이 든다.

이때, HTTP 요청 파라미터를 이용해서 회원정보를 전달했으니까 JSP의 표현식 등을 이용해서 정보를 표시해도 되지만, **커맨드 객체**를 사용해서 정보를 표시할 수도 있다.

```java
@PostMapping("/register/step3")
public String handleStep3(RegisterRequest regReq) {
  ...
}
```
위와 같이 커맨드 객체의 기본 이름은 클래스 이름을 사용하고,
```
<p><strong>${registerRequest.name}님</strong>
회원 가입을 완료했습니다.
```
뷰 코드에서는 커맨드 객체의 첫 글자를 소문자로 바꾼 클래스 이름을 속성 이름으로 사용해서 커맨드 객체에 접근할 수 있다.

> ### 🤔 클래스 이름의 첫 글자만 소문자로 바꾼 걸 속성 이름으로? 이거 바꿀 수 있나?
> 커맨드 객체로 사용할 파라미터에 @ModelAttribute 애노테이션을 적용하면 속성 이름을 변경할 수 있다.
>
> Ex.
> ```java
> import org.springframework.web.bind.annotation.ModelAttribute;
> 
> @PostMapping("/register/step3")
> public String handleStep3(@ModelAttribute("formData") RegisterRequest regReq) { ... }
> ```
> 이렇게 되면 속성이름을 registerRequest에서 formData로 바꾼 게 된다.

# 커맨드 객체와 스프링 폼 연동
회원가입 과정 중 회원 정보를 입력할 때 중복된 이메일 주소를 입력하는 등의 가입 조건 불만족으로 폼을 다시 입력해야 하는 상황이 있었을 것이다.

이 경우 다시 폼을 보여줄 때 커맨드 객체의 값을 폼에 채워주면, 즉 이전에 입력했던 특정값이 지워지지 않게 해주면 다시 입력해야 하는 불편함은 없을 것이다.
```
<input type="text" name="email" id="email" value="${registerRequest.email}">
...
```

근데 위 태그 말고, 스프링 MVC가 제공하는 커스텀 태그를 사용하면 좀 더 간단하게 커맨드 객체의 값을 출력할 수 있다.
스프링은 ```<form:form>``` 태그와 ```<form:input>``` 태그를 제공하고 있다. 위 코드를 커스텀 태그로 작성한 코드로 바꾸면 다음과 같다.
```
<form:input path="email"/>
...
```
스프링이 제공하는 이런 폼 태그를 사용하기 위해서는 taglib 디렉티브를 설정해야한다.
```
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
```

- ```<form:input>``` 태그: <input> 태그를 생성한다. path로 지정한 커맨드 객체의 프로퍼티를 <input> 태그의 value 속성값으로 사용한다.
- ```<form:password>``` 태그: <password> 타입의 <input> 태그를 생성하므로 value 속성의 값을 빈 문자열로 설정한다.
* ```<form:form>``` 태그 → 이 태그를 사용하려면 커맨드 객체가 존재해야 한다.
