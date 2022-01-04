# 해결하지 못한 에러
## 1) 브라우저 url을 http://localhost:8080/sp5-chap09/hello?name=bk로 바꿔도 404 에러가 여전히 떠있다.
https://easy7.tistory.com/167를 참고해 어느 부분을 수정해야할지 보았다.

### 시도해본 방법
1. web.xml에서 servlet-name쪽에 servlet should have a mapping이란 에러와 filter-name에도 mapping이 필요하다는 에러가 나서 
https://youtrack.jetbrains.com/issue/IDEA-151597#comment=27-1326420를 참고해 에러를 해결해봤지만 여전히 404는 사라지지 않는다.
2. pom.xml에서 plugin maven-compiler-plugin not found 라는 에러가 나있었다. 
이전에 실습할 때는 해당 에러가 문제가 되지 않았지만 혹시나 하고 https://stackoverflow.com/questions/63468269/maven-plugin-not-found-in-intellij-ide/64402809를 참고해 에러를 해결해보려 했지만
해결되지 않았다. 
