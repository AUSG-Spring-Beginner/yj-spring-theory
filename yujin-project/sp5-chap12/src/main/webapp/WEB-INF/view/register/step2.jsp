<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html>
<head>
    <title><spring:message code="member.register" /></title>
</head>
<body>
<h2><spring:message code="member.info" /></h2>
<form:form action="step3" modelAttribute="registerRequest">
    <p>
        <label><spring:message code="email" />:<br>
            <form:input path="email" />
        </label>
    </p>
    <p>
        <label><spring:message code="name" />:<br>
            <form:input path="name" />
        </label>
    </p>
    <p>
        <label><spring:message code="password" />:<br>
            <form:password path="password" />
        </label>
    </p>
    <p>
        <label><spring:message code="password.confirm" />:<br>
            <form:password path="confirmPassword" />
        </label>
    </p>
    <input type="submit" value="<spring:message code="register.btn" />">
</form:form>

<%--
    <form action="step3" method="post">
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
    <p>
        <label>비밀번호:<br>
        <input type="password" name="password" id="password">
        </label>
    </p>
    <p>
        <label>비밀번호 확인:<br>
        <input type="password" name="confirmPassword" id="confirmPassword">
        </label>
    </p>
    <input type="submit" value="가입 완료">
    </form>
 --%>
</body>
</html>