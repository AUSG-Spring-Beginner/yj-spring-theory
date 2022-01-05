# Chap08

# DataSource 설정

JDBC API 는 DataMAnager  외에 DataSource를 이용해서 DB 연결을 구하는 방법을 정의하고 있다. 이를 사용하면 다음 방식으로 Connection을 구할 수 있다. 

```java
Connectiuon conn = null;
try{
	conn = dataSource.getConnection();
}
```

DB 연동에 사용할 DataSource 를 스프링 빈으로 등록하고 DB 연동 기능을 구현한 빈 객체는 DataSource 를 주입받아 사용한다.

```java
@Configuration
public class DbConfig {
    @Bean(destroyMethod = "close")
    public DataSource dataSource(){
        DataSource ds = new DataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://localhost/spring5fs?characterEncoding=utf8");
        ds.setUsername("spring5");
        ds.setPassword("spring5");
        ds.setInitialSize(2);
        ds.setMaxActive(10);

        return ds;

    }
}
```

- 5행:  Datasource 객체를 생성함
- 6행: JDBC 드라이버 클래스 지정
- 7행 : JDBC URL 지정

## Tomcat JDBC 프로퍼티

```java
public int count() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("select count(*) from MEMBER")) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                }
        }
    }
```

2행에서  DataSourse 에서 커넥션을 구하는데, 풀에서 커넥션을 가져온다.  이 때 커넥션 conn  은 활성 상태이다. 커넥션 사용이 끝나고 conn.close() 를 하면 실제 커넥션을 끊지 않고 풀에 반환하고 유휴 상태가 된다.

 `maxActive()` 로 활성 상태가 가능한 최대 커넥션 개수를 지정한다. 만약 모든 커넥션이 사용되고 있을 때 새로운 커넥션을 요청하면 다른 커넥션이 반환될 때 까지 대기하는데, 이 대기 시간을 `maxWait()` 로 지정한다. 만약 반환되지 않았다면 익셉션이 발생한다.

 커넥션 풀을 사용하면 성능이 좋아진다. 매번 새로운 커넥션을 생성하면 연결 시간이 소모된다. 커넥션 풀을 사용한다면 미리 커넥션을 생성해 필요한 때 커넥션을 꺼내 쓰므로 커넥션 구하는 시간으 줄어 전체 응답 시간도 짧아진다. 그래서 `initialSize()` 로 커넥션 풀을 초기화할 때 생성할 커넥션 개수를 정한다.

 한 커넥션이 영원히 유지되지는 않는다. DBMS 에서 설정된 시간 내에 쿼리를 실행하지 않으면 연결을 끊기도 하는데, 이는 커넥션 풀에는 남아 있어 해당 커넥션을 풀에서 가져와 사용하면 익셉션이 발생한다. 그래서 커넥션이 유효한지 여부를 주기적으로 검사해야 하는데, 이와 관련된 속성이 `minEvictableIdleTimeMillis`, `timeBetweenEvictionRunsMillis`, `testWhileIdle` 이다. 만약 10초 주기로 유휴 커넥션이 유효한지 여부를 검사하고 최소 유휴 시간을 3분으로 지정하고 싶다면 다음 설정을 사용한다.

```java
ds.setTestWhileIdle(true);
ds.setMinEvictableIdleTimeMillis(60000 * 3);
ds.setTimeBetweenEvictionRunsMillis(10 * 1000);
```

# JDBC Template 을 이용한 쿼리 실행

스프링을 사용하면 DataSource 나 Connection, Statemement, ResultSet 을 직접 사용하지 않고 JdbcTemplate 을 이용해서 편리하게 쿼리를 실행할 수 있다.

## Jdbc Template 생성

```java
public class MemberDao {
    private JdbcTemplate jdbcTemplate;

    public MemberDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
```

 JdbcTempleate 객체를 생성하려면 DataSource를 생성자에 전달한다. 이를 위해 DataSource 를 주입받도록 MemberDao 클래스의 생성자를 구현하였다. 설정 메서드 방식을 이용해도 된다.

 템플릿을 추가했으니 스프링 설정에 MemberDao 빈 설정을 추가한다.

```java
@Bean
    public MemberDao memberDao(){
        return new MemberDao(dataSource());
    }
```

## JdbcTemplate을 이용한 조회 쿼리 실행

`query()` 메서드는 sql 파라미터로 전달받은 쿼리를 실행하고 RowMapper를 이용해서 ResultSet 결과를 자바 객체로 변환한다.

```java
List<T> query(String sql, RowMapper<T> rowMapper)
```

RowMapper의 `mapRow()` 객체는  SQL 실행 결과로 구한 ResultSet 에서 한 행의 데이터를 읽어와 자바 객체로 변환하는 매퍼 기능을 구현한다. 임의 클래스나 람다식으로  RowMapper  객체를 생성해 `query`  메서드에 전달할 때가 많다.

