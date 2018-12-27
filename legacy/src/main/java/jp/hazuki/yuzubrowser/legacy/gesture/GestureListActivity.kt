package jp.hazuki.yuzubrowser.legacy.gesture

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.legacy.utils.app.ThemeActivity

class GestureListActivity : ThemeActivity() {

    private var mGestureId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        val intent = intent ?: throw IllegalStateException("intent is null")

        val title = intent.getStringExtra(Intent.EXTRA_TITLE)

        if (title != null)
            setTitle(title)

        val actionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA) ?: ActionNameArray(applicationContext)

        mGestureId = intent.getIntExtra(GestureManager.INTENT_EXTRA_GESTURE_ID, -1)
        if (mGestureId < 0)
            throw IllegalStateException("Unknown intent id:" + mGestureId)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, GestureListFragment(mGestureId, actionNameArray))
                    .commit()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.gesture_test).setOnMenuItemClickListener {
            val intent = Intent(applicationContext, GestureTestActivity::class.java)
            intent.putExtra(GestureManager.INTENT_EXTRA_GESTURE_ID, mGestureId)
            intent.putExtra(Intent.EXTRA_TITLE, title)
            startActivity(intent)
            false
        }
        return super.onCreateOptionsMenu(menu)
    }
}
