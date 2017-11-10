package me.ykrank.s1next.view.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.ListPopupWindow
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SpinnerAdapter
import com.github.ykrank.androidtools.util.L
import com.github.ykrank.androidtools.widget.net.WifiBroadcastReceiver
import me.ykrank.s1next.R
import me.ykrank.s1next.data.api.Api
import me.ykrank.s1next.data.api.model.Forum
import me.ykrank.s1next.view.adapter.SubForumArrayAdapter
import me.ykrank.s1next.view.fragment.ThreadListFragment
import me.ykrank.s1next.view.fragment.ThreadListPagerFragment
import me.ykrank.s1next.widget.track.event.RandomImageTrackEvent
import me.ykrank.s1next.widget.track.event.ViewForumTrackEvent

/**
 * An Activity shows the thread lists.
 */
class ThreadListActivity : BaseActivity(), ThreadListPagerFragment.SubForumsCallback, WifiBroadcastReceiver.NeedMonitorWifi {

    private var mMenuSubForums: MenuItem? = null
    private var mListPopupWindow: ListPopupWindow? = null
    private var mSubForumArrayAdapter: SubForumArrayAdapter? = null

    private lateinit var forum: Forum
    private var refreshBlackList = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        disableDrawerIndicator()

        val forum = intent.getParcelableExtra<Forum?>(ARG_FORUM)
        if (forum == null) {
            L.report(IllegalStateException("ThreadListActivity intent forum is null"))
            finish()
            return
        }
        this.forum = forum
        trackAgent.post(ViewForumTrackEvent(forum.id, forum.name))
        L.leaveMsg("ThreadListActivity##forum" + forum)

        if (savedInstanceState == null) {
            val fragment = ThreadListFragment.newInstance(forum)
            supportFragmentManager.beginTransaction().add(R.id.frame_layout, fragment,
                    ThreadListFragment.TAG).commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_thread, menu)

        mMenuSubForums = menu.findItem(R.id.menu_sub_forums)
        if (mListPopupWindow == null) {
            mMenuSubForums?.isVisible = false
        }

        val newThreadMenu = menu.findItem(R.id.menu_new_thread)
        if (!mUser.isLogged) {
            newThreadMenu.isVisible = false
        }

        val randomImageMenu = menu.findItem(R.id.menu_random_image)
        if (TextUtils.equals(forum.id, "6")) {
            randomImageMenu.isVisible = true
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sub_forums -> {
                mListPopupWindow?.anchorView = toolbar.get().findViewById(R.id.menu_sub_forums)
                mListPopupWindow?.show()

                return true
            }
            R.id.menu_new_thread -> {
                NewThreadActivity.startNewThreadActivityForResultMessage(this, Integer.parseInt(forum.id))
                return true
            }
            R.id.menu_random_image -> {
                trackAgent.post(RandomImageTrackEvent())
                GalleryActivity.start(this, Api.randomImage())
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        if (refreshBlackList) {
            showShortSnackbar(R.string.blacklist_refresh_warn)
            refreshBlackList = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == PostListActivity.RESULT_BLACKLIST) {
            if (resultCode == Activity.RESULT_OK) {
                refreshBlackList = true
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onPause() {
        refreshBlackList = false
        super.onPause()
    }

    override fun setupSubForums(forumList: List<Forum>) {
        if (mListPopupWindow == null) {
            mListPopupWindow = ListPopupWindow(this)

            mSubForumArrayAdapter = SubForumArrayAdapter(this, R.layout.item_popup_menu_dropdown,
                    forumList)
            mListPopupWindow?.setAdapter(mSubForumArrayAdapter)
            mListPopupWindow?.setOnItemClickListener { parent, view, position, id ->
                // we use the same activity (ThreadListActivity) for sub forum
                ThreadListActivity.startThreadListActivity(this, mSubForumArrayAdapter?.getItem(
                        position))

                mListPopupWindow?.dismiss()
            }

            mListPopupWindow?.setContentWidth(measureContentWidth(mSubForumArrayAdapter))

            // mMenuSubForums = null when configuration changes (like orientation changes)
            // but we don't need to care about the visibility of mMenuSubForums
            // because mListPopupWindow != null and we won't invoke
            // mMenuSubForums.setVisible(false) during onCreateOptionsMenu(Menu)
            if (mMenuSubForums != null) {
                mMenuSubForums?.isVisible = true
            }
        } else {
            mSubForumArrayAdapter?.clear()
            mSubForumArrayAdapter?.addAll(forumList)
            mSubForumArrayAdapter?.notifyDataSetChanged()
        }

        // We need to invoke this every times when mSubForumArrayAdapter changes,
        // but now we only invoke this in the first time due to cost-performance.
        // mListPopupWindow.setContentWidth(measureContentWidth(mSubForumArrayAdapter));
    }

    /**
     * Forked from android.widget.Spinner#measureContentWidth(SpinnerAdapter, Drawable).
     */
    private fun measureContentWidth(spinnerAdapter: SpinnerAdapter?): Int {
        if (spinnerAdapter == null) {
            return 0
        }

        var width = 0
        var itemView: View? = null
        var itemType = 0
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

        // Make sure the number of items we'll measure is capped.
        // If it's a huge data set with wildly varying sizes, oh well.
        var start = 0
        val end = Math.min(spinnerAdapter.count, start + MAX_ITEMS_MEASURED)
        val count = end - start
        start = Math.max(0, start - (MAX_ITEMS_MEASURED - count))
        val parent = toolbar.get()
        for (i in start until end) {
            val positionType = spinnerAdapter.getItemViewType(i)
            if (positionType != itemType) {
                itemType = positionType
                itemView = null
            }
            itemView = spinnerAdapter.getView(i, itemView, parent)
            if (itemView.layoutParams == null) {
                itemView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            itemView.measure(widthMeasureSpec, heightMeasureSpec)
            width = Math.max(width, itemView.measuredWidth)
        }

        return width
    }

    companion object {

        private val ARG_FORUM = "forum"

        /**
         * Only measures this many items to get a decent max width.
         */
        private val MAX_ITEMS_MEASURED = 15

        fun startThreadListActivity(context: Context, forum: Forum?) {
            val intent = Intent(context, ThreadListActivity::class.java)
            intent.putExtra(ARG_FORUM, forum)

            context.startActivity(intent)
        }
    }
}