# Chap9

# 스프링 MVC를 위한 설정

## 스프링 MVC 설정

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.jsp("/WEB-INF/view/", ".jsp");
    }

}
```

@EnableWebMvc 애노테이션은 스프링 MVC 설정을 활성화한다. 내부적으로 다양한 빈 설정을 추가해준다. 또 `WebMvcConfigurer` 인터페이스는 스프링 MVC 의 개별 설정을 조정할 때 사용한다.

DispatchServlet을 통하여 웹 요청을 처리한다.

ConfigureViewResolvers로 컨트롤러의 실행 결과를 보여준다.

## web.xml 파일에 DispatcherServlet 설정

```java
<servlet>
        <servlet-name>dispatcher</servlet-name>
        <servlet-class>
            org.springframework.web.servlet.DispatcherServlet
        </servlet-class>
        <init-param>
            <param-name>contextClass</param-name>
            <param-value>
                org.springframework.web.context.support.AnnotationConfigWebApplicationContext
            </param-value>
        </init-param>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>
                config.MvcConfig
                config.ControllerConfig
            </param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
```

## 코드 구현

### 컨트롤러 구현

```java
package chap09;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HelloController {

	@GetMapping("/hello")
	public String hello(Model model,
			@RequestParam(value = "name", required = false) String name) {
		model.addAttribute("greeting", "안녕하세요, " + name);
		return "hello";
	}
}
```

- `@Contoller` 애노테이션으로 MVC의 컨트롤러로 사용한다.
- `@getMapping` 애노테이션으로 메서드가 처리할 요청 경로를 지정한다.
- Model 파라미터는 컨트롤러의 처리 결과를 뷰에 전달할 때 사용한다.
- `@RequestParam` 애노테이션은 HTTP 요청 파라미터의 값을 메서드의 파라미터로 전달할 때 사용한다.

`Controller` 란 웹 요청을 처리하고 그 결과를 뷰에 전달하는 스프링 빈 객체이다. 스프링 컨트롤러로 사용될 클래스는  @Controller 애노테이션을 붙여야 하고 @GetMapping 애노테이션이나 @PostMapping 애노테이션과 같은 요청 매핑 애노테이션을 이용하여 처리할 경로를 지정해 주어야 한다.

`model.addAttribute()`  메서드의 첫 번째 파라미터는 데이터를 식별하는데 사용되는 속성 이름이고 두 번째 파라미터는 속성 이름에 해당하는 값이다.

마지막으로 `@GetMapping` 이 붙은 메서드는 컨트롤러의 실행 결과를 보여줄 뷰 이름을 리턴한다. 이 뷰 이름은 논리적인 이름이며 실제 뷰는 ViewResolver 가 처리한다.

### JSP 구현

컨트롤러가 생성한 결과를 보여줄 뷰 코드이다. 뷰 코드는 JSP를 이용해 구현한다.

```java
<%@ page contentType="text/html; charset=utf-8" %>
<!DOCTYPE html>
<html>
  <head>
    <title>Hello</title>
  </head>
  <body>
    인사말: ${greeting}
  </body>
</html>
```

${greeting} 은 위의 컨트롤러의 addAttribute 속성의 이름인 greeting 과 동일하다.이렇게 컨트롤러에서 설정한 속성을 뷰 JSP 코드에서 접근할 수 있다. 이는 MVC 프레임워크가 모델에 추가한 속성을 JSP 에서 접근할 수 있게 HttpServletRequest 에 옮겨준다.

위의 HelloController 의 hello() 메서드가 리턴한 뷰 이름이 hello 였는데, jsp 파일의 이름이 hello.jsp 이다. 뷰 이름과 jsp 파일과의 연결이 MvcConfig 의 다음 설정을 통해 이루어진다.

```java
@override
public void configureViewResolvers(ViewResolverRegistry registry){
	registry.jsp("/WEB-INF/view/",".jsp");
}
```

`registry.jsp()` 는 jsp를 뷰 구현으로 사용할 수 있도록 해주는 설정이다. 첫 번째 인자는 파일 경로이고 두 번째 인자는 접미사이다.
