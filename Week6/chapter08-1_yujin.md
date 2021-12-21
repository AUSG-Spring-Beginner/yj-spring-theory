많은 웹 애플리케이션은 데이터를 보관하기 위해 DBMS를 사용한다. 

자바에서는 JDBC API를 사용하거나 JPA, MyBatis 같은 기술을 사용하여 DB 연동을 처리한다. 

이 글에서는 JDBC를 위해 스프링이 제공하는 JdbcTemplate에 대해 얘기해보려 한다.

# JDBC 프로그래밍의 단점을 보완하는 스프링
JDBC API를 이용하면 DB 연동에 필요한 Connection을 구한 다음 쿼리를 실행하기 위한 PreparedStatement를 생성한다. 그리고 쿼리를 실행한 뒤에는 PreparedStatement, Connection 등을 닫는다. 

여기서 실제 핵심 코드는 얼마 없고, 사실상 데이터 처리와 상관없지만 JDBC 프로그래밍을 할 때 구조적으로 반복되는 코드가 생길 수 있다. 

이런 구조적인 반복을 줄이기 위한 방법은 Template Method 패턴과 Strategy 패턴을 함께 사용하는 것인데, 스프링은 바로 이 두 패턴을 묶은 **JdbcTemplate** 클래스를 제공한다.

> ## 💡Template Method 패턴이란?
> - 어떤 작업을 처리하는 일부분을 **서브 클래스로 캡슐화**해 전체 일을 수행하는 구조는 바꾸지 않으면서 특정 단계에서 수행하는 내역을 바꾸는 패턴
> - 전체적으로는 동일하면서 부분적으로는 다른 구문으로 구성된 메서드의 코드 중복을 최소화 할 때 유용하다.
> ## 💡Strategy 패턴이란?
> - 객체들이 할 수 있는 행위 각각에 대해 전략 클래스를 생성하고, **유사한 행위들을 캡슐화 하는 인터페이스를 정의**하여, 객체의 행위를 동적으로 바꾸고 싶은 경우 **직접 행위를 수정하지 않고 전략을 바꿔주기만 함**으로써 행위를 유연하게 확장하는 방법
> - 즉, **객체가 할 수 있는 행위들 각각을 전략으로 만들어 놓고**, 동적으로 행위의 수정이 필요한 경우 전략을 바꾸는 것만으로 행위의 수정이 가능하도록 만든 패턴

> ## 💡@Transactional 애노테이션
> JDBC API로 트랜잭션을 처리하려면 자동 커밋을 비활성화한 뒤 따로 트랜잭션을 커밋하거나 롤백해야 하지만, 스프링을 사용하면 트랜잭션을 적용하고 싶은 메서드에 @Transactional 애노테이션을 붙이기만 하면 된다.
> > 커밋과 롤백 처리는 스프링이 알아서 처리한다😎

# 커넥션 풀 (Connection pool)

## 1. 커넥션 풀이란?
실제 서비스 운영 환경에서는 서로 다른 장비를 이용해서 자바 프로그램과 DBMS를 실행하는데, 자바 프로그램에서 DBMS로 커넥션을 생성하는 시간은 컴퓨터 입장에서 매우 길기 때문에 성능에 영향을 줄 수 있다. 또한 동시 접속자의 수가 많아지면 사용자마다 DB 커넥션을 생성하기 때문에 DBMS에 부하를 줄 수 있다.

이때, 최초 연결에 따른 응답 속도 저하와 동시 접속자가 많을 때 생기는 부하를 줄이기 위해 사용하는 것이 **커넥션 풀**이다.

### 커넥션 풀은 
- 일정 개수의 DB 커넥션을 미리 만들어두는 기법이기 때문에,
커넥션을 사용하는 시점에서 커넥션을 생성하는 시간을 아낄 수 있다.
- 동시 접속자의 수가 많아지더라도 커넥션을 생성하는 부하가 적기 때문에 더 많은 동시 접속자를 처리할 수 있다. 
- 커넥션도 일정 개수로 유지해서 DBMS에 대한 부하를 일정 수준으로 유지할 수 있게 해 준다.

이런 이유들로 실제 서비스 운영 환경에서는 커넥션 풀을 사용해서 DB 연결을 관리하는데, DB 커넥션 풀 기능을 제공하는 모듈로는 Tomcat JDBC, HikariCP, DBCP, c3p0 등이 존재한다.
현시점에서는 지속적인 개발, 성능 등을 고려하여 Tomcat JDBC나 HikariCP를 권한다.

