package com.melonheadstudios.kanjispotter.activities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.jrejaud.onboarder.OnboardingActivity
import com.jrejaud.onboarder.OnboardingPage
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.models.InfoPanelPreferenceChanged
import com.melonheadstudios.kanjispotter.services.JapaneseTextGrabberService
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.APP_BLACKLISTED
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.BLACKLIST_SELECTION_STATUS_FLAG
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.BLACKLIST_STATUS_FLAG
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.PREFERENCES_KEY
import com.melonheadstudios.kanjispotter.utils.Constants.Companion.SERVICE_STATUS_FLAG
import com.melonheadstudios.kanjispotter.viewmodels.BlacklistSelectionModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import com.melonheadstudios.kanjispotter.BuildConfig
import com.melonheadstudios.kanjispotter.utils.iap.*


class MainActivity : AppCompatActivity(), IabBroadcastReceiver.IabBroadcastListener {
    private val TAG = "MainActivity"
    private val fastAdapter = FastAdapter<BlacklistSelectionModel>()
    private val itemAdapter = ItemAdapter<BlacklistSelectionModel>()
    private var items = ArrayList<BlacklistSelectionModel>()

    private var mIsPremium = false
    private val REMOVE_ADS = "remove_ads"
    private val RC_REQUEST = 10001
    private var mHelper: IabHelper? = null
    private var mBroadcastReceiver: IabBroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (shouldLaunchOnboarding()) {
            launchOnboarding()
        }

        Bus.observe<InfoPanelPreferenceChanged>()
                .subscribe { updateUI() }
                .registerInBus(this)

        report_issue_button.setOnClickListener {
            reportIssue()
        }

        remove_ads_button.setOnClickListener {
            onUpgradeAppButtonClicked()
        }

        blacklist_switch.setOnClickListener {
            val isChecked = !isBlacklistEnabled()
            setBlacklistEnabled(isChecked)
            updateUI()
        }

        blacklist_all_check.setOnCheckedChangeListener { _, isChecked ->
            selectAllBlacklist(isChecked)
            updateUI()
        }

        spotter_overlay_switch.setOnClickListener {
            val isChecked = !isOverlayEnabled()
            setOverlayEnabled(isChecked)
            updateUI()
        }

        blacklist_list.layoutManager = LinearLayoutManager(this)
        blacklist_list.layoutManager.isAutoMeasureEnabled = true
        blacklist_list.itemAnimator = DefaultItemAnimator()
        blacklist_list.adapter = itemAdapter.wrap(fastAdapter)

        fastAdapter.withItemEvent(BlacklistSelectionModel.CheckButtonClickEvent())

