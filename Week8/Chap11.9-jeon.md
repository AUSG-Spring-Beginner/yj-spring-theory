# Chap11

# 요청 매핑 애노테이션을 이용한 경로 매핑

웹 애플리케이션을 개발하는 것은 아래의 두 요소를 개발하는 것이다.

- 특정 요청 URL 을 처리할 코드
    - @Controller 애노테이션을 사용한 컨트롤러 클래스로 구현
    - 컨트롤러 클래스는 요청 매핑 에노테이션을 사용해 메서드가 처리할 요청 경로 지정
    - 요청 매핑 애노테이션 적용 메서드를 두 개 이상 정의할 수도 있음. 여러 단계를 거쳐 하나의 기능이 완성되는 경우 관련 요청 경로를 한 개의 컨트롤러 클래스에서 처리하면 코드가 깔끔해진다
    
    ```java
    @RequestMapping("/register/step1")
    	public String handleStep1() {
    		return "register/step1";
    	}
    
    @PostMapping("/register/step2")
    	public String handleStep2(
    			@RequestParam(value = "agree", defaultValue = "false") Boolean agree,
    			Model model) {
    		if (!agree) {
    			return "register/step1";
    		}
    		model.addAttribute("registerRequest", new RegisterRequest());
    		return "register/step2";
    	}
    ```
    
    - 공통되는 부분의 경로를 담은 @RequestMapping 애노테이션을 클래스에 적용하고 각 메서드는 나머지 경로를 값으로 갖는 요청 매핑 애노테이션을 적용할 수 있다.
    
    ```java
    @Controller
    @RequestMapping("/register")
    public class RegisterController {	
    	@RequestMapping("/step1")
    		public String handleStep1() {
    			return "register/step1";
    		}
    	
    		@PostMapping("/step2")
    		public String handleStep2(
    				//생략
    		}
    }
    ```
    

- 처리 결과를 HTML 과 같은 형식으로 응답하는 코드
    - step1 에서는 별다를 처리 없이 약관 내용을 보여주면 돼서 이를 보여줄 뷰 이름을 리턴한다.
    

# GET&POST 구분

@GetMapping 을 사용하면 GET 방식으로 들어오는 요청만 처리하고, @PostMapping 을 사용하면 POST 방식으로 들어오는 요청만 처리한다.

# 요청 파라미터 접근

1. HttpServletRequest 직접 이용

```jsx
public String handleStep2(
		String agree = request.getParameter("agree");
		if (!agree) {
			return "register/step1";
		}
		model.addAttribute("registerRequest", new RegisterRequest());
		return "register/step2";
	}
```

1. @Requestparam 애노테이션 사용
    1. 요청 파라미터 개수가 몇 개 안 될때
    2. 애노테이션 속성
    
    | 속성 | 타입 | 설명 |
    | --- | --- | --- |
    | value | String | Http 요청 파라미터 이름 지정 |
    | required | boolean | 필수 엽 지정 |
    | default | String | 요청 파라미터가 값이 없을 때 사용할 문자열 값 |
    
    ```jsx
    public String handleStep2(
    			@RequestParam(value = "agree", defaultValue = "false") Boolean agree,
    			Model model) {
    		if (!agree) {
    			return "register/step1";
    		}
    		model.addAttribute("registerRequest", new RegisterRequest());
    		return "register/step2";
    	}
    ```
    

스프링 MVC 는 파라미터 타입에 따라 String 값을 변환해준다. 예시 코드는 agree 요청 파라미터의 값을 읽어와 Boolean 타입으로 변환하여 전달한다.

# 리다이렉트 처리

잘못된 전송 방식으로 요청이 왔을 때 알맞은 경로로 리다이렉트하는 것이 좋을 때가 있다. 컨트롤러에서 특정 페이지로 리다이렉트하려면, “redirect:경로” 를 뷰 이름으로 리턴하면 된다.

```jsx
@GetMapping("/register/step2")
	public String handleStep2Get() {
		return "redirect:/register/step1";
	}
```

@RequestMapping, @Getmapping 등 요청 매핑 관련 애노테이션을 적용한 메서드가 redirect: 로 시작하는 경로를 리턴하면 나머지 경로를 이용해서 리다이렉트 할경로를 구한다. 웹 어플리켜이션 경로가 `sp5-chap11` 이었으면 리다이렉트 경로는 `sp5-chap11/register/step1` 이다.

이동 경로가 /로 시작하지 않으면 현재 경로를 기준으로 상대 경로를 사용한다.

# 커맨드 객체를 이용해서 요청 파라미터 사용하기

step2.jsp 가 생성하는 폼은 다음 파라미터를 이용해 정보를 서버에 전달한다.

- email
- name
- password
- confirmpassword

스프링은 파라미터 값이 여러 개일때 각 줄에서 값을 읽어와야 하는데 이런 불편함을 줄이기 위해 `command` 객체를 이용한다. 이름이 name 인 요청 파라미터의 값을 커맨드 객체의 setName() 메서드를 사용해 커맨드 객체에 전달하는 기능을 제공한다. 요청 파라미터의 값을 전달받을 세터 메서드를 포함하는 객체를 커맨드 객체로 사용하면 된다. 이는 적용될 메서드의 파라미터에 위치한다. 

```jsx
@PostMapping("/register/step3")
	public String handleStep3(RegisterRequest regReq) {
		try {
			memberRegisterService.regist(regReq);
			return "register/step3";
		} catch (DuplicateMemberException ex) {
			return "register/step2";
		}
	}
```

RegisterRequest 클래스에는 세터 메서드들이 있다. 스프링은 이들을 사용해서 각 요청 파라미터의 값을 커맨드 객체에 복사해 regReq 파라미터로 전달한다.

# 뷰 JSP 코드에서 커맨드 객체 사용하기

위의 RegisterRequest 가 커맨드 객체에 접근할 때 사용할 속성 이름이 된다.스흐링 MVC 는 커맨드 객체의 클래스 이름과 동일한 속성 이름을 사용해 커맨드 객체를 뷰에 전달한다. 

```jsx
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>회원가입</title>
</head>
<body>
    <p><strong>${registerRequest.name}님</strong> 
        회원 가입을 완료했습니다.</p>
    <p><a href="<c:url value='/main'/>">[첫 화면 이동]</a></p>
</body>
</html>
```

# @ModelAttribute 애노테이션으로 커맨드 객체 속성 이름 변경

커맨드 객체로 사용할 파라미터에 @ModelAttribute 애노테이션을 적용한다. 이는 모델에서 사용할 속성 이름을 값으로 설정하고, 아래 코드는 뷰 코드에서 “formdata” 라는 이름으로 커맨드 객체에 접근한다.

```jsx
public String handleStep3(@ModelAttribute("formdata") RegisterRequest regReq) {
		try {
			memberRegisterService.regist(regReq);
			return "register/step3";
		} catch (DuplicateMemberException ex) {
			return "register/step2";
		}
```

# 커맨드 객체와 스프링 폼 연동

폼을 보여줄 때 커맨드 객체의 값을 폼에 채워주면 다시 폼을 보여줘도 값을 다시 입력하지 않아도 된다.

```jsx
<p>
        <label>이메일:<br>
        <input type="text" name="email" id="email" value="${registerRequest.email}">
        </label>
    </p>
    <p>
        <label>이름:<br>
        <input type="text" name="name" id="name" value="${registerRequest.name}">
        </label>
    </p>
```