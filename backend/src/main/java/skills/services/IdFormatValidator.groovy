package skills.services

import groovy.transform.CompileStatic
import org.apache.commons.lang3.StringUtils

@CompileStatic
class IdFormatValidator {

    static void validate(String id) {
        if (!id || id.length() < 3) {
            throw new skills.controller.exceptions.SkillException("Bad Id [$id] - must be as least 3 chars.")
        }

        if (id.length() > 50) {
            throw new skills.controller.exceptions.SkillException("Bad Id [$id] - must not exceed 50 chars.")
        }

        if (!isAlphanumericOrUnderscore(id)) {
            throw new skills.controller.exceptions.SkillException("Bad Id [$id] - must be alpha numeric - no spaces or special characters.")
        }
    }

    private static boolean isAlphanumericOrUnderscore(final CharSequence cs) {
        if (StringUtils.isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            char checkMe = cs.charAt(i)
            if (!Character.isLetterOrDigit(checkMe) && checkMe != '_') {
                return false;
            }
        }
        return true;
    }
}