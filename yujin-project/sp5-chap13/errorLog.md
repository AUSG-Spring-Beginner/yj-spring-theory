# 해결되지 않은 에러
jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; 

nested exception is com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure

## 시도해본 방법
1. DBMS 재실행
2. User 재생성 및 privilege 설정
3. autoReconnect=true, validationQuery=”SELECT 1″, testOnBorrow=”true”, serverTimezone=UTC

DB 연결에 문제가 있는 거 같긴 한데 뭔지를 못 찾겠다...
