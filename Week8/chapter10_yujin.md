다음 코드는 스프링 MVC 설정에 관한 코드이다.
```
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    
    @Override
    public void configureDefaultServletHandling
    		(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
    
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.jsp("/WEB-INF/view/", ".jsp");
    }
}
```

> ## 💡 용어 정리 및 코드 설명
> - **@EnableWebMvc**: 내부적으로 다양한 빈 설정을 추가해주는 애노테이션
> - **WebMvcConfigurer 인터페이스**: 스프링 MVC의 개별 설정을 조정할 때 사용
> - ```configureDefaultServletHandling()```: DispatcherServlet의 매핑 경로를 '/'로 주었을 때, JSP/HTML/CSS 등을 올바르게 처리하기 위한 설정을 추가한다.
> - ```registry.jsp("/WEB-INF/view/", ".jsp")```: JSP를 이용해서 컨트롤러의 실행결과를 보여주기 위한 설정을 추가한다.

위 설정이면 스프링 MVC를 이용해서 웹 어플리케이션 개발하는데 필요한 최소 설정이 끝난다. 남은 작업은 컨트롤러와 뷰 생성을 위한 JSP 코드를 작성하는 것이다. 

단순해보이는 이 설정은 실제로 수십 줄에 가까운 설정을 대신 만들어주는데, 이걸 다 알 필요는 없다.

단, 
- 스프링 MVC를 구성하는 주요 요소가 무엇이고 
- 각 구성 요소들이 서로 어떻게 연결되는지

를 이해하면 다양한 환경에서 스프링 MVC를 빠르게 적용하는데 많은 도움이 된다. 

