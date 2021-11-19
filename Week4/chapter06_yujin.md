# 컨테이너 초기화와 종료
스프링 컨테이너는 초기화와 종료라는 라이프사이클을 갖는다. 
다음 코드와 함께 보자.
```
// 1. 컨테이너 초기화
AnnotationConfigApplicationContext ctx = 
	new AnnotationConfigApplicationContext(AppContext.class);
    
// 2. 컨테이너에서 빈 객체를 구해서 사용
Greeter g = ctx.getBean("greeter", Greeter.class);
String msg = g.greet("스프링");
System.out.println(msg);

// 3. 컨테이너 종료
ctx.close();
```
위 코드를 보면, 컨텍스트 객체를 생성하는 시점에 스프링 컨테이너를 초기화하는 것을 알 수 있다. 스프링 컨테이너는 설정 클래스에서 정보를 읽어와 알맞은 빈 객체를 생성하고 각 빈을 연결(의존 주입)하는 작업을 수행한다.

> 컨테이너를 사용한다 = 컨테이너에 보관된 빈 객체를 구한다

컨테이너 사용이 끝나면 컨테이너를 종료한다. 이 때 사용하는 메서드가 ```close()```이다. 
> 💡 close() 메서드는 ```AbstractApplicationContext``` 클래스에 정의되어 있다. 자바 설정을 사용하는 ```AnnotationConfigApplicationContext``` 클래스나 XML 설정을 사용하는 ```GenericXmlApplicationContext``` 클래스 모두 ```AbstractApplicationContext``` 클래스를 상속받고 있기 때문에, close() 메서드로 컨테이너를 종료할 수 있다.

컨테이너를 초기화하고 종료할 때에는 다음의 작업도 같이 수행한다.
- 컨테이너 초기화
	- 빈 객체의 생성
 	- 의존 주입
 	- 초기화
- 컨테이너 종료
	- 빈 객체의 소멸

