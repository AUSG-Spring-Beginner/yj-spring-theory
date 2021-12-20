package aspect;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.Signature;

@Aspect
public class ExeTimeAspect {

    @Pointcut("execution(public * chap07..*(..))")
    private void publicTarget(){ }

    // 여기서의 공통 기능: 실행 시간 측정 / 핵심 기능: 팩토리얼
    @Around("publicTarget()")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed(); // 실제 대상 객체 메서드 호출해서 핵심 기능 수행하도록
            return result;
        } finally {
            long finish = System.nanoTime();
            Signature sig = joinPoint.getSignature();
            System.out.printf("%s.%s(%s) 실행 시간 : %d ns\n", joinPoint.getTarget().getClass().getSimpleName(), sig.getName(), Arrays.toString(joinPoint.getArgs()), (finish - start));
        }
    }

}
