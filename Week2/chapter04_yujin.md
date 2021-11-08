# 자동 주입이란?
앞서 설명했던 스프링 DI의 설정 클래스에서는 의존 대상을 생성자나 메서드를 이용하여 주입했다.


```
@Configuration
public class AppCtx {

    @Bean
    public MemberDao memberDao() {
    	return new MemberDao();
    }
    
    @Bean
    public ChangePasswordService changePwdSvc() {
    	ChangePasswordService pwdSvc = new ChangePasswordService();
        pwdSvc.setMemberDao(memberDao());
        return pwdSvc;
    }
    
    ...
}
```
이렇게 의존 대상을 설정 코드에서 직접 주입하지 않고,

#### 스프링이 자동으로 의존하는 빈 객체를 주입해주는 기능도 있다!
### 이를 자동 주입이라고 한다

스프링 3이나 4 버전 초기에는 의존 자동 주입에 호불호가 있었으나, 스프링 부트가 나오면서 의존 자동 주입을 사용하는 추세로 바뀌었다고 한다.

스프링에서 의존 자동 주입을 설정하려면 @Autowired 애노테이션이나 @Resource 애노테이션을 사용하면 되는데, 

이 글에서는 **@Autowired** 애노테이션의 사용 방법을 살펴보자.

# @Autowired 애노테이션을 이용한 의존 자동 주입
_자동 주입 기능을 사용하면 의존 객체를 명시하지 않아도 스프링이 알아서 필요한 의존 빈 객체를 찾아 주입한다._

자동 주입 기능을 사용하는 것은 매우 간단하다. 

**의존을 주입할 대상에 @Autowired 애노테이션을 붙이기만 하면 된다.**

@Autowired 애노테이션을 붙이면 설정 클래스에서 의존을 따로 주입하지 않아도 된다.

### @Autowired 애노테이션은 메서드에도 붙일 수 있다.

빈 객체의 메서드에 @Autowired 애노테이션을 붙이면 
스프링은 **해당 메서드를 호출**하고, **메서드 파라미터 타입에 해당하는 빈 객체를 찾아 인자로 주입**한다.

> 근데, **@Autowired 애노테이션을 적용한 대상의 개수가 하나가 아니라면** 어떻게 될까?🤔

## 1. 일치하는 빈이 없는 경우
```NoSuchBeanDefinitionException: No qualifyinh bean of type ... available: expected at least 1 bean```과 같은 익셉션이 발생한다.
## 2. 일치하는 빈이 두 개 이상인 경우
```NoUniqueBeanDefinitionException: No qualifyinh bean of type ... available: expected single matching bean but found 2```와 같은 익셉션이 발생한다.

> 📌 자동 주입을 하려면 해당 타입을 가진 빈이 어떤 빈인지 **정확하게 한정**할 수 있어야 한다.

### 💡 자동 주입할 빈을 지정하는 방법, @Qualifier

일치하는 빈이 두 개 이상일 경우, 자동 주입할 빈을 지정할 수 있는 방법으로, **@Qualifier 애노테이션**이 있다. 

이 애노테이션은 **자동 주입 대상 빈을 한정**할 수 있다.

@Qualifier 애노테이션은 두 위치에서 사용가능하다.

#### 1. @Bean 애노테이션을 붙인 빈 설정 메서드
다음의 설정 예시를 보며 이해해보자.
```
import ...


@Configuration
public class AppCtx {
	
    ...
    
    @Bean
    @Qualifier('printer')
    public MemberPrinter memberPrinter1() {
    	return new MemberPrinter();
    }
    
    @Bean
    public MemberPrinter memberPrinter2() {
    	return new MemberPrinter();
    }
    
    ...
    
}
```
위 코드에서 ```memberPrinter1()``` 메서드에 "printer" 값을 갖는 @Qualifier 애노테이션을 붙였다. 이 설정은 해당 빈의 한정 값으로 "printer"를 지정한다.

이렇게 지정한 한정 값은 다음 위치에서 사용한다. 
#### 2. @Autowired 애노테이션에서 자동 주입할 빈을 한정할 곳

```
public class MemberListPrinter {
	
    private MemberDao memberDao;
    private MemberPrinter printer;
    
    ...
    
    @Autowired
    @Qualifier("printer")
    public void setMemberPrinter(MemberPrinter printer) {
    	this.printer = printer;
    }
}
```
```setMemberPrinter()``` 메서드에 @Autowired 애노테이션을 붙였으므로 MemberPrinter 타입의 빈을 자동 주입한다. 

이때, @Qualifier 애노테이션 값이 "printer"이므로,
**한정 값이 "printer"인 빈을 의존 주입 후보로 사용**한다.

> ### 💡 빈 이름과 기본 한정자
> 빈 설정에 @Qualifier 애노테이션이 없으면, **빈의 이름을 한정자로 지정**한다.

# 상위/하위 타입 관계와 자동 주입

먼저 예시를 살펴보자. 아래 클래스는 Car 클래스를 상속한 Mclaren 클래스이다.

```
public class Mclaren extends Car {

    @Override
    public void print(Car car) {
    	System.out.printf("차 정보: 이름=%s, 출시년도=%s\n", 
        		car.getName(), car.getRelease());
    }
}
```

아래의 설정 클래스에서 carPrinter1() 메서드가 Car 타입의 빈 객체를, carPrinter2() 메서드가 Mclaren 타입의 빈 객체를 설정하도록 하자. 별도의 @Qualifier 애노테이션은 붙이지 않는다.