# 스프링 빈 객체의 라이프사이클
스프링 컨테이너는 빈 객체의 라이프사이클을 관리한다.
![](https://images.velog.io/images/nanaeu/post/c2cedb20-b205-456b-82e2-064fc2058efe/image.png)
- 초기화
	- 스프링 컨테이너는 가장 먼저 빈 객체 생성하고 의존 주입
	- 의존 자동 주입을 통한 의존 설정이 이 시점에 수행됨
	- 모든 의존 설정이 완료되면 빈 객체의 초기화 수행
	- 빈 객체를 초기화하기 위해 스프링은 빈 객체의 지정된 메서드 호출
- 소멸
	- 스프링 컨테이너는 지정한 메서드를 호출하며 빈 객체의 소멸을 처리
## 🎈 빈 객체의 초기화와 소멸: 스프링 인터페이스
스프링 컨테이너는 빈 객체를 초기화하고 소멸하기 위해 빈 객체의 지정한 메서드를 호출하는데, 이 메서드는 다음의 두 인터페이스에 정의되어있다.
- org.springframework.beans.factory.InitializingBean
- org.springframework.beans.factory.DisposableBean

두 인터페이스는 다음과 같다.
```
public interface InitializingBean {
	void afterPropertiesSet() throws Exception;
}

public interface DisposableBean {
	void destroy() throws Exception;
}
```
### 1. InitializingBean
빈 객체가 해당 인터페이스를 구현하면 스프링 컨테이너는 초기화 과정에서 빈 객체의 ```afterPropertiesSet()``` 메서드를 실행한다. 

빈 객체를 생성한 뒤 초기화 과정이 필요하면 해당 인터페이스를 상속하고 ```afterPropertiesSet()``` 메서드를 알맞게 구현하면 된다.
### 2. DisposableBean
빈 객체가 해당 인터페이스를 구현한 경우 소멸 과정에서 빈 객체의 ```destroy()``` 메서드를 실행한다.

마찬가지로 소멸 과정이 필요한 경우 해당 인터페이스를 상속하고 ```destroy()``` 메서드를 알맞게 구현하면 된다.

## 🎈 빈 객체의 초기화와 소멸: 커스텀 메서드
모든 클래스가 InitializingBean 인터페이스와 DisposableBean 인터페이스를 상속받아 구현할 수 있는 건 아니다. 또한 이 두 인터페이스를 사용하지 싶지 않을 때도 있다.

이런 경우, 스프링 설정에서 직접 메서드를 지정할 수 있다.

**@Bean 태그에서 initMethod 속성과 destroyMethod 속성**을 사용하여 초기화 메서드와 소멸 메서드의 이름을 지정하면 된다.

예제와 함께 보자.

```
public class Client {
    private String host;
    
    public void setHost(String host) {
    	this.host = host;
    }
    
    public void connect() {
    	System.out.println("Client.connect() 실행");
    }
    
    public void send() {
    	System.out.println("Client.send() to " + host);
    }
    
    public void close() {
    	System.out.println("Client.close() 실행");
    }
}
```
Client 클래스를 빈으로 사용하려면 초기화 과정에서 connect() 메서드를 실행하고 소멸 과정에서 close() 메서드를 실행해야 한다고 하자. 그럼 다음과 같이 @Bean 애노테이션의 initMethod 속성과 destroyMethod 속성에 메서드 이름을 지정해주면 된다.
```
@Bean(initMethod = "connect", destroyMethod = "close")
public Client client() {
    Client client = new Client();
    client.setHost("host");
    return client;
}
```
> 💡 initMethod 속성과 destroyMethod 속성에 지정한 메서드는 **파라미터가 없어야 한다.** 파라미터가 존재할 경우 익셉션이 발생한다.

# 빈 객체의 생성과 관리 범위
이전의 글에서 스프링 컨테이너는 빈 객체를 하나만 생성한다고 했었다. 이렇게 한 식별자에 대해 한 개의 객체만 존재하는 빈은 **싱글톤(singleton)** 범위(scope)를 갖는다. 별도 설정을 하지 않으면 빈은 싱글톤 범위를 갖는다. 
```
Client cl01 = ctx.getBean("client", Client.class);
Client cl02 = ctx.getBean("client", Client.class);
// cl01 == cl02
```

여기서, ~~사용 빈도가 낮긴 하지만~~ **프로토타입** 범위의 빈을 설정할 수도 있다. 즉, 빈의 범위를 프로토타입으로 지정하면 빈 객체를 구할 때마다 매번 새로운 객체를 생성한다는 말이다. 

```
// client 빈의 범위가 프로토타입일 경우, 매번 새로운 객체 생성
Client cl01 = ctx.getBean("client", Client.class);
Client cl02 = ctx.getBean("client", Client.class);
// cl01 != cl02
```

이렇게 특정 빈을 프로토타입으로 지정하려면, 다음과 같이 @Scope 애노테이션에 "prototype"을 값으로 주어 @Bean 애노테이션과 함께 사용하면 된다.

```
@Configuration
public class AppCtxWithPrototype {
    
    @Bean
    @Scope("prototype")
    public Client client() {
   		Client client = new Client();
        client.setHost("host");
        return client;
    }
    ...
}
```
별도로 설정해주지 않는 한 기본적으로 싱글톤 범위를 갖지만, 
싱글톤 범위를 **명시적으로 지정**하고 싶다면 "prototype" 대신에 "singleton"을 주면 된다.

> ### 💡 주의
> 프로토타입 범위를 갖는 빈은 **완전한 라이프사이클을 따르지 않는다.**
> 초기화 작업까지는 수행하지만, 컨테이너를 종료한다고 해서 생성한 프로토타입 빈 객체의 소멸 메서드는 실행하지 않는다. 따라서 소멸처리는 코드에서 직접 해주어야 한다.
