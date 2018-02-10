package com.melonheadstudios.kanjispotter.managers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.PurchaseEvent
import com.melonheadstudios.kanjispotter.MainApplication
import com.melonheadstudios.kanjispotter.models.IABUpdateUIEvent
import com.melonheadstudios.kanjispotter.utils.MainThreadBus
import com.melonheadstudios.kanjispotter.utils.iap.IabBroadcastReceiver
import com.melonheadstudios.kanjispotter.utils.iap.IabHelper
import com.melonheadstudios.kanjispotter.utils.iap.Inventory
import com.melonheadstudios.kanjispotter.utils.iap.Purchase
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


/**
 * kanjispotter
 * Created by jake on 2017-04-18, 5:38 PM
 */
@Singleton
class IABManager : IabBroadcastReceiver.IabBroadcastListener {
    private val TAG = "IABManager"
    private var mIsPremium = false
    private val REMOVE_ADS = "remove_ads"
    private val RC_REQUEST = 10001
    private var mHelper: IabHelper? = null
    private var mBroadcastReceiver: IabBroadcastReceiver? = null
    private var isRegistered = false

    @Inject
    lateinit var bus: MainThreadBus

    init {
        MainApplication.graph.inject(this)
    }

    fun handleResult(requestCode: Int, resultCode: Int, data: Intent, completion: () -> Unit) {
        Log.d(TAG, "onActivityResult($requestCode,$resultCode,$data")
        mHelper ?: return

        // Pass on the activity result to the helper for handling
        if (!mHelper!!.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            completion()
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.")
        }
    }

    fun unregister(context: Context) {
        try {
            // very important:
            if (mBroadcastReceiver != null && isRegistered) {
                try {
                    context.unregisterReceiver(mBroadcastReceiver)
                } catch (e: IllegalArgumentException) {
                    // do nothing, this is dumb
                }
                isRegistered = false
                mBroadcastReceiver = null
            }

            // very important:
            Log.d(TAG, "Destroying helper.")
            if (mHelper != null) {
                try {
                    mHelper?.disposeWhenFinished()
                    mHelper = null
                } catch (e: Exception) {
                    Log.d(TAG, "Exception while disposing.")
                }
            }
        } catch (e: Exception) {
            Crashlytics.logException(e)
        }
    }

    fun setupIAB(context: Context) {
        if (isRegistered) return

        val base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzePrg0Qw/zZOsO6z7zJOdqU9x2OXhQjwMFPVE05GAlsogF/RedT7rsWN9khinBC1P2iy7c6iwX/21a6JU+0zksRtnpEM18Aqa6xXIfW0e85njaW7oG9sEkNOyXGrUptajAKjMQK/TAqrtqowLJuCu/EnbN9hznPDenggRkNAI1RxvFt6jB7ytXHgBA194/VkIKXfY+AcUcYTxehNfexNdcHMfN99NH/KsW7swuGZVUo0SMqEeIuEHk1LMnG367PWemn7b78q825LtLXixr+yTa0CyYIi8yIV9N7jkXLsgjxMJ54Pj/uB1YcxuAcJ9csBKPKwqfB7VbnH9MmjANb4CQIDAQAB"

        Log.d(TAG, "Creating IAB helper.")
        mHelper = IabHelper(context, base64EncodedPublicKey)

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
            context.registerReceiver(mBroadcastReceiver, broadcastFilter)
            isRegistered = true

            // IAB is fully set up. Now, let's get an inventory of stuff we own.
            Log.d(TAG, "Setup successful. Querying inventory.")
            try {
                mHelper?.queryInventoryAsync(mGotInventoryListener)
            } catch (e: IabHelper.IabAsyncInProgressException) {
                alert("Error querying inventory. Another async operation in progress.")
            }
        })
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

    /** Verifies the developer payload of a purchase.  */
    private fun verifyDeveloperPayload(p: Purchase): Boolean {
        p.developerPayload

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

    private var cachedInventory: Inventory? = null

    private fun hasPurchasedPro(inventory: Inventory? = null): Boolean {
        val usableInventory: Inventory? = inventory ?: cachedInventory
        if (usableInventory == null) {
            Log.d(TAG, "usable inventory is null!")
            return false
        }
        cachedInventory = inventory

        val premiumPurchase = usableInventory.getPurchase(REMOVE_ADS)
        return premiumPurchase != null && verifyDeveloperPayload(premiumPurchase)
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
        bus.post(IABUpdateUIEvent(mIsPremium))
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
                Answers.getInstance().logPurchase(PurchaseEvent()
                        .putItemPrice(BigDecimal.valueOf(3.00))
                        .putCurrency(Currency.getInstance("CAD"))
                        .putItemName("Premium Status")
                        .putItemId(REMOVE_ADS)
                        .putSuccess(true))
                bus.post(IABUpdateUIEvent(isPremium = mIsPremium))
            }
        }
    }

    fun onUpgradeAppButtonClicked(activity: Activity) {
        Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.")

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        val payload = ""

        try {
            mHelper?.launchPurchaseFlow(activity, REMOVE_ADS, RC_REQUEST, mPurchaseFinishedListener, payload)
        } catch (e: IabHelper.IabAsyncInProgressException) {
            alert("Error launching purchase flow. Another async operation in progress.")
        } catch (e: Exception) {
            alert(e.localizedMessage)
            Crashlytics.logException(e)
        }
    }

    private fun alert(message: String) {
        Log.d(TAG, "Showing alert dialog: " + message)
//        val bld = AlertDialog.Builder(appContext, R.style.DialogTheme)
//        bld.setMessage(message)
//        bld.setNeutralButton("OK", null)
//        bld.create().show()
    }

}