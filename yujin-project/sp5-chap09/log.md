# 에러 - 해결
## 1) 브라우저 url을 http://localhost:8080/sp5-chap09/hello?name=bk 로 바꿔도 404 에러가 여전히 떠있다.
https://easy7.tistory.com/167 를 참고해 어느 부분을 수정해야할지 보았다.

### 시도해본 방법
1. web.xml에서 servlet-name쪽에 servlet should have a mapping이란 에러와 filter-name에도 mapping이 필요하다는 에러가 나서 
https://youtrack.jetbrains.com/issue/IDEA-151597#comment=27-1326420 를 참고해 에러를 해결해봤지만 여전히 404는 사라지지 않는다.
2. pom.xml에서 plugin maven-compiler-plugin not found 라는 에러가 나있었다. 
이전에 실습할 때는 해당 에러가 문제가 되지 않았지만 혹시나 하고 https://stackoverflow.com/questions/63468269/maven-plugin-not-found-in-intellij-ide/64402809 를 참고해 에러를 해결해보려 했지만 해결되지 않았다. 

3. **(해결)** 톰캣 서버 설정에 들어가 application context를 확인해보니, 책에서처럼 /sp5-chap09로 되어있지 않고 /sp5_chap09_war_exploded로 설정되어 있었다. 
**해당 application context를 /sp5-chap09로 변경**해주니 해결되었다.
![image](https://user-images.githubusercontent.com/55730357/148333770-d8bd2e8d-803b-4d54-a97c-e11ef7b27918.png)

해결방법 - 참고)

https://justdo-heal.tistory.com/9

https://goddaehee.tistory.com/247
