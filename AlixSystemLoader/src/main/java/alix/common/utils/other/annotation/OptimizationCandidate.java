package alix.common.utils.other.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation serves as a future reminder for possible optimizations.
 *
 * @author ShadowOfHeaven
 **/

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.LOCAL_VARIABLE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
public @interface OptimizationCandidate {
}