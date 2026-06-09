package sharktank.teamlancer.shared.util;

import org.springframework.stereotype.Component;
import java.text.Normalizer;

@Component
public class SlugUtils {

    public String toSlug(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    public String toUniqueSlug(String input, java.util.function.Predicate<String> exists) {
        String base = toSlug(input);
        String candidate = base;
        int count = 1;
        while (exists.test(candidate)) {
            candidate = base + "-" + count++;
        }
        return candidate;
    }
}