```
@Configuration
public class AppCtx {

	...
    
    @Bean
    public Car carPrinter1() {
    	return new Car();
    }
    
    @Bean
    public Mclaren carPrinter2() {
    	return new Mclaren();
    }
    
    ...
    
}
```
두 클래스 CarListPrinter, CarInfoPrinter가 Car 타입의 빈 객체를 자동 주입할 경우, Main에서 두 클래스를 사용해 차에 관련된 정보를 출력하려고 할 때 ```NoUniqueDefinitionException```이 발생할 것이다.

## 🤔 왜?
### Mclaren 클래스가 Car 클래스를 상속했기 때문에, Mclaren 클래스는 Car 클래스에도 할당할 수 있다!

스프링 컨테이너는 Car 타입의 빈을 자동 주입해야 하는 @Autowired 애노테이션을 만나면, carPrinter1(```Car``` 타입) 빈과 carPrinter2(```Mclaren```타입) 빈 중 어떤 빈을 주입해야 하는지 알 수 없기 때문에 익셉션이 발생한다.

따라서, CarListPrinter 클래스와 CarInfoPrinter 클래스에서 어떤 빈을 주입해야 할지 **@Qualifier 애노테이션**을 통해 한정해야 한다.

먼저, CarListPrinter 클래스에서 @Qualifier 애노테이션을 통해 주입할 빈을 한정하자.
```
@Configuration
public class AppCtx {

	...
    
    @Bean
    @Qualifier("printer")
    public Car carPrinter1() {
    	return new Car();
    }
    
    ...
    
}

public class CarListPrinter {

	...
    
    @Autowired
    @Qualifier("printer")
    public void setCar(Car car) {
    	this.car = car;
    }
}
```
그 다음, CarInfoPrinter 클래스에 자동 주입할 Car 타입 빈은 두 가지 방법으로 처리할 수 있다. 첫 번째는 위의 CarListPrinter와 같이 **@Qualifier 애노테이션을 사용**하는 방법이다.
```
@Configuration
public class AppCtx {

	...
    
    @Bean
    @Qualifier("mcprinter")
    public Car carPrinter2() {
    	return new Car();
    }
    
    ...
    
}

public class CarInfoPrinter {

	...
    
    @Autowired
    @Qualifier("mcprinter")
    public void setCar(Car car) {
    	this.car = car;
    }
}
```
두 번째 방법은 **CarInfoPrinter가 Mclaren을 사용하도록 수정**하는 것이다. Mclaren 타입 빈은 한 개만 존재하므로 익셉션이 발생하지 않는다.
```
public class CarInfoPrinter {
	...
    
    @Autowired
    public void setCar(Mclaren mcCar) {
    	this.car = car;
    }
}
```


# @Autowired 애노테이션의 필수 여부 설정하는 3가지 방법

@Autowired 애노테이션은 기본적으로 @Autowired 애노테이션을 붙인 타입에 해당하는 빈이 존재하지 않으면 익셉션을 발생시킨다. 그런데 상황에 따라 익셉션까지는 필요없고, 그냥 해당 필드를 null로 받아도 되는 등 자동 주입할 대상이 필수가 아닌 경우가 있다. 

이때, @Autowired 애노테이션의 필수 여부를 조정할 수 있는 3가지 방법이 있다.
## 1. @Autowired(required = false)
@Autowired 애노테이션의 required 속성을 false로 지정하면 매칭되는 빈이 없어도 익셉션이 발생하지 않으며 자동 주입을 수행하지 않는다. 

## 2. 자바 8의 Optional

_스프링 5 버전부터는 @Autowired 애노테이션의 required 속성을 false로 하는 대신에 다음과 같이 의존 주입 대상에 자바 8의 Optional을 사용해도 된다._

```
public class MemberPrinter {
	
    private DateTimeFormatter dateTimeFormatter;
    
    public void print(Member member) {
    	...
    }
    
    @Autowired
    public void setDateFormatter(Optional<DateTimeFormatter> formatterOpt) {
    	if (formatterOpt.isPresent()) {
        	this.dateTimeFormatter = formatterOpt.get();
        } else {
        	this.dateTimeFormatter = null;
        }
    }
    
}
```

자동 주입 대상 타입이 Optional인 경우, 다음과 같이 동작한다.

1. 일치하는 빈이 존재하지 않으면, 값이 없는 Optional을 인자로 전달
2. 일치하는 빈이 존재하면 해당 빈을 값으로 갖는 Optional을 인자로 전달
## 3. @Nullable 애노테이션

해당 애노테이션을 붙이면, 다음과 같이 동작한다.

1. 자동 주입할 빈이 존재하면 해당 빈을 인자로 전달
2. 자동 주입할 빈이 존재하지 않으면 null을 인자로 전달

> ### 💡 @Autowired(require=false) vs. @Nullable
> @Nullable 애노테이션을 사용하면 자동 주입할 빈이 존재하지 않아도 메서드가 호출된다. 반면 @Autowired 애노테이션의 경우 require의 속성이 false인데 대상 빈이 존재하지 않으면 세터 메서드를 호출하지 않는다.

앞서 설명한 세 가지 방식은 필드에서도 그대로 적용된다.

> 📌 설정 클래스에서 세터 메서드를 통해 의존을 주입해도, 해당 세터 메서드에 @Autowired 애노테이션이 붙어 있으면 **자동 주입을 통해 일치하는 빈을 주입**한다. 
따라서, **@Autowired 애노테이션을 사용했다면** 설정 클래스에서 객체를 주입하기보다는 **스프링이 제공하는 자동 주입 기능을 사용하는 편이 낫다.**



