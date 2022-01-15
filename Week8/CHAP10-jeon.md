# CHAP10-스프링 프레임워크 동작 방식

# 스프링 MVC 핵심 구성 요소

컨트롤러 구성 요소를 개발자가 직접 구현해야 한다. 컨트롤러 구성 요소는 개발자가 직접 구현해야 하고 스프링 빈으로 등록해야 한다.

- `DispacherServlet` 은 모든 연결을 담당한다. 웹 브라우저로부터 요청이 들어오면 이는 그 요청을 처리하기 위한 컨트롤러 객체를 검색한다. 이 때 `DispatcherServlet` 은 직접 컨트롤러를 검색하지 않고 `HandlerMapping` 이라는 빈 객체에게 컨트롤러 검색을 요청한다.
- `HandlerMapping` 은 클라이언트의 요청 경로를 이용해서 이를 처리할 컨트롤러 빈 객체를 `DispacherServlet` 에 전달한다.  이는 @Controller 애노테이션을 이용해 구현한 컨트롤러뿐만 아니라 Controller 인터페이스를 구현한 컨트롤러, 그리고 특수 목적으로 사용되는 HttpRequestHandler 인터페이스를 구현한 클래스를 동일한 방식으로 실행할 수 있도록 만들어졌다. 그래서 @Controller, Controller 인터페이스, HttpRequestHandler 인터페이스를 동일한 방식으로 처리하기 위해 사용되는 것이 이 `HandlerMapping`  빈이다.
- `DispatcherServlet` 은 HandlerMapping 이 찾아준 컨트롤러 객체를 처리할 수 있는 `HandlerAdapter` 빈에게 요청 처리를 위임한다. `HandlerAdapter` 는 컨트롤러의 알맞은 메서드를 호출해 요청을 처리하고 그 결과를 `DispatcherServlet` 에 리턴한다. 이때 `HAndlerAdapter` 는 컨트롤러의 처리 결과를 `ModelAndView` 라는 객체로 변환해서 `DispatcherServlet` 에 리턴한다.
- 컨트롤러의 요청 처리 결과를 `ModelAndView` 로 받으면 `DispatcherServlet` 은 결과를 보여줄 뷰를 찾기 위해 `ViewResolver` 빈 객체를 사용한다. `ModelAndView` 는 컨트롤러가 리턴한 뷰 이름을 담고 있는데 `ViewResolver` 는 이 뷰 이름에 해당하는 View 객체를 찾거나 생성해서 리턴한다.
- `DispatcherServlet` 은 `ViewResolver` 가 리턴한 View 객체에게 응답 결과 생성을 요청한다. 이로써 모든 과정이 끝난다.

## 컨트롤러와 핸들러

컨트롤러는 클라이언트의 요청을 실제로 처리하고, DispatcherServlet 은 클라이언트의 요청을 전달받는 창구 역할을 한다. 이는 요청을 처리할 컨트롤러를 찾기 위해  HandlerMapping 을 사용한다. 이는 @Controller 애노테이션을 붙인 클래스 뿐만 아니라  HttpRequestHandler나 자신이 직접 만든 클래스를 이용해서 클라이언트의 요청을 처리할 수도 있게 한다. 

 따라서, 특정 요청 경로를 처리해주는 핸들러를 찾아주는 객체를 HandlerMapping 이라고 한다.

 DispatcherServlet 은 핸들러 객체의 실제 타입에 상관없이 실행 결과를 ModelAndView 타입으로만 받을 수 있다. 그런데 핸들러의 실제 구현 타입에 따라 이를 리턴하는 객체도 있고 아닌 객체도 있다. 그래서 핸들러의 처리 결과를 ModelAndView로 변환시켜줘야 하며 이를 HandlerAdapter 가 수행한다.

핸들러 객체의 실제 타입마다 그에 알맞은 HandlerMapping과 HandlerAdapter가 존재해서, 사용할 핸들러에 따라 둘을 스프링 빈으로 등록해야 한다. 이는 추후에 다룬다..

# DispatcherServlet 과 스프링 컨테이너

