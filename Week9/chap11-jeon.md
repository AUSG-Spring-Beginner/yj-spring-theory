# Chap11 - 2

# 커맨드 객체와 스프링 폼 연동

폼을 보여줄 때 커맨드 객체의 값을 폼에 채워주면 다시 폼을 보여줘도 값을 다시 입력하지 않아도 된다.

```jsx
<p>
        <label>이메일:<br>
        <input type="text" name="email" id="email" value="${registerRequest.email}">
        </label>
    </p>
    <p>
        <label>이름:<br>
        <input type="text" name="name" id="name" value="${registerRequest.name}">
        </label>
    </p>
```

---

# 컨트롤러 구현 없는 경로 매핑

특별히 처리할 게 없는 경로의 경우, 컨트롤러 클래스를 만들지 않고 단순히 뷰 이름만 리턴한다. 특별한 로직이 없는 컨트롤러 클래스를 만드는 번거로움을 줄일 수 있다.

```c
public void addViewControllers(ViewControllerRegistry registry){
	registry.addViewController("/main").setViewName("main");
}
```

이는 /main 요청 경로에 대해 뷰 이름으로 main을 사용하도록 설정한다.

# 주요 에러 발생 상황

## 요청 매핑 애노테이션과 관련된 익셉션

흔히 말하는 **404** 에러이다. 이것이 발생하면 확인해야 할 것들은

- 요청 경로가 올바른지
- 컨트롤러에 설정된 경로가 올바른지
- 컨트롤러 클래스를 빈으로 등록했는지
- 컨트롤러 클래스에 @Controller 애노테이션을 등록했는지
- 뷰 이름에 해당하는 JSP 파일이 있는지

또한 지원하지 않는 전송 방식을 사용한 경우 **405** 에러가 발생한다.

## @RequestParam 이나 커맨드 객체와 관련된 주요 익셉션

@RequestParam 애노테이션을 필수로 필요해야 하지만 기본값을 지정하지 않았을 때, 아무 값도 전송하지 않았을 경우 **400** 에러가 발생한다. 

또한 요청 파라미터의 값을 @RequestParam이 적용된 파라미터의 타입으로 변환할 수 없는 경우에도 에러가 발생한다. 예를 들면 checkbox의 값을 true에서 true1로 변경할 때 이러한 상황이 발생한다. 혹은 요청 파라미터의 값을 커맨드 객체에 복사하는 과정에서, 변환할 수 없는 경우에도 발생한다.

# 커맨드 객체: 중첩·콜렉션 프로퍼티

스프링 MVC는 커맨드 객체가 리스트 타입의 프로퍼티를 가졌거나 중첩 프로퍼티를 가진 경우에도 요청 파라미터의 값을 알맞게 커맨드 객체에 설정해준다.

```java
public class AnsweredData {

	private List<String> responses;
	private Respondent res;

	public List<String> getResponses() {
		return responses;
	}

	//리스트 형식의 프로퍼티 설정
	public void setResponses(List<String> responses) {
		this.responses = responses;
	}

	public Respondent getRes() {
		return res;
	}

	//중첩 프로퍼티 설정
	public void setRes(Respondent res) {
		this.res = res;
	}

}
```

```java
@Controller
@RequestMapping("/survey")
public class SurveyController {

	@GetMapping
	public String form(Model model) {
		List<Question> questions = createQuestions();
		model.addAttribute("questions", questions);
		return "survey/surveyForm";
	}

	private List<Question> createQuestions() {
		Question q1 = new Question("당신의 역할은 무엇입니까?",
				Arrays.asList("서버", "프론트", "풀스택"));
		Question q2 = new Question("많이 사용하는 개발도구는 무엇입니까?",
				Arrays.asList("이클립스", "인텔리J", "서브라임"));
		Question q3 = new Question("하고 싶은 말을 적어주세요.");
		return Arrays.asList(q1, q2, q3);
	}

	@PostMapping
	public String submit(@ModelAttribute("ansData") AnsweredData data) {
		return "survey/submitted";
	}

}
```

### GET 요청시

- HTTP 요청 파라미터 이름이 이름[인덱스] 형식이면 List 타입 프로퍼티의 값 목록으로 처리한다.

```html
<label><input type="radio" name="responses[${status.index}]" value="${option}"></label>
```

- 이름.이름 형식이면 중첩 프로퍼티 값을 처리한다.

```html
<input type="text" name="res.location">
```

### Post 요청시

ansdata.response[index] 와 ansdata.res.프로퍼티이름 으로 사용한다.

```html
<p>응답 내용:</p>
    <ul>
        <c:forEach var="response" 
                   items="${ansData.responses}" varStatus="status">
        <li>${status.index + 1}번 문항: ${response}</li>
        </c:forEach>
    </ul>
    <p>응답자 위치: ${ansData.res.location}</p>
    <p>응답자 나이: ${ansData.res.age}</p>
```

# Model을 통해 컨트롤러에서 뷰에 데이터 전달하기

컨트롤러는 뷰가 응답 화면을 구성하는데 필요한 데이터를 생성해서 전달한다. 이 때 사용하는 것이 Model이다. 이는

- 요청 매핑 애노테이션이 적용된 메서드의 파라미터로 Model 추가
- Model 파라미터의 addAttribute() 메서드로 뷰에서 사용할 데이터 전달

을 하면 된다.

위 예제에서, 제목과 옵션 정보를 담은 Model 인 Question 을 생성한다.

```java
public class Question {

	private String title;
	private List<String> options;

	public Question(String title, List<String> options) {
		this.title = title;
		this.options = options;
	}

	public Question(String title) {
		this(title, Collections.<String>emptyList());
	}

	public String getTitle() {
		return title;
	}

	public List<String> getOptions() {
		return options;
	}

	public boolean isChoice() {
		return options != null && !options.isEmpty();
	}

}
```

그 후 SurveyController가 Question 객체 목록을 생성해 뷰에 전달한다.