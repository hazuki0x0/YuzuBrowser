package jp.hazuki.yuzubrowser.legacy.pattern;

import jp.hazuki.yuzubrowser.legacy.utils.matcher.AbstractPatternChecker;

public abstract class PatternChecker extends AbstractPatternChecker<PatternAction> {
    protected PatternChecker(PatternAction pattern_action) {
        super(pattern_action);
    }
}