        setupIAB()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI(forceRepopulate = true)
    }

    override fun receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.")
        try {
            mHelper?.queryInventoryAsync(mGotInventoryListener)
        } catch (e: IabHelper.IabAsyncInProgressException) {
            alert("Error querying inventory. Another async operation in progress.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        Log.d(TAG, "onActivityResult($requestCode,$resultCode,$data")
        mHelper ?: return

        // Pass on the activity result to the helper for handling
        if (!mHelper!!.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data)
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver)
        }

        // very important:
        Log.d(TAG, "Destroying helper.")
        if (mHelper != null) {
            mHelper?.disposeWhenFinished()
            mHelper = null
        }
    }

    private fun alert(message: String) {
        val bld = AlertDialog.Builder(this, R.style.DialogTheme)
        bld.setMessage(message)
        bld.setNeutralButton("OK", null)
        Log.d(TAG, "Showing alert dialog: " + message)
        bld.create().show()
    }

    private fun onUpgradeAppButtonClicked() {
        Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.")

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        val payload = ""

        try {
            mHelper?.launchPurchaseFlow(this, REMOVE_ADS, RC_REQUEST, mPurchaseFinishedListener, payload)
        } catch (e: IabHelper.IabAsyncInProgressException) {
            alert("Error launching purchase flow. Another async operation in progress.")
        }
    }

    /** Verifies the developer payload of a purchase.  */
    private fun verifyDeveloperPayload(p: Purchase): Boolean {
        val payload = p.developerPayload

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true
    }

    private fun setupIAB() {
        val base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzePrg0Qw/zZOsO6z7zJOdqU9x2OXhQjwMFPVE05GAlsogF/RedT7rsWN9khinBC1P2iy7c6iwX/21a6JU+0zksRtnpEM18Aqa6xXIfW0e85njaW7oG9sEkNOyXGrUptajAKjMQK/TAqrtqowLJuCu/EnbN9hznPDenggRkNAI1RxvFt6jB7ytXHgBA194/VkIKXfY+AcUcYTxehNfexNdcHMfN99NH/KsW7swuGZVUo0SMqEeIuEHk1LMnG367PWemn7b78q825LtLXixr+yTa0CyYIi8yIV9N7jkXLsgjxMJ54Pj/uB1YcxuAcJ9csBKPKwqfB7VbnH9MmjANb4CQIDAQAB"

        Log.d(TAG, "Creating IAB helper.")
        mHelper = IabHelper(this, base64EncodedPublicKey)

        // enable debug logging (for a production application, you should set this to false).
        mHelper?.enableDebugLogging(true)

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.")
        mHelper?.startSetup({ result ->
            Log.d(TAG, "Setup finished.")

            if (!result.isSuccess) {
                // Oh noes, there was a problem.
                //complain("Problem setting up in-app billing: " + result)
                return@startSetup
            }

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return@startSetup

            // Important: Dynamically register for broadcast messages about updated purchases.
            // We register the receiver here instead of as a <receiver> in the Manifest
            // because we always call getPurchases() at startup, so therefore we can ignore
            // any broadcasts sent while the app isn't running.
            // Note: registering this listener in an Activity is a bad idea, but is done here
            // because this is a SAMPLE. Regardless, the receiver must be registered after
            // IabHelper is setup, but before first call to getPurchases().
            mBroadcastReceiver = IabBroadcastReceiver(this)
            val broadcastFilter = IntentFilter(IabBroadcastReceiver.ACTION)
            registerReceiver(mBroadcastReceiver, broadcastFilter)

            // IAB is fully set up. Now, let's get an inventory of stuff we own.
            Log.d(TAG, "Setup successful. Querying inventory.")
            try {
                mHelper?.queryInventoryAsync(mGotInventoryListener)
            } catch (e: IabHelper.IabAsyncInProgressException) {
                alert("Error querying inventory. Another async operation in progress.")
            }
        })
    }

    private fun updateUI(forceRepopulate: Boolean = false) {
        remove_ads_button.visibility = if (mIsPremium) GONE else VISIBLE
        val overlayEnabled = isOverlayEnabled()
        spotter_overlay_switch.isChecked = overlayEnabled
        blacklist_check_container.visibility = if (overlayEnabled) VISIBLE else GONE

        val blacklistEnabled = overlayEnabled && isBlacklistEnabled()
        blacklist_switch.isChecked = blacklistEnabled
        blacklist_list.visibility = if (blacklistEnabled) VISIBLE else GONE
        blacklist_all_container.visibility = if (blacklistEnabled) VISIBLE else GONE
        blacklist_all_check.isChecked = allBlacklistChecked()
        if (blacklistEnabled) {
            populateBlacklist(forceRepopulate = forceRepopulate)
        }
    }

    private fun populateBlacklist(forceRepopulate: Boolean = false) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0)

        if (!forceRepopulate && items.isNotEmpty()) return

        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)

        items.clear()
        for (app in pkgAppsList) {
            val appLabel = app.loadLabel(packageManager).toString()
            val packageName = app.activityInfo.taskAffinity ?: continue
            val packageIcon = app.loadIcon(packageManager)
            items.add(BlacklistSelectionModel(sharedPreferences = prefs, appName = appLabel, packageName = packageName, icon = packageIcon))
        }
        items.sortBy { it.appName }
        itemAdapter.set(items)
    }

    private fun hasPurchasedPro(inventory: Inventory): Boolean {
        val premiumPurchase = inventory.getPurchase(REMOVE_ADS)
        return premiumPurchase != null && verifyDeveloperPayload(premiumPurchase)
    }

    @SuppressLint("CommitPrefEdits")
    private fun setOverlayEnabled(enabled: Boolean) {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(SERVICE_STATUS_FLAG, enabled).commit()
        Bus.send(InfoPanelPreferenceChanged(enabled = enabled))
    }

    private fun isOverlayEnabled(): Boolean {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(SERVICE_STATUS_FLAG, true)
    }

    private fun isBlacklistEnabled(): Boolean {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(BLACKLIST_STATUS_FLAG, false)
    }

    private fun setBlacklistEnabled(enabled: Boolean) {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(BLACKLIST_STATUS_FLAG, enabled).apply()
        if (enabled) {
            populateBlacklist()
        }
    }

    private fun allBlacklistChecked(): Boolean {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(BLACKLIST_SELECTION_STATUS_FLAG, false)
    }

    @SuppressLint("CommitPrefEdits")
    private fun selectAllBlacklist(selectedAll: Boolean) {
        val prefs = applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putBoolean(BLACKLIST_SELECTION_STATUS_FLAG, selectedAll)
        for (item in items) {
            edit.putBoolean(APP_BLACKLISTED + item.packageName, selectedAll)
        }
        edit.commit()

        itemAdapter.set(items)
    }

    private fun launchOnboarding() {
        // TODO replace with real assets
        val page1 = OnboardingPage("Welcome to Kanji Spotter!", "You'll need to enable the app to draw over other apps on screen before we continue, though.", R.drawable.abc_ab_share_pack_mtrl_alpha, "Enable permission")
        val page2 = OnboardingPage(null, "Thanks! Now we'll just need to enable the accessibility service that powers the app!", R.drawable.abc_action_bar_item_background_material, "Enable Service")
        val page3 = OnboardingPage("Perfect!", "Test out the app by clicking the kanji below!\n 食べる、男の人、ご主人", R.drawable.abc_scrubber_control_to_pressed_mtrl_000, "Looking good!")

        val onboardingPages = LinkedList<OnboardingPage>()
        onboardingPages.add(page1)
        onboardingPages.add(page2)
        onboardingPages.add(page3)
        val onboardingActivityBundle = OnboardingActivity.newBundleColorBackground(android.R.color.white, onboardingPages)
        val intent = Intent(this, KanjiOnboardingActivity::class.java)
        intent.putExtras(onboardingActivityBundle)
        startActivity(intent)
    }

    @SuppressLint("NewApi")
    private fun shouldLaunchOnboarding(): Boolean {
        val needsPermission = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        val hasPermission = Settings.canDrawOverlays(this)
        val canDrawOverApps = !needsPermission || (needsPermission && hasPermission)
        val serviceIsRunning = isMyServiceRunning(JapaneseTextGrabberService::class.java)
        return !(canDrawOverApps && serviceIsRunning)
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { serviceClass.name == it.service.className }
    }

    private fun reportIssue() {
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "melonheadstudiosapps@gmail.com", null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Issue report")
        val userInfo = "Build Number: ${BuildConfig.VERSION_CODE}\n Version: ${BuildConfig.VERSION_NAME}\n " +
                "Model: ${Build.MODEL}\n Manufacturer: ${Build.MANUFACTURER}\n" +
                "API Version: ${Build.VERSION.SDK_INT}\n Android Version ${Build.VERSION.RELEASE}\n "
        emailIntent.putExtra(Intent.EXTRA_TEXT, userInfo)
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }

    private var mGotInventoryListener: IabHelper.QueryInventoryFinishedListener = IabHelper.QueryInventoryFinishedListener { result, inventory ->
        Log.d(TAG, "Query inventory finished.")

        // Have we been disposed of in the meantime? If so, quit.
        if (mHelper == null) return@QueryInventoryFinishedListener

        // Is it a failure?
        if (result.isFailure) {
            alert("Failed to query inventory: " + result)
            return@QueryInventoryFinishedListener
        }

        Log.d(TAG, "Query inventory was successful.")

        /*
         * Check for items we own. Notice that for each purchase, we check
         * the developer payload to see if it's correct! See
         * verifyDeveloperPayload().
         */

        // Do we have the premium upgrade?
        mIsPremium = hasPurchasedPro(inventory)
        Log.d(TAG, "User is " + if (mIsPremium) "PREMIUM" else "NOT PREMIUM")

        updateUI()
        Log.d(TAG, "Initial inventory query finished; enabling main UI.")
    }

    private var mPurchaseFinishedListener: IabHelper.OnIabPurchaseFinishedListener = IabHelper.OnIabPurchaseFinishedListener { result, purchase ->
        Log.d(TAG, "Purchase finished: $result, purchase: $purchase")

        // if we were disposed of in the meantime, quit.
        if (mHelper == null) return@OnIabPurchaseFinishedListener

        if (result.isFailure) {
            alert("Error purchasing: " + result.message)
            return@OnIabPurchaseFinishedListener
        }
        if (!verifyDeveloperPayload(purchase)) {
            alert("Error purchasing. Authenticity verification failed.")
            return@OnIabPurchaseFinishedListener
        }

        Log.d(TAG, "Purchase successful.")

        when (purchase.sku) {
            REMOVE_ADS -> {
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.")
                alert("Thank you for upgrading to premium!")
                mIsPremium = true
                updateUI()
            }
        }
    }
}
