package jp.hazuki.yuzubrowser.pattern;

import jp.hazuki.yuzubrowser.utils.matcher.AbstractPatternChecker;

public abstract class PatternChecker extends AbstractPatternChecker<PatternAction> {
    protected PatternChecker(PatternAction pattern_action) {
        super(pattern_action);
    }
}