> ### 💡주의!
> 커넥션 풀에 생성된 커넥션은 지속적으로 재사용되지만, 
> _한 커넥션이 영원히 유지되는 것은 아니다._
>
> DBMS 설정에 따라 일정 시간 내에 쿼리를 실행하지 않으면 연결을 끊기도 한다. 이 경우, 해당 커넥션의 연결은 끊겨있지만 커넥션은 여전히 풀 속에 남아있는 상태가 된다.
>
> 이 상태에서 해당 커넥션을 풀에서 가져와 사용하면 연결이 끊어진 커넥션이므로 익셉션이 발생한다. 업무용 시스템과 같이 특정 시간대에 사용자가 없으면 이런 상황이 발생할 수 있다.
> ### 🤔 그럼 어떻게 해야돼?
> **커넥션 풀의 커넥션이 유효한지 주기적으로 검사**해야 한다!


# DataSource 설정
JDBC API는 DriverManager 외에 DataSource를 이용해서 DB 연결을 구하는 방법을 정의하고 있다. 

스프링이 제공하는 DB 연동 기능은 DataSource를 사용해서 DB Connection을 구한 뒤, DB 연동에 사용할 DataSource를 스프링 빈으로 등록하고 DB 연동 기능을 구현한 빈 객체는 DataSource를 주입받아 사용한다.

# JdbcTemplate을 이용한 쿼리 실행
스프링을 사용하면 DataSource나 Connection, Statement, ResultSet을 직접 사용하지 않고 JdbcTemplate을 이용해서 편리하게 쿼리를 실행할 수 있다.

예제 코드와 함께 보자.

## 1. JdbcTemplate 생성
```
package spring;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public class MemberDao {
    private JdbcTemplate jdbcTemplate;
    
    public MemberDao(DataSource dataSource) {
    	this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    ...
}
```

JdbcTemplate 객체를 생성하려면 DataSource를 생성자에 전달하면 된다. 

JdbcTemplate을 생성하는 코드를 MemberDao 클래스에 추가했으니 
이제 스프링 설정 클래스에 MemberDao 빈 설정을 추가하자.

```
@Configuration
public class AppCtx {

    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        DataSource ds = new DataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://localhost/spring5fs?characterEncoding=utf8");
        ds.setUserName("spring5");
        ds.setPassword("spring5");
        ... // 생략
        return ds;
    }
    
    @Bean
    public MemberDao memberDao() {
        return new MemberDao(dataSource());
    }
}
```
## 2. JdbcTemplate을 이용한 조회 쿼리 실행
JdbcTemplate 클래스는 SELECT 쿼리 실행을 위한 query() 메서드를 제공한다. 

자주 사용되는 쿼리 메서드는 다음과 같다.
- List< T > query(String sql, RowMapper< T > rowMapper)
- List< T > query(String sql, Object[] args , RowMapper< T > rowmapper)
- List< T > query(String sql, RowMapper< T > rowmapper, Object...args)

query() 메서드는 sql 파라미터로 전달받은 쿼리를 실행하고 RowMapper를 이용해서 ResultSet의 결과를 자바 객체로 변환한다. 만약 sql 파라미터가 인덱스 기반 파라미터를 가진 쿼리라면, args 파라미터를 이용해 각 인덱스 파라미터의 값을 지정한다. 

### RowMapper 인터페이스
```
package org.springframework.jdbc.core;

public interface RowMapper< T > {
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
```

RowMapper의 mapRow() 메서드는 SQL 실행 결과로 구한 ResultSet에서 한 행의 데이터를 읽어와 자바 객체로 변환하는 매퍼 기능을 구현한다.

RowMapper 인터페이스를 구현한 클래스를 작성할 수도 있지만, 임의 클래스나 람다식으로 RowMapper의 객체를 생성해서 query 메서드에 전달할 때도 많다.

다음은 임의 클래스(Member)를 활용한 예시다.
```
public Member selectByEmail(String email) {
        List<Member> results = jdbcTemplate.query(
                "select * from MEMBER where EMAIL = ?",
                new RowMapper<Member>() {
                    @Override
                    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Member member = new Member (
                            rs.getString("EMAIL"),
                            rs.getString("PASSWORD"),
                            rs.getString("NAME"),
                            rs.getTimestamp("REGDATE").toLocalDateTime());
                        member.setId(rs.getLong("ID"));
                        return member;
                    }
                },
                email);

        return results.isEmpty() ? null : results.get(0);
}
```