# 스프링 MVC 핵심 구성 요소
![](https://images.velog.io/images/nanaeu/post/9b4f5b55-f716-4757-b77c-9b037fa2372c/image.png)(이미지 참고: https://pangtrue.tistory.com/83)

이 그림은 스프링 MVC의 핵심 구성 요소와 각 요소 간의 관계를 나타낸 그림으로, 매우 중요한 그림이니 수시로 참조하며 내용을 이해해보자.

## 🎈 웹 요청 처리 과정
위 그림의 중앙에 위치한 DispatcherServlet은 모든 연결을 담당한다. 

1. 웹 브라우저로부터 요청이 들어오면
2. 그 요청을 처리하기 위한 컨트롤러 객체를 검색한다.
	2.1 이때, 직접 검색하지 않고 **HandlerMapping**이라는 빈 객체에게 컨트롤러 검색을 요청한다.

> ### 💡HandlerMapping
> 클라이언트의 요청 경로를 이용해서 이를 처리할 컨트롤러 빈 객체를 DispatcherServlet에 전달한다. 

컨트롤러 객체를 DispactcherServlet이 전달받았다고 해서 바로 컨트롤러 객체의 메서드를 실행할 수 있을까?

그럴리가 ^^

DispatcherServlet은 
- @Controller 애노테이션을 이용해서 구현한 컨트롤러
- Controller 인터페이스를 구현한 컨트롤러
- 특수 목적으로 사용되는 HttpRequestHandler 인터페이스를 구현한 클래스

를 동일한 방식으로 실행할 수 있도록 만들어졌다. 이를 위해 중간에 사용되는 것이 **HandlerAdapter** 빈이다.

> ### 💡HandlerAdapter
> DispatcherServlet은 HandlerMapping이 찾아준 컨트롤러 객체를 처리할 수 있는 HandlerAdapter 빈에게 요청 처리를 위임한다. 
>
> 그러면 HandlerAdapter는 컨트롤러의 알맞은 메서드를 호출해서 요청을 처리하고, 그 결과를 DispatcherServlet에 리턴한다. 
>
> 이때, HandlerAdapter는 컨트롤러의 처리 결과를 ModelAndView라는 객체로 변환해서 DispatcherServlet에 리턴한다.

HandlerAdapter로부터 컨트롤러의 요청 처리 결과를 ModelAndView로 받으면, DispatcherServlet은 _결과를 보여줄 뷰를 찾기 위해_ **ViewResolver 빈 객체**를 사용한다.

> ### 💡ViewResolver
> ModelAndView는 컨트롤러가 리턴한 뷰 이름을 담고 있는데, ViewResolver는 이 뷰 이름에 해당하는 View 객체를 찾거나 생성해서 리턴한다.
>
> 응답을 생성하기 위해 JSP를 사용하는 ViewResolver는 매번 새로운 View 객체를 생성해서 DispatcherServlet에 리턴한다.

DispatcherServlet은 ViewResolver가 리턴한 View 객체에게 응답 결과 생성을 요청한다. 

처리 과정을 보면, DispatcherServlet을 중심으로 
- HandlerMapping
- HandlerAdapter
- 컨트롤러
- ViewResolver
- View
- JSP

가 각자 역할을 수행해서 클라이언트의 요청을 처리하는 것을 알 수 있다.

**이 중 하나라도 어긋나면 클라이언트의 요청을 처리할 수 없다!**
따라서, 각 구성 요소를 올바르게 설정할 수 있도록 주의하자.

## 컨트롤러와 핸들러
> - 클라이언트의 요청을 실제로 처리하는 것은 **컨트롤러**
> - 클라이언트의 요청을 전달받는 창구 역할은 **DispatcherServlet**

앞서 언급했듯이, DispatcherServlet은 클라이언트의 요청을 처리할 컨트롤러를 찾기 위해 HandlerMapping을 사용한다. 

### 🤔 '컨트롤러'를 찾아주는 객체면 ControllerMapping이 맞지 않나?
스프링 MVC는 웹 요청을 처리할 수 있는 범용 프레임워크이다. 

@Controller를 붙인 클래스 뿐만 아니라 자신이 직접 만든 클래스를 이용해서 클라이언트의 요청을 처리할 수도 있는 것이다.

즉, DiapatcherServlet 입장에서는 클라이언트 요청을 처리하는 객체 타입이 **반드시 @Controller를 적용한 클래스일 필요는 없다!**

이러한 이유로 스프링 MVC는 웹 요청을 실제로 처리하는 객체를 **핸들러(Handler)**라고 표현하고 있으며, 특정 요청 경로를 처리해주는 핸들러를 찾아주는 객체를 **HandlerMapping**이라 부른다.

DispatcherServlet은 핸들러 객체의 실제 타입과 관계없이 실행 결과를 **ModelAndView** 타입으로만 받을 수 있으면 된다.

### 🤔 핸들러의 실제 구현 타입에 따라 ModelAndView 타입이 아닌 객체를 리턴하는 경우도 있던데?
이 경우, 핸들러의 처리 결과를 ModelAndView 타입으로 변환해주는 객체가 필요하다.

이 역할을 해주는 객체가 바로 **HandlerAdapter**이다.

핸들러 객체의 실제 타입마다 그에 알맞은 HandlerMapping과 HandlerAdpater가 존재하기 때문에, 사용할 핸들러의 종류에 따라 해당 HandlerMapping과 HandlerAdapter를 스프링 빈으로 등록해야 한다. 

근데 사실 이 부분은 직접 등록하지 않아도, 스프링이 제공하는 설정 기능을 사용하면 된다. (@EnableWebMvc 애노테이션 - 이 글에서 추후 설명 예정)

# DispatcherServlet과 스프링 컨테이너
다음은 스프링 MVC 실습 중 web.xml의 일부 코드이다.
```
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
```
DispatcherServlet은 전달받은 설정 파일을 이용해서 스프링 컨테이너를 생성하는데, 앞에서 나온 HandlerMapping, HandlerAdapter, 컨트롤러, ViewResolver 등의 빈이 여기서 구해진다. 따라서 DispatcherServlet이 사용하는 설정 파일에 이들 빈에 대한 정의가 포함되어야 한다.

# @Controller를 위한 HandlerMapping과 HandlerAdapter
앞에서 얘기한 바와 같이, DispatcherServlet은 웹 브라우저의 요청을 처리할 핸들러 객체를 찾기 위해 HandlerMapping을 사용하고, 핸들러를 실행하기 위해 HandlerAdapter를 사용한다.

따라서, DispatcherServlet은 스프링 컨테이너에서 HandlerMapping과 HandlerAdapter 타입의 빈을 사용하므로, 핸들러에 각각의 알맞은 빈이 등록되어 있어야 한다.

근데 사실, 이 둘을 직접 등록해주지 않아도 된다. 설정 클래스에 ```@EnableWebMvc``` 애노테이션만 추가해주면 된다. 이 애노테이션은 매우 다양한 스프링 빈 설정을 추가해준다.

이 태그가 빈으로 추가해주는 클래스 중, @Controller 타입의 핸들러 객체를 처리하기 위한 다음의 두 클래스들도 포함되어 있다.
- org.springframework.web.servlet.mvc.method.annotation.**RequestMappingHandlerMapping**
- org.springframework.web.servlet.mvc.method.annotation.**RequestMappingHandlerAdapter**

### 1. RequestMappingHandlerMapping 애노테이션
@Controller 애노테이션이 적용된 객체의 요청 매핑 애노테이션(@GetMapping) 값을 이용해서 웹 브라우저의 요청을 처리할 컨트롤러 빈을 찾는다.

### 2. RequestMappingHandlerAdapter 애노테이션
컨트롤러의 메서드를 알맞게 실행하고 그 결과를 ModelAndView 객체로 변환해서 DispatcherServlet에 리턴한다.

다음의 Controller 예제 클래스를 보자.
```
@Controller
public class HelloController {

    @GetMapping("/hello")
    public String hello(Model model, @RequestParam(value="name", required = false) String name) {
        model.addAttribute("greeting", "안녕하세요, " + name);
        return "hello";
    }
}
```
RequestMappingHandlerAdapter 클래스는 "/hello" 요청 경로에 대해 hello() 메서드를 호출한다. 이때 Model 객체를 생성해서 첫 번째 파라미터로 전달하고, 이름이 "name"인 HTTP 요청 파라미터 값을 두 번째 파라미터로 전달한다.  

RequestMappingHandlerAdapter는 컨트롤러 메서드 결과 값이 String 타입이면 해당 값을 뷰 이름으로 갖는 ModelAndView 객체를 생성해서 Model 객체에 보관된 값과 함께 DispatcherServlet에 전달한다.

# WebMvcConfigurer 인터페이스와 설정
@EnableWebMvc 애노테이션을 사용하면
- @Controller 애노테이션을 붙인 컨트롤러를 위한 설정
- WebMvcConfigurer 타입의 빈을 이용해서 MVC 설정

을 생성해준다.

맨 위에서 썼던 설정 클래스 코드를 다시 보자.
```
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    
    @Override
    public void configureDefaultServletHandling
    		(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
    
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.jsp("/WEB-INF/view/", ".jsp");
    }
}
```

여기서 설정 클래스는 WebMvcConfigurer 인터페이스를 상속하고 있다. @Configuration 애노테이션을 붙인 클래스 역시 컨테이너에 빈으로 등록되므로 MvcConfig 클래스는 WebMvcConfigurer 타입의 빈이 된다.

@EnableWebMvc 애노테이션을 사용하면 WebMvcConfigurer 타입인 빈 객체의 메서드를 호출해서 MVC 설정을 추가한다. 따라서, WebMvcConfigurer 인터페이스를 구현한 설정 클래스는 configureViewResolver() 메서드를 재정의해서 알맞은 뷰 관련 설정을 추가해주면 된다.

# JSP를 위한 ViewResolver
컨트롤러 처리 결과를 JSP를 이용해서 생성하기 위해 다음 설정을 사용한다.

```
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    
    ...
    
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.jsp("/WEB-INF/view/", ".jsp");
    }
}
```
위 설정은 org.springframework.web.servlet.view.InternalResourceViewResolver 클래스를 이용해서 다음 설정과 같은 빈을 등록한다.
```
@Bean
public ViewResolver viewResolver() {
    InternalResourceViewResolver vr = new InternalResourceViewResolver();
    vr.setPrefix("/WEB-INF/view/");
    vr.setSuffix(".jsp");
    return vr;
```

컨트롤러의 실행 결과를 받은 DispatcherServlet은 ViewResolver에게 뷰 이름에 해당하는 View 객체를 요청한다.

그러면 InternalResourceViewResolver는 "prefix+뷰이름+suffix"에 해당하는 경로를 뷰 코드로 사용하는 InternalResourceView 타입의 View 객체를 리턴한다.

DispatcherServlet은 컨트롤러의 실행 결과를 HandlerAdapter를 통해 ModelAndView 타입으로 변환된 객체로 받는다고 했는데, 여기서 Model에 담긴 값은 View 객체에 Map 형식으로 전달된다. 그럼 View 객체는 전달받은 Map 객체에 담긴 값을 이용해서 알맞은 응답 결과를 출력한다.

Ex.
```
@Controller
public class HelloController {

    @GetMapping("/hello")
    public String hello(Model model, @RequestParam(value="name", required = false) String name) {
    	// request 속성에 저장
        model.addAttribute("greeting", "안녕하세요, " + name);
        return "hello";
    }
}
```

결과적으로 컨트롤러에서 지정한 Model 속성은 request 객체 속성으로 JSP에 전달된다. 따라서 다음과 같이 JSP는 모델에 지정한 속성 이름을 사용해서 값을 사용할 수 있다.
```
<%-- JSP 코드에서 모델의 속성 이름을 사용해서 값 접근 --%>
인사말: ${greeting}
```
# 디폴트 핸들러와 HandlerMapping의 우선순위
위에서 언급한 web.xml 코드의 다른 일부를 보면, DispatcherServlet에 대한 매핑 경로를 '/'로 준 부분이 있었다.
```
    <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
```
이 경우, ```.jsp```로 끝나는 요청을 제외한 모든 요청을 DispatcherServlet이 처리한다. 

그런데 @EnableWebMvc 애노테이션이 등록하는 HandlerMapping은 @Controller 애노테이션을 적용한 빈 객체가 처리할 수 있는 요청 경로로만 대응할 수 있다. (Ex. 등록된 컨트롤러는 하나고 그 컨트롤러가 @GetMapping("/hello") 설정을 사용한다면 /hello 경로만 처리 가능)

이 경우, 직접 다른 컨트롤러 객체를 구현하는 것보다는 **WebMvcConfigurer의 configureDefaultServletHandling() 메서드**를 사용하면 편리하다.

```
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    
    @Override
    public void configureDefaultServletHandling
    		(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
    ...
}
```
위 설정에서 해당 메서드는
- DefaultServletHttpRequestHandler
- SimpleUrlHandlerMapping

의 두 빈 객체를 추가한다.

> ### DefaultServletHttpRequestHandler
> 클라이언트의 모든 요청을 WAS가 제공하는 디폴트 서블릿에 전달한다.
> ### SimpleUrlHandlerMapping
> 모든 경로("/**")를 DefaultServletHttpRequestHandler를 이용해서 처리하도록 설정

 ### 💡 HandlerMapping 적용 우선순위
 1. @EnableWebMvc 애노테이션이 등록하는 RequestMappingHandlerMapping
 2. DefaultServletHandlerConfigurer.enable() 메서드가 등록하는 SimpleUrlHandlerMapping
 
따라서, 웹 브라우저의 요청이 들어오면 DispatcherServlet은 다음과 같이 요청을 처리한다.

1. RequestMappingHandlerMapping을 사용해서 요청을 처리할 핸들러 검색

	1.1 존재하면 해당 컨트롤러를 이용해서 요청 처리
2. 존재하지 않으면 SimpleUrlHandlerMapping을 사용해서 요청을 처리할 핸들러 검색

	2.1 DefaultServletHandlerConfigurer.enable() 메서드가 등록한 SimpleUrlHandlerMapping은 "/**" 경로, 즉 **모든 경로**에 대해 DefaultServletHttpRequestHandler를 리턴한다.
  
    2.2 DispatcherServlet은 DefaultServletHttpRequestHandler에 처리를 요청한다.
  
    2.3 DefaultServletHttpRequestHandler는 디폴트 서블릿에 처리를 위임한다.
