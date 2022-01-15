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
