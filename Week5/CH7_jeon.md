# AOP 프로그래밍

## 프로젝트 준비

```xml
		<dependency>
			<groupId>org.aspectj</groupId>
			<artifactId>aspectjweaver</artifactId>
			<version>1.8.13</version>
		</dependency>

```

`aspectjweaver` 는 스프링이 AOP를 구현할 때 사용하는 모듈이다.

## ✨AOP란?
> ### Aspect-Oriented Programming의 약자이다. 흩어진 Aspect들을 모아서 모듈화 하는 기법이다.

스프링의 AOP 기능은 spring-aop모듈이 제공하는데, spring-context 모듈을 의존 대상에 추가하면 같이 추가된다. aspectjweaver 모듈은 AOP 설정에 필요한 애노테이션을 제공한다.

AOP 를 알아보기 위해 factorial 을 반복문과 재귀 호출을 이용해 구현한 두 클래스를 사용할 것이다.

## 프록시와 AOP 

> ❓ 두 클래스의 실행 시간을 출력하려면 어떻게 해야 할까?

* ### 반복문의 경우

메서드의 시작과 끝에서 시간을 구하고 그 차이를 구하면 된다.

* ### 재귀 호출의 경우

이 경우는 좀 복잡하다. 메서드의 시작과 끝에서 구할 시 메시지가 여러 번 출력되는 문제가 있다. 그래서, 이 경우 프록시 객체를 사용한다.

```java
public class ExeTimeCalculator implements Calculator {

	private Calculator delegate;

	public ExeTimeCalculator(Calculator delegate) {
        this.delegate = delegate;
    }

	@Override
	public long factorial(long num) {
		long start = System.nanoTime();
		long result = delegate.factorial(num);
		long end = System.nanoTime();
		System.out.printf("%s.factorial(%d) 실행 시간 = %d\n",
				delegate.getClass().getSimpleName(),
				num, (end - start));
		return result;
	}

}
```

ExeTimeCaculator 클래스는 Calculator 인터페이스를 구현하고 있다. 이 클래스는 생성자를 통해 다른 Calculator 객체를 전달받아 delegate 필드에 할당하고 factorial() 메서드를 실행한다. 이 전후에 시간을 구해 차이를 출력하여, 실행 시간을 구한다.

이 코드를 통해 다음을 알 수 있다.
* 기존 코드를 변경하지 않고 실행 시간을 출력할 수 있다.
* 실행 시간 구하는 코드의 중복을 제거했다.

이는 아래의 이유를 통해 가능하게 되었다.
* factorial() 의 실행을 다른 객체로 위힘한다.
* 계산 기능 외에 부가적인 기능을 실행한다.

이렇게 핵심 기능의 실행을 다른 객체에 위임하고 부가적인 기능을 제공하는 객체를 `프록시` 하고 부른다. 핵심 기능을 실행하는 객체는 `대상 객체` 라고 부른다. 

### AOP
> 여러 객체에 공통으로 적용할 수 있는 기능을 분리해서 재사용성을 높여주는 프로그래밍 기법. 

핵심 기능과 공통 기능의 구현을 분리하여 핵심 기능 코드의 수정 없이 공통 기능을 적용한다. 핵심 기능에 공통 기능을 삽입하는 방법에는 다음 세 가지가 있다.

* 컴파일 시점에 코드에 공통 기능 삽입
* 클래스 로딩 시점에 바이트 코드에 공통 기능 삽입
* 런타임에 프록시 객체를 생성해 공통 기능 삽입

스프링이 제공하는 AOP 방식은 세 번째 방식이다. 스프링 AOP 는 프록시 객체를 자동으로 만들어준다. 그래서 ExeTimeCalculator 클래스처럼 프록시 클래스를 직접 구현할 필요가 없다.

AOP 에서 공통 기능을 `Aspect` 라고 하고, 아래의 용어들을 알아두어야 한다.


|용어|의미|
|---|-----|
|Advice|언제 공통 기능을 핵심 기능에 적용하는지|
|Joinpoint|Advice를 적용 가능한 지점. 메서드 호출, 필드 값 변경 등|
|Pointcut|Joinpoint의 부분 집합으로, 실제 Advice가 적용되는 Joinpoint|
|Weaving|Advice를 핵심 코드에 적용하는 것|

### Advice의 종류
스프링은 프록시로 메서드 호출 시점에 Aspect를 적용해서, 구현 가능한 Advice 종류는 아래와 같다.

|종류|의미|
|---|-----|
|Before Advice|메서드 호출 전|
|After Returing Advice|대상 객체 메서드가 익셉션 없이 실행된 이후|
|After Throwing Advice|익셉션이 발생한 경우|
|After Advice|익셉션 발생 여부와 상관없이 대상 객체의 메서드 실행 후|
|Around Advice|메서드 실행 전,후,익셉션 발생 시점|

이 중 Around Advice 가 널리 사용된다. 다양한 시점에 원하는 기능을 삽입할 수 있기 때문이다.

## 스프링 AOP 구현
방법은 아래와 같다.
* @Aspect 애노테이션 붙임
* @Pointcut 으로 공통 기능 적용할 Pointcut 정의
* @Around 를 공통 기능 메서드에 적용

```java
@Aspect
public class ExeTimeAspect {

	@Pointcut("execution(public * chap07..*(..))")
	private void publicTarget() {
	}

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

* @Aspect 애노테이션을 적용한 클래스는 Advice와 Pointcut 을 함께 제공한다.
* @Pointcut 은 공통 기능을 적용할 대상을 설정함.
* @Around 애노테이션은, "publictarget()" 메서드에 정의한 pointcut에 공통 기능을 적용한다는 것을 의미한다. 이는 chap07 패키지와 그 하위 패키지의 public 메서드를 pointcut 으로 설정하고 있으므로 이에 속한 빈 객체의 public 메서드에 @Around 가 붙은 measure 메서드를 적용한다.

```java
@Configuration
@EnableAspectJAutoProxy
public class AppCtx {
	@Bean
	public ExeTimeAspect exeTimeAspect() {
		return new ExeTimeAspect();
	}

	@Bean
	public Calculator calculator() {
		return new RecCalculator();
	}

}
```
@Aspect 애노테이션을 붙인 클래스를 공통 기능ㅇ로 적용하려면 @EnableAspectJAutoProxy 애노테이션을 설정 클래스에 붙여야 한다.이를 추가하면 @Aspect 애노테이션이 붙은 빈 객체를 찾아 빈 객체의 @Pointcut 설정과 @Around 설정을 사용한다.