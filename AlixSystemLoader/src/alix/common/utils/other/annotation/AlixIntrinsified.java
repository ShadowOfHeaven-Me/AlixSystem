package alix.common.utils.other.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is a purely cosmetic one.
 * It's used to indicate that a certain mechanism or
 * a way of handling has been changed to fit
 * Alix's way of functioning.
 * It's usually used for methods that have been
 * given a faster implementation in Alix, while
 * there already were slower, working implementations.
 * <p>
 * For example translating color codes from & to §
 * is normally done with ChatColor.translateAlternateColorCodes,
 * while in Alix it's done with AlixFormatter.translateColors.
 * <p>
 * Another example is the String.split method, which was replaced
 * with a more primitive implementation of AlixUtils.split.
 * The java one accepts all kinds of Strings, while
 * the Alix implementation only accepts non-complex ones.
 *
 * @author ShadowOfHeaven
 **/

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface AlixIntrinsified {

    String method();//What method it replaces

}