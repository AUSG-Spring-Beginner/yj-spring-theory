# 컴포넌트 스캔

> 스프링이 직접 클래스를 검색해서 빈으로 등록해주는 기능

자동 주입과 함께 사용하는 추가 기능이다. 설정 클래스에 빈으로 등록하지 않아도 원하는 클래스를 빈으로 등록할 수 있어 코드가 크게 줄어듦. 

## 1. @Component 애노테이션으로 스캔 대상 지정

스프링이 검색해서 빈으로 등록할 수 있으려면 `@Component` 애노테이션을 붙여야 한다. 이는 해당 클래스를 스캔 대상으로 표시한다. 애노테이션에 속성 값을 주면 그 값을 빈 이름으로 사용한다.

> @Component('listprinter') -> listprinter
> public class MemberListPrinter

> public class MemberListPrinter -> memberListprinter

가 빈 이름이 된다.

## 2. @ComponentScan 애노테이션으로 스캔 설정

``` java

@Configuration
@ComponentScan(basePackages = {"spring"})
public class AppCtx {

```

스프링 컨테이너가 @Component 애노테이션을 붙인 클래스를 검색해서 빈으로 등록해준다. 또한 @ComponentScan 애노테이션의 기본 속성값은 "spring" 이다. 이는 spring 패키지와 그 하위 패키지에 속한 클래스를 스캔 대상으로 설정한다.

## 3. 스캔 대상에서 제외하거나 포함하기
`excludeFilters` 속성을 서용하면 스캔 시 특정 대상을 자동 등록 대상에서 제외할 수 있다.

```java
@Configuration
@ComponentScan(basePackages = {"spring", "spring2" }, 
	excludeFilters = { 
			@Filter(type = FilterType.REGEX, pattern = "spring\\..*Dao" )			
})
```

이 코드는 @Filter 애노테이션의 type 속성값으로 FilterType.REGEX 를 주었다. 이는 **정규표현식을 사용해 제외 대상을 지정** 한다는 것을 의미한다. pattern 속성은 FilterType 에 적용할 값을 설정한다. Spring 으로 시작하고 Dao로 끝나는 정규표현식을 나타내었다. <br>
FilterType.ASPECTJ 를 필터 타입으로 설정할 수도 있다. 이는 정규표현식 대신 AspectJ 패턴을 사용해서 대상을 지정한다.

```java
@Configuration
@ComponentScan(basePackages = {"spring", "spring2" }, 
	excludeFilters = { 
			@Filter(type = FilterType.REGEX, pattern = "spring.*Dao" )			
})
```

patterns 속성은 String[] 타입이므로 배열을 이용해서 패턴을 여러 개 지정할 수 있다.<br>
특정 애노테이션을 붙인 타입을 컴포넌트 대상에서 제외할 수 있다. `@NoProduct` 나 `@ManualBean` 애노테이션을 붙인 클래스를 컴포넌트 스캔 대상에서 제외하려면, 다음과 같이 excludeFilters 속성을 설정한다.

``` java
@Configuration
@ComponentScan(basePackages = {"spring", "spring2" }, 
	excludeFilters = { 
			@Filter(type = FilterType.ANNOTATION, classes = ManualBean.class )			
})
```

type 속성값으로 FilterType.ANNOTATION 을 사용하면 classes 속성에 필터로 사용할 애노테이션 타입을 값으로 준다. 이 코드는 @ManualBean 애노테이션을 제외 대상에 추가한다.<br>

특정 타입이나 그 하위 타입을 컴포넌트 스캔 대상에서 제외하려면 ASSIGNABLE_TYPE 을 FilterType 으로 사용한다.


@Configuration
@ComponentScan(basePackages = {"spring", "spring2" }, 
	excludeFilters = { 
			@Filter(type = FilterType.ASSGINABLE_TYPE, classes = MemberDao.class )			
})

classes 속성에는 제외할 타입 목록을 지정한다.

### 기본 스캔 대상

@Component 애노테이션을 붙인 클래스만 컴포넌트 스캔 대상에 포함되는 것은 아니다.
* @Component(org.springframework.stereotype 패키지)
* @Controller(org.springframework.stereotype 패키지)
* @Service(org.springframework.stereotype 패키지)
* @Repository(org.springframework.stereotype 패키지)
* @Aspect(org.aspectj.lang.annotation 패키지)
* @Configuration(org.springframework.context.annotation 패키지)

@Aspect 애노테이션을 제외한 나머지 에노테이션은 실제로 @Component 애노테이션에 대한 특수 애노테이션이다. 예를들어 @Controller 애노테이션은, @Component 애노테이션과 동일하게 컴포넌트 스캔 대상에 포함한다. @Controller 나 @Repository 는 스캔 대상이 될뿐만 아니라 스프링 프레임워크에서 특별한 기능과 연관되어 있다.

## 4. 컴포넌트 스캔에 따른 충돌 처리

### 빈 이름 충돌

서로 다른 타입인데 같은 빈 이름을 사용하는 경우 둘 중 적어도 하나에 명시적으로 빈 이름을 지정해 이름 충돌을 피해야 한다.

### 수동 등록한 빈과 충돌

스캔할 때 사용하는 빈 이름과 수동 등록한 빈 이름이 같은 경우 수동 등록한 빈이 우선이다.

