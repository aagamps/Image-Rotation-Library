package com.android.picture_rotation_library

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.provider.Browser
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import butterknife.ButterKnife
import butterknife.Unbinder
import com.android.picture_rotation_library.Constants.Companion.BUNDLE
import com.android.picture_rotation_library.Constants.Companion.NAVIGATION_URL
import com.google.android.material.snackbar.Snackbar
import com.kredily.web.utils.ConnectivityHelper
import com.kredily.web.utils.ConnectivityHelperImpl
import com.kredily.web.utils.SnackbarUtil
import io.reactivex.CompletableTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class WebActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private var unbinder: Unbinder? = null
    private lateinit var manager: DownloadManager
    private val fragmentManager = supportFragmentManager
    private var connectivityHelper: ConnectivityHelper? = null
    private var downloadRequest: DownloadManager.Request? = null
    private var downloadUrl: String? = null
    private lateinit var deepLinkUrl: String
    private lateinit var webview: WebView
    private lateinit var rootView: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        unbinder = ButterKnife.bind(this)
        manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        kredilyApi = NetworkClient.getApiService()
        connectivityHelper = ConnectivityHelperImpl(this)
        webview = findViewById(R.id.webview)
        rootView = findViewById(R.id.rootView)
        initializeWebView()
        parseDeepLink(null)
        loadUrl()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        parseDeepLink(intent)
        loadUrl()
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun initializeWebView() {
        webview.setInitialScale(1)
        val webSettings = webview.settings
        //Device Screen Settings
        webSettings?.useWideViewPort = true
        //Cache Settings
        webSettings?.setAppCachePath(applicationContext?.cacheDir?.absolutePath)
        webSettings?.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings?.setAppCacheEnabled(true)
        //IncreasePerformance
        webSettings?.setRenderPriority(WebSettings.RenderPriority.HIGH)
        webview.scrollBarSize = View.SCROLLBARS_INSIDE_OVERLAY
        webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //JavaScript
        webSettings?.javaScriptEnabled = true
        webSettings?.javaScriptCanOpenWindowsAutomatically = true
        //File Upload Settings
        webSettings?.allowContentAccess = true
        webSettings?.allowFileAccess = true
        webSettings?.allowFileAccessFromFileURLs = true
        webSettings?.allowUniversalAccessFromFileURLs = true
        //Local Storage Settings
        webSettings?.databaseEnabled = true
        webSettings?.domStorageEnabled = true
        //Zoom Settings
        webSettings?.loadWithOverviewMode = true
        webSettings?.setSupportZoom(true)
        webSettings?.builtInZoomControls = true
        webSettings?.displayZoomControls = false
        //Text Encoding
        webSettings?.defaultTextEncodingName = "utf-8"
        //Web Client Settings
        webview?.webViewClient = webClient
        //Location Settings
        webSettings?.setGeolocationEnabled(true)
        //Hardware Acceleration
        if (Build.VERSION.SDK_INT >= 21) {
            if (Build.VERSION.SDK_INT >= 21) {
                webSettings?.mixedContentMode = 0
            }
            webview?.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webview?.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        @Suppress("DEPRECATION")
        webSettings?.pluginState = WebSettings.PluginState.ON
    }

    private fun parseDeepLink(passedIntent: Intent?) {
        val receivedIntent = passedIntent ?: intent
        if (null != receivedIntent) {
            val data = receivedIntent.data
            if (null != data) {
                deepLinkUrl = data.toString()
            } else {
                var bundle = receivedIntent.getBundleExtra(BUNDLE)
                if (null != bundle) {
                    deepLinkUrl = bundle.getString(NAVIGATION_URL, "")
                } else {
                    bundle = receivedIntent.extras
                    if (null != bundle) {
                        deepLinkUrl = bundle.getString(NAVIGATION_URL, "")
                    }
                }
            }
        }
    }

    /**
     * Check internet is available or not
     * If internet is available then load url
     * Otherwise show tapToRetry view with user readable message
     */
    private fun loadUrl() {
        val isConnected = connectivityHelper?.isConnected()
        if (isConnected != null && isConnected) {
            webview?.visibility = View.VISIBLE
//            tapToRetry?.hide()
            var urlToLoad = deepLinkUrl
            webview?.loadUrl(urlToLoad)
        } else {
            webview?.visibility = View.GONE
//            tapToRetry?.show()
//            tvErrorMessage.text = getString(R.string.no_internet_available)
        }
    }

//    @OnClick(R.id.tapToRetry)
//    fun onTapToRetryClicked() {
//        loadUrl()
//    }


    /**
     * Web Client to handle page loading and error related events
     */
    private val webClient = object : WebViewClient() {

        @Suppress("DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (!url.isNullOrBlank() && url.contains(":") && !url.startsWith("http", true)) {
                openAppIntent(url)
                return true
            }
            return super.shouldOverrideUrlLoading(view, url)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url?.toString()
            if (!url.isNullOrBlank() && url.contains(":") && !url.startsWith("http", true)) {
                openAppIntent(url)
                return true
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) = Unit

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            super.onReceivedSslError(view, handler, error)

            SnackbarUtil.showSnackbarWithSingleAction(
                this@WebActivity, error.toString(), 3000, getString(R.string.dismiss),
                ContextCompat.getColor(this@WebActivity, R.color.design_default_color_primary), null
            )

            handler?.cancel()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            val cookie = CookieManager.getInstance().getCookie(url)
            if (!cookie.isNullOrBlank() && cookie.contains(Constants.SESSION_ID, true)) {
                val cookies = cookie.split(" ")
                if (cookies.isNotEmpty()) {
                    for (item in cookies) {

                    }
                }
            }
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) = Unit

    }

    private fun resetDownloadRequest() {
        downloadRequest = null
        downloadUrl = null
    }

    private fun openAppIntent(passed: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(passed)
            startActivity(intent)
        } catch (exception: Exception) {
            showMessage(getString(R.string.no_intent_client_found))
        }
    }

    private fun openChrome(passed: String) {
        val cookie = CookieManager.getInstance().getCookie(passed)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(passed))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("com.android.chrome")
        val bundle: Bundle = Bundle()
        val cookies = cookie.split(";")
        for (cookie in cookies) {
            val keyVal = cookie.split("=")
            bundle.putString(keyVal[0], keyVal[1])
        }
        intent.putExtra(Browser.EXTRA_HEADERS, bundle)
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null)
            startActivity(intent)
        }
    }

    /**
     * To show user friendly message when encounters any error
     */
    private fun showMessage(message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
    }

