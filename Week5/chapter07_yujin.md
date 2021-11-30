# 프록시와 AOP
## 🤔 프록시란?
> 핵심 기능의 실행은 다른 객체에 위임하고 부가적인 기능을 제공하는 객체
> * **대상 객체**: 실제 핵심 기능을 실행하는 객체

예제와 함께 보자. 먼저 다음과 같이 Calculator 인터페이스가 있다.
```
package chap07;

public interface Calculator {
	public long factorial(long num) {}
}
```
다음과 같이 Calculator 인터페이스를 구현하여 for문을 통해 팩토리얼 값을 구하는 클래스가 있다.
```
package chap07;

public class ImpeCalculator implements Calculator {
    @Override
    public long factorial(long num) {
        long result = 1;
        for (long i = 1 ; i <= num ; i++) {
            result *= i;
        }
        return result;
    }
}
```
그리고 Calculator 인터페이스를 구현하여 재귀호출을 이용해 팩토리얼 값을 구하는 클래스도 있다.
```
package chap07;

public class RecCalculator implements Calculator {
    @Override
    public long factorial(long num) {
        if (num == 0) 
            return 1;
        else
            return num * factorial(num - 1);
    }
}
```

앞에서 구현한 팩토리얼 구현 클래스의 실행 시간을 출력하려면 어떻게 해야할까?

가장 먼저 생각나는 직관적인 방법은 **클래스 내 메서드의 시작과 끝에서 시간을 구하고 이 두 시간의 차이를 출력하는 것**이다.

이 방법으로 ImpeCalculator 클래스의 몇줄만 수정하여 실행시간을 출력할 수 있겠지만, **재귀호출**을 사용하는 RecCalculator 클래스에서 수정을 할 경우 실행시간을 출력하는 메시지가 여러 번 출력될 수 있는 문제가 있다. 

이 경우, 기존 클래스들의 코드를 수정하기보다는 메서드를 호출하는 곳 전후에 시작시간과 끝시간을 구해 그 차이를 구하여 실행시간을 출력하는 것이 나을지도 모른다.

```
ImpeCalculator impeCal = new ImpeCalculator();
long start1 = System.currentTimeMills();
long fourFactorial1 = impeCal.factorial(4);
long end1 = System.currentTimeMills();

System.out.printf("ImpeCalculator.factorial(4) 실행시간 = %d\n", 
							(end1 - start1));

RecCalculator recCal = new RecCalculator();
long start2 = System.currentTimeMills();
long fourFactorial2 = recCal.factorial(4);
long end2 = System.currentTimeMills();

System.out.printf("RecCalculator.factorial(4) 실행시간 = %d\n", 
							(end2 - start2));
```

하지만 위 경우에서도 문제가 있다. 실행시간을 밀리초 단위가 아닌 나노초 단위로 구해야한다면, 두 클래스마다 실행시간을 구하고 출력하는 코드가 **중복**되어 있어 두 곳을 모두 변경해야 한다.

### 기존 코드를 변경하지 않고, 코드 중복도 피할 수 있는 방법은 없을까?

이럴 때, 가장 좋은 방법이 **프록시 객체**이다. 핵심 기능의 구현은 다른 객체에게 위임하고, 공통 기능을 제공하는 코드를 따로 작성해보자.

```
package chap07;

public class ExeTimeCalculator implements Calculator {
    private Calculator delegate;
    
    public ExeTimeCalculator(Calculator delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public long factorial(long num) {
        long start = System.nanoTime();
        long result = delegate.factorial(num); // 핵심 기능
        long end = System.nanoTime();
        
        System.out.printf("%s.factorial(%d) 실행 시간 = %d\n", 
        	delegate.getClass().getSimpleName(), num,
                (end  - start));
        return result;
    }
}
```

위와 같은 ExeTimeCalculator 클래스를 사용하면 다음과 같은 방법으로 ImpeCalculator의 실행시간을 측정할 수 있다.
```
ImpeCalculator impeCal = new ImpeCalculator();
ExeTimeCalculator exeCal = new ExeTimeCalculator(impeCal);
long result = exeCal.factorial(4);
```
실행흐름은 다음과 같다.

