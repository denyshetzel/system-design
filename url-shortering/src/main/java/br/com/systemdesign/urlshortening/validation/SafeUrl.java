package br.com.systemdesign.urlshortening.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeUrlValidator.class)
public @interface SafeUrl {
    String message() default "{validation.url.safe}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