```java
public Member selectByEmail(String email){
        List<Member> results = jdbcTemplate.query(
                "select * from MEMBER where EMAIL = ?",
                new RowMapper<Member>() {
                    @Override
                    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Member member = new Member(
                                rs.getString("EMAIL"),
                                rs.getString("PASSWORD"),
                                rs.getString("NAME"),
                                rs.getTimestamp("REGDATE").toLocalDateTime());
                        member.setId(rs.getLong("ID"));
                        return member;
                    }
                }, email);

        return results.isEmpty() ? null : results.get(0);
    }
```

임의 클래스를 이용해 RowMapper의 객체를 전달하고 있다. `mapRow()` 메서드는 파라미터로 전달받은 ResultSet 에서 데이터를 읽어와 Member 객체를 생성해 리턴하도록 구현했다. 동일한 RowMapper 를 여러 곳에서 사용한다면 클래스를 만들어 코드 중복을 막을 수도 있다.

```java
public class MemberRowMapper implements RowMapper<Member> {
    @Override
    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
        Member member = new Member(
                rs.getString("EMAIL"),
                rs.getString("PASSWORD"),
                rs.getString("NAME"),
                rs.getTimestamp("REGDATE").toLocalDateTime());
        member.setId(rs.getLong("ID"));
        return member;
    }
}
```

MemberDao 에서 `query()` 를 사용하는 또 다른 메서드는 `selectAll()` 이 있다.

이 코드는 클래스를 만들어 코드를 간결하게 작성하였다. 

```java
public Collection<Member> selectAll(){
        List<Member> results = jdbcTemplate.query("select * from MEMBER",  new MemberRowMapper());
        return results;
    }
```

## 결과가 1행일 경우 사용할 수 있는 queryForObject() 메서드

`count(*)` 쿼리는 결과를 리스트로 받기보다는 정수 타입으로 받는게 편리할 것이다. 이를 위한 메서드가 `queryForObject()` 이다. 이는 쿼리 실행 결과 행이 한 개인 경우 사용할 수 있다. 두 번째 파라미터에는 컬럼을 읽을 때 사용할 타입을 지정한다.

```java
public int count(){
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from MEMBER", Integer.class);
        return count;
   }
```

실행 결과 칼럼이 두 개 이상이면 RowMapper 를 파라미터로 전달해 결과를 생성할 수 있다. 특정 ID 를 갖는 회원 데이터를 queryForObject()로 읽어오고 싶다면 query() 메서드와 같이 코드를 작성하면 되는데, 차이점은 리턴 타입이 List 가 아니라 RowMapper로 변환해주는 타입이라는 점이다.

## JdbcTemplate 을 이용한 변경 쿼리 실행

INSERT, UPDATE, DELETE 쿼리는 `update()` 메서드를 사용한다. 이는 쿼리 실행 결과로 변경된 행의 개수를 리턴한다. 

```java
public void insert(Member member){
        jdbcTemplate.update(
                "update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?",
                member.getName(), member.getPassword(), member.getEmail());
    }
```

## PreparedStatementCreate 를 이용한 쿼리 실행

`PreparedStatement` 의 set 메서드를 사용해서 직접 인덱스 파라미터의 값을 설정해야 할 때도 있다. 이 경우 `PreparedStatementCreator` 를 인자로 받는 메서드를 이용해 직접 PreparedStatement 를 생성하고 설정해야 한다. createPreparedStatement() 메서드는 Connection 타입의 파라미터를 받는다. 이를 이용해서 PreparedStatement 객체를 생성하고 인덱스 파라미터를 설정한 뒤 리턴하면 된다. 

```java
public PreparedStatement createPreparedStatement(Connection con)
                    throws SQLException {
                // 파라미터로 전달받은 Connection을 이용해서 PreparedStatement 생성
                PreparedStatement pstmt = con.prepareStatement(
                        "insert into MEMBER (EMAIL, PASSWORD, NAME, REGDATE) " +
                                "values (?, ?, ?, ?)",
                        new String[] { "ID" });
                // 인덱스 파라미터 값 설정
                pstmt.setString(1, member.getEmail());
                pstmt.setString(2, member.getPassword());
                pstmt.setString(3, member.getName());
                pstmt.setTimestamp(4,
                        Timestamp.valueOf(member.getRegisterDateTime()));
                // 생성한 PreparedStatement 객체 리턴
                return pstmt;
            }
```

## INSERT 쿼리 실행 시 KeyHolder 를 이용해서 자동 생성 키값 구하기

MySQL 의 `AUTO_INCREMENT` 칼럼은 행이 추가되면 자동으로 값이 할당되는 칼럼으로 PRIMARY KEY  칼럼에 사용된다. 이와 같은 자동 증가 칼럼을 가진 테이블에 값을 삽입하면 해당 칼럼의 값이 자동으로 생성된다. INSERT 에 자동 증가 칼럼에 해당되는 값은 지정하지 않는다. 이 때 자동으로 생성된 키값을 리턴하지는 않는데, 이를 구할 때 사용하는 것이  `KeyHolder` 이다. 

```java
KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            //중략
        }, keyHolder);
        Number keyValue = keyHolder.getKey();
        member.setId(keyValue.longValue());
```

# MemberDao 테스트

## 익셉션

