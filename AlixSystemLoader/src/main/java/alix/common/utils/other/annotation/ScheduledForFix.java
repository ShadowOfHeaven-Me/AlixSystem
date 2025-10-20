package alix.common.utils.other.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * This annotation means that *something* of not a high priority, doesn't fully work, and is scheduled for a fix in the future.
 *
 * @author ShadowOfHeaven
 **/

@Retention(RetentionPolicy.SOURCE)
@Target({TYPE, METHOD, FIELD, PARAMETER, LOCAL_VARIABLE, ANNOTATION_TYPE, CONSTRUCTOR, PACKAGE, TYPE_PARAMETER, TYPE_USE, MODULE, RECORD_COMPONENT})
public @interface ScheduledForFix {
}