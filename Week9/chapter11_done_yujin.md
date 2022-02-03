# 컨트롤러 구현 없는 경로 매핑
컨트롤러는 뷰가 응답 화면을 구성하는데 필요한 데이터를 생성해서 전달하는 역할이지만, 특별히 처리할 것 없이 단순히 뷰 이름만 리턴하는 컨트롤러를 구현해야 할 때도 있다. 

단순 연결을 위해 특별한 로직이 없는 컨트롤러 클래스를 만드는 것보다, ```addViewControllers()``` 메서드를 재정의하면 컨트롤러 구현 없이 간단한 코드로 요청 경로와 뷰 이름을 연결할 수 있다.

Ex.
```java
@Override
public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/main").serViewName("main");
}
```

# 주요 에러 발생 상황
처음 스프링 MVC를 이용해서 웹 개발을 하다보면 사소한 설정 오류나 오타로 고생하는 경우가 빈번하다. 입문 과정에서 겪게 되는 주 에러 사례 2가지를 정리해보았다.

## 1. 요청 매핑 애노테이션 관련 주요 익셉션

흔한 에러는 **404 에러**이다. 
- 요청 경로를 처리할 컨트롤러가 존재하지 않는다.
- WebMvcConfigurer를 이용한 설정이 없다.
- 뷰 이름에 해당하는 JSP 파일이 존재하지 않는다.

위 세 가지 경우 404 에러가 발생할 수 있다.

404 에러가 발생했다면 다음 사항을 확인하자.
- 요청 경로가 올바른지
- 컨트롤러에 설정한 경로가 올바른지
- 컨트롤러 클래스를 빈으로 등록했는지
- 컨트롤러 클래스에 @Controller 애노테이션을 적용했는지

404 에러 발생 요인 세번째(뷰 이름에 해당하는 JSP 파일이 없을 경우)는 Message란에 **존재하지 않는 JSP 파일의 경로가 출력**된다. 해당 메시지와 함께 404 에러가 발생했다면 컨트롤러에서 리턴하는 뷰 이름에 해당하는 JSP 파일이 존재하는지 확인하자.

지원하지 않는 전송 방식(method)을 사용한 경우 **405 에러**가 발생한다. 예를 들어 POST 방식만 처리하는 요청 경로를 GET 방식으로 연결하면 405 에러가 발생한다.


## 2. @RequestParam이나 커맨드 객체와 관련된 주요 익셉션

### 📌 @RequestParam 애노테이션을 필수로 설정하고 기본값을 지정하지 않은 경우
기본값을 지정하지 않았을 때, 필수로 지정된 특정 파라미터로 아무 값도 전송되지 않는다면 @RequestParam 애노테이션을 처리하는 과정에서 해당 파라미터가 존재하지 않는다면 익셉션이 나타나고, **400 에러**가 발생한다. 

### 📌 요청 파라미터의 값을 @RequestParam이 적용된 파라미터의 타입으로 변환할 수 없는 경우
요청 파라미터의 값을 커맨드 객체에 복사하는 과정에서도 해당 요인으로 동일하게 발생할 수 있다.

> ## 💡 콘솔에 출력된 로그 메시지를 참고하면 도움이 된다!
LogBack 등 로그 모듈의 로그 레벨을 'DEBUG' 레벨로 낮추면 더 자세한 로그를 얻을 수 있다.

# 커맨드 객체: 중첩 · 콜렉션 프로퍼티
스프링 MVC는 커맨드 객체가 리스트 타입의 프로퍼티를 가졌거나 중첩 프로퍼티를 가진 경우에도 요청 파라미터의 값을 알맞게 커맨드 객체에 설정해주는 기능을 제공한다. 규칙은 다음과 같다.
- HTTP 요청 파라미터 이름이 "프로퍼티이름[인덱스]" 형식이면 List 타입 프로퍼티의 값 목록으로 처리
- HTTP 요청 파라미터 이름이 "프로퍼티이름.프로퍼티이름" 형식이면 중첩 프로퍼티 값을 처리

# Model을 통해 컨트롤러에서 뷰에 데이터 전달하기
지금까지 구현해본 컨트롤러는 두 가지 특징이 있다.
- Model을 이용해서 뷰에 전달할 데이터 설정
- 결과를 보여줄 뷰 이름 리턴

이 두 가지를 한번에 처리할 수 있는 방법이 있는데, 그게 **ModelAndView**이다.
## 1. ModelAndView를 통한 뷰 선택과 모델 전달
ModelAndView는 모델과 뷰 이름을 함께 제공한다. 뷰에 전달할 모델 데이터는 ```addObject()``` 메서드로 추가하고, 뷰 이름은 ```setViewName()``` 메서드를 이용해서 지정한다.
## 2. GET 방식과 POST 방식에 동일 이름 커맨드 객체 사용하기
만약 같은 경로에 대하여 GET 방식과 POST 방식으로 처리하는 결과가 다르도록 컨트롤러를 만들어야 한다면, 각각 GET 요청과 POST 요청을 처리하는 메서드에 @ModelAttribute 애노테이션을 붙인 커맨드 객체를 파라미터로 추가하면 된다.

# 주요 폼 태그 설명

| 커스텀 태그 | 설명 |
| --- | --- |
| <form:form> | form 태그 생성 시 사용. 다음 속성을 추가로 제공한다. (action: 폼 데이터를 전송할 URL입력, enctype: 전송될 데이터의 인코딩 타입, method: 전송 방식)|
| <form:input> | text 타입의 input 태그 |
| <form:password> | password 타입의 input 태그 |
| <form:hidden> | hidden 타입의 input 태그 |
| <form:select> | select 태그 생성. option 태그를 생성할 때 필요한 콜렉션을 전달받을 수도 있다. |
| <form:options> | 지정한 콜렉션 객체를 이용해서 option 태그를 생성. |
| <form:option> | option 태그 한 개 생성. |
| <form:checkboxes> | 커맨드 객체의 특정 프로퍼티와 관련된 checkbox 타입의 input 태그 목록 생성 |
| <form:checkbox> | 커맨드 객체의 특정 프로퍼티와 관련된 한 개의 checkbox 타입의 input 태그 생성 |
| <form:radiobuttons> | 커맨드 객체의 특정 프로퍼티와 관련된 radio 타입의 input 태그 목록 생성 |
| <form:radiobutton> | 커맨드 객체의 특정 프로퍼티와 관련된 한 개의 radio 타입의 input 태그 생성 |
| <form:textarea> | 게시글 내용과 같이 여러 줄을 입력받아야 하는 경우 사용. |

## 🎈 CSS 및 HTML 태그와 관련된 공통 속성
<form:input>, <form:select> 등 입력 폼과 관련해서 제공하는 스프링 커스텀 태그는 HTML의 CSS 및 이벤트 관련 속성을 제공하고 있다.
### 1. CSS
- cssClass: HTML의 class 속성값
- cssErrorClass: 폼 검증 에러가 발생했을 때 사용할 HTML의 class 속성값
- cssStyle: HTML의 style 속성값
### 2. HTML 태그가 사용하는 속성 (사용 가능)
- id, title, dir
- disabled, tabindex
- onfocus, onblur, onchange
- onkeydown, onkeypress, onkeyup
- onmousedown, onmousemove, onmouseup
- onmouseout, onmouseover

또한 각 커스텀 태그는 htmlEscape 속성을 사용해서 커맨드 객체의 값에 포함된 HTML 특수 문자를 엔티티 레퍼런스로 변환할지를 결정할 수 있다.
