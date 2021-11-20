# 빈 라이프사이클과 범위

## 1.컨테이너 초기화와 종료

>스프링 컨테이너는 `초기화`와 `종료`라는 라이프사이클을 갖는다. 

* AnnotationConfigApplicationContext 의 생성자를 이용해서 컨텍스트 객체를 생성하고 이 시점에 스프링 컨테이너를 초기화한다. 스프링 컨테이너는 설정 클래스에서 정보를 읽어와 알맞은 *빈 객체를 생성*하고 각 *빈을 연결(의존 주입)* 하는 작업을 수행한다.
* 컨테이너를 사용한다. 컨테이너를 사용한다는 것은 getBean() 과  같은 메서드를 이용해 컨테이너에 보관된 빈 객체를 구한다.
* 컨테이너를 종료한다. 이 때 close() 메서드를 사용한다. *빈 객체를 소멸시킨다.*

## 2. 스프링 빈 객체의 라이프사이클

* 객체 생성
* 의존 설정
    * 의존 자동 주입을 통한 설정 수행
* 초기화
    * 빈 객체 초기화. 빈 객체의 지정된 메서드를 호출
* 소멸
    * 지정된 메서드 호출

의 라이프사이클을 갖는다. 

### 빈 객체의 초기화와 소멸: 스프링 인터페이스

스프링 컨테이너는 빈 객체를 초기화 하고 소멸하기 위해 빈 객체의 지정한 메서드를 호출한다. 스프링은 다음의 두 인터페이스에 이 메서드를 정의하고 있다.

* org.springframework.beans.factory.initializingBean
* org.springframework.beans.factory.DisposableBean

빈 객체가 InitializingBean 인터페이스를 구현하면 스프링 컨테이너는 초기화 과정에서 빈 객체의 afterPropertiesSet() 메서드를 실행한다. 빈 객체를 생성한 뒤 초기화 과정이 필요하면 InitializingBean 인터페이스를 상속하고 afterPropertiesSet() 메서드를 알맞게 구현한다.<br>

스프링 컨테이너는 빈 객체가 DisposableBean  인터페이스를 구현한 경우 소멸 과정에서 빈 객체의 destroy() 메서드를 실행한다.<br>

이는 데이터베이스 커넥션이나, 채팅 클라이언트에서 사용된다.

### 빈 객체의 초기화와 소멸: 커스텀 메서드

모든 클래스가 Initializing 이나 DisposableBean 인터페이스를 상속받아 구현할 수 있는 것은 아니다. 외부 클래스를 스프링 빈 객체로 설정하고 싶을 때도 있다. 이 경우 스프링 설정에서 직접 메서드를 지정할 수 있다. 방법은 간단하다. @Bean 태그에서 initMethod 속성과 destroyMethod 속성을 사용해 초기화 메서드와 소멸 메서드 이름을 지정하면 된다.

```java
@Bean(initMethod = "connect", destroyMethod = "close")
	public Client2 client2() {
		Client2 client = new Client2();
		client.setHost("host");
		return client;
	}
```

초기화 메서드가 두 번 실행되지 않게 하도록 주의해야 한다. 빈 설정 메서드에서 afterPropertiesSet() 을 호출하고, InitializingBean 인터페이스를 구현한 클래스를 생성하면 afterPropertiesSet() 이 두 번 호출된다. 

## 3. 빈 객체의 생성과 관리 범위

스프링 컨테이너는 빈 객체를 한 개만 생성한다. 동일한 이름을 갖는 빈 객체를 구하면 객체는 동일한 빈 객체를 참조한다.

* 한 식별자에 대해 한 개의 객체만 존재하는 빈을 `싱글톤` 범위를 갖는다고 한다. 별도 설정을 하지 않으면 빈은 싱글톤 범위를 갖는다. 
* 반면 `프로토타입` 범위의 빈은 , 빈 객체를 구할 때마다 매번 새로운 객체를 생성한다. 특정 빈을 프로토타입 범위로 지정하려면 `@Scope("prototype")` 애노테이션을 @Bean 애노테이션과 함께 사용해야 한다.

``` java
@Bean
@Scope("prototype")
public Client client(){
    Client client = new Client();
    return client;
}

```

> ❗ 프로토타입 범위를 갖는 빈은 완전한 라이프사이클을 따르지 않는다는 점에 주의해야 한다.

스프링 컨테이너는 프로토타입의 빈 객체를 생성하고 프로퍼티 설정하고 초기화 작업까지는 수행하지만, 컨테이너를 종료한다고 생성된 **프로토타입 빈 객체의 소멸 메서드를 실행하지는 않는다**. 따라서 빈 객체의 소멸 처리를 코드에서 직접 해야 한다.