1. 계정/암호가 틀렸을때
2. DB 실행이 안돼있거나 방화벽에 막혔을 때
3. 줄바꿈 부분에서 공백문자를 누락했을 때

## 익셉션 변환 처리

SQL 문법이 잘못됐을 때 보면 익셉션 클래스가 org.springframework.jdbc 패키지에 속한 BadSqlGrammarException 클래스임을 알 수 있다. 스프링은 SQLException 을 그대로 전파하지 않고 DataAccessException 으로 변환한다. 이는 Bad~  익셉션을 상속한다. 이는 연동 기술에 상관없이 익셉션을 처리할 수 있게 하기 위함이다. Data~ Exception .은 RuntimeException 이라 try-catch 를 이용해서 처리하면 된다.

# 트랜잭션 처리

스프링이 제공하는 트랜잭션 기능을 사용하는 중복이 없는 매우 간단한 코드로 트랜잭션 범위를 지적할 수 있다.

## @Transactional 을 이용한 트랜잭션 처리

트랜잭션 범위에서 실행하고 싶은 메서드에  @Transactional 애노테이션만 붙이면 된다. 이 애노테이션이 제대로 동작하려면 다음 두가지 내용을 스프링 설정에 추가해야 한다.

- 플랫폼 트랜잭션 매니저 빈 설정
- @Transactional 애노테이션 활성화 설정

`PlatformTransactionManager` 는 스프링이 제공하는 트랜잭션 매니저 인터페이스이고, 구현기술에 상관없이 동일한 방식으로 트랜잭션을 처리하기 위해 이 인터페이스를 사용한다. JDBC는 `DataSourceTransactionManager` 클래스를 사용한다.

`@EnableTransactionMawnagement` 애노테이션 `@Transactional` 애노테이션이 붙은 메서드를 트랜잭션 범위에서 실행하는 기능을 활성화한다.

설정을 완료하면 트랜잭션 범위에서 실행하고 싶은 스프링 빈 객체의 메서드에 `@Transactional` 애노테이션을 붙이면 된다.

```java
@Transactional
    public void changePassword(String email, String oldPwd, String newPwd) {
        Member member = memberDao.selectByEmail(email);
        if (member == null)
            throw new MemberNotFoundException();

        member.changePassword(oldPwd, newPwd);

        memberDao.update(member);
    }
```

### @Transactional 과 프록시

스프링은 트랜잭션 처리를 프록시를 통해서 한다. 트랜잭션을 처리하기 위해 내부적으로 AOP 를 사용하기 때문이다.

### @Transactional 적용 메서드의 롤백 처리

커밋을 수행하는 주체가 프록시 객체였던 것처럼 롤백을 처리하는 주체 또한 프록시 객체이다.  별도 설정을 추가하지 않으면 발생한 익셉션이 RuntimeException 일 때 트랜잭션을 롤백한다. Jdbc 는 DB 연동 과정에 문제가 있으면 DataAccessException 을 발생한다고 했는데 이 는 RuntimeException 을 상속받고 있으므로 jdbc 를 사용하는 도중 익셉션이 발생하면 프록시는 트랜잭션을 롤백한다. SQLException 은 그렇지 않으므로 트랜잭션을 롤백하지 않는데, 이를 롤백하고 싶다면 @Transactional 의 `rollbackFor` 속성을 사용해야  한다.

```java
@Transactional(rollbackFor=SQLException.class)
```

`rollbackFor` 과 반대 기능을 하는 설정은 `noRollbackFor` 송성이다.

### @Transactional 의 주요 속성

| 속성 | 타입 | 설명 |
| --- | --- | --- |
| value | String | PlatformTransactionManager 빈의 이름 지정 |
| propagation | Propagation | 트랜잭션 전파 타입 지정 |
| isolation | Isolation | 트랜잭션 격리 레벨 지정 |
| timeout | int  | 트랜잭션 제한 시간 설정. 초 단위로 지정함 |

### @EnableTransactionManagement 애노테이션의 주요 속성

| 속성 | 설명 |
| --- | --- |
| proxyTargetClass | 클래스를 이용해서 프록시를 생성할지 여부 결정. 기본값 false |
| order | AOP 적용 순서 결정, 기본값은 int 의 최댓값 |

### @트랜잭션 전파

@Transactional 의 propagation 속성은 기본값이 `Propagation.REQUIRED` 이다. 이는 현재 진행 중인 트랜잭션이 존재하면 해당 트랜잰션을 사용하고 존재하지 않으면 새로운 트랜잭션을 생성한다. 즉, 두 메서드에서 트랜잭션이 실행되면 한 트랜잭션으로 묶어서 실행한다.

만약 속성값이 `REQUIRES_NEW` 라면 기존 트랜잭션이 존재하는지 여부에 상관없이 항상 새로운 트랜잭션을 시작한다.

또한 jdbcTemplete 에서는 진행 중인 트랜잭션이 존재하면 해당 트랜잭션 범위에서 쿼리를 수행한다. 따라서 트랜잭션과 쿼리는 한 트랜잭션 범위 내에서 실행된다.