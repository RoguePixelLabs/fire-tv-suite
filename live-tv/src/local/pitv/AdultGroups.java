package local.pitv;
import java.util.regex.Pattern;
/** Provider group labels only; channel titles do not classify a group. */
final class AdultGroups {
 static final Pattern LABEL=Pattern.compile("(?i)(^|[^a-z])(adults?|xxx|18\\s*\\+|adult\\s*entertainment)([^a-z]|$)");
 static boolean matches(String group){return group!=null&&LABEL.matcher(group).find();}
}
