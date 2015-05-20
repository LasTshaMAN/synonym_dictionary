package dictionary.util;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@Retention(value = RetentionPolicy.RUNTIME)
public @interface DictionaryRepoQualifier {
}