![](https://images.velog.io/images/nanaeu/post/07b90b6c-5b91-4e7f-b709-4a3eb2c177e2/image.png)

위와 같이 프록시 객체를 이용해 기존 코드를 변경하지 않고 코드의 중복도 제거하며 실행시간을 출력해보았다.

이렇게 **공통 기능 구현과 핵심 기능 구현을 분리**하는 것이 AOP의 핵심이다.

## 🤔 AOP란?
AOP는 Aspect Oriented Programming의 약자로, **여러 객체에 공통으로 적용할 수 있는 기능을 분리**해서 핵심 기능을 구현한 코드의 수정 없이 공통 기능을 적용할 수 있도록 하여 **재사용성**을 높여주는 프로그래밍 기법이다. 

> Aspect Oriented Programming을 우리말로 '관점 지향 프로그래밍'이라 주로 번역하는데, 여기서 Aspect는 구분되는 기능이나 요소를 의미하기 때문에 '관점' 보다는 '**기능**' 또는 '**관심**'이라는 표현이 더 맞다.

AOP의 기본 개념은 **핵심 기능에 공통 기능을 삽입**하는 것이다. 
즉, **핵심 기능의 코드를 수정하지 않으면서 공통 기능의 구현을 추가**하는 것이라 할 수 있겠다.

핵심 기능에 공통 기능을 삽입하는 방법은 다음 세 가지가 있다
- 컴파일 시점에 코드에 공통 기능 삽입
- 클래스 로딩 시점에 바이트 코드에 공통 기능 삽입
- 런타임에 프록시 객체를 생성해서 공통 기능 삽입

첫 번째와 두 번째 방법은 스프링 AOP에서는 지원하지 않고, AOP 전용 도구를 사용해서 적용할 수 있다.

스프링이 제공하는 AOP 방식은 **프록시를 이용한 세 번째 방식**이다.
프록시 방식은 중간에 프록시 객체를 생성한 뒤 실제 객체의 기능을 실행하기 전&후에 공통 기능을 호출한다. 

이때 _프록시 객체는 스프링 AOP가 자동으로 만들어준다._ 
따라서 상위 타입의 인터페이스를 상속받은 프록시 클래스를 직접 구현할 필요가 없다. 우리는 **공통 기능을 구현한 클래스만** 알맞게 구현하면 된다.

### AOP 주요 용어
- **Advice**: 언제 공통 관심 기능을 핵심 로직에 적용할 지를 정의한다. 
- **Joinpoint**: Advice를 적용 가능한 지점을 의미한다. 메서드 호출, 필드 값 변경 등이 Joinpoint에 해당된다. 스프링은 프록시를 이용해서 AOP를 구현하기 때문에 **메서드 호출에 대한 Joinpoint만 지원한다.**
- **Pointcut**: Joinpoint의 부분 집합으로서 실제 Advice가 적용되는 Joinpoint를 의미한다. 스프링에서는 정규 표현식이나 AspectJ의 문법을 이용하여 Pointcut을 정의할 수 있다.
- **Weaving**: Advice를 핵심 로직 코드에 적용하는 것을 의미한다.
- **Aspect**: 여러 객체에 공통으로 적용되는 기능을 의미한다. 트랜잭션이나 보안 등이 Aspect의 좋은 예이다.

### Advice의 종류
- **Before Advice**: 대상 객체의 메서드 호출 전에 공통 기능 실행
- **After Returning Advice**: 대상 객체의 메서드가 익셉션 없이 실행된 이후에 공통 기능 실행
- **After Throwing Advice**: 대상 객체의 메서드를 실행하는 도중 익셉션이 발생한 경우에 공통 기능 실행
- **After Advice**: 익셉션 발생 여부에 상관없이 대상 객체의 메서드 실행 후 공통 기능 실행 (try-catch-finally의 finally 블록과 유사)
- **Around Advice**: 대상 객체의 메서드 실행 전, 후 또는 익셉션 발생 시점에 공통 기능 실행

이 중에서 주로 사용되는 것은 Around Advice이다. 대상 객체의 메서드를 실행하기 전/후, 익셉션 발생 시점 등 다양한 시점에 원하는 기능을 삽입할 수 있기 때문이다. 이 글에서도 Around Advice의 구현 방법에 대해서만 다룰 예정이다.

# 스프링 AOP 구현
스프링 AOP를 이용해서 공통 기능을 구현하고 적용하는 방법은 단순하다.

1. Aspect로 사용할 클래스에 @Aspect 애노테이션을 붙인다.
2. @Pointcut 애노테이션으로 공통 기능을 적용할 Pointcut을 정의한다.
3. 공통 기능을 구현한 메서드에 @Around 애노테이션을 적용한다.

우리는 공통 기능을 제공하는 Aspect 구현 클래스를 만들고 자바 설정을 이용하여 Aspect를 어디에 적용할지 설정하면 된다. 아까 언급한 바와 같이 프록시는 스프링 프레임워크가 알아서 만들어준다.

```
package chap07;

import ...

@Aspect
public class ExeTimeAspect {

    @Pointcut("execution(public * chap07 ..*(..))")
    private void publicTarget() { }
    
    @Around("publicTarget()")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            long finish = System.nanoTime();
            Signature sig = joinPoint.getSignature();
            System.out.printf("%s.%s(%s) 실행 시간 : %d ns\n",
            		joinPoint.getTarget().getClass().getSimpleName(),
                        sig.getName(), Arrays.toString(joinPoint.getArgs()),
                        (finish - start));
        }
    }
}
```

예제와 함께 각 애노테이션과 메서드를 살펴보자.

- @Aspect 애노테이션을 적용한 클래스를 Advice와 Pointcut을 함께 제공한다.
- @Pointcut 애노테이션의 값으로 사용할 수 있는 execution 명시자에 대해서는 곧 살펴볼 것이다. 지금은 chap07 패키지와 그 하위 패키지에 위치한 타입의 public 메서드를 Pointcut으로 설정한다는 정도만 알고 넘어가자.
- @Around 애노테이션의 값이 "publicTarget()"인데, 이는 **publicTarget() 메서드에 정의한 Pointcut에 공통 기능을 적용한다**는 뜻이다.
- measure() 메서드의 **ProceedingJoinPoint 타입 파라미터**는 프록시 대상 객체의 메서드를 호출할 때 사용한다. **proceed()** 메서드를 사용해서 실제 대상 객체의 메서드를 호출하기 때문에, 이 코드 이전/이후에 공통 기능을 위한 코드를 위치시키면 된다.

공통 기능을 적용하는데 필요한 코드를 구현했으므로, 그 다음에는 스프링 설정 클래스를 작성하면 된다. 

이때, @Aspect 애노테이션을 붙인 클래스를 공통 기능으로 적용하려면, **@EnableAspectJAutoProxy 애노테이션을 설정 클래스에 붙여야 한다.** 이 애노테이션을 붙이면 스프링은 @Aspect 애노테이션이 붙은 빈 객체를 찾아 빈 객체의 @Pointcut 설정과 @Around 설정을 사용한다.

> 💡 @Enable 류 애노테이션은 관련 기능을 적용하는데 필요한 다양한 스프링 설정을 대신 처리한다. 복잡한 스프링 설정을 대신 해주기 때문에 개발자가 쉽게 스프링을 사용할 수 있도록 만들어준다.

## ProceedingJoinPoint의 메서드
Around Advice에서 사용할 공통 기능 메서드는 대부분 파라미터로 전달받은 ProceedingJoinPoint의 proceed() 메서드만 호출하면 된다. 

근데 여기서 호출되는 대상 객체에 대한 정보, 실행되는 메서드에 대한 정보, 메서드를 호출할 때 전달된 인자에 대한 정보가 필요할 때가 있다. 

이 경우 이들 정보에 접근할 수 있도록 ProceedingJoinPoint 인터페이스는 다음 메서드를 제공한다.
- Signature getSignature(): 호출되는 메서드에 대한 정보를 구함
- Object getTarget(): 대상 객체를 구함
- Object[] getArgs(): 파라미터 목록을 구함

org.aspectj.lang.Signature 인터페이스는 다음 메서드를 제공한다.
- String getName(): 호출되는 메서드의 이름을 구한다.
- String toLongString(): 호출되는 메서드의 리턴 타입, 파라미터 타입이 모두 표시되는 완전하게 표현된 문장을 구한다.
- String toShortString(): 호출되는 메서드의 이름만 나오는, 축약해서 표현한 문장을 구한다.

# 프록시 생성 방식

메인 클래스와 설정 클래스의 일부 코드를 다음과 같이 수정했다고 하자.

1. 메인 클래스
```
// 수정 전
Calculator cal = ctx.getBean("calculator", Calculator.class);
// 수정 후 (import에도 RecCalculator 추가)
RecCalculator recCal = ctx.getBean("calculator", RecCalculator.class);
```
2. 설정 클래스
```
// AppCtx의 일부
@Bean
public Calculator calculator() {
	return new RecCalculator();
}
```
다음과 같이 코드를 수정한 뒤 메인 클래스를 실행하면 잘 될 것 같은데,
생각과는 달리 메인 클래스를 실행하게 되면 다음과 같은 익셉션이 발생한다.
```
Exception in thread "main" o..~.BeanNotOfRequiredTypeException: Bean named 
'calculator' is expected to be type 'chap07.RecCalculator' but was actually 
of type 'com.sun.proxy.$Proxy17' 
```

즉, getBean() 메서드에 사용한 타입이 RecCalculator인데 반해 실제 타입은 $Proxy17라는 메시지가 나온다. 이 $Proxy17 클래스는 RecCalculator 클래스가 상속받은 Calculator 인터페이스를 상속받게 된다. 

![](https://images.velog.io/images/nanaeu/post/09e7fdc8-5305-4ea8-b9a2-5bc2e89570ee/image.png)

스프링은 AOP를 위한 프록시 객체를 생성할 때, 실제 생성할 빈 객체가 인터페이스를 상속하면 인터페이스를 이용해서 프록시를 생성한다.

따라서 빈 객체가 인터페이스를 상속할 때 인터페이스가 아닌 
**클래스를 이용해서 프록시를 생성하고 싶다면** 다음과 같이 설정하면 된다.

```
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AppCtx {
```
**@EnableAspectJAutoProxy 애노테이션의 proxyTargetClass 속성을 true로 지정**해주면 인터페이스가 아닌 자바 클래스를 상속받아 프록시를 생성한다.

## 🎈 execution 명시자 표현식
execution 명시자의 기본 형식은 다음과 같다.
> execution(수식어패턴? 리턴타입패턴 클래스이름패턴?메서드이름패턴(파라미터패턴))

- 수식어패턴: 생략 가능. public, protected 등이 온다. 스프링 AOP는 public 메서드에만 적용할 수 있기 때문에 사실상 public만 의미있다.
- 리턴타입패턴: 리턴 타입 명시
- 클래스이름패턴 / 메서드이름패턴: 클래스 이름 및 메서드 이름으로 패턴 명시
- 파라미터패턴: 매칭될 파라미터에 대해 명시

각 패턴은 '*'를 이용하여 모든 값을 표현할 수 있으며, '..'(점 두 개)를 이용하여 0개 이상이라는 의미를 표현할 수 있다.

## 💡 Advice 적용 순서
한 Pointcut에 여러 Advice를 적용할 수도 있다. 이때, 각 프록시 객체의 대상 객체를 찾아가며 실제 대상 객체를 실행하기까지의 순서가 있는데, 이 순서를 지정하고 싶다면 @Order 애노테이션을 클래스에 붙여 순서를 지정할 수 있다.
```
import org.springframework.core.annotation.Order;

@Aspect
@Order(1)
public class ExeTimeAspect {
	...
}

@Aspect
@Order(2)
public class CacheAspect {
	...
}
```
## 💡 @Around의 Pointcut 설정과 @Pointcut의 재사용

@Pointcut 애노테이션이 아닌 **@Around 애노테이션에 execution 명시자를 직접 지정할 수도 있다.** 다음은 설정 예시이다.
```
@Aspect
public class CacheAspect {
    
    @Around("execution(public * chap07..*(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable{
    	...
    }
}
```

만약 같은 Pointcut을 여러 Advice가 함께 사용한다면 공통 Pointcut을 재사용할 수 있다. 

- **다른 클래스에 위치**한 @Around 애노테이션_에서 해당 메서드의 Pointcut을 사용하고 싶다면, 해당 메서드를 public으로 바꾸고 해당 Pointcut의 완전한 클래스 이름을 포함(패키지명 포함)한 메서드 이름을 @Around 애노테이션에서 사용하면 된다. 

- **여러 Aspect에서 공통으로 사용하는 Pointcut**이 있다면, 별도 클래스에 Pointcut을 정의하고, 각 Aspect 클래스에서 해당 Pointcut을 사용하도록 구성하면 Pointcut 관리가 한결 편해진다.

> 별도 클래스로 정의한 Pointcut의 클래스는 빈으로 등록할 필요가 없다. @Around 애노테이션에서 해당 클래스에 접근 가능하면 해당 Pointcut을 사용할 수 있다.