동일한 RowMapper 구현을 여러 곳에서 사용한다면 RowMapper 인터페이스를 구현한 클래스를 만들어 코드 중복을 피할 수 있다.
```
public class MemberRowMapper implements RowMapper<Member> {
	public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
        	Member member = new Member (
            	rs.getString("EMAIL"),
            	rs.getString("PASSWORD"),
            	rs.getString("NAME"),
            	rs.getTimestamp("REGDATE").toLocalDateTime());
       	member.setId(rs.getLong("ID"));
       	return member;
    }
}

// MemberRowMapper 객체 생성
List<Member> results = jdbcTemplate.query(
	"select * from MEMBER where EMAIL = ? and NAME = ?",
    new MemberRowMapper(),
    email, name);
```


> ## 💡 queryForObject() 메서드
>queryForObject() 메서드는 쿼리 실행 결과 행이 한 개인 경우에 사용>할 수 있는 메서드다. (예. select count(*) from MEMBER)
>
>이 메서드에서도 쿼리에 인덱스 파라미터를 사용할 수 있다. 인덱스 파라미터가 존재하면 파라미터의 값을 가변 인자로 전달한다.
>
>실행 결과 칼럼이 두 개 이상이면 RowMapper를 파라미터로 전달해서 결과를 생성할 수 있다. 

## 3. JdbcTemplate을 이용한 변경 쿼리 실행
INSERT, UPDATE, DELETE 쿼리는 update() 메서드를 사용한다.
- int update(String sql)
- int update(String sql, Object... args)

update() 메서드는 쿼리실행결과로 변경된 행의 개수를 리턴한다.
Ex.
```
	jdbcTemplate.update(
    	   "update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?",
            member.getName(), member.getPassword(), member.getEmail());
```
## 4. PreparedStatementCreator를 이용한 쿼리 실행
위의 update() 메서드 예시를 보면 인덱스 파라미터의 값을 각각 전달해주는 것을 볼 수 있다. 대부분 이와 같은 방법으로 쿼리의 인덱스 파라미터 값을 전달할 수 있다.

하지만 여기서, PreparedStatement 의 set 메서드를 사용해 직접 인덱스 파라미터의 값을 설정해야 할 때도 있다. 이 경우, PreparedStatementCreator를 인자로 받는 메서드를 이용해서 직접 PreparedStatement를 생성하고 설정해야 한다.

### PreparedStatementCreator 인터페이스
```
package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementCreator {
	PreparedStatement createPreparedStatement(Connection conn) 
    throws SQLException;
}
```
PreparedStatementCreator 인터페이스의 createPreparedStatement() 메서드는 Connection 타입의 파라미터를 갖는다. 

PreparedStatementCreator를 구현한 클래스는 위 메서드의 파라미터로 전달받는 Connection을 이용해서 PreparedStatement 객체를 생성하고 인덱스 파라미터를 설정한 뒤에 리턴하면 된다.


## 5. INSERT 쿼리 실행 시 KeyHolder를 이용해서 자동 생성 키값 구하기
MySQL의 ```AUTO_INCREMENT``` 칼럼은 행이 추가되면 자동으로 값이 증가되어 할당되는 칼럼이다. 따라서, INSERT 쿼리문을 작성할 때 따로 해당 값은 지정하지 않는다.

하지만 쿼리 실행 후에 생성된 키값을 알고 싶다면 어떻게 해야할까?

JdbcTemplate는 이를 알 수 있는 방법을 제공하고 있다. 
바로 **KeyHolder**를 사용하는 것!

```
KeyHolder keyHolder = new GeneratedKeyHolder();
jdbcTemplate.update(new PreparedStatementCreator() { 
	... 
    PreparedStatement pstmt = connection.preparedStatement(
    	"insert into ..."
        , new String[] {"ID"} );
        
        ...
        
}, keyHolder);
```

1. Connection의 preparedStatement() 메서드를 이용해서 PreparedStatement 객체를 생성할 때 두 번째 파라미터인 String 배열{(자동 생성 키 칼럼)}로 자동 생성되는 키 칼럼 목록을 지정한 뒤,
2. JdbcTemplate.update() 메서드의 두 번째 파라미터인 KeyHolder 객체를 전달해주면 KeyHolder 객체에 키값이 전달되어 보관된다.

KeyHolder에 보관된 키값은 getKey() 메서드를 통해 얻을 수 있다.


### 참고
템플릿 메서드 패턴 https://gmlwjd9405.github.io/2018/07/13/template-method-pattern.html
전략 패턴 https://victorydntmd.tistory.com/292