//    private fun shareFcmToken() {
//        io.reactivex.Observable.timer(5, TimeUnit.SECONDS)
//            .compose(applySchedulersObservable())
//            .flatMapCompletable {
//                Completable.complete()
//            }.subscribe(object : CompletableObserver {
//                override fun onSubscribe(disposable: Disposable) {
//                    compositeDisposable.addAll(disposable)
//                }
//
//                override fun onComplete() {
//                    val request =
//                        FcmDto.Request(deviceId = getFcmToken(), deviceType = "android")
//                    submitFcmToken(request).subscribe(object : CompletableObserver {
//                        override fun onSubscribe(disposable: Disposable) {
//                            compositeDisposable.addAll(disposable)
//                        }
//
//                        override fun onComplete() {
//                            saveCallApi()
//                            isApiCalled = false
//                        }
//
//                        override fun onError(e: Throwable) {
//                            isApiCalled = false
//                        }
//                    })
//                }
//
//                override fun onError(e: Throwable) {
//                    isApiCalled = false
//                }
//            })
//    }

//    private fun checkAppVersion() {
//        if (connectivityHelper!!.isConnected()) {
//            val request = VersionCheckDto.Request()
//            checkVersion(request).subscribe(object : SingleObserver<VersionCheckDto.Response> {
//                override fun onSuccess(response: VersionCheckDto.Response) {
//                    response.status?.let { status ->
//                        if (status) {
//                            this?.let {
//                                val packageName = applicationContext.packageName
//                                val packageInfo =
//                                    applicationContext.packageManager.getPackageInfo(
//                                        packageName,
//                                        0
//                                    )
//                                val appVersion: Double =
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                                        packageInfo.longVersionCode.toDouble()
//                                    } else {
//                                        packageInfo.versionCode.toDouble()
//                                    }
//                                val serverVersion = response.versionInfo?.version ?: 0.0
//                                if (appVersion < serverVersion) {
//                                    response.versionInfo?.isForceUpdate?.let { isForceUpdate ->
//                                        var showDialog = false
//                                        var singleButton = false
//                                        val message: String?
//                                        var btnYesText = getString(R.string.update)
//                                        val btnNoText = getString(R.string.skip)
//                                        if (isForceUpdate) {
//                                            showDialog = true
//                                            singleButton = true
//                                            btnYesText = getString(R.string.update_now)
//                                            message = response.versionInfo?.message
//                                        } else {
//                                            message = response.versionInfo?.message
//                                            val versionTimeStamp =
//                                                sharedPreference.getString(
//                                                    KEY_VERSION_TIMESTAMP,
//                                                    ""
//                                                )
//                                            if (versionTimeStamp.isNullOrBlank()) {
//                                                showDialog = true
//                                            } else {
//                                                val version =
//                                                    DateUtil.getFormattedDate(
//                                                        VERSION_FORMAT,
//                                                        versionTimeStamp
//                                                    )
//                                                val current = DateUtil.getCurrentDate()
//                                                if (null != version) {
//                                                    if (version.before(current)) {
//                                                        showDialog = true
//                                                    }
//                                                }
//                                            }
//                                        }
//                                        if (showDialog) {
//                                            val bundle = Bundle()
//                                            if (!message.isNullOrBlank()) {
//                                                bundle.putString(DIALOG_MESSAGE, message)
//                                            }
//                                            bundle.putBoolean(DIALOG_SINGLE_BTN, singleButton)
//                                            bundle.putString(DIALOG_BTN_NO_TEXT, btnNoText)
//                                            bundle.putString(DIALOG_BTN_YES_TEXT, btnYesText)
//                                            val versionDialog = DialogVersion()
//                                            versionDialog.setStyle(
//                                                DialogFragment.STYLE_NORMAL,
//                                                R.style.KredilyDialog
//                                            )
//                                            versionDialog.isCancelable = false
//                                            versionDialog.arguments = bundle
//                                            versionDialog.setDialogClickListener(object :
//                                                KredilyDialog.OnDialogClickListener {
//                                                override fun onDialogPositiveButtonClicked(
//                                                    dialogFragment: DialogFragment
//                                                ) {
//                                                    goToPlayStore(packageName)
//                                                }
//
//                                                override fun onDialogNegativeButtonClicked(
//                                                    dialogFragment: DialogFragment
//                                                ) {
//                                                    versionDialog.dismiss()
//                                                    val current = DateUtil.getCurrentDate()
//                                                    val calendar = DateUtil.getCalender()
//                                                    calendar.time = current
//                                                    calendar.add(Calendar.DATE, 5)
//                                                    val storingDate = DateUtil.getFormattedTime(
//                                                        VERSION_FORMAT,
//                                                        calendar.timeInMillis
//                                                    )
//                                                    sharedPreference.edit().putString(
//                                                        KEY_VERSION_TIMESTAMP,
//                                                        storingDate
//                                                    ).apply()
//                                                }
//                                            })
//                                            showDialogFragment(versionDialog)
//                                        }
//                                    }
//                                }
//                            }
//                        } else {
//                            showMessage(response.message!!)
//                        }
//                    }
//                }
//
//                override fun onSubscribe(disposable: Disposable) {
//                    compositeDisposable.addAll(disposable)
//                }
//
//                override fun onError(error: Throwable) {
//                    showMessage(error.toString())
//                }
//
//            })
//
//        } else {
//            showMessage("No Internet Connection")
//        }
//
//    }

    private fun showDialogFragment(dialogFragment: DialogFragment) {
        dialogFragment.show(fragmentManager, null)
    }

    private fun goToPlayStore(packageName: String) {
        try {
            val playStoreLink = getString(R.string.play_store_link, packageName)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(playStoreLink)
                setPackage("com.android.vending")
            }
            startActivity(intent)
        } catch (e: Exception) {
            showMessage(this.getString(R.string.error_no_app_found))
        }
    }

    private fun goToPlayStoreApp(playStoreLink: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(playStoreLink)
                setPackage("com.android.vending")
            }
            startActivity(intent)
        } catch (e: Exception) {
            showMessage(this.getString(R.string.error_no_app_found))
        }
    }

//    private fun submitFcmToken(request: FcmDto.Request): Completable {
//        return kredilyApi.submitFcmToken(request).compose(applyCompletableSchedulers())
//    }
//
//    private fun checkVersion(request: VersionCheckDto.Request): Single<VersionCheckDto.Response> {
//        return kredilyApi.checkVersion(request).compose(applySchedulersSingle())
//    }

    private fun applyCompletableSchedulers(): CompletableTransformer {
        return CompletableTransformer { completable ->
            completable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }
    }

    private fun <T> applySchedulersObservable(): ObservableTransformer<T, T> {
        return ObservableTransformer { single ->
            single.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }
    }

    private fun <T> applySchedulersSingle(): SingleTransformer<T, T> {
        return SingleTransformer { single ->
            single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * Device back button event
     * If user is navigated to other pages in web-view, allow user to go back to the previous page
     * If user is on the home/landing page and press back button, exit the application
     */
    override fun onBackPressed() {
        val canGoBack = webview?.canGoBack()
        if (canGoBack != null && canGoBack) {
            webview?.goBack()
        } else {
            webview?.clearCache(true)
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.clear()
        }
        unbinder!!.unbind()
    }
}