DispatcherServlet 은 전달받은 설정 파일을 이용해 스프링 컨테이너를 생성한다. HandlerMapping, HandlerAdapter, ViewResolver, 컨트롤러 등의 빈을 컨테이너에서 구한다. 그래서 DispatcherServlet 이 사용하는 설정 파일에 이들 빈에 대한 정의가 포함되어야 한다.

# @Controller 를 위한 HandlerMapping과 HandlerAdapter

@Controller 적용 객체는 DispatcherServlet 입장에서 보면 한 종류의 핸들러 객체이다. 이 객체에 알맞은 HandlerMapping 이나 HandlerAdapter 클래스를 빈으로 등록하려면, @EnableWebMvc 애노테이션을 추가하면 된다. 

- o.s.w.servlet.mvc.methed.annotation.RequestMappingHandlerMapping
- o.s.w.servlet.mvc.methed.annotation.RequestMappingHandlerAdapter

클래스가 포함되어 있다. 전자는 @GetMapping 값을 이용해서 웹 브라우저의 요청을 처리할 컨트롤러 빈을 찾고, 후자는 컨트롤러의 메서드를 알맞게 실행하고 그 결과를 ModelAndview 객체로 변환해 DispatcherServlet 에 리턴한다.

# WebMvcConfigurer 인터페이스와 설정

@EnableWebMvc 애노테이션을 사용하면 @Controller 애노테이션을 붙인 컨트롤러를 위한 설정을 생성한다. 또한 @EnableWebMvc 애너테이션을 사용하면 WebMvcConfigurer 타입의 빈을 이용해 MVC 설정을 추가로 생성한다.

# 디폴트 핸들러와 HandlerMapping 의 우선순위

매핑 경로가 `/` 인 경우 .jsp 로 끝나는 요청을 제외한 모든 요청을 DispatcherServlet 이 처리한다. 그런데 @EnableWebMvc 애노테이션이 등록하는 HandlerMapping 은 @Controller 애노테이션을 적용한 빈 객체가 처리할 수 있는 요청 경로만 대응할 수 있다. 그래서 `/index.html` 등의  요청을 처리할 수 있는 컨트롤러 객체를 찾지 못해 DispatcherServlet 이 404 응답을 전송할 수 있다. 이를 위해서는 WebMvcConfigurer 의 `configureDefaultServletHandling()` 메서드를 사용하는 것이 편리하다.  이 메서드는 다음의 두 빈 객체를 추가한다.

- DefaultServletHttpRequestHandler
- SimpleUrlHandlerMapping

전자는 클라이언트의 모든 요청을 WAS가 제공하는 디폴트 서블릿에 전달한다. 

@EnableWebMvc 애노테이션이 등록하는 RequestMappingHandlerMapping 의 적용 우선순위가 DefaultServletHandlerConfigurer#enable() 메서드가 등록하는 SimplaUrlHandlerMapping 의 우선순위보다 높다. 때문에 웹 브라우저의 요청이 들어오면 DispatcherServlet 은 다음과 같은 방식으로 요청을 처리한다.

1. RequestMappingHandlerMapping 을 사용해 요청을 처리할 핸들러를 검색한다.
    1. 존재하면 해당 컨트롤러를 이용해 요청을 처리한다.
2. 존재하지 않으면 SimpleUrlHandlerMapping 을 사용해 요청을 처리할 핸들러를 검색한다.
    1. DefaultServletHandlerConfigurer#enable() 메서드가 등록한  SimpleUrlHandlerMapping 은 `/**` 경로에 대해 DefaultServletHttpRequestHandler 를 리턴한다.
    2. DispatcherServlet은 DefaultServletHttpRequestHandler 에 처리를 요청한다.
    3. DefaultServletHttpRequestHandler 는 디폴트 서블릿에 처리를 위임한다.
    
    그래서 `/index.html`  경로로 요청이 들어오면 1번 과정에서 해당하는 컨트롤러를 찾지 못하므로 2번 과정을 통해 디폴트 서블릿이 이 요청을 처리하게 된다.